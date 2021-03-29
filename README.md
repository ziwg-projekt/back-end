
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

# Autentykacja

## Rejestracja obywatela

POST na `http://40.112.78.100:8080/api/v1/auth/registration/citizen/notify` z takim body:
```
{
    "pesel": "123456789",
    "communication_channel_type": 1
}
```
Gdzie `communication_channel_type` jest 0 dla SMSa i 1 dla maila. Serwer odsyła następujące body:
```
{
    "verify_api_path": "/api/v1/auth/registration/citizen/verify?token=GKBrBSSCjsAdH54S1ovJJIjPJD7sLw"
}
```
Tymczasowo `registration_code` jest sztywno ustawiony na `123456` dopóki nie będą zaimplementowane wysyłanie SMSów oraz maili. Następnie wysyła się kolejny POST na adres z `verify_api_path` z takim body:
```
{
    "registration_code": "123456"
}
```
Serwer weryfikuje czy kod się zgadza i odsyła następujące body:
```
{
    "register_api_path": "/api/v1/auth/registration?token=ewMwdCUDZkcb05rJ51pHwGfN8ec3Er",
    "person": {
        "name": "Jan",
        "surname": "Kowalski",
        "pesel": "123456",
        "email": "janek@gmail.com",
        "phone_number": null
    }
}
```
Front może teraz wyświetlić wszystkie dane (oczywiście bez możliwości edycji) pobrane z serwera (na podstawie PESEL) i udostępnić userowi wpisanie hasła, które następnie należy wysłać w takim body POSTem na `register_api_path`:
```
{
    "password":"123456"
    "username":"testuser"
}
```
Serwer dokonuje rejestracji użytkownika i w sumie tyle. 

## Logowanie obywatela

Żeby zalogować się to POST na `http://40.112.78.100:8080/api/v1/auth/login` z następującym body:
```
{
    "password":"123456"
    "username":"testuser"
}
```
Serwer odsyła JWT w odpowiedzi:
```
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbnRlc3QiLCJpYXQiOjE2MTY5NzEyMjgsImV4cCI6MTYxNzA1NzYyOH0.2Kg0fYvNy3ZRT6NRlSg0Y5yhg0oKaRCg70-tYQxeuWEH8ixCprpuAUXedrFHD7JIVVtZjW7dUa-APNq_6WKi_g",
    "type": "Bearer",
    "username": "testuser",
    "authorities": [
        {
            "authority": "ROLE_CITIZEN"
        }
    ]
}
```

## Logowanie admina
Żeby zalogować się to POST na `http://40.112.78.100:8080/api/v1/auth/login` z następującym body:
```
{
    "password": "adminpassword",
    "username": "admin"
}
```

## Rejestracja szpitala
Rejestracja szpitala może zostać tylko wykonana z poziomu admina, więc uprzednio trzeba się na niego zalogować. Strzelamy POST z JWT w headerze na `http://40.112.78.100:8080/api/v1/auth/registration/hospital/register`:
```
{
    "password": "password",
    "username": "szpitalicho",
    "hospital": {
                "name": "szpitalisko we wroclawiu",
                "address": {
                    "city": "Wroclaw",
                    "street": "Grunwaldzka",
                    "house_number": "12c",
                    "latitude": 40.13,
                    "longitude": 23.1
                }
    }
}
```
