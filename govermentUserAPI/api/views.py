from rest_framework import viewsets
from rest_framework.authentication import TokenAuthentication
from rest_framework.permissions import IsAuthenticated

from api.models import Person
from api.serializers import PersonSerializer


class PersonViewSet(viewsets.ModelViewSet):
    authentication_classes = (TokenAuthentication,)
    permission_classes = (IsAuthenticated,)
    queryset = Person.objects.all()
    serializer_class = PersonSerializer
    lookup_field = 'pesel'
