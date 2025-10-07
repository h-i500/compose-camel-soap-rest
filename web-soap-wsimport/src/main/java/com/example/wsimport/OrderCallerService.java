package com.example.wsimport;

import com.example.wsimport.client.PlaceOrderRequest;
import com.example.wsimport.client.PlaceOrderResponse;
import com.example.wsimport.client.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderCallerService {
  private final OrderService port;
  public OrderCallerService(OrderService port) { this.port = port; }

  public PlaceOrderResponse place(String orderId, String amount) {
    PlaceOrderRequest req = new PlaceOrderRequest();
    req.setOrderId(orderId);
    req.setAmount(new BigDecimal(amount));
    return port.placeOrder(req);
  }
}
