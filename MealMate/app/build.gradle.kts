plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.sonarqube") version "3.3"
    id("com.google.firebase.appdistribution")
}

android {
    namespace = "com.groupeleven.mealmate"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.groupeleven.mealmate"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotesFile = "releaseNotes.txt"
                testers = "kunalmakwana18@gnu.ac.in, mealmateconfirmation@gmail.com, kunalmakwana.univ@gmail.com"
            }
        }
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-firestore-ktx:24.8.1")
    implementation("com.google.firebase:firebase-auth-ktx:22.1.2")

    implementation("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")
    implementation("com.google.firebase:firebase-firestore-ktx:24.8.1")
    implementation("com.google.firebase:firebase-auth-ktx:22.1.2")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("com.google.firebase:firebase-database:20.3.0")

    implementation("jp.wasabeef:glide-transformations:4.3.0")


    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("org.mockito:mockito-core:5.1.1")
    androidTestImplementation ("androidx.test:runner:1.5.2")
    androidTestImplementation ("androidx.test:rules:1.5.0")
    androidTestImplementation ("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.airbnb.android:lottie:3.4.1")
    implementation(platform("com.google.firebase:firebase-firestore:24.8.1"))
    implementation(platform("com.google.firebase:firebase-bom:32.3.1"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-core:12.0.1"))
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation ("com.google.code.gson:gson:2.10.1")
}

firebaseAppDistribution {
    releaseNotes = "Release notes for this version"
    serviceCredentialsFile = "app/mealmate-8a2c7-2dc2df3c32f9.json"
}


