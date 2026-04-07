const API_URL = "http://localhost:9090/api/translate";

async function loadSelectedText() {
    const result = await chrome.storage.local.get("selectedText");
    if (result.selectedText) {
        document.getElementById("sourceText").value = result.selectedText;
    }
}

async function translateText() {
    const input = document.getElementById("sourceText").value.trim();
    const output = document.getElementById("translation");
    const status = document.getElementById("status");

    if (!input) {
        output.textContent = "Please enter text.";
        status.textContent = "";
        return;
    }

    output.textContent = "Translating...";
    status.textContent = "";

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
                "Authorization": "Basic " + btoa("user:password")
            },
            body: JSON.stringify({ text: input })
        });

        const data = await response.json();

        if (!response.ok) {
            output.textContent = data.translation || `Error ${response.status}`;
            status.textContent = `Request failed with status ${response.status}`;
            return;
        }

        output.textContent = data.translation || "No translation returned.";
        status.textContent = "Translation completed.";
    } catch (error) {
        console.error("Translation error:", error);
        output.textContent = "Network error: could not reach backend.";
        status.textContent = "Backend connection failed.";
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    await loadSelectedText();

    const translateBtn = document.getElementById("translateBtn");
    if (translateBtn) {
        translateBtn.addEventListener("click", translateText);
    }
});