# compose-camel-soap-rest

REST → Apache Camel → SOAP の最小構成デモ。
React 製の Web（フロント）から JSON を投げると、Camel が SOAP メッセージに変換して CXF (JAX-WS) の SOAP サービスへ中継します。SOAP サービスは PostgreSQL へ書き込み、ActiveMQ Artemis にメッセージを送信します。

> 目的：
>
> * **REST ⇄ SOAP 連携**の雛形
> * **Camel** のルーティング（JSON→SOAP 変換 / CXF 呼び出し）
> * **Spring Boot** + **PostgreSQL** + **ActiveMQ Artemis** の最小連携
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
```

* **SOAP エンドポイント**: `/services/OrderService`

  * Operation: `PlaceOrder` (Namespace: `http://example.com/order`)
  * Request: `PlaceOrderRequest { orderId, amount }`
  * Response: `PlaceOrderResponse { status }`
* **REST エンドポイント（Camel Gateway）**: `POST /api/v1/orders`

  * Body(JSON): `{ "orderId": "ORD-001", "amount": "123.45" }`
  * 200 OK: `{ "status": "OK" }`

---

## 技術スタック

* **Frontend**: React + Vite/CRA（ビルド） / nginx（配信 & 逆プロキシ）
* **Gateway**: Spring Boot 3.3.x, Apache Camel 4.6.x, camel-cxf
* **Backend (SOAP)**: Spring Boot 3.3.x, Apache CXF (JAX-WS), Jakarta EE 10 APIs
* **Messaging**: ActiveMQ Artemis (vromero/activemq-artemis)
* **DB**: PostgreSQL 16
* **Runtime**: Docker / Docker Compose

---

## リポジトリ構成（抜粋）

```
backend-soap/
  src/main/java/com/example/soap/...
    config/CxfConfig.java            # CXF Endpoint publish(/services/OrderService)
    ws/OrderServiceEndpoint.java     # JAX-WS IF (PlaceOrder)
    ws/OrderServiceEndpointImpl.java # 実装：DB INSERT + JMS 送信
    ws/PlaceOrderRequest.java        # JAXB バインディング
    ws/PlaceOrderResponse.java
  src/main/resources/
    application.yml                  # 共通設定
    application-local.yml            # プロファイル local
    schema.sql (任意)                # 初期テーブル作成 (ある場合)
camel-gateway/
  src/main/java/com/example/gw/RestToSoapRoute.java # JSON→SOAP 変換→CXF 呼び出し
  src/main/resources/application-local.yml          # SOAP Backend URL など
web/
  public/index.html
  src/App.jsx                                   # フォーム＋fetch
  nginx/
    default.conf                                # /api を Gateway にリバプロ
docker-compose.yml
```

---

## 事前準備

* Docker / Docker Compose が使えること
* ポート使用状況

  * Web: `8088` (nginx)
  * Gateway: `8081`
  * SOAP Backend: `8080`
  * PostgreSQL:（内部接続、ホスト公開なし）
  * Artemis: `61616`（Broker）, `8161`（Console）

---

## 起動

```bash
# 初回 or 変更時
docker compose up -d --build

# 状態確認
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

### ヘルスチェック

```bash
curl -s http://localhost:8081/actuator/health       # Gateway
curl -s http://localhost:8080/actuator/health       # SOAP Backend
```

両方とも `"status":"UP"` になれば OK。

---

## 動作確認（スモークテスト）

### 1) SOAP サービス（WSDL）

```bash
curl -s http://localhost:8080/services/OrderService?wsdl | head
```

### 2) REST → Camel → SOAP

```bash
curl -s -X POST "http://localhost:8081/api/v1/orders" \
  -H "Content-Type: application/json" \
  -d '{"orderId":"ORD-001","amount":"123.45"}'
