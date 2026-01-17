# Link A Verse: Secure Android App Architecture

This document outlines the full architecture for a secure Android app called "Link A Verse".

## 1. High-Level Architecture

The architecture is designed to be secure and scalable, separating the client-side (Android app) from the backend logic and sensitive data.

*   **Android App (Client)**: This is the user-facing part of the application. It's responsible for the user interface (UI) and user experience (UX). It will not contain any sensitive information like API keys and will only communicate with our secure backend.
*   **Firebase Cloud Functions (Backend)**: This is the serverless backend that will act as a middleman between the Android app and the Gemini API. It will handle all the business logic, including authenticating requests from the app, calling the Gemini API, and processing the results before sending them back to the app.
*   **Gemini API (Third-Party Service)**: This is the external service that provides the core functionality of finding cross-references for Bible verses. Our backend will securely call this API.
*   **Firebase Authentication**: This service will be used to authenticate users, ensuring that only legitimate users can access the backend services.
*   **Firebase Secret Manager**: This service will be used to securely store the Gemini API key, ensuring it's never exposed in the code or to the client.

Here's a textual representation of the flow:

```
[Android App] <--> [Firebase Cloud Functions] <--> [Gemini API]
      ^
      |
      v
[Firebase Auth]
```

## 2. API Request/Response Flow

1.  **User Action**: The user enters a Bible verse in the Android app and requests to find cross-references.
2.  **Authentication**: The Android app first authenticates the user with Firebase Authentication. If successful, it receives a JSON Web Token (JWT).
3.  **API Request from App to Backend**: The Android app makes an HTTPS request to a specific Firebase Cloud Function endpoint (e.g., `https://us-central1-your-project-id.cloudfunctions.net/linkAVerse`). The request will include the JWT in the `Authorization` header and the verse as a JSON payload.
    *   **Request Body Example**:
        ```json
        {
          "verse": "John 3:16"
        }
        ```
4.  **Backend Processing**:
    *   The Firebase Cloud Function receives the request and first verifies the JWT to ensure the request is from an authenticated user.
    *   It retrieves the Gemini API key securely from Firebase Secret Manager.
    *   It then makes a secure HTTPS request to the Gemini API, passing the verse.
5.  **Gemini API Response**: The Gemini API processes the request and returns the cross-references to the Firebase Cloud Function.
6.  **Backend Response to App**: The Firebase Cloud Function processes the response from the Gemini API, formats it into a structured JSON, and sends it back to the Android app.
    *   **Response Body Example**:
        ```json
        {
          "verse": {
            "reference": "John 3:16",
            "text": "For God so loved the world..."
          },
          "crossReferences": [
            {
              "reference": "Romans 5:8",
              "text": "but God shows his love for us...",
              "theme": {
                "name": "God's Love"
              }
            }
          ]
        }
        ```
7.  **Display in App**: The Android app receives the JSON response, parses it, and displays the cross-references to the user in a friendly format.

## 3. Data Models

These are the data structures we'll use in the app and backend.

*   **Verse**: Represents a single Bible verse.
    *   `reference` (String): The Bible reference (e.g., "John 3:16").
    *   `text` (String): The full text of the verse.
*   **Theme**: Represents a theological theme that connects verses.
    *   `name` (String): The name of the theme (e.g., "God's Love", "Faith").
*   **CrossReference**: Represents a link between two verses.
    *   `reference` (String): The reference of the cross-referenced verse.
    *   `text` (String): The text of the cross-referenced verse.
    *   `theme` (Theme): The common theme connecting the verses.

## 4. Security Best Practices Checklist

*   **Never Store API Keys in the App**: The Gemini API key will be stored in Firebase Secret Manager and only accessed by the Cloud Functions.
*   **Use HTTPS for All Communication**: All communication between the app and the backend, and between the backend and the Gemini API, will be over HTTPS.
*   **Authentication**: Every request to the backend will be authenticated using Firebase Authentication to ensure only legitimate users can access the service.
*   **Rate Limiting**: Implement rate limiting on the Cloud Functions to prevent abuse and denial-of-service attacks. This can be done using a combination of Firebase features.
*   **Input Validation**: The backend will validate all input from the app to prevent injection attacks and other vulnerabilities.
*   **Principle of Least Privilege**: The Cloud Functions will have the minimum permissions necessary to perform their tasks.
*   **Code Obfuscation**: Use ProGuard or R8 in the Android app to obfuscate the code, making it harder to reverse-engineer.
*   **Google Play Store Compliance**:
    *   Provide a clear privacy policy to users.
    *   Handle user data responsibly.
    *   Ensure the app's functionality and content are appropriate for the target audience.

## 5. Folder Structure

