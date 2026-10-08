# CargoLink API Documentation - Phase 1

## Base URL
- Development (Emulator): `http://10.0.2.2:8000/`
- Development (Local device): `http://<your-local-ip>:8000/`
- Production: `https://api.cargolink.co.ke/`

## Authentication
All protected endpoints require a valid Firebase ID token passed in the HTTP Authorization header:
```http
Authorization: Bearer <firebase-id-token>
```

---

## Endpoints

### 1. Get Current User Profile
- **URL:** `/api/users/me/`
- **Method:** `GET`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Response (200 OK):**
  ```json
  {
    "id": 1,
    "firebase_uid": "abc123xyz...",
    "name": "Jane Doe",
    "email": "jane@cargolink.co.ke",
    "phone": "+254712345678",
    "participation_type": "TRUCK_OPERATOR",
    "account_status": "ACTIVE",
    "created_at": "2025-01-01T00:00:00Z",
    "updated_at": "2025-01-01T00:00:00Z"
  }
  ```

### 2. Create User Profile (Registration Synchronization)
- **URL:** `/api/users/me/`
- **Method:** `POST`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Body (JSON):**
  ```json
  {
    "name": "Jane Doe",
    "email": "jane@cargolink.co.ke",
    "phone": "+254712345678",
    "participation_type": "TRUCK_OPERATOR"
  }
  ```
- **Response (201 Created / 200 OK):**
  ```json
  {
    "id": 1,
    "firebase_uid": "abc123xyz...",
    "name": "Jane Doe",
    "email": "jane@cargolink.co.ke",
    "phone": "+254712345678",
    "participation_type": "TRUCK_OPERATOR",
    "account_status": "ACTIVE",
    "created_at": "2025-01-01T00:00:00Z",
    "updated_at": "2025-01-01T00:00:00Z"
  }
  ```

### 3. Update User Profile
- **URL:** `/api/users/me/`
- **Method:** `PATCH`
- **Headers:**
  - `Authorization: Bearer <token>`
- **Body (JSON):**
  ```json
  {
    "name": "Jane Updated",
    "participation_type": "BOTH"
  }
  ```
- **Response (200 OK):**
  Updated user profile object.
