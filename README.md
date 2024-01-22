# custom-provider
### desc
實作一個cusotmer-provider

## must know
1. docker
2. maven
3. postgres sql

### step-1 package the project as jar
1. git clone project
2. cd to this project
3. run : `mvn package spring-boot:repackage`

### step-2 run docker compose
1. run : `docker compose up -d ` (check volumes)
2. go to container postgres sql to create db (defined in thid project)  , cmd : `docker exec -it postgres bash`
3. run ddl、dml in the db what you created , cmd : `pssql -U username -d database -f script.ddl`
4. go to keycloak web `localhost:80/auth` to log in , reference nginx.conf 
5. check out the user federation has a custom provider

### Reference Documentation

For further reference, please consider the following sections:

* [Using Custom User Providers with Keycloak | Baeldung](https://www.baeldung.com/java-keycloak-custom-user-providers)
