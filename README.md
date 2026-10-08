# CargoLink

## Project Purpose
CargoLink is a two-sided freight marketplace Android application connecting truck operators who have available truck capacity with cargo owners who need cargo transported. The initial target market is Kenya, with planned expansion across East Africa.

## Current Phase
**Phase 1: Authentication & User Roles Foundation** (Completed)

## Architecture
CargoLink follows a modern, production-grade Android architecture utilizing:
- **Kotlin** & **Jetpack Compose** for declarative UI.
- **MVVM Architecture** with Kotlin Coroutines and StateFlow.
- **Repository Pattern** for clean data abstraction.
- **Firebase Authentication** for identity and session management.
- **Django REST Framework + PostgreSQL** for business logic and profile storage.

## Package Structure
```
com.example.cargolink/
├── core/
│   ├── common/         # Resource / Result wrappers
│   ├── constants/      # App constants & Base URL
│   └── network/        # SafeApiCall, AuthInterceptor, RetrofitClient
├── data/
│   ├── models/         # Data transfer objects (UserDto)
│   ├── remote/         # Retrofit ApiService interface
│   └── repository/     # UserRepository implementation
├── domain/             # Domain business models & repository interfaces
└── presentation/
    ├── navigation/     # NavGraph & Screen routes
    ├── screens/        # Welcome, Login, Registration, Profile screens
    └── viewmodel/      # AuthViewModel & ProfileViewModel
```

## Backend Structure (`backend/`)
```
backend/
├── config/             # Django settings & URLs
├── apps/users/         # CargoLinkUser model, Firebase auth backend, API views
└── requirements/       # Python dependencies
```

## Documentation
- [Architecture Guide](docs/ARCHITECTURE.md)
- [API Documentation](docs/API.md)
- [Development Guide](docs/DEVELOPMENT.md)

## How to Build and Run
1. Configure Firebase in Android (`app/google-services.json`) and Django (`firebase-service-account.json`).
2. Start the Django backend (`python manage.py runserver`).
3. Build and run the Android app in Android Studio.

## Next Planned Phase
**Phase 2: Truck Availability & Cargo Requirement Listings**
