package com.example.gw;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;
import org.apache.camel.component.cxf.common.message.CxfConstants;

// WSDL 生成クラス（上の -p で固定したパッケージ）
import com.example.order.contract.PlaceOrderRequest;

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
        PlaceOrderRequest req = new PlaceOrderRequest();
        // ↓ 生成クラスの setter 名は WSDL/types に依存。ビルド後に実クラス名を確認して合わせてください。
        req.setOrderId(r.orderId());
        req.setAmount(r.amount());

        // CXF POJO: 引数は Object[] で渡す
        e.getMessage().setBody(new Object[]{ req });
      })

      // 操作名とNSを明示（安定）
      .setHeader(CxfConstants.OPERATION_NAME, constant("PlaceOrder"))
      .setHeader(CxfConstants.OPERATION_NAMESPACE, constant("http://example.com/order"))

      // POJO 送信
      .to("cxf:{{gateway.soapBackendUrl}}"
          + "?dataFormat=POJO")
          // SEI を使うなら serviceClass も付けられます（WSDL から SEI が生成されている場合）
          // + "&serviceClass=com.example.order.contract.OrderService"

      // 今回はデモなので固定レスポンス
      .setHeader("Content-Type", constant("application/json"))
      .setBody(simple("{\"status\":\"OK\"}"));
  }
}