# => {"status":"OK"}
```

### 3) DB 確認（コンテナ内で psql）

```bash
docker exec -it compose-camel-soap-rest-db-1 \
  psql -U app -d appdb -c "SELECT * FROM orders ORDER BY created_at DESC NULLS LAST;"
```

レコードが入っていれば OK（`order_id`, `amount`, `created_at` など）。

### 4) ブローカ確認（任意）

* Artemis Console: [http://localhost:8161](http://localhost:8161) （デフォルト: `admin` / `admin`）
* キュー `orders.in` のメッセージ統計を確認

---

## フロントエンド（nginx 逆プロキシ）

* `web` コンテナがビルド済み静的ファイルを **nginx** で配信
* `/api` を **Camel Gateway (8081)** へプロキシ設定
  （`web/nginx/default.conf` を参照。例）

```nginx
server {
  listen 80;
  server_name localhost;

  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files $uri /index.html;
  }

  # APIは同一オリジン内でGatewayへ転送（CORS不要）
  location /api/ {
    proxy_pass http://camel-gateway:8081/api/;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
  }
}
```

> React からは `fetch('/api/v1/orders', ...)` の**相対パス**推奨。
> ローカル実行時は nginx が同一オリジンで API を中継するので CORS 設定は不要です。

---

## プロファイルと設定

* **Spring Profile**: `local`（Composeで `SPRING_PROFILES_ACTIVE=local` を指定）
* 代表的な設定ファイル：

  * `backend-soap/src/main/resources/application.yml`
  * `backend-soap/src/main/resources/application-local.yml`
  * `camel-gateway/src/main/resources/application-local.yml`
* SOAP バックエンド URL（Gateway → SOAP）は env でも上書き可能：

  * `SOAP_BACKEND_URL=http://backend-soap:8080/services/OrderService`

---

## データベース初期化

* `schema.sql` が存在する場合は起動時に自動実行されます。
* ない場合は、以下のように手動作成してください（例）:

```sql
CREATE TABLE IF NOT EXISTS orders (
  id SERIAL PRIMARY KEY,
  order_id VARCHAR(64) NOT NULL,
  amount NUMERIC(18,2) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX IF NOT EXISTS uq_orders_order_id ON orders(order_id);
```

> 既存データとの衝突に注意。クリーンにする場合は `docker compose down -v`（**全データ削除**）を利用。

---

## トラブルシューティング

* **`No service was found.` / `Can't find the request ... Observer`**

  * `/services/OrderService` が未公開の可能性。`backend-soap` のログで
    `Setting the server's publish address to be /services/OrderService` を確認。
* **`Unmarshalling Error: unexpected element ...`**

  * SOAP Payload の namespace が一致していない可能性。Camel ルートで
    `defaultOperationName=PlaceOrder` と `defaultOperationNamespace=http://example.com/order` を設定済みか確認。
* **Gateway から 404（SOAP Backend に到達せず）**

  * `SOAP_BACKEND_URL` の値、`camel-gateway` から `backend-soap` の DNS 解決（Compose ネットワーク）を確認。
* **DB にテーブルがない / `relation "orders" does not exist`**

  * `schema.sql` の配置 or 手動作成。
  * `backend-soap` の DB 接続先が `db:5432`（コンテナ名）になっているか確認。
* **フロントから API が飛ばない**

  * ブラウザの Network タブ/Console を確認。
  * nginx の `/api` リバプロ設定と `camel-gateway` のポート開放を確認。

---

## 開発 Tips

* ログ確認

  ```bash
  docker logs -f compose-camel-soap-rest-backend-soap-1
  docker logs -f compose-camel-soap-rest-camel-gateway-1
  docker logs -f compose-camel-soap-rest-web-1
  docker logs -f compose-camel-soap-rest-broker-1
  ```
* 再ビルド

  ```bash
  docker compose build web camel-gateway backend-soap
  docker compose up -d
  ```
* クリーン（※DB データ含む）

  ```bash
  docker compose down -v
  ```

---

