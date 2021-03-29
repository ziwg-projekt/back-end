from django.db import models


class Person(models.Model):
    name = models.CharField(max_length=30)
    surname = models.CharField(max_length=50)
    pesel = models.CharField(max_length=11)
    email = models.EmailField()
    phone_number = models.CharField(max_length=9)

    def __str__(self):
        return f"{self.pesel} - {self.name} {self.surname}"
