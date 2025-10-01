import { useState } from "react";

const API = "/api/v1";

export default function App() {
  const [orderId, setOrderId] = useState("ORD-001");
  const [amount, setAmount] = useState("123.45");
  const [res, setRes] = useState("");

  const submit = async () => {
    const r = await fetch(`${API}/orders`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ orderId, amount })
    });
    setRes(await r.text());
  };

  return (
    <div style={{ padding: 24, fontFamily: "sans-serif" }}>
      <h2>REST → Camel(OSS) → SOAP デモ</h2>
      <div>
        <label>Order ID: </label>
        <input value={orderId} onChange={e=>setOrderId(e.target.value)} />
      </div>
      <div>
        <label>Amount: </label>
        <input value={amount} onChange={e=>setAmount(e.target.value)} />
      </div>
      <button onClick={submit}>Place Order</button>
      <pre>{res}</pre>
      <p>API: {API}</p>
    </div>
  );
}
