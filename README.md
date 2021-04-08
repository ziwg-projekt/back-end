
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
23.102.47.94
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

# Testowi użytkownicy
Można tworzyć ich samemu, ale obecnie w systemie jest też po jednym przykładowym użytkowniku każdego typu
- admin - username - `admin`, hasło - `adminpassword`,
- szpital - username - `first_hospital`, hasło - `password`,
- obywatel - username - `first_user`, hasło - `password`.


# Autentykacja

## Rejestracja obywatela
POST na `/api/v1/auth/registration/citizen/notify` z takim body:
```
{
    "pesel": "123456789",
    "communication_channel_type": 1
}
```
Gdzie `communication_channel_type` jest 0 dla SMSa i 1 dla maila. Jeśli okaże się, że osoba z tym peselem jest już zarejestrowana to serwer odsyła następujące body:
```
{
    "timestamp": "29-03-2021 03:10:23",
    "exception": "UserAlreadyRegisteredException",
    "message": "Person with that pesel is already registered"
}
```
Jeśli okaże się, że osoba z takim peselem istnieje, ale nie obsługuje wybranego sposobu komunikacji to serwer odsyła następujące body:
```
{
    "timestamp": "29-03-2021 07:52:34",
    "exception": "NotSupportedCommunicationChannelException",
    "message": "Person with pesel '123456789' has no phone number assigned!"
}
```
Jeżeli jest wszystko git to serwer zwraca następujące body:
```
{
    "verify_api_path": "/api/v1/auth/registration/citizen/verify?token=UXkMwFQgKDRsdrk8vBp95VjmceabGO"
}
```
Następnie wysyła się kolejny POST na adres z `verify_api_path` z takim body:
```
{
    "registration_code": "123456"
}
```
Tymczasowo `registration_code` jest sztywno ustawiony na `123456` dopóki nie będą zaimplementowane wysyłanie SMSów oraz maili. Serwer weryfikuje czy kod się zgadza i odsyła następujące body (`person` na podstawie danych z rządowego API):
```
{
    "person": {
        "name": "Jan",
        "surname": "Kowalski",
        "pesel": "123456789",
        "email": "janek@gmail.com",
        "phone_number": null
    },
    "register_api_path": "/api/v1/auth/registration/citizen/register?token=SS9sbDZR7otkblw88fo0qOQmmIdkXr"
}
```
Jeśli kod jest niepoprawny serwer odsyła takie body z statusem 401:
```
{
    "timestamp": "29-03-2021 03:06:28",
    "exception": "IncorrectRegistrationCodeException",
    "message": "Registration code is incorrect"
}
```
Bądź jeśli został przekroczony czas na wpisanie kodu (60 sekund) to odsyła takie body, również z 401:
```
{
    "timestamp": "29-03-2021 03:07:22",
    "exception": "RegistrationCodeExpiredException",
    "message": "Token expired, cause of 191s > 60s"
}
```
Front może teraz wyświetlić wszystkie dane (oczywiście bez możliwości edycji) i udostępnić obywatelowi wpisanie hasła i nazwy użytkownika, które następnie należy wysłać w takim body POSTem na `register_api_path`:
```
{
    "password": "123456",
    "username": "testuser",
    "city": "Kraków",
    "street": "Wrocławska",
    "street_number": "19"
}
```
Serwer dokonuje rejestracji użytkownika i wysyła status 200 bez body i w sumie tyle. Jeśli którykolwiek z tokenów będzie niepoprawny to dostaniemy takie info:
```
{
    "timestamp": "29-03-2021 03:08:24",
    "exception": "TokenDoesNotExistsException",
    "message": "There is no such a token"
}
```
Zaś jeśli nazwa użytkownika będzie zajęta to dostaniemy takie info:
```
{
    "timestamp": "29-03-2021 03:15:20",
    "exception": "UsernameNotAvailableException",
    "message": "Username 'admin' is in use!"
}
```

## Logowanie obywatela
Żeby zalogować się to POST na `api/v1/auth/login` z następującym body:
```
{
    "password": "123456",
    "username": "testuser"
}
```
Serwer odsyła JWT w odpowiedzi z informacją o uprawnieniach:
```
{
    "access_token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbnRlc3QiLCJpYXQiOjE2MTY5NzEyMjgsImV4cCI6MTYxNzA1NzYyOH0.2Kg0fYvNy3ZRT6NRlSg0Y5yhg0oKaRCg70-tYQxeuWEH8ixCprpuAUXedrFHD7JIVVtZjW7dUa-APNq_6WKi_g",
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
Żeby zalogować się to również POST na `/api/v1/auth/login` z następującym body:
```
{
    "password": "adminpassword",
    "username": "admin"
}
```
Serwer odsyła JWT w odpowiedzi z informacją o wszystkich uprawnieniach:
```
{
    "access_token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYxNzAyMzY0OCwiZXhwIjoxNjE3MTEwMDQ4fQ.un27rc4DziLY9Dd-X-xYrJUiJIKXszxA6kMXGennKt96PwcyuNioYXLn46KGkqu9HIIXoa1NIT1DFAKxBKqkeA",
    "type": "Bearer",
    "username": "admin",
    "authorities": [
        {
            "authority": "ROLE_HOSPITAL"
        },
        {
            "authority": "ROLE_ADMIN"
        },
        {
            "authority": "ROLE_CITIZEN"
        }
    ]
}
```

## Rejestracja szpitala
Rejestrację szpitala można wykonać tylko z poziomu admina, więc uprzednio trzeba się na niego zalogować i wykorzystać otrzymany JWT , wywołując metodę POST na `/api/v1/auth/registration/hospital/register` z następującym body:
```
{
    "password": "password",
    "username": "szpitalicho",
    "hospital_name": "szpitalisko we wroclawiu",
    "city": "Wroclaw",
    "street": "Grunwaldzka",
    "street_number": "12c"
}
``` 
Serwer powinien zwrócić status 200 bez żadnego body, bądź wyjątek o nazwie użytkownika, jeśli zarequestowana jest już w użyciu.

## Logowanie szpitala
Odbywa się tak samo jak logowanie admina i obywatela, lecz zwracany jest JWT z innymi uprawnieniami:
```
{
    "access_token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJzenBpdGFsaWNobyIsImlhdCI6MTYxNzAyNTI2OCwiZXhwIjoxNjE3MTExNjY4fQ.TAfc5_6POXz6zKP4Qn84EUMO1XweAUccoxTMKa3oqSy8qq6a51vjAB2wRXfGU3cvG0KupCCPLkyAaJ8P6KpOGA",
    "type": "Bearer",
    "username": "szpitalicho",
    "authorities": [
        {
            "authority": "ROLE_HOSPITAL"
        }
    ]
}
```
