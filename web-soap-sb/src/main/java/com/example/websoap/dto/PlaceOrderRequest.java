package com.example.websoap.dto;

import jakarta.xml.bind.annotation.*;
import java.math.BigDecimal;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaceOrderRequest", propOrder = {"orderId", "amount"})
@XmlRootElement(name = "PlaceOrderRequest", namespace = "http://example.com/order")
public class PlaceOrderRequest {

  @XmlElement(namespace = "http://example.com/order", required = true)
  private String orderId;

  @XmlElement(namespace = "http://example.com/order", required = true)
  private BigDecimal amount;

  public PlaceOrderRequest() {}

  public String getOrderId() { return orderId; }
  public void setOrderId(String orderId) { this.orderId = orderId; }

  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }
}
