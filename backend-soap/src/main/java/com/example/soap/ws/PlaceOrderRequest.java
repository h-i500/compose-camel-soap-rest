package com.example.soap.ws;

import java.math.BigDecimal;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaceOrderRequest", propOrder = {"orderId","amount"})
@XmlRootElement(name = "PlaceOrderRequest")
public class PlaceOrderRequest {
  private String orderId;
  private BigDecimal amount;

  public String getOrderId() { return orderId; }
  public void setOrderId(String orderId) { this.orderId = orderId; }
  public BigDecimal getAmount() { return amount; }
  public void setAmount(BigDecimal amount) { this.amount = amount; }
}
