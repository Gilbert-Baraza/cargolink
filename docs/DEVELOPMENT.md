# CargoLink Development Guide - Phase 1

## 1. Firebase Setup
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/).
2. Enable **Email/Password** under Authentication providers.
3. Register an Android app with package name `com.example.cargolink`.
4. Download `google-services.json` and place it in `app/google-services.json`.
5. For Django backend verification, generate a Firebase Admin SDK Service Account JSON key from project settings, save it as `backend/config/firebase-service-account.json`, and reference it in `.env`.

---

## 2. Django Backend Setup
1. Navigate to the `backend/` directory:
   ```bash
   cd backend
   ```
2. Create and activate a Python virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements/base.txt
   ```
4. Configure environment variables:
   - Copy `.env.example` to `.env` and fill in your PostgreSQL credentials and Firebase service account path.
5. Run migrations:
   ```bash
   python manage.py makemigrations
   python manage.py migrate
   ```
6. Run automated tests:
   ```bash
   python manage.py test
   ```
7. Start the development server:
   ```bash
   python manage.py runserver 0.0.0.0:8000
   ```

---

## 3. Android App Setup
1. Open the project root in Android Studio.
2. Ensure `app/google-services.json` is present.
3. Configure the API Base URL in `AppConstants.kt`:
   - For Android Emulator: `http://10.0.2.2:8000/`
   - For physical device: `http://<your-machine-ip>:8000/`
4. Sync project with Gradle files.
5. Build and run the app on an emulator or device (API 26+).