A well-organized folder structure is crucial for maintainability.

### Android App (`/app/src/main/java/com/example/linkaverse`)

```
.
├── data
│   ├── model
│   │   ├── Verse.kt
│   │   ├── Theme.kt
│   │   └── CrossReference.kt
│   └── repository
│       └── VerseRepository.kt
├── di
│   └── AppModule.kt
├── network
│   ├── ApiService.kt
│   └── RetrofitClient.kt
├── ui
│   ├── screens
│   │   ├── HomeScreen.kt
│   │   └── ResultsScreen.kt
│   ├── theme
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   └── viewmodel
│       ├── HomeViewModel.kt
│       └── ResultsViewModel.kt
└── MainActivity.kt
```

### Firebase Backend (`/functions`)

```
.
├── src
│   ├── api
│   │   └── controllers
│   │       └── verseController.ts
│   ├── config
│   │   └── firebase.ts
│   ├── models
│   │   ├── Verse.ts
│   │   ├── Theme.ts
│   │   └── CrossReference.ts
│   └── services
│       └── geminiService.ts
├── index.ts
├── package.json
└── tsconfig.json
```

## 6. Firebase Cloud Function Backend

This section details the complete backend solution for "Link A Verse" using Firebase Cloud Functions.

### Beginner-Friendly Explanation

Think of a Firebase Cloud Function as your own private, powerful assistant living in the cloud. Your Android app can't talk to the Gemini AI directly because that would mean giving away your secret API key, which is like giving away the password to your house.

Instead, your app will send a secure message (an HTTPS request) to your Cloud Function assistant. This assistant is the only one who knows the secret Gemini API key. It takes the app's request, securely talks to the Gemini AI, gets the answer, and then formats it neatly before sending it back to your app. This keeps your secret key safe and gives you full control.

---

### 6.1. SYSTEM PROMPT for Gemini

This is the set of instructions we give to the Gemini AI. It tells the AI how to behave and exactly how to format its response. This ensures we always get clean, predictable JSON data back that our app can easily understand.

```
You are a master theologian and biblical scholar. Your purpose is to find and explain connections between Bible verses.

You will receive a user's query, which could be a direct question, a theme to explore, or both. Your response MUST be a single, clean JSON object with no other text or markdown formatting before or after it.

The JSON object must have the following structure:
{
  "mainAnswer": "A concise, insightful answer to the user's query, explaining the primary connection or answer.",
  "verses": [
    {
      "reference": "Book Chapter:Verse",
      "text": "The full verse text from the King James Version (KJV) Bible."
    }
  ],
  "crossThemeConnections": [
    "A related theological theme",
    "Another related theme"
  ]
}

RULES:
1.  **JSON ONLY**: Your entire output must be a raw JSON object.
2.  **KJV ONLY**: All bible verses must be from the King James Version.
3.  **STRUCTURED RESPONSE**: Strictly adhere to the JSON structure defined above.
4.  **BE INSIGHTFUL**: Provide meaningful connections in the `mainAnswer`.
5.  If the query is unclear or not related to biblical topics, respond with a helpful message in the `mainAnswer` and leave the other fields as empty arrays.
```

---

### 6.2. Firebase Cloud Function Code

This is the heart of your backend. You will need to have the Firebase CLI installed and a Firebase project set up.

First, you need to add the Google Generative AI and CORS libraries to your project. In your `functions` folder, open the `package.json` file and add these to your `dependencies`:

```json
"dependencies": {
  "@google/generative-ai": "^0.11.3",
  "cors": "^2.8.5",
  "firebase-admin": "^12.0.0",
  "firebase-functions": "^5.0.0"
}
```

Then, run `npm install` from your `functions` directory.

Now, replace the contents of `functions/src/index.ts` (or `index.js`) with the following code:

```typescript
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";
import { GoogleGenerativeAI } from "@google/generative-ai";
import * as cors from "cors";

// Initialize Firebase Admin SDK
admin.initializeApp();

// Initialize CORS middleware to allow requests from your app
const corsHandler = cors({ origin: true });

// Gemini AI System Prompt (loaded from a constant for clarity)
const SYSTEM_PROMPT = `You are a master theologian and biblical scholar. Your purpose is to find and explain connections between Bible verses. You will receive a user's query, which could be a direct question, a theme to explore, or both. Your response MUST be a single, clean JSON object with no other text or markdown formatting before or after it. The JSON object must have the following structure: {"mainAnswer": "A concise, insightful answer to the user's query, explaining the primary connection or answer.", "verses": [{"reference": "Book Chapter:Verse", "text": "The full verse text from the King James Version (KJV) Bible."}], "crossThemeConnections": ["A related theological theme", "Another related theme"]} RULES: 1. JSON ONLY: Your entire output must be a raw JSON object. 2. KJV ONLY: All bible verses must be from the King James Version. 3. STRUCTURED RESPONSE: Strictly adhere to the JSON structure defined above. 4. BE INSIGHTFUL: Provide meaningful connections in the mainAnswer. 5. If the query is unclear or not related to biblical topics, respond with a helpful message in the mainAnswer and leave the other fields as empty arrays.`;

