from django.db import models

class ParticipationType(models.TextChoices):
    TRUCK_OPERATOR = 'TRUCK_OPERATOR', 'Truck Operator'
    CARGO_OWNER = 'CARGO_OWNER', 'Cargo Owner'
    BOTH = 'BOTH', 'Both'

class AccountStatus(models.TextChoices):
    ACTIVE = 'ACTIVE', 'Active'
    SUSPENDED = 'SUSPENDED', 'Suspended'
    PENDING = 'PENDING', 'Pending'

class CargoLinkUser(models.Model):
    firebase_uid = models.CharField(max_length=128, unique=True, db_index=True)
    name = models.CharField(max_length=255)
    email = models.EmailField(unique=True)
    phone = models.CharField(max_length=32)
    participation_type = models.CharField(
        max_length=32,
        choices=ParticipationType.choices,
        default=ParticipationType.CARGO_OWNER
    )
    account_status = models.CharField(
        max_length=32,
        choices=AccountStatus.choices,
        default=AccountStatus.ACTIVE
    )
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return f"{self.name} ({self.email}) - {self.participation_type}"
