# Implementation Plan - Phase 1: Authentication & User Roles Foundation

This plan outlines the implementation of Phase 1 for CargoLink, establishing the end-to-end authentication and user profile foundation using **Firebase Authentication** on Android and **Django REST Framework + PostgreSQL** on the backend.

## User Review Required

> [!IMPORTANT]
> - **Firebase Project Configuration**: You will need to create a Firebase project, enable Email/Password authentication, and place your `google-services.json` in `app/`.
> - **Django & PostgreSQL**: Django verifies Firebase ID tokens using the Firebase Admin SDK. You will need a PostgreSQL instance and environment variables (`.env`) configured for Django.

## Proposed Changes

### Backend (`backend/`)
#### [NEW] [backend/requirements/base.txt](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/backend/requirements/base.txt)
- Python dependencies: Django, djangorestframework, firebase-admin, psycopg2-binary, python-dotenv, corsheaders.

#### [NEW] [backend/.env.example](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/backend/.env.example)
- Environment variable template for Django and PostgreSQL (`DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD`, `DATABASE_HOST`, `DATABASE_PORT`, `DJANGO_SECRET_KEY`, `FIREBASE_CREDENTIALS_PATH`).

#### [NEW] [backend/config/settings.py](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/backend/config/settings.py) & [urls.py](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/backend/config/urls.py)
- Django settings configured for PostgreSQL, CORS, and DRF.

#### [NEW] [backend/apps/users/models.py](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/backend/apps/users/models.py)
- CargoLink `User` model (`firebase_uid`, name, email, phone, participation_type: `TRUCK_OPERATOR`, `CARGO_OWNER`, `BOTH`, account_status, timestamps).

#### [NEW] [backend/apps/users/authentication.py](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/backend/apps/users/authentication.py)
- DRF Authentication backend validating Firebase ID tokens using Firebase Admin SDK.

#### [NEW] [backend/apps/users/views.py](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/backend/apps/users/views.py) & [serializers.py](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/backend/apps/users/serializers.py)
- Endpoints for `GET /api/users/me/`, `POST /api/users/me/`, `PATCH /api/users/me/`.

#### [NEW] [backend/apps/users/tests.py](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/backend/apps/users/tests.py)
- Automated tests covering token validation, profile creation, retrieval, updates, and permissions.

### Android App (`app/`)
#### [MODIFY] [gradle/libs.versions.toml](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/gradle/libs.versions.toml) & [app/build.gradle.kts](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/app/build.gradle.kts)
- Add Firebase BOM, Firebase Auth KTX, and Google Services plugin.

#### [NEW] [app/src/main/java/com/example/cargolink/core/network/AuthInterceptor.kt](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/app/src/main/java/com/example/cargolink/core/network/AuthInterceptor.kt)
- OkHttp interceptor attaching Firebase ID token Bearer token to API requests.

#### [NEW] [app/src/main/java/com/example/cargolink/data/remote/ApiService.kt](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/app/src/main/java/com/example/cargolink/data/remote/ApiService.kt)
- Retrofit interface for user profile endpoints.

#### [NEW] [app/src/main/java/com/example/cargolink/data/repository/AuthRepositoryImpl.kt](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/app/src/main/java/com/example/cargolink/data/repository/AuthRepositoryImpl.kt) & User Repository
- Repositories wrapping Firebase Auth and Django API.

#### [NEW] [app/src/main/java/com/example/cargolink/presentation/screens/WelcomeScreen.kt](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/app/src/main/java/com/example/cargolink/presentation/screens/WelcomeScreen.kt)
- Welcome screen with Create Account & Login buttons.

#### [NEW] [app/src/main/java/com/example/cargolink/presentation/screens/LoginScreen.kt](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/app/src/main/java/com/example/cargolink/presentation/screens/LoginScreen.kt)
- Login screen with email/password and validation.

#### [NEW] [app/src/main/java/com/example/cargolink/presentation/screens/RegistrationScreen.kt](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/app/src/main/java/com/example/cargolink/presentation/screens/RegistrationScreen.kt)
- Registration screen with participation type selection (Truck Operator, Cargo Owner, Both).

#### [NEW] [app/src/main/java/com/example/cargolink/presentation/screens/ProfileScreen.kt](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/app/src/main/java/com/example/cargolink/presentation/screens/ProfileScreen.kt)
- Profile screen displaying Django user profile and logout action.

#### [MODIFY] [CargoLinkNavGraph.kt](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/app/src/main/java/com/example/cargolink/presentation/navigation/CargoLinkNavGraph.kt) & [MainActivity.kt](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/app/src/main/java/com/example/cargolink/MainActivity.kt)
- Wire up auth navigation flow and Firebase Auth state check on startup.

### Documentation (`docs/`)
#### [NEW] [README.md](file:///C:/Users/Engineer Jed/AndroidStudioProjects/CargoLink/README.md) & Docs (`docs/ARCHITECTURE.md`, `docs/API.md`, `docs/DEVELOPMENT.md`)
- Complete setup and architecture documentation.

## Verification Plan

### Automated Tests
- Run Django backend tests via `python manage.py test`.
- Run Android unit tests via `gradle_build("app:testDebugUnitTest")`.

### Manual Verification
- Verify Android app compilation and navigation flows.
