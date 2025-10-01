package com.example.websoap.config;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// ★ 生成後に “あなたの” Port インタフェース名に合わせて import/型を直してください
// 例）com.example.order.OrderService あるいは com.example.order.OrderPortType 等
import com.example.order.OrderService; // ★ここを生成結果に合わせる

@Configuration
public class SoapClientConfig {

  @Value("${soap.endpoint}")
  private String soapEndpoint;

  @Value("${soap.connect-timeout:3000}")
  private long connectTimeout;

  @Value("${soap.read-timeout:5000}")
  private long readTimeout;

  @Bean
  public OrderService orderServicePort() { // ★戻り値の型も生成結果に合わせる
    JaxWsProxyFactoryBean f = new JaxWsProxyFactoryBean();
    f.setServiceClass(OrderService.class);
    f.setAddress(soapEndpoint);
    OrderService port = (OrderService) f.create();

    Client client = ClientProxy.getClient(port);
    HTTPConduit conduit = (HTTPConduit) client.getConduit();
    HTTPClientPolicy policy = new HTTPClientPolicy();
    policy.setConnectionTimeout(connectTimeout);
    policy.setReceiveTimeout(readTimeout);
    policy.setAllowChunking(false);
    conduit.setClient(policy);

    return port;
  }
}
