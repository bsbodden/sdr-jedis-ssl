package com.redis.sdr;

import com.heroku.sdk.EnvKeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPoolConfig;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Configuration
class RedisConfiguration {
    Logger logger = LoggerFactory.getLogger(RedisConfiguration.class);
    public static final String TRUSTED_CERT = "TRUSTED_CERT";
    public static final String KEYSTORE_KEY = "KEYSTORE_KEY";
    public static final String KEYSTORE_CERT = "KEYSTORE_CERT";
    public static final String KEYSTORE_PASSWORD = "KEYSTORE_PASSWORD";
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.password}")
    private String password;

    @Value("${spring.redis.tls:false}")
    private boolean tls;

    @Value("${spring.redis.insecure:false}")
    private boolean insecure;


    @Value("${spring.redis.tls.version:TLS}")
    private String tlsVersion;

    @Bean
    public JedisConnectionFactory redisConnectionFactory() throws IOException, GeneralSecurityException {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        logger.info("Will connect to {} on port {}  ", host, port);

        if (password != null) {
            logger.info("Connection is password protected");
            redisStandaloneConfiguration.setPassword(password);
        }

        JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfigurationBuilder =
                JedisClientConfiguration.builder();
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        jedisClientConfigurationBuilder.usePooling().poolConfig(poolConfig);

        if (tls) {
            logger.info("Configuring TLS");
            jedisClientConfigurationBuilder //
                    .useSsl() //
                    .sslSocketFactory(getSslSocketFactory());
        }

        JedisClientConfiguration jedisClientConfiguration = jedisClientConfigurationBuilder.build();

        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
    }

    private SSLSocketFactory getSslSocketFactory() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException, KeyManagementException {

        //set trust manager to verify server certificate
        TrustManager[] trustManagers = null;
        if (insecure) {
            logger.info("Using insecure mode");
            trustManagers = getAllTrustingManager();
        } else {
            logger.info("Using CA certificate for verification");
            trustManagers = getTrustManagers();
        }

        //setup mTLS i.e. client key and certificate
        KeyManager[] keyManagers = null;
        if (System.getenv(KEYSTORE_KEY) != null && System.getenv(KEYSTORE_CERT) != null) {
            logger.info("Using mutual TLS");
            keyManagers = getKeyManagers();
        }

        //see for values
        // https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html#sslcontext-algorithms
        SSLContext sc = SSLContext.getInstance(tlsVersion);

        sc.init(keyManagers, trustManagers, new SecureRandom());
        SSLSocketFactory sf = sc.getSocketFactory();
        return sf;
    }


    /**
     * Get TrustManager configured with CA certificate to verify Redis Server certificate
     *
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     */
    private  TrustManager[] getTrustManagers() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {

        KeyStore trustStore = EnvKeyStore.createWithRandomPassword(TRUSTED_CERT).keyStore();
        String algorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }

    /**
     * Get KeyManager configured with client certificates used for mutual TLS
     *
     * @return
     * @throws CertificateException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     * @throws UnrecoverableKeyException
     */
    private  KeyManager[] getKeyManagers() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException {
        EnvKeyStore eks = EnvKeyStore.create(KEYSTORE_KEY, KEYSTORE_CERT, KEYSTORE_PASSWORD);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(eks.keyStore(), eks.password().toCharArray());
        return kmf.getKeyManagers();
    }

    private  TrustManager[] getAllTrustingManager() {
        TrustManager[] trustAllManagers = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                            throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                            throws CertificateException {
                    }
                }
        };
        return trustAllManagers;
    }
}
