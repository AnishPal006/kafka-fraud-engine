import React, { useState, useEffect } from "react";
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

function App() {
  const [alerts, setAlerts] = useState([]);

  useEffect(() => {
    // 1. Initialize the Stomp Client
    const client = new Client({
      // We use the SockJS factory to handle the connection
      webSocketFactory: () => new SockJS("http://localhost:8083/ws-fraud"),

      // 2. What to do when connected
      onConnect: (frame) => {
        console.log("Connected: " + frame);

        // 3. Subscribe to the Topic
        client.subscribe("/topic/alerts", (message) => {
          const alert = JSON.parse(message.body);
          console.log("Alert Received:", alert);
          setAlerts((prevAlerts) => [alert, ...prevAlerts]);
        });
      },

      // Optional: Log errors if something goes wrong
      onStompError: (frame) => {
        console.error("Broker reported error: " + frame.headers["message"]);
        console.error("Additional details: " + frame.body);
      },
    });

    // 4. Activate the connection
    client.activate();

    // Cleanup on unmount (close connection when you close the tab)
    return () => {
      client.deactivate();
    };
  }, []);

  return (
    <div style={{ padding: "20px", fontFamily: "Arial, sans-serif" }}>
      <h1 style={{ color: "#d32f2f" }}>🚨 Real-Time Fraud Monitor</h1>
      <p>Listening for suspicious activity via Kafka...</p>

      <div style={{ display: "grid", gap: "10px" }}>
        {alerts.length === 0 ? (
          <p>No fraud detected yet. Waiting...</p>
        ) : (
          alerts.map((alert, index) => (
            <div
              key={index}
              style={{
                border: "1px solid #ffcdd2",
                backgroundColor: "#ffebee",
                padding: "15px",
                borderRadius: "8px",
                boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
              }}
            >
              <h3 style={{ margin: "0 0 10px 0", color: "#b71c1c" }}>
                Fraud Detected!
              </h3>
              <p>
                <strong>User:</strong> {alert.userId}
              </p>
              <p>
                <strong>Amount:</strong> ₹{alert.amount}
              </p>
              <p>
                <strong>Reason:</strong> {alert.timestamp}
              </p>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default App;
