# CargoLink Architecture - Phase 1

## Overview
CargoLink is a two-sided freight marketplace connecting truck operators and cargo owners across Kenya and East Africa. Phase 1 establishes the secure authentication and user profile management foundation.

## Authentication & Authorization Flow

```
┌───────────────────────────────────────────────┐
│              CargoLink Android                │
│          Kotlin + Jetpack Compose             │
└───────────────────────┬───────────────────────┘
                        │
                        │ Firebase Authentication
                        ▼
              ┌───────────────────┐
              │ Firebase Auth     │
              │                   │
              │ Register / Login  │
              └─────────┬─────────┘
                        │
                        │ Firebase ID Token
                        ▼
              ┌───────────────────┐
              │ Django REST API   │
              │                   │
              │ Verify token (SDK)│
              │ User / profile    │
              └─────────┬─────────┘
                        │
                        ▼
              ┌───────────────────┐
              │    PostgreSQL     │
              │                   │
              │ CargoLink Users   │
              └───────────────────┘
```

### Separation of Concerns
1. **Firebase Authentication:** Answers **"Who is this user?"** Handles registration, login, password management, and issues cryptographically signed Firebase ID tokens. Passwords are never stored in Django/PostgreSQL.
2. **Django REST Framework:** Answers **"What can this user do in CargoLink?"** Verifies Firebase ID tokens using the Firebase Admin SDK and manages marketplace profile data (`CargoLinkUser`), business rules, and authorization.
3. **PostgreSQL:** The authoritative database storing CargoLink user profiles and participation types (`TRUCK_OPERATOR`, `CARGO_OWNER`, `BOTH`).

## Android Architecture (MVVM + Clean Architecture)
- **Presentation:** Jetpack Compose UI screens (`WelcomeScreen`, `LoginScreen`, `RegistrationScreen`, `ProfileScreen`), ViewModels (`AuthViewModel`, `ProfileViewModel`), and Navigation Compose.
- **Domain:** Business models and use cases.
- **Data:** Retrofit API services, OkHttp `AuthInterceptor` (attaching Firebase ID tokens), and repository implementations.
- **Core:** Common resource/result wrappers and network helpers (`safeApiCall`).

## Backend Architecture (Django)
- **Config:** Django settings, WSGI/ASGI, and URL routing.
- **Apps/Users:** Custom `CargoLinkUser` model, DRF `FirebaseAuthentication` backend, serializers, and profile API views.
- **Database:** PostgreSQL via `psycopg2`.
