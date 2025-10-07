package com.example.websoap.dto;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaceOrderResponse", propOrder = {"status"})
@XmlRootElement(name = "PlaceOrderResponse", namespace = "http://example.com/order")
public class PlaceOrderResponse {

  @XmlElement(namespace = "http://example.com/order", required = true)
  private String status;

  public PlaceOrderResponse() {}

  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
