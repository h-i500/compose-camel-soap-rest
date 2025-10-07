


# compose-camel-soap-rest

REST → Apache Camel → SOAP の最小構成デモ。
加えて、Spring Boot 製の Web アプリ（`web-soap-sb`・`web-soap-wsimport`）から SOAP 通信を直接行う構成も含みます。

React 製の Web（フロント）から JSON を投げると、Camel が SOAP メッセージに変換して CXF (JAX-WS) の SOAP サービスへ中継します。  
また、Spring Boot 製クライアント (`web-soap-sb`, `web-soap-wsimport`) は SOAP サービスへ直接アクセスします。

SOAP サービスは PostgreSQL へ書き込み、ActiveMQ Artemis にメッセージを送信します。

> 目的：
>
> * **REST ⇄ SOAP 連携**の雛形  
> * **Camel** のルーティング（JSON→SOAP 変換 / CXF 呼び出し）  
> * **Spring Boot SOAP クライアント**（手書き版・自動生成版）の比較  
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
[web-soap-sb]         (Spring Boot SOAP Client: 手書きDTO)
[web-soap-wsimport]   (Spring Boot SOAP Client: wsimport自動生成)

```

---

## 技術スタック

| コンポーネント | 技術構成 |
|----------------|-----------|
| **Frontend** | React + nginx（静的配信 & 逆プロキシ） |
| **Gateway** | Spring Boot 3.3.x, Apache Camel 4.6.x, camel-cxf |
| **Backend (SOAP)** | Spring Boot 3.3.x, Apache CXF (JAX-WS) |
| **Web SOAP Client (手書き版)** | Spring Boot 3.3.x + Spring Web Services（`web-soap-sb`） |
| **Web SOAP Client (自動生成版)** | Spring Boot 3.3.x + wsimport + JAX-WS/Metro 4（`web-soap-wsimport`） |
| **Messaging** | ActiveMQ Artemis |
| **DB** | PostgreSQL 16 |
| **Runtime** | Docker / Docker Compose |

---

## リポジトリ構成（抜粋）

```

backend-soap/
└─ ws/OrderServiceEndpointImpl.java     # SOAPサーバ（DB+JMS）
camel-gateway/
└─ RestToSoapRoute.java                 # JSON→SOAP変換（Camel）
web/
└─ nginx/default.conf                   # /api → Gateway
web-soap-sb/
├─ SoapConfig.java                      # Jaxb2Marshaller設定
├─ OrderSoapService.java                # WebServiceTemplateでSOAP呼出
├─ WebController.java                   # フォーム送信ハンドラ
└─ templates/index.html                 # 入力フォーム（Thymeleaf）
web-soap-wsimport/
├─ WsClientConfig.java                  # wsimport生成クライアント設定
├─ OrderCallerService.java              # 呼出サービス
├─ templates/index.html                 # 同様のフォームUI
├─ src/main/wsdl/OrderService.wsdl      # 固定WSDL（自動生成元）
└─ pom.xml                              # wsimportプラグイン設定
docker-compose.yml

````

---

## 起動

```bash
docker compose up -d --build
docker compose ps
````

アクセスURL一覧：

| サービス                        | URL                                                                                                  | 内容                     |
| --------------------------- | ---------------------------------------------------------------------------------------------------- | ---------------------- |
| **Web (React+nginx)**       | [http://localhost:8088](http://localhost:8088)                                                       | REST経由でSOAP呼び出し        |
| **Gateway (Camel)**         | [http://localhost:8081/actuator/health](http://localhost:8081/actuator/health)                       | REST→SOAP中継            |
| **SOAP Backend**            | [http://localhost:8080/services/OrderService?wsdl](http://localhost:8080/services/OrderService?wsdl) | WSDL確認                 |
| **Web SOAP Client (手書き版)**  | [http://localhost:8089](http://localhost:8089)                                                       | Spring BootからSOAP呼び出し  |
| **Web SOAP Client (自動生成版)** | [http://localhost:8090](http://localhost:8090)                                                       | wsimport生成クラスを利用した呼び出し |

---

## 🧩 SOAP クライアントの2方式

### ① `web-soap-sb`: 手書きDTO + WebServiceTemplate版

* Spring Web Services の `Jaxb2Marshaller` を利用し、
  手動で作成した DTO (`PlaceOrderRequest`, `PlaceOrderResponse`) をマーシャリング。
* 設定・制御がシンプルで、Spring Boot との親和性が高い。

```java
PlaceOrderRequest req = new PlaceOrderRequest();
req.setOrderId("ORD-001");
req.setAmount(BigDecimal.valueOf(123.45));

