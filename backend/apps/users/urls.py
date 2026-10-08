from django.urls import path
from .views import user_me_view

urlpatterns = [
    path('me/', user_me_view, name='user-me'),
]
