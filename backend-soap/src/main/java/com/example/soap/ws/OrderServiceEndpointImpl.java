package com.example.soap.ws;

import jakarta.jws.WebService;
import jakarta.transaction.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.jms.*;

// @Service
// @WebService(endpointInterface = "com.example.soap.ws.OrderServiceEndpoint",
//   targetNamespace = "http://example.com/order",
//   serviceName = "OrderService",
//   portName = "OrderServicePort")

// @Service
// @WebService(endpointInterface = "com.example.soap.ws.OrderServiceEndpoint")
@Service
@WebService(
  endpointInterface = "com.example.soap.ws.OrderServiceEndpoint",
  targetNamespace = "http://example.com/order",
  serviceName = "OrderService",
  portName = "OrderServicePort"
)
public class OrderServiceEndpointImpl implements OrderServiceEndpoint {

  private final JdbcTemplate jdbc;
  private final ConnectionFactory connectionFactory; // JMS 2.0

  public OrderServiceEndpointImpl(JdbcTemplate jdbc, ConnectionFactory connectionFactory) {
    this.jdbc = jdbc;
    this.connectionFactory = connectionFactory;
  }

  @Override
  @Transactional  // （JTA想定）ビルド段階では単一DBでもOK
  public PlaceOrderResponse placeOrder(PlaceOrderRequest req) {
    jdbc.update("INSERT INTO orders(order_id, amount) VALUES(?,?)",
        req.getOrderId(), req.getAmount());

    // ビルド重視の最小 JMS 送信（XA 設定は後で）
    try (JMSContext ctx = connectionFactory.createContext(JMSContext.SESSION_TRANSACTED)) {
      Queue q = ctx.createQueue("orders.in");
      ctx.createProducer().send(q, req.getOrderId());
      ctx.commit();
    } catch (JMSRuntimeException ex) {
      throw ex;
    }

    PlaceOrderResponse res = new PlaceOrderResponse();
    res.setStatus("OK");
    return res;
  }
}
