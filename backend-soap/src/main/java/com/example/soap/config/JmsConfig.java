package com.example.soap.config;

import jakarta.jms.ConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JmsConfig {
  @Bean
  public ConnectionFactory connectionFactory(
      @Value("${broker.url}") String url,
      @Value("${broker.user}") String user,
      @Value("${broker.pass}") String pass) {
    return new ActiveMQConnectionFactory(url, user, pass);
  }
}
