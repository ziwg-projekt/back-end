# back-end

//test

Chyba tutaj leci też serwer?

Publiczne IP maszyny:
40.112.78.100

Otwarte porty:
22: SSH
7070: Jenkins w dockerze
8080: tomcat na żywo
9090: PHPMyAdmin

Zamknięte porty:
Każdy inny, w szczególności wrażliwe jak web czy 3306 dla MySQL - jak trzeba coś otworzyć to dajcie znać

Logowanie:
40.112.78.100
Login: azureuser
Hasło: GIsyXTB6DzqhJ7YWLKUX

Konto administratora do postawionych rzeczy:
Login: admin
Hasło: admin12345@@
initialPassword Jenkinsa: 80f5747ea05944ff8dd1a8a601f0fbf8
Hasło możliwe że do zmiany, jak ktoś będzie chciał osobiście zostać administratorem czegoś, np. bazy danych czy jenkinsa

Maszyna działa 24/7
Są dwa robocze foldery w katalogu /home - /azureuser i /data. 
/azureuser jest dzielony z innymi komponentami systemu, całość nie przekroczy 30GB
/data jest póki co na dysku 50GB, tutaj bez problemu można zwiększyć do 32TB - proponuję w tym folderze wrzucać co tam potrzeba, tutaj też leży volume MySQL
volume Jenkinsa jest standardowo w środku dockera
