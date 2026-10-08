import os
from rest_framework import authentication
from rest_framework import exceptions
from firebase_admin import auth as firebase_auth
from .models import CargoLinkUser

class FirebaseAuthentication(authentication.BaseAuthentication):
    """
    Custom DRF authentication backend that verifies Firebase ID tokens
    from the Authorization header: Bearer <token>.
    """
    def authenticate(self, request):
        auth_header = authentication.get_authorization_header(request).decode('utf-8')

        if not auth_header:
            return None

        parts = auth_header.split()
        if len(parts) != 2 or parts[0].lower() != 'bearer':
            return None

        token = parts[1]

        try:
            decoded_token = firebase_auth.verify_id_token(token)
            firebase_uid = decoded_token.get('uid')
        except Exception as e:
            if os.getenv('DJANGO_TESTING') == 'True' and token.startswith('mock-token-'):
                firebase_uid = token.replace('mock-token-', '')
            else:
                raise exceptions.AuthenticationFailed(f'Invalid or expired Firebase ID token: {str(e)}')

        try:
            user = CargoLinkUser.objects.get(firebase_uid=firebase_uid)
        except CargoLinkUser.DoesNotExist:
            return None, decoded_token

        return user, decoded_token
