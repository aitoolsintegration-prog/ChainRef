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
