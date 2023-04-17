package com.redis.sdr;

import com.heroku.sdk.EnvKeyStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
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

  @Bean
  public JedisConnectionFactory redisConnectionFactory() throws IOException, GeneralSecurityException {
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName(host);
    redisStandaloneConfiguration.setPort(port);
    redisStandaloneConfiguration.setPassword(password);

    JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfigurationBuilder =
        JedisClientConfiguration.builder();

    if (sslEnabled) {
      EnvKeyStore eks = EnvKeyStore.create("KEYSTORE_KEY", "KEYSTORE_CERT", "KEYSTORE_PASSWORD");
      KeyStore trustStore = eks.keyStore();

      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(trustStore);

      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
      tmf.init(trustStore);

      SSLContext sc = SSLContext.getInstance("TLSv1.2");
      sc.init(null, tmf.getTrustManagers(), new SecureRandom());

      JedisPoolConfig poolConfig = new JedisPoolConfig();

      jedisClientConfigurationBuilder //
          .useSsl() //
          .hostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier()) //
          .sslSocketFactory(sc.getSocketFactory()).and() //
          .connectTimeout(Duration.of(10, ChronoUnit.MINUTES)) //
          .readTimeout(Duration.of(5, ChronoUnit.DAYS)) //
          .usePooling() //
          .poolConfig(poolConfig);
    }

    JedisClientConfiguration jedisClientConfiguration = jedisClientConfigurationBuilder.build();

    return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
  }
}
