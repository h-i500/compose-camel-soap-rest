package com.example.wsimport;

import com.example.wsimport.client.PlaceOrderResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@Validated
public class WebController {
  private final OrderCallerService caller;
  public WebController(OrderCallerService caller){ this.caller = caller; }

  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("form", new OrderForm());
    return "index";
  }

  @PostMapping("/place")
  public String place(@ModelAttribute("form") @Validated OrderForm form, Model model) {
    PlaceOrderResponse res = caller.place(form.getOrderId(), form.getAmount());
    model.addAttribute("result", res);
    return "index";
  }

  public static class OrderForm {
    @NotBlank private String orderId;
    @Pattern(regexp="^-?\\d+(\\.\\d+)?$") private String amount = "0";

    public String getOrderId(){ return orderId; }
    public void setOrderId(String v){ this.orderId = v; }
    public String getAmount(){ return amount; }
    public void setAmount(String v){ this.amount = v; }
  }
}
