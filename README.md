## Multi-Tenant Spring Boot Application

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Spring Boot application showcasing an implementation of multi-tenancy. It provides multiple databases, one for each tenant, which can be created dynamically. The registered users are stored in the default database generated at startup. Depending on the authenticated user and accessed endpoint, the application dynamically selects the correct database.

The application can be reached at `localhost:8080`.

### Creating a database

First a user has to be created and authenticated. To register a new user, a POST request to `api/v1/user/signup` must be sent.

#### Signup request:
```json
{
  "email": "user@example.com",
  "password": "asdfasdfasdf",
  "confirmation": "asdfasdfasdf"
}
```

After the successful registration, the user must be authenticated by sending a POST request to `api/v1/authentication`. A `Bearer` token is returned which is used for future requests.   

#### Authentication request
```json
{
  "email": "user@example.com",
  "password": "asdfasdfasdf"
}
```

### Accessing the database

I have created an endpoint `api/v1/movie` with sample data to test this setup. The email address of a user can also be changed at `api/v1/user/email`. 

#### Additional HTTP header
In order to change the email address, the header `X-Datasource: default` must be sent along. Otherwise, the lookup process would return the database of the authenticated user. It is only needed for `api/v1/user/email` and should not be used anywhere else.

#### Response of `GET` request `api/v1/movie/1`:

```json
{
  "id": 1,
  "title": "Movie title",
  "runtime": 123,
  "releaseDate": "2020-01-01"
}
```

#### `PATCH` request to change email address:
```json
{
  "email": "new_user@example.com"
}
```

The database is generated after the first access / request. The hashed email address serves as filename `database/[hashed_email].mv.db` and can be accessed through [localhost:8080/h2-console](http://localhost:8080/h2-console) (username and password is `admin`).

#### Information about schema generation

Because of the dynamically generated databases, JPA (Hibernate) can't automatically initialise the databases. It instead generates a file [create.sql](src/main/resources/create.sql) which will be executed as soon as a new database is created. To update the file simply change [application.yml](src/main/resources/application.yml) accordingly:
```yml
spring.jpa.properties.javax.persistence.schema-generation.scripts.action: create
```
