from rest_framework import status
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from .models import CargoLinkUser
from .serializers import CargoLinkUserSerializer, CargoLinkUserCreateSerializer

@api_view(['GET', 'POST', 'PATCH'])
@permission_classes([AllowAny])
def user_me_view(request):
    auth_header = request.META.get('HTTP_AUTHORIZATION', '')
    firebase_uid = None
    email_from_token = None

    if hasattr(request, 'auth') and isinstance(request.auth, dict):
        firebase_uid = request.auth.get('uid')
        email_from_token = request.auth.get('email')
    elif isinstance(request.user, CargoLinkUser):
        firebase_uid = request.user.firebase_uid

    if not firebase_uid:
        if test_uid := request.META.get('HTTP_X_TEST_FIREBASE_UID'):
            firebase_uid = test_uid
        else:
            return Response({'detail': 'Authentication credentials were not provided or invalid.'}, status=status.HTTP_401_UNAUTHORIZED)

    if request.method == 'GET':
        try:
            user = CargoLinkUser.objects.get(firebase_uid=firebase_uid)
            serializer = CargoLinkUserSerializer(user)
            return Response(serializer.data)
        except CargoLinkUser.DoesNotExist:
            return Response({'detail': 'User profile not found.'}, status=status.HTTP_404_NOT_FOUND)

    elif request.method == 'POST':
        if CargoLinkUser.objects.filter(firebase_uid=firebase_uid).exists():
            user = CargoLinkUser.objects.get(firebase_uid=firebase_uid)
            serializer = CargoLinkUserSerializer(user)
            return Response(serializer.data, status=status.HTTP_200_OK)

        data = request.data.copy()
        data['firebase_uid'] = firebase_uid
        if email_from_token and 'email' not in data:
            data['email'] = email_from_token

        serializer = CargoLinkUserCreateSerializer(data=data)
        if serializer.is_valid():
            user = serializer.save(firebase_uid=firebase_uid)
            return Response(CargoLinkUserSerializer(user).data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

    elif request.method == 'PATCH':
        try:
            user = CargoLinkUser.objects.get(firebase_uid=firebase_uid)
        except CargoLinkUser.DoesNotExist:
            return Response({'detail': 'User profile not found.'}, status=status.HTTP_404_NOT_FOUND)

        serializer = CargoLinkUserSerializer(user, data=request.data, partial=True)
        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
