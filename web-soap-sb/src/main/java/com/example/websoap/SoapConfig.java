// src/main/java/com/example/websoap/SoapConfig.java
package com.example.websoap;

import com.example.websoap.dto.PlaceOrderRequest;
import com.example.websoap.dto.PlaceOrderResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.WebServiceTemplate;

@Configuration
public class SoapConfig {

  @Bean
  public Jaxb2Marshaller marshaller() {
    Jaxb2Marshaller m = new Jaxb2Marshaller();
    // ★ これに変更：DTOクラスを直接バインド
    m.setClassesToBeBound(PlaceOrderRequest.class, PlaceOrderResponse.class);
    return m;
  }

  @Bean
  public WebServiceTemplate webServiceTemplate(Jaxb2Marshaller marshaller) {
    WebServiceTemplate t = new WebServiceTemplate();
    t.setDefaultUri(System.getenv().getOrDefault(
        "SOAP_BACKEND_URL", "http://backend-soap:8080/services/OrderService"));
    t.setMarshaller(marshaller);
    t.setUnmarshaller(marshaller);
    return t;
  }
}
