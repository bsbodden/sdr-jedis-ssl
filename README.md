# Spring Data Redis w/ SSL

Example of configuring Spring Data Redis with SSL. This example used Heroku's EnvKeyStore library to dynamically 
create a KeyStore from environment variables.

The EnvKeyStore library follows the 12-factor manifesto and uses environment variables to store secrets and configuration.

* See [EnvKeyStore](https://github.com/heroku/env-keystore)
* See [12-factor manifesto](http://12factor.net/)
* See [Spring Data Redis](http://projects.spring.io/spring-data-redis/)
* See [Spring Data Redis SSL](http://docs.spring.io/spring-data/data-redis/docs/current/reference/html/#redis:ssl)

## Create Secrets

If you’re terminating an SSL connection on the server side, you have to manage a secret key, a public certificate and a password. 
All of these can be stored as environment variables the EnvKeyStore can extract.

```{bash}
$ openssl genrsa -des3 -passout pass:x -out server.pass.key 2048
...
$ openssl rsa -passin pass:x -in server.pass.key -out server.key
writing RSA key
$ rm server.pass.key
$ openssl req -new -key server.key -out server.csr
...
Country Name (2 letter code) [AU]:US
State or Province Name (full name) [Some-State]:California
...
A challenge password []:
...
$ openssl x509 -req -days 365 -in server.csr -signkey server.key -out server.crt
```

Set the environment variables:

```{bash}
Now set your environment variables thusly:

$ export KEYSTORE_KEY="$(cat server.key)"
$ export KEYSTORE_CERT="$(cat server.crt)"
$ export KEYSTORE_PASSWORD="password"
```

Set environment variables in your application:

```{properties}
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.password=greatpassword
spring.redis.ssl=true
```

Launch the Spring Boot application:

```{bash}   
./mvnw spring-boot:run
```