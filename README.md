## 📱 Relative Location of Two Smartphones

### 📝 Table of Contents

- [📱 Relative Location of Two Smartphones](#-relative-location-of-two-smartphones)
  - [📝 Table of Contents](#-table-of-contents)
  - [1. 💡 Project Overview](#1--project-overview)
    - [1.1 Project Name](#11-project-name)
    - [1.2 Project Description](#12-project-description)
    - [1.3 🎯 Objectives](#13--objectives)
  - [2. 📋 Team Project Summary](#2--team-project-summary)
  - [3. ✨ Features](#3--features)
  - [4. ⚙️ Technologies Used](#4-️-technologies-used)
  - [5. 📝 Diagrams](#5--diagrams)
    - [1. Game Loop Flow](#1-game-loop-flow)
    - [2. UWB Page Access Flow](#2-uwb-page-access-flow)
  - [6. 📊 Measurement Results](#6--measurement-results)
  - [7. System Requirements](#7-system-requirements)
  - [8. 📜 Application Configuration Instructions](#8--application-configuration-instructions)

---

### 1. 💡 Project Overview

#### 1.1 Project Name

**_SeekMate_** – Hide and Seek game based on relative location of 2 phones connected via UWB technology.

#### 1.2 Project Description

Utilizes Ultra-Wideband (UWB) technology to create a turn-based mobile game of Hide and Seek on Android. Seekers receive hints to locate the hider based on UWB proximity and direction.

#### 1.3 🎯 Objectives

-   Develop an Android app capable of measuring the relative positions of two smartphones.
-   Analyze mutual location data, including accuracy measurements.

---

### 2. 📋 Team Project Summary

[🔗 Go to Team Project Summary](./docs/TaskSummary.md)

---

### 3. ✨ Features

-   **📱 Relative Location Recording**: Real-time measurement of relative positions between paired devices via UWB
-   **🔗 Pairing UWB Phones via QR Code**: Device pairing for improved convenience
-   **🎯 360° Directional Arrow**: Visual arrow pointing to the other device, dynamically updating within 360 degrees
-   **📏 Accuracy Metrics**: Display the location measurements.
-   **🌐 Cloud Integration**: Synchronize data with cloud storage solutions.

---

### 4. ⚙️ Technologies Used

The project uses the following technologies:

-   **Kotlin**: Primary programming language for Android development.
-   **[Supabase](https://supabase.com/)**: Backend-as-a-Service for authentication and data storage.
-   **[Kotlin Supabase Client](https://supabase.com/docs/reference/kotlin/introduction)** Supabase's Kotlin Android library
-   **[Android UWB Library](https://developer.android.com/jetpack/androidx/releases/core-uwb)**: A library for Ultra-Wideband (UWB) proximity sensing and location tracking.

---

### 5. 📝 Diagrams

#### 1. Game Loop Flow

```mermaid
graph TD;
    A[Start Game Screen] --> B[Generate QR Code];
    B --> C[Player 2 Scans QR Code];
    C --> D[Game Created in Supabase];
    D --> E[Background UWB Connection Established];
    E --> F[Game Loop Starts];
    F --> G{Gameplay};
    G -->|Hider Plays Minigame for Points| H[Points Spent on Hints];
    G -->|Seeker Searches Hider| I[Seeker Buys Hints];
    G --> J{End Condition};
    J -->|Seeker Finds Hider| K[Seeker Wins];
    J -->|Time Runs Out, Hider Stays Hidden| Z[Hider Wins];
    K --> M[End Game Screen with Statistics of Each Player];
    Z --> M
```

#### 2. UWB Page Access Flow

```mermaid
graph TD;
    A[User Onboarding/Login] --> B[Email Verification];
    B --> C[Demo Page Access];
    C --> D{Device Role Selection};
    D -->|Controller| E[Controlee Pairing];
    D -->|Controlee| E;
    E --> F[Start UWB Data Access];
    F --> G[Retrieve Data: Distance & Angle];
```

---

### 6. 📊 Measurement Results

[🔗 Go to Measurement Results](./docs/measurements/Measurements.md)

---

### 7. System Requirements

-   **OS**: Android 15
-   **AGP**: Android Gradle plugin 8.8.0
-   **Hardware**: Devices with Ultra-Wideband (UWB) capability
-   **Network**: Access to internet for Supabase functionalities

---

### 8. 📜 Application Configuration Instructions

[🔗 Go to Instructions](./docs/configuration/Supabase.md)

---
