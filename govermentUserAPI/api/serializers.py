from rest_framework import serializers

from .models import Person


class PersonSerializer(serializers.ModelSerializer):

    class Meta:
        model = Person
        fields = '__all__'
        lookup_field = 'pesel'
        extra_kwargs = {
            'url': {'lookup_field': 'pesel'}
        }
