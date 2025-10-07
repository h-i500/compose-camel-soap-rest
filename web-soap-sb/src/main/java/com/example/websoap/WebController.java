// com/example/websoap/WebController.java
package com.example.websoap;
import com.example.websoap.dto.PlaceOrderResponse;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller @RequiredArgsConstructor @Validated
public class WebController {
  private final OrderSoapService service;

  @GetMapping("/") public String index(Model model) {
    model.addAttribute("form", new OrderForm()); return "index";
  }

  @PostMapping("/place")
  public String place(@ModelAttribute("form") @Validated OrderForm form, Model model) {
    PlaceOrderResponse res = service.place(form.getOrderId(), form.getAmount());
    model.addAttribute("result", res);
    return "index";
  }

  @Data public static class OrderForm {
    @NotBlank private String orderId;
    @Pattern(regexp="^-?\\d+(\\.\\d+)?$") private String amount = "0";
  }
}