PlaceOrderResponse res =
    (PlaceOrderResponse) webServiceTemplate.marshalSendAndReceive(req);
```

---

### ② `web-soap-wsimport`: 自動生成クライアント版

* `jaxws-maven-plugin` で WSDL からクライアントコードを自動生成。
* `OrderService_Service` クラスと `OrderService` ポートを経由して呼び出す。
* Metro (Jakarta JAX-WS) 4.0 ベースで、型安全・保守性が高い。

```java
OrderService_Service service = new OrderService_Service();
OrderService port = service.getOrderServicePort();

PlaceOrderRequest req = new PlaceOrderRequest();
req.setOrderId("ORD-001");
req.setAmount(new BigDecimal("123.45"));
PlaceOrderResponse res = port.placeOrder(req);
```

WSDLは `src/main/wsdl/OrderService.wsdl` に固定保存。
（開発時は `mvn -Pwsdl-remote` で動的取得も可）

---

## 動作確認

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

### Spring Boot SOAPクライアント（手書きDTO版）

[http://localhost:8089](http://localhost:8089)

### Spring Boot SOAPクライアント（wsimport自動生成版）

[http://localhost:8090](http://localhost:8090)

---

## 🔍 トラブルシューティング（補足）

| 症状                                             | 原因 / 対応                                             |
| ---------------------------------------------- | --------------------------------------------------- |
| `Unknown JAXB exception`                       | DTOパッケージ設定ミス。`marshaller.setPackagesToScan` の対象確認。  |
| `Could not resolve host: backend-soap`         | ホストからは `localhost:8080`、コンテナ内は `backend-soap:8080`。 |
| `ClassNotFoundException: OrderService_Service` | `mvn clean package` 実行で生成クラスを再ビルド。                  |
| `relation "orders" does not exist`             | DB初期化漏れ。`schema.sql` または手動作成。                       |

---

## 💡 wsimportプロファイルの使い方

```bash
# backend-soap 起動後、WSDLを直接取得してクライアント生成
mvn -q -Pwsdl-remote clean package -DskipTests
```

`web-soap-wsimport/pom.xml` にて `jaxws-tools:4.0.2` を利用しているため、
Jakarta EE 10以降の `javax` → `jakarta` 移行にも対応済み。

---

## 🧰 デバッグTips

`web-soap-sb` / `web-soap-wsimport` 共通でSOAPメッセージを出したい場合：

```yaml
logging:
  level:
    org.springframework.ws.client.MessageTracing.sent: TRACE
    org.springframework.ws.client.MessageTracing.received: TRACE
```

または JAX-WS 標準の Metro ログ：

```bash
-Dcom.sun.xml.ws.transport.http.client.HttpTransportPipe.dump=true
```

---

## ✨ まとめ

| 項目        | web-soap-sb                 | web-soap-wsimport    |
| --------- | --------------------------- | -------------------- |
| 実装方式      | 手書きDTO + WebServiceTemplate | wsimport 自動生成クライアント  |
| 型安全性      | △（手動管理）                     | ◎（自動生成）              |
| Spring統合性 | ◎                           | ○                    |
| 保守性       | 手軽（軽量）                      | WSDL変更時も再生成で追随       |
| 利用場面      | 内部APIや軽量呼び出し向け              | 外部SOAP連携、スキーマ厳格な業務向け |

---

