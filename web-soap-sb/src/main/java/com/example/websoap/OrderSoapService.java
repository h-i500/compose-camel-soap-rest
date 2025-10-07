package com.example.websoap;

import com.example.websoap.dto.PlaceOrderRequest;
import com.example.websoap.dto.PlaceOrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

@Service
public class OrderSoapService {
  private final WebServiceTemplate ws;
  public OrderSoapService(WebServiceTemplate ws){ this.ws = ws; }

  public PlaceOrderResponse place(String orderId, String amount) {
    PlaceOrderRequest req = new PlaceOrderRequest();
    req.setOrderId(orderId);
    req.setAmount(new java.math.BigDecimal(amount));
    return (PlaceOrderResponse) ws.marshalSendAndReceive(req);
  }
}
