# MealMate Android Application

Welcome to MealMate, a mobile application designed to help users plan and organize their meals. This README provides comprehensive documentation for building, deploying, and using the application.

## Table of Contents

1\. [Dependencies](#dependencies)

2\. [Build and Deploy](#build-and-deploy)

3\. [Use Case Scenarios](#use-case-scenarios)

## Images

[Drive link: App screenshots](https://drive.google.com/drive/folders/1z-g7yKF12AMW-SaturFcymJogDtAuQ6s)

## Dependencies

Ensure you have the following dependencies installed and configured before building and deploying the application.

- Android Studio: [Download and Install](https://developer.android.com/studio)

- Java Development Kit (JDK) 8 or later: [Download and Install](https://www.oracle.com/java/technologies/javase-downloads.html)

- Firebase Account: [Set up Firebase](https://console.firebase.google.com/)

### Android Studio Plugins

- com.android.application

- com.google.gms.google-services

## Android Gradle Dependencies

The MealMate Android application leverages several key dependencies to enhance its functionality. Here's an overview of some major dependencies included in the project:

### Testing Dependencies

#### JUnit

- **Description:** Widely used testing framework for Java.

- **Purpose:** Enables the implementation of unit tests to ensure the reliability and correctness of the application.

#### Mockito

- **Description:** Mocking framework for unit tests.

- **Purpose:** Facilitates the creation of mock objects, aiding in the isolation of code units during testing.

#### Robolectric

- **Description:** Unit testing framework for Android.

- **Purpose:** Allows the execution of Android tests in a local JVM, speeding up the testing process and providing reliable results.

#### Espresso

- **Description:** UI testing framework for Android.

- **Purpose:** Enables the creation and execution of UI tests to ensure the proper functioning of user interfaces.

### Firebase Dependencies

#### Firebase Authentication

- **Description:** Firebase service for user authentication.

- **Purpose:** Manages user sign-up, login, and password reset functionalities securely.

#### Firebase Firestore

- **Description:** Cloud-based NoSQL database provided by Firebase.

- **Purpose:** Stores and synchronizes data, allowing real-time updates and seamless data management.

#### Firebase Realtime Database

- **Description:** Firebase's original cloud database solution.

- **Purpose:** Provides a real-time data synchronization mechanism, particularly useful for collaborative applications.

#### Firebase Storage

- **Description:** Firebase service for storing and serving user-generated content.

- **Purpose:** Enables the secure storage and retrieval of images, videos, and other files.

#### Firebase Analytics

- **Description:** Analytics service provided by Firebase.

- **Purpose:** Gathers insights into user engagement and usage patterns, aiding in data-driven decision-making.

#### Firebase App Distribution

- **Description:** Firebase App Distribution streamlines the distribution of pre-release versions.

- **Purpose:** Provides a centralized platform for distributing our app to testers (clients).

### Other Major Dependencies

#### Glide

- **Description:** Image loading and caching library.

- **Purpose:** Efficiently loads and caches images, enhancing the performance of image-intensive applications.

#### Lottie

- **Description:** Library for rendering After Effects animations in real-time.

- **Purpose:** Enhances the visual appeal of the application by seamlessly integrating complex animations.

#### Picasso

- **Description:** Image loading and caching library similar to Glide.

- **Purpose:** Simplifies the process of loading images from various sources and caching them for improved performance.

These dependencies collectively contribute to the robustness, functionality, and user experience of the MealMate Android application. They address key aspects such as testing, authentication, data storage, analytics, and media handling.

## Build and Deploy

Follow these instructions to build and deploy the MealMate application.

1\. Clone the repository:

```bash

git clone https://github.com/your-username/meal-mate.git

```

2\. Open the project in Android Studio.

3\. Connect your Android device or use an emulator.

4\. Build and run the application.

The CI/CD pipeline is configured in the `.gitlab-ci.yml` file. The pipeline consists of the following stages: `build`, `test`, `static_analyser`, and `deploy`.

### CI/CD Configuration

The CI/CD pipeline is configured to use the following environment:

- **Android Compile SDK:** 34

- **Android Build Tools:** 34.0.0

- **Android SDK Tools:** 6858069_latest

- **Gradle Version:** 8.0

### Setup

Make sure you have the following prerequisites installed on your machine:

- Docker
- Git
- Java Development Kit (JDK) 17
- Android Studio or SDK Tools
- Gradle 8.0

##### Setup Android SDK

##

```yaml
# Download and unzip Android SDK Tools
curl -s "https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS}.zip" -o "android-sdk-tools.zip"
unzip "android-sdk-tools.zip" -d "$ANDROID_HOME"

  # Add SDK tools to the PATH
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"

  # Accept Android SDK licenses
echo y | sdkmanager --sdk_root=${ANDROID_HOME} --licenses

  # Install necessary components
echo y | sdkmanager --sdk_root=${ANDROID_HOME} "platform-tools" "platforms;android-${ANDROID_COMPILE_SDK}" "build-tools;${ANDROID_BUILD_TOOLS}"
```

##### Set Up Gradle

##

```yaml
# Download and unzip Gradle
curl -LO "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
unzip -d /opt/gradle "gradle-${GRADLE_VERSION}-bin.zip"

  # Set Gradle environment variables
export GRADLE_HOME=/opt/gradle/gradle-${GRADLE_VERSION}
export PATH=$PATH:$GRADLE_HOME/bin
```

### Build Stage

The `build` stage ensures that the project is successfully built. The Android SDK and Gradle are configured with the required versions. The build script downloads necessary dependencies, sets up the Android SDK, and builds the debug version of the application.

```yaml
build:

  stage: build

  script:     - cd MealMate

  - /opt/gradle/gradle-8.0/bin/gradle assembleDebug
```

### Static Analyzer Stage

The `static_analyser` stage performs static analysis on the code using SonarQube. It analyzes the codebase for code smells, bugs, and security vulnerabilities. This stage runs only on the `main` branch.

```yaml
static_analyser:

  stage: static_analyser

  script:     - cd MealMate

  - /opt/gradle/gradle-8.0/bin/gradle sonarqube -Dsonar.host.url=$SONAR_URL -Dsonar.login=$SONAR_LOGIN -Dsonar.password=$SONAR_PASSWORD -Dsonar.verbose=true

  only:     - main
```

The GitLab CI/CD environment variables (`$SONAR_URL`, `$SONAR_LOGIN`, and `$SONAR_PASSWORD`) must be configured for the static analysis stage.

If you want to setup the sonarqube instance on local then you can follow: https://docs.sonarsource.com/sonarqube/latest/try-out-sonarqube/

### Deploy Stage

The `deploy` stage is responsible for deploying the release version of the application. It removes existing drawable resources and builds the release version, which is then uploaded for app distribution.

```yaml
deploy:

  stage: deploy

  script:     - cd MealMate

  - ls app/src/main/res/

  - rm -rf app/src/main/res/drawable-*

  - /opt/gradle/gradle-8.0/bin/gradle assembleRelease appDistributionUploadRelease --stacktrace
```

We are releasing our application on firebase using app-distribution service. So if you want to distribute on your firebase account, then you can change the service credentials path in the project and make the changes in build.gradle.kts file of an application.

```
firebaseAppDistribution {
    releaseNotes = "Release notes for this version"
    serviceCredentialsFile = "app/mealmate-8a2c7-2dc2df3c32f9.json"
}
```

You can also include the testers by adding them in build.gradle.kts file.

```
firebaseAppDistribution {
    artifactType = "APK"
    releaseNotesFile = "releaseNotes.txt"
    testers = "kunalmakwana18@gnu.ac.in, mealmateconfirmation@gmail.com, kunalmakwana.univ@gmail.com"
}
```

## Testing

### Frameworks Used

**Robolectric:** Being utilized for running Android unit tests in a simulated environment.

**Mockito:** Being employed for creating mock objects to simulate behavior in tests.

**JUnit:** Being used for assertions and organizing test cases.

### Test Runner Configuration

Before running unit tests, you need to set up the test runner inside Android Studio and execute the unit tests, follow these steps:

1. **Open MealMate project in Android Studio.**
2. **Navigate to the Run/Debug Configurations:**
   At the top toolbar, look for the dropdown menu beside the "Run/Debug" configurations selector. Click on it and select "Edit Configurations".
3. **Add New Configuration:** Click on "+" icon and select "Gradle".
4. Select "**MealMate:app**" in Gradle project field.
5. Enter "**-noverify**" in VM options.
6. **Specify classes and packages for Code Coverage:** Go to "Modify options" and then select "Specify classes and packages". Click on "+" icon under the "Code Coverage" section and add "com.groupeleven.\*" as package.
7. Next, write any of the following command in "Run" field.

### Run All Unit Tests

```shell
:app:testDebugUnitTest
```

### Run Specific Package Tests

```shell
:app:testDebugUnitTest --tests "com.groupeleven.mealmate.<package_name>.*"
```

For example:

```shell
:app:testDebugUnitTest --tests "com.groupeleven.mealmate.MealPlanner.*"
```

### Run Specific Test

```shell
:app:testDebugUnitTest --tests "com.groupeleven.mealmate.<package_name>.<test_file_name>"
```

For example:

```shell
:app:testDebugUnitTest --tests "com.groupeleven.mealmate.MealPlanner.CalendarUtilsTest"
```

## Use Case Scenarios

### 1. Authentication

#### Scenario: User Registration

If a new user, wants to create an account to access the application's features:

1\. Open the MealMate application.

2\. Click on the "Sign Up" option.

3\. Enter name, email, and password.

4\. Ensure the password is at least 8 characters.

5\. Click on the "Create Account" button.

6\. Receive a confirmation email.

7\. Click on the verification link in the email.

8\. Log in with the new credentials in the app.

#### Scenario: Password Reset

If a user, wants to reset my password if needed:

1\. Open the MealMate application.

2\. Enter the registered email address.

3\. On the Login screen, Click on the "Forgot Password" option.

4\. Receive a password reset email.

5\. Click on the reset link in the email.

6\. Set a new password.

### 2. Account Management

#### Scenario: Accessing Account Options

If a user, wants to manage various options related to his account:

1\. Log in to the MealMate application.

2\. Navigate to the last tab with options like Next Week, Favorite Recipes, My Recipes, Your Profile, Downloads, and Log Out.

3\. Click on each item to navigate to the respective sections.

### 3. Inventory Management

#### Scenario: Adding Ingredients

If a user, wants to manage the inventory of ingredients:

1\. Navigate to the "Inventory" section.

2\. Add the desired ingredients from the predetermined list.

3\. Specify the quantity for each ingredient.

4\. View and edit the list of ingredients and quantities.

5\. Select the number of people for whom the recipe is prepared.

6\. Click the "Start Recommending" button.

### 4. Favorite Recipe

#### Scenario: Viewing Favorite Recipes

If an authenticated user, wants to view the favorite recipes:

1\. Navigate to the "Favorite Recipes" section.

2\. View a list of recipes marked as favorites.

3\. Click on any recipe to be redirected to the recipe instruction page.

### 5. CI/CD Pipeline

#### Scenario: Continuous Integration

If a developer, wants to trigger the CI/CD pipeline on pushing changes to the branch:

1\. Make changes to the codebase.

2\. Push the changes to the GitLab repository.

3\. Observe the GitLab CI/CD pipeline being triggered automatically.

### 6. Recipe Instruction

#### Scenario: Viewing Recipe Instructions

If an authenticated user, wants to see detailed steps to prepare a dish:

1\. Click on their choice of recipe, this will open the Instructions Activity.

2\. View the name, image and description of the recipe.

3\. Follow each step-in detail.

4\. Use the "Share Recipe" to share the recipes with your loved ones.

5\. The "Edit Recipe" and "Delete recipe" buttons will be visible on the top of this activity if the user is the owner of this recipe which would enable them to edit and delete their own recipe.

### 7. Recommend Recipe

#### Scenario: Viewing Recommended Recipes

If an authenticated user, wants to see recipes recommended based on available ingredients:

1\. Navigate to the "Recommend Recipe" section.

2\. View three sections: My Recipes, Recommended Recipes, and Other Recommended Recipes.

3\. Toggle between veg and non-veg recipes.

4\. Click on any recipe to view details and start cooking.

### 8. Next Week Meal & Grocery Planner

#### Scenario: Planning Next Week's Meals

If a user, wants to plan meals for the next week and generate a grocery shopping list:

1\. Navigate to the "Next Week Meal & Grocery Planner" section.

2\. Add multiple meals for each day based on preferences.

3\. Select the number of people for each meal.

4\. Search for meals using a search bar.

5\. Receive a shopping list considering current inventory quantities.

### 9. My Recipes & Share Recipes

#### Scenario: Managing Custom Recipes

If a user, wants to manage my custom recipes, create new ones, edit, delete, and share them:

1\. Navigate to the "My Recipes & Share Recipes" section.

2\. View created recipes.

3\. Edit or delete existing recipes.

4\. Create a new recipe and save it.

5\. Share a recipe by entering an email address.

6\. Use the generated link to share with authenticated users only.

Feel free to explore and enjoy the MealMate application! If you encounter any issues or have suggestions, please open an issue on GitHub. We appreciate your feedback.

Happy meal planning! 🍲

## References

1. "Android Studio", developer.android.com, [Online]. Available: "https://developer.android.com/studio"
2. "Java Development Kit (JDK)", oracle.com, [Online]. Available: "https://www.oracle.com/java/technologies/downloads/"
3. "Firebase", firebase.google.com, [Online]. Available: "https://console.firebase.google.com/"
4. "SonarQube", sonarsource.com, [Online]. Available: "https://www.sonarsource.com/products/sonarqube/downloads/"
