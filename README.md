# TerraTrac Mobile Application

## Overview

TerraTrac is an open-source Android mobile application available on the Google Play Store, designed to assist buyers in complying with the European Union Deforestation Regulation (EUDR). The app enables users such as buying agents, employees, and factory managers to register and manage farms from which they source commodities.
This mobile app is  one of the key components of the broad architecture for the DPI-Digital Public Infrastructure for EUDR compliance.

### Key Features

- **Site Management**: Create, edit, and manage collection sites
- **Farm Management**: Create, edit, and manage Farm data  with precise GPS coordinates and polygons with area measurements
- **Offline Capability**: Full functionality in areas with limited or no connectivity
- **Data Export/Share**: Export collected data in various (CSV/GeoJson) and share with other users
- **Location Services**: Integrated GPS functionality for precise location tracking
- **Synchronization**: Seamless data sync with remote servers when online
- **Data Restore**: Restore the Data stored on the server
- **Data Import** : Importing Data from External Files ( CSV/GeojSon)

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK API Level 21+ (Android 5.0 or higher)
- Google Play Services for location features
- Android device or emulator running Android 5.0+

### Installation
1. Clone the repository:
```bash
git https://github.com/agstack/TerraTrac-field-app.git
cd TerraTrac-field-app
```
2. Open the project in Android Studio
3. Sync Gradle files
4. Configure your local.properties file with required API keys
5. Build and run the application

### Environment Setup

Configure development environment:

Set up Android Studio
Install required SDK tools
Configure Android Virtual Device (AVD)


Add required environment variables:
```
GOOGLE_MAPS_API_KEY=your_google_maps_api_key
BASE_URL=your_server_url
```

## Architecture

The application follows Clean Architecture pattern with the following key components::

### Presentation Layer (UI)
- User Interface components
- Key Screens:
  - Site Management
  - Farm Management
  - Export/Share Module

### Business Layer
- Core Business Logic:
  - Site Handler
  - Farm Handler
  - Polygon Calculator
  - Import/Export Logic
  - Share Logic
- Validation Layer:
  - Size Validator
  - Data Validator
  - Duplicate Checker

### Data Layer
- Local Storage:
  - ROOM Database
  - Data Entities (Farms, Collection Sites)
- Core Services:
  - Location Service
  - Sync Service

### DPI for EUDR Compliance - DRAFT architecture

The objective of this document is to provide a draft, high-level description of the major components of the  new Linux Foundation Agstack project for “Digital Public Infrastructure for EUDR compliance. 

The DPI for EUDR Compliance - DRAFT architecture is available in the [GitHub repository](https://github.com/agstack/TerraTrac-field-app/blob/dev/app/docs/EUDR%20DPI%20-%20DRAFT%20architecture.pdf).


## Contributing

We welcome contributions to TerraTrac! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature' `)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please read our CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License - see the LICENSE.md file for details.

## Acknowledgments

- Google Maps Platform for location services
- Room Persistence Library
- Kotlin Coroutines for asynchronous programming
- Android Architecture Components
- All contributors who have helped shape TerraTrac

## Support

For support, please:
- Open an issue in the GitHub repository
- Contact our support team at support@tnslabs.atlassian.net
- Check our documentation

## Project Status

Current Version: 2.36 (2024-10-16)

The project is under active development.

## Regression Test List

The regression test list is now available in the [GitHub repository](https://github.com/agstack/TerraTrac-field-app/blob/dev/app/docs/Regression%20Test%20List%20for%20TerraTrac%20Mobile%20Application%20v3.0.xlsx). This sheet contains the test cases and expected behaviors for all major features of the app. Testers can update their feedback directly in the sheet to indicate whether the feature works as expected.

### How to Use the Regression Test List

Once you make any updates or changes to the code, it is important to ensure that the new changes do not break any existing functionality. You can do this by referring to the regression test list in the Google Sheet, which helps you verify that everything is still working as expected.

1. **Click on the link to open the Google Sheet.**
2. **Create a new sheet** within the Google Sheet and name it with your tester name (e.g., "Tester: John Doe").
3. **Review the test cases** in the sheet and identify the tests that apply to the updated functionality.
4. **Execute the test cases** either manually or via automated tests (if applicable).
5. **Fill in the columns** in your newly created sheet with your feedback:
  - `Test Case`: The name of the test case.
  - `Expected Behavior`: The expected behavior for the feature.
  - `Behavior Found`: What behavior was observed during testing.
  - `Status`: Whether the test passed or failed (Mark as `Pass` or `Fail`).
  - `Comments`: Any additional comments or observations.
6. **Update the status and comments** in the sheet directly after testing. This way, the whole team can track the feedback in real-time.

### Testers' Feedback

Testers can access the Google Sheet to enter their feedback after running the tests. This will help us track the stability of the app after each update and ensure that no functionality is broken.

### Instructions:
- After making any changes to the app (e.g., bug fixes, new features, refactoring), run the tests in the regression test list in the Google Sheet to confirm that the update did not introduce any issues or regressions.
- You can update the test list if needed if new scenarios or features are added.

By following this process, you ensure that your application remains stable and that any new changes do not break existing functionality.

---
*TerraTrac is committed to improving agricultural management through technology.*
