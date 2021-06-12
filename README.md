## Multi-Tenant Spring Boot Application          [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Spring Boot application that utilises a multi-tenancy architecture by providing multiple databases, one for each tenant. 

Tenants, respectively users, are stored in the default database `db.mv.db`. All requests toward  `/api/v1/user` are subsequently accessing it while the rest is accessing the db of the authenticated user.

### How to initialise

In order to use the application, a user must be logged in This authentication process returns a Bearer token which is used for all other requests. 

#### 1. *POST* request to `api/v1/user/signup`.
```json
{
  "email": "user@example.com",
  "password": "asdfasdfasdf",
  "confirmation": "asdfasdfasdf"
}
```  

#### 2. *POST* request to `api/v1/user/authentication`
```json
{
  "email": "user@example.com",
  "password": "asdfasdfasdf"
}
```

### How to use

The application opens a [h2-console](http://localhost:8080/h2-console) to easily manage all available databases. Each one is generated right after the first access / request to it. The filename is the hashed email adress `database/[hashed_email].mv.db`.

- **Movie endpoint**
  - POST `api/v1/movie`:
  - GET `api/v1/movie/{id}`:
- **User endpoint**
  - PATCH `api/v1/user/email`

#### Example request to `api/v1/movie`
```json
{
  "title": "The Hitchhiker's Guide to the Galaxy",
  "runtime": 109,
  "releaseDate": "2005-04-28"
}
```

#### Example request to `api/v1/user/email`
```json
{
  "email": "new_user@example.com"
}
```
