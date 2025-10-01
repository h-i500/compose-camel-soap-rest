package com.example.soap.config;

import com.example.soap.ws.OrderServiceEndpointImpl;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.xml.ws.Endpoint;

@Configuration
public class CxfConfig {

  @Bean
  public Endpoint orderEndpoint(Bus bus, OrderServiceEndpointImpl impl) {
    EndpointImpl ep = new EndpointImpl(bus, impl);
    ep.publish("/OrderService");
    return ep;
  }

}
