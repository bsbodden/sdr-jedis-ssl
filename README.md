# Spring Boot Redis w/ TLS

Example of configuring Spring Boot with TLS. This example used Heroku's EnvKeyStore library to dynamically 
create a KeyStore from environment variables.

See [Redis TLS Support](https://redis.io/docs/management/security/encryption/) and [Redis Enterprise TLS client connections](https://docs.redis.com/latest/rs/security/tls/enable-tls/#client)

This sample tries to mimic [redis-cli](https://redis.io/docs/ui/cli/) options.

Set the environment variables:

```{bash}
Now set your environment variables thusly:

#Private key file to authenticate with. Equivalent to --key in redis-cli
$ export KEYSTORE_KEY="$(cat client.key)"

#Client certificate to authenticate with. Equivalent to --cert in redis-cli
$ export KEYSTORE_CERT="$(cat client.crt)"

# Password to use when creating java keystore
$ export KEYSTORE_PASSWORD="password"

#CA Certificate file to verify with. Equivalent to --cacert in redis-cli
$ export TRUSTED_CERT="$(cat ca.crt)
```

Set environment variables in your application:

```{properties}
spring.redis.host=127.0.0.1
spring.redis.port=6379
spring.redis.password=password
# use tls. Equivalent to --tls in redis-cli.
spring.redis.tls=true
#https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html#sslcontext-algorithms#
spring.redis.tls.version=TLS
#Allow insecure TLS connection by skipping cert validation. Equivalent to --insecure in redis-cli
spring.redis.insecure=false
```

Launch the Spring Boot application:

```{bash}   
./mvnw spring-boot:run
```