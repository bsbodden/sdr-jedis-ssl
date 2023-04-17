package com.redis.sdr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.Jedis;

@SpringBootApplication
public class DemoApplication {

	Logger logger = LoggerFactory.getLogger(DemoApplication.class);

	@Bean CommandLineRunner testConnection(JedisConnectionFactory jcf) {
		logger.info("⚙️ Testing connection...");
		return args -> {
			try (Jedis jedis = (Jedis)jcf.getConnection().getNativeConnection()) {
				jedis.set("foo", "bar");
				logger.info("Wrote key 'foo'...");
				String foo = jedis.get("foo");
				logger.info("Read key 'foo' → {}", foo);
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
