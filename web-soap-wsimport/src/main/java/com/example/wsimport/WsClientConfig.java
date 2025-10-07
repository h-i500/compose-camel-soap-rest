package com.example.wsimport;

import com.example.wsimport.client.OrderService;
import com.example.wsimport.client.OrderService_Service;
import jakarta.xml.ws.BindingProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import javax.xml.namespace.QName;

@Configuration
public class WsClientConfig {

  @Bean
  public OrderService orderServicePort() throws Exception {
    // WSDLの参照方法：クラスパス上の固定WSDL or ファイルURL or リモートURL
    URL wsdl = WebSoapWsimportApplication.class.getResource("/wsdl/OrderService.wsdl");
    // WSDLのtargetNamespace/name に合わせる（生成クラスが持っている定数でもOK）
    QName serviceName = new QName("http://example.com/order", "OrderService");

    OrderService_Service service = new OrderService_Service(wsdl, serviceName);
    OrderService port = service.getOrderServicePort();

    // 実行時の接続先（Docker内なら backend-soap）
    String endpoint = System.getenv().getOrDefault(
        "SOAP_BACKEND_URL", "http://backend-soap:8080/services/OrderService");
    ((BindingProvider) port).getRequestContext()
        .put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

    return port;
  }
}
