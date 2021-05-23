
| Jenkins status  |
| ------------- | 
| ![alt text](http://23.102.47.94:7070/buildStatus/icon?job=backend-pipeline)  |

[Endpointy w Swagger UI](http://23.102.47.94:8080/swagger-ui/)

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

# Logika wizyt

## Wizyty do wyświetlenia dla obywatela 

Wszystkie wolne terminy szczepień (appointments) w szpitalu o danym id - GET na `/api/v1/hospitals/1/appointments?page=0&size=2`, można zdefiniować stronę i rozmiar strony, żeby to jakoś sensownie wyglądało przy większej liczbie wizyt, w odpowiedzi jest info o paginacji:
```
    "pageable": {
        "sort": {
            "sorted": false,
            "unsorted": true,
            "empty": true
        },
        "offset": 0,
        "pageSize": 2,
        "pageNumber": 0,
        "paged": true,
        "unpaged": false
    },
    "last": false,
    "totalPages": 4,
    "totalElements": 7,
    "number": 0,
    "sort": {
        "sorted": false,
        "unsorted": true,
        "empty": true
    },
    "size": 2,
    "first": true,
    "numberOfElements": 2,
    "empty": false
```
Można pominąć parametry paginacji wysyłając po prostu GET na `/api/v1/hospitals/1/appointments`, wtedy zostaną zwrócone wszystkie wizyty.


## Wizyty do wyświetlenia dla szpitala
Dla szpitala będą zwracane wszystkie terminy szczepień (nawet te zajęte), wystarczy GET na `/api/v1/users/self/appointments` z uprawnieniami szpitala (przed tym trzeba się oczywiście zalogować), szpital jest wyciągany z kontekstu i zwracane są wszystkie terminy (i wolne i zajęte). Możliwa jest paginacja taka jak opisana wyżej, zgodnie z tymi samymi zasadami.  

Wprowadzone został dodatkowe opcje, domyślnie zwracane są wizyty o wszystkich stanach (AVAILABLE, ASSIGNED, MADE). Żeby to zmodyfikować wystarczy ustawić odpowiednią wartość logiczną, np. wysyłając zapytanie na endpoint poniżej dostaniemy wszystkie wizyty oprócz wizyt ze statusem AVAILABLE (domyślnie wszystkie są na `true`):
```
http://localhost:8080/api/v1/users/self/appointments?available=false&made=true&assigned=true
```
Można też oczywiście dodać paginację i nie trzeba zawierać wszystkich parametrów jako, że domyślnie są ustawione na `true`:
```
http://localhost:8080/api/v1/users/self/appointments?available=false&page=0&size=2
```

## Zapisanie na termin szczepienia
Obywatel może się zapisać na dany termin szczepienia. W tym celu PATCH na `/api/v1/appointments/{id}/actions/enroll` zgodnie z danym ID terminu szczepienia. Dostępne oczywiście po zalogowaniu i posiadaniu uprawnień obywatela. 

## Wprowadzanie szczepionek do systemu
Szczepionki można wprowadzić do systemu wysyłając POST na `/api/v1/users/self/vaccines` w formacie:
```
[
    {
    "code": "xddddddd",
    "company_name": "AstraZeneca"
    },
    {
    "code": "xdddsgfddddd",
    "company_name": "Johnson&Johnson"
    }
]
```
Czyli lista JSONków, muszą być oczywiście walidne nazwy firm no i trzeba być zalogowanym na szpital, inaczej szczepionka nie zostanie dodana do systemu. Poprawnie sformatowane szczepionki zostają wprowadzone do systemu, automatycznie tworzą się wizyty (obecnie dla uproszczenia w godzinach 7-15 w dni powszednie, zależnie od liczby lekarzy) i na te wizyty mogą się zapisywać obywatele.

## Zmiana statusu szczepienia
Dwa endpointy dla szpitala. W zależności od czego czy szczepienie się uda bądź nie uda (np. pacjent nie przyjdzie) to klika odpowiedni button. Żeby oznaczyć jako wykonane to PATCH na  `/api/v1/appointments/{id}/actions/made`, a żeby oznaczyć jako niewykonane to PATCH na  `/api/v1/appointments/{id}/actions/not-made`, tworzy się wtedy kolejna wizyta ze statusem AVAILABLE, w miejsce tej która się nie odbyła, z tą samą dawką szczepionki.

## Dodawanie lekarzy 
Wysyłając PUT na `/api/v1/doctors` można dodać lekarzy do szpitala. 

# Rejestracja obywateli z portalu szpitala
## Rejestracja do systemu
Get na `/api/v1/citizens/all/{pesel}` z danym PESEL (obywatel podaje go), w odpowiedzi są zwracane jego dane z rządowego API:
```
{
    "name": "Anna",
    "surname": "Zamojska",
    "pesel": "56111245968",
    "email": "aaa@aaa.aaa",
    "phone_number": "767456123"
}
```
Następnie POST na `/api/v1/auth/registration/hospital/citizen/{pesel}/register` z takim body:
```
{
    "password": "123456",
    "username": "testuserd",
    "city": "Kraków",
    "street": "Wrocławska",
    "street_number": "19"
}
```
I użytkownik jest zarejestrowany w systemie.

## Rejestracja na szczepienie
Post na `/api/v1/appointments//{id}/hospital/actions/enroll` z takim body:
```
{
    "pesel": "56111245968"
}
```
W odpowiedzi 204 jeśli pomyślnie, oczywiście trzeba być zalogowanym na szpital.
