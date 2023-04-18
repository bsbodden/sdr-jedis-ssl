package com.redis.sdr;

import com.heroku.sdk.EnvKeyStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Configuration
class RedisConfiguration {

  @Value("${spring.redis.host}")
  private String host;

  @Value("${spring.redis.port}")
  private int port;

  @Value("${spring.redis.password}")
  private String password;

  @Value("${spring.redis.ssl:false}")
  private boolean sslEnabled;


  @Value("${spring.redis.tls.version:TLS}")
  private String tlsVersion;

  @Bean
  public JedisConnectionFactory redisConnectionFactory() throws IOException, GeneralSecurityException {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(host);
    redisStandaloneConfiguration.setPort(port);
    redisStandaloneConfiguration.setPassword(password);

    JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfigurationBuilder =
        JedisClientConfiguration.builder();

    if (sslEnabled) {

      //create trust store neeeded to verify
      KeyStore trustStore = EnvKeyStore.createWithRandomPassword("TRUSTED_CERT").keyStore();
      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
      tmf.init(trustStore);


      //create keystore only needed for mtls
      EnvKeyStore eks = EnvKeyStore.create("KEYSTORE_KEY", "KEYSTORE_CERT", "KEYSTORE_PASSWORD");
      KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(eks.keyStore(), eks.password().toCharArray());


      SSLContext sc = SSLContext.getInstance(tlsVersion);
      sc.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

      JedisPoolConfig poolConfig = new JedisPoolConfig();

      jedisClientConfigurationBuilder //
          .useSsl() //
          .sslSocketFactory(sc.getSocketFactory()).and() //
          .usePooling() //
          .poolConfig(poolConfig);
    }

    JedisClientConfiguration jedisClientConfiguration = jedisClientConfigurationBuilder.build();

    return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
  }
}
