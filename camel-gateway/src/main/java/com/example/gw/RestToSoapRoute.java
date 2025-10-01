package com.example.gw;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;
import org.apache.camel.component.cxf.common.message.CxfConstants;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Component
public class RestToSoapRoute extends RouteBuilder {
  @Override
  public void configure() {

    onException(Exception.class)
      .handled(true)
      .setHeader("Content-Type", constant("application/json"))
      .setBody(simple("{\"error\":\"${exception.message}\"}"))
      .setHeader(org.apache.camel.Exchange.HTTP_RESPONSE_CODE, constant(502));

    from("platform-http:/api/v1/orders?httpMethodRestrict=POST")
      .routeId("rest-to-soap")
      .unmarshal().json(JsonLibrary.Jackson, OrderRequest.class)

      .process(e -> {
        OrderRequest r = e.getMessage().getBody(OrderRequest.class);

        // ★ import に依存しないよう、完全修飾名でインスタンス化
        com.example.order.contract.PlaceOrderRequest req =
            new com.example.order.contract.PlaceOrderRequest();
        req.setOrderId(r.orderId());
        req.setAmount(r.amount());

        // CXF dataFormat=POJO は、引数を Object[] で渡す
        e.getMessage().setBody(new Object[]{ req });

        // ★ SOAPAction を空で送る（Camel 4 正攻法）
        //   CxfConstants.CAMEL_CXF_PROTOCOL_HEADERS が解決できない環境のため、
        //   文字列リテラル "CamelCxfProtocolHeaders" を直接使います。
        Map<String, List<String>> ph = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        ph.put("SOAPAction", Collections.singletonList(""));
        e.getMessage().setHeader("CamelCxfProtocolHeaders", ph);
      })

      // 操作名と Namespace を明示（安定）
      .setHeader(CxfConstants.OPERATION_NAME, constant("PlaceOrder"))
      .setHeader(CxfConstants.OPERATION_NAMESPACE, constant("http://example.com/order"))

      // CXF JAX-WS クライアント（serviceClass 必須）。WSDL も明示。
      .to("cxf:{{gateway.soapBackendUrl}}"
          + "?dataFormat=POJO"
          + "&serviceClass=com.example.order.contract.OrderService"
          + "&wsdlURL=classpath:order.wsdl")

      // デモ用の固定レスポンス
      .setHeader("Content-Type", constant("application/json"))
      .setBody(simple("{\"status\":\"OK\"}"));
  }
}
