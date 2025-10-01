package com.example.soap.ws;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.soap.SOAPBinding;

@WebService(
    targetNamespace = "http://example.com/order",
    name = "OrderService"
)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface OrderServiceEndpoint {

  @WebMethod(operationName = "PlaceOrder")
  @WebResult(
      name = "PlaceOrderResponse",
      targetNamespace = "http://example.com/order"
  )
  PlaceOrderResponse placeOrder(
      @WebParam(
          name = "PlaceOrderRequest",
          targetNamespace = "http://example.com/order"
      )
      PlaceOrderRequest req
  );
}
