
To connect to posgres db from adminer
  Server: host.docker.internal (Mac/Windows) or host IP (e.g., 192.168.x.x on Linux)
  Username: root
  Password: Welcome@123
 
# 🔐 Keycloak Authentication Guide

This document explains how to authenticate with **Keycloak** (running at port `9080`) and obtain a **JWT token** to access protected endpoints in a Spring Boot microservice.

---

## 🌐 Keycloak Server Info

| Parameter         | Value                        |
|------------------|------------------------------|
| **Auth Server**  | `http://localhost:9080`      |
| **Realm**        | `master`                    |
| **Client ID**    | `superadmin`                   |
| **Client Secret**| `a7B4K8bDolQ49K96r9kr1DZNSJzF1QI4` |
| **Token Endpoint** | `http://localhost:9080/realms/master/protocol/openid-connect/token` |

---

## 🛠️ How to Obtain a Token (Resource Owner Password Flow)

Use the following `curl` command to obtain a JWT token using a user's credentials:

```bash
curl -X POST "http://localhost:9080/realms/dev-realm/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=superadmin" \
  -d "username=admin" \
  -d "password=admin@123" \
  -d "client_secret=a7B4K8bDolQ49K96r9kr1DZNSJzF1QI4"  # Omit if client is public
