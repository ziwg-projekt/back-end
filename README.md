
| Jenkins status  |
| ------------- | 
| ![alt text](http://40.112.78.100:7070/buildStatus/icon?job=backend-pipeline)  |



# Dev Info

Żeby dostać się do mysqla:
```
docker exec -it mysql_container mysql -u root -p
```

Publiczne IP maszyny:
```
40.112.78.100
```


Logowanie:
```
40.112.78.100
Login: azureuser
Hasło: GIsyXTB6DzqhJ7YWLKUX
```

Credentiale do roota w MySQL:
```
Login: root
Hasło: admin12345@@
```

Maszyna działa 24/7
Są dwa robocze foldery w katalogu /home - /azureuser i /data. 
/azureuser jest dzielony z innymi komponentami systemu, całość nie przekroczy 30GB
/data jest póki co na dysku 50GB, tutaj bez problemu można zwiększyć do 32TB - proponuję w tym folderze wrzucać co tam potrzeba, tutaj też leży volume MySQL
volume Jenkinsa jest standardowo w środku dockera
