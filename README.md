# AviaryQuest

AviaryQuest is a feature-rich Android application developed in Kotlin for bird enthusiasts. This app helps bird watchers track their bird-watching activities, discover nearby birding hotspots, record bird observations, get directions to their favorite locations, and even identify bird species from photos using TensorFlow Lite for Bird Species Recognition. It integrates with eBird API 2.0 for hotspot information, utilizes Google Maps API for mapping, and leverages Firestore for data storage.

## Features

- *User Authentication*: User registration and login using Firebase Authentication.
- *User Settings*: Customize units (metric/imperial) and set the maximum distance for hotspot searches.
- *Nearby Bird Hotspots*: Fetch and display bird-watching hotspots based on user preferences.
- *User Location*: Display the user's current location on the map.
- *Directions and Route*: Calculate and display the best route to selected hotspots.
- *Bird Observation*: Capture bird observations, save them to Firestore, and view them on the map.
- *Bird Species Recognition*: Identify bird species from uploaded/taken photos using TensorFlow Lite, and provide links to additional information.

## 


## Technologies Used

- Kotlin
- Firebase Authentication
- Firestore
- Google Maps API
- eBird API 2.0
- OkHttp for API requests
- Google Maps Directions API
- TensorFlow Lite for Bird Species Recognition

## Setup and Usage

1. Clone the repository.
2. Set up Firebase Authentication and Firestore for your project.
3. Create a Google Maps API key and enable necessary APIs.
4. Add TensorFlow Lite model for bird species recognition.
5. Replace API keys and configurations in the code.
6. Build and run the app on your Android device or emulator.

## Contributing

We welcome contributions to enhance AviaryQuest!

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

Happy bird watching!
