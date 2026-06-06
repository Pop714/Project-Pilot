# 🚀 Project Pilot

**Project Pilot** is a modern collaborative project and task management application built for Android using **Kotlin** and **Jetpack Compose**. The application enables teams to organize projects, manage tasks, share files, record voice notes, and collaborate in real time through a scalable cloud-backed architecture.

The project was designed to demonstrate modern Android development practices, including **Clean Architecture**, **MVVM**, **Firebase integrations**, **Supabase Storage**, **Biometric Authentication**, and a fully reactive UI powered by **StateFlow** and **Jetpack Compose**.

---

## 📸 Screenshots

| Login      | Dashboard  | Project Details |
| ---------- | ---------- | --------------- |
| Screenshot | Screenshot | Screenshot      |

| Task Details | Voice Notes | Settings   |
| ------------ | ----------- | ---------- |
| Screenshot   | Screenshot  | Screenshot |

---

## ✨ Features

### 📁 Project Management

* Create and manage collaborative projects.
* Invite and manage project members.
* Organize work into tasks and subtasks.
* Monitor project progress through task statuses.
* Real-time synchronization between team members using Cloud Firestore.

### ✅ Task Tracking

* Create, edit, assign, and delete tasks.
* Track task progress with:

    * Pending
    * In Progress
    * Completed
* Reactive UI updates powered by Kotlin StateFlow.
* Pull-to-refresh support using Material 3 APIs for manual synchronization.

### 📎 Attachments & Media

* Upload files directly to cloud storage.
* Support for:

    * Images
    * Documents
    * PDFs
    * Voice recordings
* Add external URL references to tasks.
* Download and preview uploaded attachments.

### 🎙 Voice Notes

* Record voice instructions directly inside tasks.
* Playback support with progress tracking.
* Recording duration timer.
* Visual progress indicators using Compose components.

### 🔒 Authentication & Security

* Email and password authentication.
* Biometric login using fingerprint or face recognition.
* Encrypted local credential storage.
* Secure session management.
* Access control using Firestore Security Rules and Supabase Row-Level Security (RLS).

### 🔔 Notifications

* Instant task updates through Firebase Cloud Messaging (FCM).
* Background notification handling.
* Project activity alerts.

### 🌍 Localization

* English language support.
* Arabic language support.
* Full Right-to-Left (RTL) layout compatibility.
* Runtime language switching without reinstalling the application.

### 🎨 Customization

* Light Theme.
* Dark Theme.
* System Default Theme.
* Theme persistence using DataStore Preferences.

### ⚙ Remote Configuration

* Feature flag management using Firebase Remote Config.
* Runtime configuration updates without requiring a Play Store release.

### 🚨 Stability & Monitoring

* Crash reporting using Firebase Crashlytics.
* Usage analytics through Firebase Analytics.

---

## 🏗 Architecture

The application follows **Clean Architecture** principles with clear separation of responsibilities across layers.

### Presentation Layer

Responsible for:

* Jetpack Compose UI
* Navigation
* ViewModels
* State Management

### Domain Layer

Responsible for:

* Business Logic
* Use Cases
* Repository Contracts
* Domain Models

### Data Layer

Responsible for:

* Firebase Services
* Supabase Services
* Local Database
* Repository Implementations

### Data Flow

```text
UI
 ↓
ViewModel
 ↓
UseCase
 ↓
Repository
 ↓
Remote / Local Data Source
```

### Project Structure

```text
com.projectpilot

├── data
│   ├── local
│   ├── remote
│   ├── repository
│
├── domain
│   ├── model
│   ├── repository
│   └── usecase
│
├── presentation
│   ├── screens
│   ├── components
│   ├── navigation
│   └── viewmodel
│
├── di
├── utils
└── core
```

---

## 🛠 Tech Stack

### Core

* Kotlin
* Coroutines
* Flow / StateFlow

### UI

* Jetpack Compose
* Material Design 3
* Navigation Compose
* AndroidX Splash Screen API

### Architecture

* MVVM
* Clean Architecture
* Repository Pattern

### Dependency Injection

* Dagger Hilt
* Kotlin Symbol Processing (KSP)

### Networking

* Ktor Client

### Local Storage

* Room Database
* DataStore Preferences

### Backend Services

#### Firebase

* Authentication
* Cloud Firestore
* Cloud Messaging (FCM)
* Remote Config
* Crashlytics
* Analytics

#### Supabase

* Storage Buckets
* File Upload Management
* Row-Level Security (RLS)

### Image Loading

* Coil Compose

---

## 🔒 Security

Project Pilot implements multiple security layers:

* Firebase Authentication
* Biometric Authentication
* Firestore Security Rules
* Supabase Row-Level Security
* Encrypted local credential storage
* Access validation before file retrieval

---

## 🧪 Testing

The project architecture has been designed to support:

* Unit Testing
* Repository Testing
* ViewModel Testing
* UI Testing with Compose Testing APIs

---

## 🚀 Local Setup

### 1. Clone Repository

```bash
git clone https://github.com/Pop714/project-pilot.git
```

```bash
cd project-pilot
```

---

### 2. Firebase Setup

1. Create a Firebase project.
2. Enable:

    * Authentication
    * Cloud Firestore
    * Cloud Messaging
    * Remote Config
3. Download the generated `google-services.json`.
4. Place it inside:

```text
app/google-services.json
```

---

### 3. Supabase Setup

Create two storage buckets:

```text
task_attachments
task_voices
```

Configure appropriate Row-Level Security policies.

Obtain:

* Project URL
* Anon Key

Add them to your dependency injection configuration.

---

### 4. Firestore Rules

Deploy the provided:

```text
firestore.rules
```

file to secure project access and collaboration permissions.

---

### 5. Build & Run

```bash
./gradlew assembleDebug
```

or launch directly from Android Studio.

---

## 🎯 Learning Objectives

This project was built to explore and demonstrate:

* Modern Android Architecture
* Jetpack Compose
* Reactive UI Development
* Cloud-Backed Applications
* Firebase Ecosystem
* Supabase Storage Integration
* Secure Authentication Systems
* Scalable State Management
* Clean Architecture Principles

---

## 👨‍💻 Developer

### Albraa Alhrairy

Android Developer focused on building scalable, maintainable, and modern mobile applications using Kotlin and Jetpack Compose.

**Skills**

* Kotlin
* Jetpack Compose
* Clean Architecture
* MVVM
* Firebase
* Supabase
* Room Database
* Hilt
* Coroutines & Flows

GitHub: https://github.com/Pop714/

LinkedIn: https://www.linkedin.com/in/albraa-alhrairy/

---

## 📄 License

This project is available for educational and portfolio purposes.
