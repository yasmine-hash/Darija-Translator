# 🌍 Darija Translator – LLM-powered RESTful Web Service

## Project Overview

This project implements an **LLM-powered RESTful web service** that translates text from English to Moroccan Arabic Dialect (Darija) using **Google Gemini API**.

The system consists of:

* A Java-based REST backend
* A Chrome Extension (side panel)
* Multiple client applications (Python, PHP, React Native)

---

## 🏗️ System Architecture

```
Clients (Chrome / Python / PHP / Mobile)
            ↓
      Java REST API
            ↓
      Google Gemini API
```

---

## ⚙️ Technologies Used

* Java (Maven)
* Java HttpServer (REST API)
* Google Gemini API (LLM)
* Chrome Extension (Manifest V3 + Side Panel API)
* Python (requests)
* PHP (HTTP stream)
* React Native (mobile client)

---

## Features

* Translate English text to Moroccan Darija
* Secure REST API using Basic Authentication
* Chrome Extension with:

  * Right-click context menu
  * Side panel interface
  * Auto-fill selected text
* Multiple clients:

  * Python CLI client
  * PHP CLI/Web client
  * React Native mobile app

---

## Authentication

The API is secured using **Basic Authentication**:

```
Username: user
Password: password
```

---

## Backend Setup (Java)

### 1. Set API Key

Set your Gemini API key as an environment variable:

```bash
GEMINI_API_KEY=your_api_key_here
```

### 2. Run the Server

Using Maven:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.example.translator.Main"
```

### 3. Server Output

```
Darija Translator API Started!
URL: http://localhost:9090/api/translate
```

---

## API Endpoint

### POST `/api/translate`

### Request Body:

```json
{
  "text": "Hello, how are you?"
}
```

### Response:

```json
{
  "translation": "Salam labas 3lik?"
}
```

---

## Testing with Postman

* Method: POST
* URL: `http://localhost:9090/api/translate`
* Headers:

  * Content-Type: application/json
  * Authorization: Basic dXNlcjpwYXNzd29yZA==

---

## Chrome Extension

### Load Extension

1. Go to: `chrome://extensions`
2. Enable **Developer Mode**
3. Click **Load unpacked**
4. Select the `chrome-extension/` folder

### Usage

1. Highlight text on any webpage
2. Right-click → **Translate to Darija**
3. Click the extension icon
4. View translation in the side panel

---

## Python Client

### Run:

```bash
python translator.py "Hello"
```

### Example Output:

```
Darija: salam
```

---

## PHP Client

### Run:

```bash
php translator.php "Hello"
```

### Interactive Mode:

```bash
php translator.php
```

---

## React Native App

### Run:

```bash
npm install
npx react-native run-android
```

Features:

* Input text
* Translate button
* Display translation

---
=
## UML Diagrams

Located in `/uml` folder:

* Class Diagram
* Deployment Diagram
* UseCase Diagram
* Sequence Diagram

---

## Notes

* The project uses Java `HttpServer` for a lightweight REST implementation.
* Google Gemini API may return:

  ```
  Error: Gemini quota exceeded
  ```

  This is due to free-tier limitations.

---

## Demo Video

A demo video is included in the `/demo` folder showing:

* Backend execution
* Chrome extension usage
* Client applications

---

## Project Structure

```
project/
├── src/
├── chrome-extension/
├── php-client/
├── python-client/
├── react-native-mobile/
├── uml/
└── demo/
```

