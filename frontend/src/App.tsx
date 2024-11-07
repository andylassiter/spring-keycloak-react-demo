import React, { useEffect, useState } from 'react';
import logo from './logo.svg';
import './App.css';
import Keycloak from 'keycloak-js';

function App() {
  const [keycloak, setKeycloak] = useState<Keycloak.KeycloakInstance | null>(null);
  const [authenticated, setAuthenticated] = useState(false);
  const [message, setMessage] = useState("");
  const [adminMessage, setAdminMessage] = useState("");

  // Initialize Keycloak and check authentication
  useEffect(() => {
    const keycloakInstance = new Keycloak({
      url: "/auth",
      realm: "demo",
      clientId: "frontend",
    });

    keycloakInstance
        .init({ onLoad: "login-required" })
        .then(authenticated => {
          setKeycloak(keycloakInstance);
          setAuthenticated(authenticated);
        })
        .catch(error => {
          console.error("Keycloak initialization failed", error);
        });
  }, []);

  // Fetch message from backend when authenticated
  useEffect(() => {
    if (authenticated && keycloak?.token) {
      fetch("/api/hello", {
        headers: {
          Authorization: `Bearer ${keycloak.token}`,
          AccessControlAllowOrigin: "*"
    },
      })
          .then(response => response.text())
          .then(data => setMessage(data))
          .catch(error => console.error("Failed to fetch message:", error));
    }
  }, [authenticated, keycloak]);

    // Fetch admin message from backend
    const fetchAdminMessage = () => {
        if (keycloak?.token) {
            fetch("/api/admin", {
                headers: {
                    Authorization: `Bearer ${keycloak.token}`,
                    AccessControlAllowOrigin: "*"
                },
            })
                .then(response => response.text())
                .then(data => setAdminMessage(data))
                .catch(error => console.error("Failed to fetch admin message:", error));
        }
    };

  return (
      <div className="App">
        <header className="App-header">
          <img src={logo} className="App-logo" alt="logo" />
          {authenticated ? (
              <div>
                <p>Welcome to the authenticated app!</p>
                <h1>{message}</h1>
                  {keycloak?.hasRealmRole('admin') && (
                      <div>
                          <button onClick={fetchAdminMessage}>Admin Only Button</button>
                          <h2>{adminMessage}</h2>
                      </div>
                  )}
                <button onClick={() => keycloak?.logout()}>Logout</button>
              </div>
          ) : (
              <p>Loading...</p>
          )}
        </header>
      </div>
  );
}

export default App;
