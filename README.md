
| Jenkins status  |
| ------------- | 
| ![alt text](http://40.112.78.100:7070/buildStatus/icon?job=backend-pipeline)  |

[Endpointy w Swagger UI](http://40.112.78.100:8080/swagger-ui/)

# Dev Info

Żeby dostać się do mysqla:
```
docker exec -it mysql_container mysql -u root -p
```
Żeby wykonać skrypt z przykładowymi danymi:
```
docker exec -it mysql_container mysql -u root -p -e "use ziwg_db; $(cat /var/lib/jenkins/workspace/backend-pipeline/script.txt)"
```
Publiczne IP maszyny:
```
40.112.78.100
```
Logowanie:
```
Login: azureuser
Hasło: GIsyXTB6DzqhJ7YWLKUX
```

Credentiale do roota w MySQL:
```
Login: root
Hasło: admin12345@@
```

# Rejestracja
POST na `http://40.112.78.100:8080/api/v1/auth/registration/code/generate` z takim body:
```
{
    "pesel": "123456789",
    "verification_type": 1
}
```
Gdzie `verification_type` jest 0 dla SMSa i 1 dla maila. Serwer wysyła wygenerowany 6-cyfrowy kod za pośrednictwem wybranego kanału komunikacji i odsyła następujące body:
```
{
    "registration_code": "566483",
    "verify_api_path": "/api/v1/auth/registration/code/verify/GKBrBSSCjsAdH54S1ovJJIjPJD7sLw"
}
```
Tymczasowo `registration_code` jest wysyłany dopóki nie będą zaimplementowane wysyłanie SMSów oraz maili. Następnie wysyła się kolejny POST na adres z `verify_api_path` z takim body:
```
{
    "registration_code": "566483"
}
```
Serwer weryfikuje czy kod się zgadza i odsyła następujące body:
```
{
    "name": "Jan",
    "surname": "Kowalski",
    ... może reszta danych
    "register_api_path": "/api/v1/auth/registration/4Cojpo3Cq16DQ6EiLxjBQhLu3HJzBg"
}
```
Front może teraz wyświetlić wszystkie dane (oczywiście bez możliwości edycji) pobrane z serwera (na podstawie PESEL) i udostępnić userowi wpisanie hasła, które następnie należy wysłać w takim body POSTem na `register_api_path`:
```
{
    "password":123456
}
```
Serwer dokonuje rejestracji użytkownika i w sumie tyle. 
