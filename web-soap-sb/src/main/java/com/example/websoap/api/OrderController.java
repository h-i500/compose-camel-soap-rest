package com.example.websoap.api;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

// ★ 生成されたリクエスト/レスポンス/ポートのクラス名で import を合わせてください
import com.example.order.OrderService;          // ★Port インタフェース
import com.example.order.PlaceOrder;            // ★ラッパ（存在しない場合もある）
import com.example.order.PlaceOrderRequest;     // ★リクエスト
import com.example.order.PlaceOrderResponse;    // ★レスポンス

@RestController
@RequestMapping("/api")
@Validated
public class OrderController {

  private final OrderService port; // ★生成結果に合わせる

  public OrderController(OrderService port) {
    this.port = port;
  }

  public static record PlaceOrderJson(
      @NotBlank @Pattern(regexp = "^[A-Za-z0-9\\-]+$") String orderId,
      @Positive BigDecimal amount
  ) {}

  @PostMapping("/orders")
  public Map<String, Object> place(@RequestBody @Validated PlaceOrderJson body) {
    // ★WSDL により “ラッパ要否” が異なります（Document/Literal Wrapped ならラッパあり）
    // 1) ラッパ＋中身 のパターン
    PlaceOrder wrapper = new PlaceOrder();
    PlaceOrderRequest req = new PlaceOrderRequest();
    req.setOrderId(body.orderId());
    req.setAmount(body.amount());
    wrapper.setRequest(req);

    // ★メソッド名も生成結果に依存（例：placeOrder / placeOrderOperation など）
    PlaceOrderResponse res = port.placeOrder(wrapper);

    return Map.of(
        "orderId", body.orderId(),
        "status", res.getStatus() != null ? res.getStatus() : "UNKNOWN"
    );
  }
}
