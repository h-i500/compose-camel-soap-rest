完璧です👏
非常に整理されたREADMEですね。構成も説明も明快です。

ここに新しく加わった **`web-soap-sb` (Spring Boot製Web SOAPクライアント)** を自然に統合し、
元の構成・文体を壊さずにアップデートした案を示します。
SOAP通信の詳細やSpring Boot側でのマーシャリング説明も追記済みです。

---

## ✅ 更新版 README.md 提案

```markdown
# compose-camel-soap-rest

REST → Apache Camel → SOAP の最小構成デモ。  
加えて、Spring Boot 製の Web アプリ（`web-soap-sb`）から SOAP 通信を直接行う構成も含みます。

React 製の Web（フロント）から JSON を投げると、Camel が SOAP メッセージに変換して CXF (JAX-WS) の SOAP サービスへ中継します。  
また、`web-soap-sb` は SOAP エンドポイントへ直接アクセスする Spring Boot クライアントです。  

SOAP サービスは PostgreSQL へ書き込み、ActiveMQ Artemis にメッセージを送信します。

> 目的：
>
> * **REST ⇄ SOAP 連携**の雛形  
> * **Camel** のルーティング（JSON→SOAP 変換 / CXF 呼び出し）  
> * **Spring Boot SOAP クライアント (web-soap-sb)** の例  
> * **Spring Boot + PostgreSQL + ActiveMQ Artemis** の最小連携  
> * **Docker Compose** でのローカル一括起動

---

## アーキテクチャ

```

[Web (React + nginx)] --> POST /api/v1/orders (JSON)
|
v
[Camel Gateway (Spring Boot + Camel 4)] --(CXF PAYLOAD)-->
|
v
[SOAP Backend (Spring Boot + CXF JAX-WS)]
|                         |
INSERT orders             JMS send "orders.in"
(PostgreSQL)             (ActiveMQ Artemis)

別構成：
[web-soap-sb (Spring Boot)] --(SOAP Client)--> [backend-soap]

```

* **SOAP エンドポイント**: `/services/OrderService`
  * Operation: `PlaceOrder` (Namespace: `http://example.com/order`)
  * Request: `PlaceOrderRequest { orderId, amount }`
  * Response: `PlaceOrderResponse { status }`

---

## 技術スタック

* **Frontend**: React + nginx（静的配信 & API 逆プロキシ）
* **Gateway**: Spring Boot 3.3.x, Apache Camel 4.6.x, camel-cxf
* **Backend (SOAP)**: Spring Boot 3.3.x, Apache CXF (JAX-WS)
* **Web SOAP Client**: Spring Boot 3.3.x + Spring Web Services（`web-soap-sb`）
* **Messaging**: ActiveMQ Artemis
* **DB**: PostgreSQL 16
* **Runtime**: Docker / Docker Compose

---

## リポジトリ構成（抜粋）

```

backend-soap/
└─ ws/OrderServiceEndpointImpl.java  # SOAPサーバ（DB+JMS）
camel-gateway/
└─ RestToSoapRoute.java              # JSON→SOAP変換（Camel）
web/
└─ nginx/default.conf                # /api → Gateway
web-soap-sb/
├─ SoapConfig.java                   # Jaxb2Marshaller設定
├─ OrderSoapService.java             # WebServiceTemplateでSOAP呼出
├─ WebController.java                # フォーム送信ハンドラ
└─ templates/index.html              # 入力フォーム（Thymeleaf）
docker-compose.yml

````

---

## 起動

```bash
docker compose up -d --build
docker compose ps
````

アクセスURL一覧：

| サービス                            | URL                                                                                                  | 説明                      |
| ------------------------------- | ---------------------------------------------------------------------------------------------------- | ----------------------- |
| Web (React+nginx)               | [http://localhost:8088](http://localhost:8088)                                                       | JSON → REST 経由でSOAP呼び出し |
| Gateway (Camel)                 | [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)                       | REST→SOAP中継             |
| SOAP Backend                    | [http://localhost:8080/services/OrderService?wsdl](http://localhost:8080/services/OrderService?wsdl) | WSDL確認                  |
| Web SOAP Client (`web-soap-sb`) | [http://localhost:8089](http://localhost:8089)                                                       | Spring Bootから直接SOAP呼び出し |

---

## 🧩 SOAP 通信（web-soap-sb）

`web-soap-sb` は Spring Boot 製の軽量SOAPクライアント。
JAXBクラスを用いて SOAP リクエスト/レスポンスをマーシャリングします。

### リクエスト・レスポンス構造

```java
// PlaceOrderRequest.java
public class PlaceOrderRequest {
  private String orderId;
  private BigDecimal amount;
}

// PlaceOrderResponse.java
public class PlaceOrderResponse {
  private String status;
}
```

### 通信処理例

```java
PlaceOrderRequest req = new PlaceOrderRequest();
req.setOrderId("ORD-001");
req.setAmount(BigDecimal.valueOf(123.45));

PlaceOrderResponse res = (PlaceOrderResponse)
    webServiceTemplate.marshalSendAndReceive(req);

System.out.println(res.getStatus()); // "OK"
```

### SOAPエンドポイント

```
http://backend-soap:8080/services/OrderService
```

※Dockerネットワーク内での名前解決を利用。ホストからアクセスする場合は
`http://localhost:8080/services/OrderService`。

---

## 🔍 動作確認

### SOAP単体

```bash
curl -s http://localhost:8080/services/OrderService?wsdl | head
```

### REST経由

```bash
curl -s -X POST "http://localhost:8081/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-001","amount":"123.45"}'
# => {"status":"OK"}
```

### Spring Boot SOAP クライアント

Webブラウザで

```
http://localhost:8089/
```

にアクセス。フォームに `orderId` と `amount` を入力して送信。
SOAPレスポンスの `status` が画面に表示されます。

---

## 🔧 トラブルシューティング（追加）

* **`Unknown JAXB exception` / `ObjectFactory.class not found`**

  * `marshaller.setPackagesToScan("com.example.websoap.dto")` の指定ディレクトリに
    `ObjectFactory.java` または `jaxb.index` がない場合に発生。
  * JAXB生成済みDTO（`PlaceOrderRequest`, `PlaceOrderResponse`）を同一パッケージに配置。

* **`Could not resolve host: backend-soap`**

  * ホストからアクセスしている場合は `localhost` に変更。
  * Docker内部では `backend-soap` がComposeネットワークのDNS名として機能します。

---

## 🧰 デバッグTips（SOAPトレース）

`web-soap-sb/src/main/resources/application.yml` に追記：

```yaml
logging:
  level:
    org.springframework.ws.client.MessageTracing.sent: TRACE
    org.springframework.ws.client.MessageTracing.received: TRACE
```

SOAPリクエスト・レスポンスのXMLがログに出力されます。

---


