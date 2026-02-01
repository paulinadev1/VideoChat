# VideoChat

This is a sample video chat application for Android, built using modern Android development practices and the Vonage Video SDK.

## Setup instructions

**Prerequisites:**
- Android Studio (Version "Otter 2 Feature Drop | 2025.2.2 Patch 1" or newer)
- Device or emulator with Android 7.0 or higher
- JDK 17
- Vonage Video credentials (App ID, Session ID, Token)

**Steps:**
1. Open the project in Android Studio.
2. Replace the hardcoded values in `app/src/main/java/com/paulinaaniola/videochat/data/VonageVideoConfig.kt`
   with your own valid `APP_ID`, `SESSION_ID`, and `TOKEN`.
3. Build and run on a device or emulator
4. Grant camera and microphone permissions when prompted.

## Features

- **Video Chat:** Engage in one-to-one video conversations.
- **Controls:** Mute/unmute microphone and enable/disable camera during a call.
- **Real-time:** Utilizes the Vonage Video SDK for real-time communication.

## Architecture

The application follows the official [recommended app architecture](https://developer.android.com/topic/architecture) from Google.

- **MVVM (Model-View-ViewModel):** The app is structured using the MVVM pattern:
    - **View:** The Composable functions in `VideoChatScreen.kt` are responsible for displaying the UI and forwarding user events to the `VideoChatViewModel`.
    - **ViewModel:** `VideoChatViewModel` contains the UI state and business logic. It exposes the state as a `StateFlow` to the UI and handles user interactions.
    - **Model:** The data layer is composed of a `VideoChatRepository` which abstracts the data source (Vonage Video SDK). This repository is responsible for initializing and managing the video chat session.
- **Hilt for Dependency Injection:** Hilt is used to manage dependencies throughout the application, making the code more modular and testable.
- **Jetpack Compose:** The UI is built entirely with Jetpack Compose, a modern declarative UI toolkit for Android.

## Architecture and Design Patterns

The project employs several architectural patterns to ensure a clean, scalable, and maintainable codebase:

- **Clean Architecture:** The project is structured into `data`, `domain`, and `ui` layers, separating concerns and creating a unidirectional data flow. This makes the app more scalable, testable, and maintainable.
- **Model-View-ViewModel (MVVM):** Separates the UI (View) from the business logic (ViewModel) and the data (Model). This is the primary high-level pattern used.
- **Repository Pattern:** The `VideoChatRepository` abstracts the data source (Vonage SDK), providing a clean API for the ViewModel to interact with, and isolating the rest of the app from the specific implementation details of the data source.
- **Facade Pattern:** The `VideoChatFacade` provides a simplified interface to the more complex underlying Vonage SDK. This makes the ViewModel's interaction with the video service more straightforward and decoupled from the SDK's specifics.
- **Observer Pattern:** The UI observes the `StateFlow` from the `VideoChatViewModel` for state changes. This allows the UI to reactively update whenever the underlying data changes, without the ViewModel needing a direct reference to the View. `SharedFlow` is used for one-time events.

## Assumptions and Design Decisions

- **Simplified Session Management:** For simplicity, the application uses a hardcoded session ID and token. In a production environment, you would typically have a backend server that generates these on demand.
- **Single Subscriber:** The app is designed to handle only one remote subscriber at a time.
- **Minimal UI:** In-call UI is limited to mute, video toggle, and leave controls. Subscriber mute/unmute state is not displayed.
- **Coroutines for Asynchronous Operations:** The app uses Kotlin Coroutines to manage background threads and asynchronous operations, such as connecting to the video chat session and handling events.

## Known Issues and Limitations

- **No Backend:** As mentioned above, the app lacks a backend for dynamic session and token generation.
- **Hardcoded Strings:** UI strings are currently hardcoded in the Composable functions. In a production app, these should be extracted into string resources.
- **Limited Error Handling:** The error handling is basic and could be improved to provide more specific feedback to the user.
- **Limited Testing:** The project includes some unit tests for the `VideoChatViewModel`, but the test coverage could be expanded.

## Time Spent

Approximately 6 hours were spent on developing this application.