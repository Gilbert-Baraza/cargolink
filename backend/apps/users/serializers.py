from rest_framework import serializers
from .models import CargoLinkUser

class CargoLinkUserSerializer(serializers.ModelSerializer):
    class Meta:
        model = CargoLinkUser
        fields = [
            'id',
            'firebase_uid',
            'name',
            'email',
            'phone',
            'participation_type',
            'account_status',
            'created_at',
            'updated_at',
        ]
        read_only_fields = ['id', 'firebase_uid', 'account_status', 'created_at', 'updated_at']

class CargoLinkUserCreateSerializer(serializers.ModelSerializer):
    class Meta:
        model = CargoLinkUser
        fields = [
            'firebase_uid',
            'name',
            'email',
            'phone',
            'participation_type',
        ]
        read_only_fields = ['firebase_uid']
