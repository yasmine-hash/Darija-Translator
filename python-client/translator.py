#!/usr/bin/env python3
"""
Darija Translator - Python Client
Usage: python translator.py "Hello, how are you?"
"""

import requests
import base64
import sys
import json

# Configuration
API_URL = "http://localhost:9090/api/translate"
USERNAME = "user"
PASSWORD = "password"

def translate_to_darija(text):
    """Send text to API and return translation"""
    try:
        auth = base64.b64encode(f"{USERNAME}:{PASSWORD}".encode()).decode()
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Basic {auth}"
        }
        payload = {"text": text}
        
        response = requests.post(API_URL, json=payload, headers=headers, timeout=10)
        response.raise_for_status()
        
        result = response.json()
        return result.get("translation", "Error: No translation in response")
        
    except requests.exceptions.ConnectionError:
        return "Error: Cannot connect to server. Make sure the Java backend is running on localhost:9090"
    except requests.exceptions.Timeout:
        return "Error: Request timeout"
    except requests.exceptions.HTTPError as e:
        return f"Error: HTTP {e.response.status_code} - {e.response.text}"
    except Exception as e:
        return f"Error: {str(e)}"

def interactive_mode():
    """Interactive command-line mode"""
    print("\n=== Darija Translator (Python Client) ===")
    print("Type 'quit' to exit\n")
    
    while True:
        text = input("Enter English text: ").strip()
        
        if text.lower() in ['quit', 'exit', 'q']:
            print("Goodbye!")
            break
        
        if not text:
            print("Please enter some text.\n")
            continue
        
        print("🔄 Translating...")
        translation = translate_to_darija(text)
        print(f"🌍 Darija: {translation}\n")

def main():
    if len(sys.argv) > 1:
        # Command line argument mode
        text = " ".join(sys.argv[1:])
        print(f"\n📝 English: {text}")
        print("🔄 Translating...")
        translation = translate_to_darija(text)
        print(f"🌍 Darija: {translation}\n")
    else:
        # Interactive mode
        interactive_mode()

if __name__ == "__main__":
    main()