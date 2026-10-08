import os
os.environ['DJANGO_TESTING'] = 'True'

from django.test import TestCase
from django.urls import reverse
from rest_framework import status
from rest_framework.test import APIClient
from .models import CargoLinkUser, ParticipationType

class UserProfileApiTests(TestCase):
    def setUp(self):
        self.client = APIClient()
        self.firebase_uid = "test-firebase-uid-123"
        self.email = "test@cargolink.co.ke"
        self.user = CargoLinkUser.objects.create(
            firebase_uid=self.firebase_uid,
            name="Test Operator",
            email=self.email,
            phone="+254712345678",
            participation_type=ParticipationType.TRUCK_OPERATOR
        )
        self.url = reverse('user-me')

    def test_get_profile_authenticated(self):
        self.client.credentials(HTTP_AUTHORIZATION='Bearer mock-token-' + self.firebase_uid)
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['email'], self.email)
        self.assertEqual(response.data['participation_type'], 'TRUCK_OPERATOR')

    def test_get_profile_unauthenticated(self):
        response = self.client.get(self.url)
        self.assertEqual(response.status_code, status.HTTP_401_UNAUTHORIZED)

    def test_create_profile(self):
        new_uid = "new-firebase-uid-456"
        self.client.credentials(HTTP_AUTHORIZATION='Bearer mock-token-' + new_uid)
        payload = {
            "name": "New Cargo Owner",
            "email": "cargo@cargolink.co.ke",
            "phone": "+254798765432",
            "participation_type": "CARGO_OWNER"
        }
        response = self.client.post(self.url, payload, format='json')
        self.assertEqual(response.status_code, status.HTTP_201_CREATED)
        self.assertEqual(response.data['firebase_uid'], new_uid)
        self.assertEqual(response.data['participation_type'], 'CARGO_OWNER')

    def test_update_profile(self):
        self.client.credentials(HTTP_AUTHORIZATION='Bearer mock-token-' + self.firebase_uid)
        payload = {
            "name": "Updated Name",
            "participation_type": "BOTH"
        }
        response = self.client.patch(self.url, payload, format='json')
        self.assertEqual(response.status_code, status.HTTP_200_OK)
        self.assertEqual(response.data['name'], "Updated Name")
        self.assertEqual(response.data['participation_type'], "BOTH")