/**
 * HTTPS Cloud Function to interact with the Gemini API.
 */
export const askGemini = functions.https.onRequest((request, response) => {
  // Handle CORS for cross-origin requests from the app
  corsHandler(request, response, async () => {
    // 1. VALIDATE API KEY
    const geminiApiKey = functions.config().gemini.key;
    if (!geminiApiKey) {
      functions.logger.error("Gemini API key not set in environment config.");
      response.status(500).send({ error: "Server configuration error." });
      return;
    }

    // 2. VALIDATE REQUEST
    if (request.method !== "POST") {
      response.status(405).send({ error: "Method Not Allowed" });
      return;
    }
    const { question, selectedTheme } = request.body;
    if (!question && !selectedTheme) {
      response.status(400).send({ error: "Bad Request: 'question' or 'selectedTheme' is required." });
      return;
    }

    try {
      // 3. PREPARE PROMPT FOR GEMINI
      const genAI = new GoogleGenerativeAI(geminiApiKey);
      const model = genAI.getGenerativeModel({ model: "gemini-1.5-flash" });

      let userPrompt = "";
      if (question && selectedTheme) {
          userPrompt = `Question: \"${question}\"\nRelated Theme: \"${selectedTheme}\"`;
      } else if (question) {
          userPrompt = `Question: \"${question}\"`;
      } else {
          userPrompt = `Explore the theme: \"${selectedTheme}\"`;
      }

      const chat = model.startChat({
        history: [{ role: "user", parts: [{ text: SYSTEM_PROMPT }] }],
        generationConfig: {
          maxOutputTokens: 2048,
          responseMimeType: "application/json",
        },
      });

      // 4. CALL GEMINI API
      const result = await chat.sendMessage(userPrompt);
      const geminiResponse = result.response;
      const responseText = geminiResponse.text();

      // 5. PARSE AND SEND RESPONSE
      const responseObject = JSON.parse(responseText);
      response.status(200).json(responseObject);

    } catch (error) {
      functions.logger.error("Error calling Gemini API:", error);
      response.status(500).send({ error: "Failed to communicate with the AI service." });
    }
  });
});
```

---

### 6.3. Instructions to Set Gemini API Key

Your Gemini API key is a secret. We will store it in Firebase's environment configuration. Your deployed function can read it, but no one else can see it.

1.  **Get your API Key**: Obtain your API key from [Google AI Studio](https://aistudio.google.com/app/apikey).
2.  **Open your terminal/command prompt**: Navigate to your project's root folder (the one containing the `functions` folder).
3.  **Run this command**: Replace `YOUR_API_KEY` with the actual key you just copied.
    ```sh
    firebase functions:config:set gemini.key="YOUR_API_KEY"
    ```
    This command securely stores your key under the name `gemini.key`. The code we wrote (`functions.config().gemini.key`) knows how to find it.
4.  **Deploy your function**:
    ```sh
    firebase deploy --only functions
    ```
    After deployment, the Firebase CLI will give you the public URL for your `askGemini` function. That's the URL your Android app will call.

---

### 6.4. Example Request & Response JSON

Your Android app will make a `POST` request to the function's URL with a body like this:

#### Example Request (with a question)

```json
{
  "question": "What did Jesus say about faith?"
}
```

#### Example Request (with a theme)

```json
{
  "selectedTheme": "Redemption"
}
```

#### Example Response

The function will return a structured JSON object like this, ready for your app to parse and display:

```json
{
  "mainAnswer": "Jesus often linked faith directly to the possibility of miracles, teaching that belief, even as small as a mustard seed, could empower individuals to overcome significant obstacles. He demonstrated this by healing those who approached him in faith, emphasizing that their trust was the key to their restoration.",
  "verses": [
    {
      "reference": "Matthew 17:20",
      "text": "And Jesus said unto them, Because of your unbelief: for verily I say unto you, If ye have faith as a grain of mustard seed, ye shall say unto this mountain, Remove hence to yonder place; and it shall remove; and nothing shall be impossible unto you."
    },
    {
      "reference": "Mark 9:23",
      "text": "Jesus said unto him, If thou canst believe, all things are possible to him that believeth."
    }
  ],
  "crossThemeConnections": [
    "Healing",
    "Prayer",
    "Kingdom of God"
  ]
}
```
