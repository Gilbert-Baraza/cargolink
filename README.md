# CargoLink

## Project Purpose
CargoLink is a two-sided freight marketplace Android application connecting truck operators who have available truck capacity with cargo owners who need cargo transported. The initial target market is Kenya, with planned expansion across East Africa.

## Current Phase
**Phase 0: Project Foundation** (Completed)

## Architecture
CargoLink follows a modern, production-grade Android architecture utilizing:
- **Kotlin** & **Jetpack Compose** for declarative UI.
- **MVVM Architecture** (Model-View-ViewModel) with Kotlin Coroutines and StateFlow.
- **Repository Pattern** for clean separation of data sources.
- **Clean Architecture Package Structure** separating presentation, domain, data, and core concerns.

## Package Structure
```
com.example.cargolink/
├── core/
│   ├── common/         # Common utilities, Result, Resource wrappers
│   ├── constants/      # Application constants (Base URL, etc.)
│   └── network/        # Safe API call infrastructure & networking utilities
├── data/
│   ├── local/          # Local storage / database sources
│   ├── models/         # Data transfer objects (DTOs) & entities
│   ├── remote/         # Retrofit API service interfaces
│   └── repositories/   # Repository implementations
├── domain/
│   ├── models/         # Domain business models
│   ├── repository/     # Repository interfaces
│   └── usecase/        # Business logic use cases
└── presentation/
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Navigation graph and screen definitions
│   ├── screens/        # Feature screens (FoundationScreen)
│   └── theme/          # Material 3 theme, colors, and typography
```

## Major Dependencies
- **Android Gradle Plugin (AGP):** 9.4.1
- **Kotlin:** 2.2.10
- **Jetpack Compose BOM:** 2026.02.01 (Material 3)
- **AndroidX Navigation Compose:** 2.8.5
- **Lifecycle ViewModel Compose:** 2.11.0
- **Kotlinx Coroutines Android:** 1.10.1
- **Retrofit & OkHttp:** 2.11.0 / 4.12.0 (with logging interceptor)

## How to Build and Run
1. Open the project in Android Studio (Jellyfish or newer with AGP 9+ support).
2. Sync project with Gradle files.
3. Connect an Android device or start an Android Emulator (API 26+).
4. Run the `:app` module (`assembleDebug` / `run`).

## Intentionally NOT Implemented Yet (Future Phases)
- Authentication & Registration
- Truck Availability & Cargo Requirement listings
- Marketplace matching & Booking
- Secure Messaging & Payments
- GPS Tracking & Real-time location updates
- Ratings, Reviews & Notifications
- Admin dashboard features

## Next Planned Phase
**Phase 1: Authentication & User Roles Foundation** (Truck Operator vs Cargo Owner login/signup flow).
