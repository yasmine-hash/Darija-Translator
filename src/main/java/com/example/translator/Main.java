package com.example.translator;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class Main {

    private static final Gson gson = new Gson();
    private static final int PORT = 9090;
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String GEMINI_MODEL = "gemini-2.5-flash";

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/translate", new TranslateHandler());
        server.setExecutor(null);
        server.start();

        System.out.println("========================================");
        System.out.println("Darija Translator API Started!");
        System.out.println("URL: http://localhost:" + PORT + "/api/translate");
        System.out.println("Auth: " + USERNAME + " / " + PASSWORD);
        System.out.println("========================================");
        System.out.println("Press Enter to stop the server...");
        System.in.read();
        server.stop(0);
    }

    static class QuotaExceededException extends RuntimeException {
        public QuotaExceededException(String message) {
            super(message);
        }
    }

    static class TranslateHandler implements HttpHandler {
        private final HttpClient client = HttpClient.newHttpClient();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, gson.toJson(Map.of("translation", "Error: Method not allowed")));
                return;
            }

            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            if (!isAuthenticated(authHeader)) {
                exchange.getResponseHeaders().set("WWW-Authenticate", "Basic realm=\"Translator API\"");
                sendResponse(exchange, 401, gson.toJson(Map.of("translation", "Error: Unauthorized")));
                return;
            }

            try {
                String requestBody = readRequestBody(exchange.getRequestBody());

                Type mapType = new TypeToken<Map<String, String>>() {}.getType();
                Map<String, String> requestMap = gson.fromJson(requestBody, mapType);

                if (requestMap == null || !requestMap.containsKey("text")) {
                    sendResponse(exchange, 400, gson.toJson(Map.of("translation", "Error: Missing text field")));
                    return;
                }

                String text = requestMap.get("text");

                if (text == null || text.trim().isEmpty()) {
                    sendResponse(exchange, 400, gson.toJson(Map.of("translation", "Error: Text cannot be empty")));
                    return;
                }

                String translation = translateToDarija(text);
                sendResponse(exchange, 200, gson.toJson(Map.of("translation", translation)));

            } catch (QuotaExceededException e) {
                sendResponse(exchange, 429, gson.toJson(Map.of("translation", "Error: " + e.getMessage())));
            } catch (Exception e) {
                e.printStackTrace();
                String message = e.getMessage();
                if (message == null || message.isBlank()) {
                    message = "Translation failed.";
                }
                sendResponse(exchange, 500, gson.toJson(Map.of("translation", "Error: " + message)));
            }
        }

        private boolean isAuthenticated(String authHeader) {
            if (authHeader == null || !authHeader.startsWith("Basic ")) {
                return false;
            }

            String base64Credentials = authHeader.substring(6);
            String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
            String[] parts = credentials.split(":", 2);

            return parts.length == 2
                    && USERNAME.equals(parts[0])
                    && PASSWORD.equals(parts[1]);
        }

        private String translateToDarija(String text) throws Exception {
            String apiKey = System.getenv("GEMINI_API_KEY");
            if (apiKey == null || apiKey.isBlank()) {
                throw new RuntimeException("GEMINI_API_KEY is missing");
            }

            String prompt = "Translate the following English text into natural Moroccan Arabic Darija. "
                    + "Return only the translation. "
                    + "Do not add explanations. "
                    + "Do not add quotes. "
                    + "Do not add labels. "
                    + "Text: " + text;

            Map<String, Object> payload = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            String jsonBody = gson.toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + GEMINI_MODEL + ":generateContent"))
                    .header("Content-Type", "application/json")
                    .header("x-goog-api-key", apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return extractGeminiText(response.body());
            }

            if (response.statusCode() == 429) {
                throw new QuotaExceededException("Gemini quota exceeded. Please wait a few seconds and try again.");
            }

            if (response.statusCode() == 503) {
                throw new RuntimeException("Gemini is temporarily busy. Please try again in a few seconds.");
            }

            throw new RuntimeException("Gemini API error " + response.statusCode() + ": " + response.body());
        }

        private String extractGeminiText(String responseBody) {
            Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> responseMap = gson.fromJson(responseBody, mapType);

            try {
                List<?> candidates = (List<?>) responseMap.get("candidates");
                if (candidates == null || candidates.isEmpty()) {
                    throw new RuntimeException("No candidates returned from Gemini");
                }

                Map<?, ?> firstCandidate = (Map<?, ?>) candidates.get(0);
                Map<?, ?> content = (Map<?, ?>) firstCandidate.get("content");
                List<?> parts = (List<?>) content.get("parts");
                if (parts == null || parts.isEmpty()) {
                    throw new RuntimeException("No parts returned from Gemini");
                }

                Map<?, ?> firstPart = (Map<?, ?>) parts.get(0);
                Object text = firstPart.get("text");

                if (text == null) {
                    throw new RuntimeException("No text returned from Gemini");
                }

                return text.toString().trim();
            } catch (Exception e) {
                throw new RuntimeException("Could not parse Gemini response: " + responseBody);
            }
        }

        private String readRequestBody(InputStream inputStream) throws IOException {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder requestBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    requestBody.append(line);
                }
                return requestBody.toString();
            }
        }

        private void addCorsHeaders(HttpExchange exchange) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
        }

        private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
    }
}