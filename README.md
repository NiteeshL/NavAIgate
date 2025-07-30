![NavAIgate](https://socialify.git.ci/NiteeshL/NavAIgate/image?custom_description=NavAIgate+is+an+Android+application+designed+to+assist+visually+impaired+individuals+in+navigating+their+surroundings.&description=1&language=1&name=1&owner=1&theme=Light)
# NavAIgate

## Overview
NavAIgate is an Android application designed to assist visually impaired individuals in navigating their surroundings. The app utilizes phone's camera and gemini API to provide real-time guidance and obstacle detection.

## Features
- **Real-Time Navigation**: Utilizes the phone's camera to provide continuous, real-time guidance.
- **Advanced Obstacle Detection**: Identifies and describes obstacles such as stairs, curbs, vehicles, and pedestrians, providing actionable instructions to navigate safely.
- **Intelligent Environment Analysis**: Describes the user's surroundings, including object colors, sizes, and states (e.g., "a blue car is parked on your right," "the laptop on the table is on").
- **Interactive Q&A**: Allows users to ask specific questions about their environment (e.g., "Is there a bench nearby?").
- **Text Recognition**: Reads text from signs, books, and other surfaces in the environment.
- **User-Friendly Interface**: A simple, haptic-feedback-based interface designed for accessibility.

<img width="945" height="735" alt="image" src="https://github.com/user-attachments/assets/8b4d1e29-d2b4-4991-b3da-0755b7aabc55" />

## Architecture
NavAIgate employs a multi-model architecture powered by Google's Gemini API to handle different aspects of user assistance. This separation of concerns allows for more specialized and accurate responses.

- **Navigation AI (`GeminiAPI.kt`)**: The primary model for real-time navigation. It analyzes a continuous stream of camera frames to identify obstacles, navigational cues, and environmental details. Its system prompt is heavily engineered to provide concise, safe, and context-aware instructions for a visually impaired user.

- **Conversational Q&A AI (`GeminiAPI 1.kt`)**: This model acts as a secondary assistant that answers specific questions from the user. It uses the context provided by the Navigation AI to deliver detailed responses about objects, their attributes, and the overall scene.

- **Text Recognition AI (`GeminiAPI2.kt`)**: A specialized model fine-tuned to perform Optical Character Recognition (OCR). When the user wants to read something, this model processes an image to extract and read out any text present.

All models are based on `gemini-1.5-flash` for fast response times.

## How It Works
The application operates in a continuous loop, ensuring the user receives timely information about their surroundings.

1.  **Camera Input**: The app continuously captures video frames from the device's camera.
2.  **Frame Analysis**: Every few seconds, a frame is sent to the **Navigation AI**.
3.  **Guidance Generation**: The Navigation AI analyzes the frame and streams back a short, descriptive text (3-4 sentences) outlining the immediate environment, potential hazards, and safe navigation paths.
4.  **Audio Feedback**: The generated text is converted to speech and played to the user, providing real-time audio guidance.
5.  **User Interaction**: The user can interact at any time:
    -   **Ask a Question**: The user can ask a specific question. The query, along with context from the Navigation AI, is sent to the **Q&A AI** for a detailed answer.
    -   **Read Text**: The user can trigger the text recognition feature. A high-quality image is captured and sent to the **Text Recognition AI**, which reads the text aloud.
6.  **Haptic Feedback**: The interface uses haptic feedback for button presses, confirming user actions without needing visual verification.

## Installation
1. Clone the repository:
    ```sh
    git clone https://github.com/NiteeshL/NavAIgate.git
    ```
2. Open the project in Android Studio.
3. Ensure you have the Android SDK installed and configured.
4. In Android Studio, go to `Build > Rebuild Project` to resolve dependencies.
5. Build and run the app on your Android device or emulator.

## Configuration
1. You need a Google Gemini API key. You can get one from [Google AI Studio](https://aistudio.google.com/app/apikey).
2. Add your API key in the following files by replacing `"your_api_key_here"`:
    - `app/src/main/java/com/example/assistantapp/GeminiAPI.kt`
    - `app/src/main/java/com/example/assistantapp/GeminiAPI 1.kt`
    - `app/src/main/java/com/example/assistantapp/GeminiAPI2.kt`
    ```kotlin
    // In each of the three GeminiAPI files
    val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = "your_api_key_here",
        // ...
    )
    ```

## Usage
1. Launch the app on your Android device.
2. Follow the on-screen instructions to set up your profile.
3. Start navigating by entering your destination or using voice commands.


https://github.com/user-attachments/assets/5d3bef0c-869d-405a-8442-1dea719a6f89


## Contributing
We welcome contributions! Please follow these steps:
1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Commit your changes (`git commit -m 'Add some feature'`).
4. Push to the branch (`git push origin feature-branch`).
5. Open a pull request.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Contact
For any inquiries or support, please contact us at:
- Email: niteeshleela@gmail.com
- GitHub Issues: [https://github.com/NiteeshL/NavAIgate/issues](https://github.com/NiteeshL/NavAIgate/issues)

## Acknowledgements
- Thanks to all the contributors and the open-source community.
- Special thanks to the visually impaired community for their valuable feedback.
