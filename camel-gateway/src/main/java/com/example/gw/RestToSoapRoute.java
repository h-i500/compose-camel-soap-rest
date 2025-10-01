package com.example.gw;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

@Component
public class RestToSoapRoute extends RouteBuilder {
  @Override
  public void configure() {

    onException(Exception.class)
      .handled(true)
      .setHeader("Content-Type", constant("application/json"))
      .setBody(simple("{\"error\":\"${exception.message}\"}"))
      .setHeader(org.apache.camel.Exchange.HTTP_RESPONSE_CODE, constant(502));

    // from("platform-http:/api/v1/orders?httpMethodRestrict=POST")
    //   .routeId("rest-to-soap")
    //   .unmarshal().json(JsonLibrary.Jackson, OrderRequest.class)
    //   .process(e -> {
    //     OrderRequest r = e.getMessage().getBody(OrderRequest.class);
    //     String xml = """
    //       <ns2:PlaceOrderRequest xmlns:ns2="http://example.com/order">
    //         <orderId>%s</orderId><amount>%s</amount>
    //       </ns2:PlaceOrderRequest>
    //     """.formatted(r.orderId(), r.amount());
    //     e.getMessage().setBody(xml);
    //   })
    //   // .setHeader("operationName", constant("PlaceOrder"))
    //   .setHeader("operationNamespace", constant("http://example.com/order")) 
    //   // .toD("cxf:{{gateway.soapBackendUrl}}?dataFormat=PAYLOAD")
    //   .toD("cxf:{{gateway.soapBackendUrl}}"
    //       + "?dataFormat=PAYLOAD"
    //       + "&defaultOperationName=PlaceOrder"
    //       + "&defaultOperationNamespace=http://example.com/order")
    //   .setHeader("Content-Type", constant("application/json"))
    //   .setBody(simple("{\"status\":\"OK\"}"));
    from("platform-http:/api/v1/orders?httpMethodRestrict=POST")
      .routeId("rest-to-soap")
      .unmarshal().json(JsonLibrary.Jackson, OrderRequest.class)
      .process(e -> {
        OrderRequest r = e.getMessage().getBody(OrderRequest.class);
        String xml = """
          <ord:PlaceOrderRequest xmlns:ord="http://example.com/order">
            <ord:orderId>%s</ord:orderId>
            <ord:amount>%s</ord:amount>
          </ord:PlaceOrderRequest>
        """.formatted(r.orderId(), r.amount());
        e.getMessage().setBody(xml);
      })
      .to("cxf:{{gateway.soapBackendUrl}}"
          + "?dataFormat=PAYLOAD"
          + "&defaultOperationName=PlaceOrder"
          + "&defaultOperationNamespace=http://example.com/order")
      .setHeader("Content-Type", constant("application/json"))
      .setBody(simple("{\"status\":\"OK\"}"));

  }
}
