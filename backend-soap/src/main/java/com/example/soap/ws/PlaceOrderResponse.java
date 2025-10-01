// backend-soap/src/main/java/com/example/soap/ws/PlaceOrderResponse.java
package com.example.soap.ws;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PlaceOrderResponse", propOrder = {"status"})
@XmlRootElement(name = "PlaceOrderResponse")
public class PlaceOrderResponse {
  private String status;
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
}
