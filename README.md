# microservices-payment-app

A production-grade microservices payment processing system built with Spring Boot 4, Kafka, RabbitMQ, and Kubernetes. Developed as part of the iPrody Java Microservices course.

---

## Architecture

```
┌─────────────────────┐        Kafka          ┌──────────────────────────┐
│  payment-service-app│ ──── requests ──────► │  xpayment-adapter-app    │
│                     │ ◄─── responses ─────  │                          │
│  REST API           │                       │  Kafka consumer          │
│  PostgreSQL         │                       │  X Payment Provider REST │
│  Keycloak JWT       │                       │  RabbitMQ polling        │
└─────────────────────┘                       └──────────────────────────┘
         │                                               │
         ▼                                               ▼
    PostgreSQL                                      RabbitMQ
    (payment state)                           (delayed status polling)
```

### Payment flow

1. Client sends `POST /payments` with a JWT token
2. `payment-service-app` persists the payment with status `PROCESSING` and publishes a Kafka request message
3. `xpayment-adapter-app` consumes the message, calls X Payment Provider REST API, and sends back a Kafka response
4. If the provider returns `PROCESSING`, RabbitMQ schedules periodic status checks (up to 60 retries × 60 seconds)
5. Once a terminal status (`SUCCEEDED` or `CANCELED`) is reached, a final Kafka response updates the payment to `APPROVED` or `DECLINED`

---

## Modules

### `payment-service-app`
REST API for payment CRUD operations.

- **Port:** `8080` (API), `8081` (Actuator)
- **Stack:** Spring Boot 4, Spring Data JPA, Liquibase, Spring Security OAuth2, Spring Kafka
- **Database:** PostgreSQL
- **Security:** Keycloak JWT (realm roles: `admin`, `reader`)
- **Endpoints:**
  - `POST /payments` — create payment (admin only)
  - `GET /payments/{guid}` — get by ID
  - `GET /payments` — list all
  - `GET /payments/search` — filter by currency, amount, date range, status
  - `PUT /payments/{guid}` — full update (admin only)
  - `PATCH /payments/{guid}/note` — update note (admin only)
  - `DELETE /payments/{guid}` — delete (admin only)

### `xpayment-adapter-app`
Async adapter between the payment service and X Payment Provider.

- **Port:** `8082` (API), `8081` (Actuator)
- **Stack:** Spring Boot 4, Spring Kafka, Spring AMQP, OpenAPI Generator (RestTemplate client)
- **Responsibilities:**
  - Consumes Kafka payment requests
  - Validates messages (ISO 4217 currency scale, null checks) — invalid messages routed to DLT
  - Calls X Payment Provider REST API to create charges
  - Polls payment status via RabbitMQ delayed exchange (up to 60 attempts)
  - Sends terminal results back via Kafka

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.2 |
| Messaging | Apache Kafka, RabbitMQ (delayed message exchange) |
| Persistence | PostgreSQL, Spring Data JPA, Liquibase |
| Security | Keycloak, Spring Security OAuth2 Resource Server |
| Mapping | MapStruct |
| API Client | OpenAPI Generator (RestTemplate) |
| Observability | Spring Actuator, Micrometer Tracing, Zipkin, Loki, Promtail, Grafana |
| Containerization | Docker, Kubernetes (Docker Desktop) |
| Build | Maven (multi-module) |
| Testing | JUnit 5, Mockito, Testcontainers, Spring Security Test |

---

## Local Development

### Prerequisites

- Java 21
- Docker Desktop with Kubernetes enabled
- Maven 3.9+

### Start infrastructure

```bash
cd payment-service-app
docker compose up -d
```

This starts: PostgreSQL, Keycloak, Kafka, Kafka UI, RabbitMQ, pgAdmin.

### Start X Payment Provider mock

```bash
cd xpayment-adapter-app
docker compose up -d
```

### Run applications from IntelliJ

Run `PaymentServiceAppApplication` and `XpaymentAdapterAppApplication` with the `default` profile.

### Get a token

```bash
TOKEN=$(curl -s -X POST http://localhost:8085/realms/iprody-lms/protocol/openid-connect/token \
  -d "client_id=basic_client" \
  -d "client_secret=myclient-secret" \
  -d "username=admin_user" \
  -d "password=admin" \
  -d "grant_type=password" | jq -r .access_token)
```

### Create a payment

```bash
curl -s -X POST http://localhost:8080/payments \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "inquiryRefId": "11111111-1111-1111-1111-111111111111",
    "amount": "50.00",
    "currency": "USD",
    "note": "test payment"
  }' | jq .
```

---

## Kubernetes Deployment

### Prerequisites

- Docker Desktop with Kubernetes enabled
- Helm 3
- Local registry running on port 5001

```bash
docker run -d -p 5001:5000 --name registry --restart always registry:2
```

### Build and push images

```bash
# payment-service-app
cd payment-service-app
mvn clean package -DskipTests
docker build -t localhost:5001/payment-service:latest .
docker push localhost:5001/payment-service:latest

# xpayment-adapter-app
cd ../xpayment-adapter-app
mvn clean package -DskipTests
docker build -t localhost:5001/x-payment-adapter-app:latest .
docker push localhost:5001/x-payment-adapter-app:latest

# RabbitMQ with delayed exchange plugin
cd ../payment-service-app
docker build -f ../k8s/rabbitmq-Dockerfile -t localhost:5001/rabbitmq-delayed:latest .
docker push localhost:5001/rabbitmq-delayed:latest
```

### Deploy infrastructure

```bash
# PostgreSQL
helm upgrade --install my-pg bitnami/postgresql \
  -n db --create-namespace \
  --set auth.postgresPassword=secret \
  --set auth.database=payment-db

# Kafka
kubectl create ns kafka
kubectl apply -f k8s/kafka.yml

# RabbitMQ
kubectl create ns rabbit
kubectl apply -f k8s/rabbitmq.yml

# Keycloak
kubectl create ns keycloak
kubectl create configmap keycloak-realm -n keycloak \
  --from-file=realm-export.json=payment-service-app/realm-export.json
kubectl apply -f k8s/keycloak.yml
```

### Deploy applications

```bash
kubectl apply -f k8s/payment-service.yml
kubectl apply -f k8s/xpayment-adapter-service.yml
```

### Deploy observability stack

```bash
kubectl create ns observability
helm repo add grafana https://grafana.github.io/helm-charts
helm repo update

helm upgrade --install loki grafana/loki -n observability -f k8s/loki-values.yml
helm upgrade --install promtail grafana/promtail -n observability -f k8s/promtail-values.yaml
helm upgrade --install grafana grafana/grafana -n observability \
  --set adminPassword=admin \
  --set service.type=ClusterIP
```

### Access Grafana

```bash
kubectl -n observability port-forward svc/grafana 3000:80
```

Open http://localhost:3000 (admin/admin), add Loki datasource:
```
http://loki-gateway.observability.svc.cluster.local
```

---

## Health Checks

Both applications expose Kubernetes-ready health probes on port `8081`:

| Probe | Endpoint |
|---|---|
| Startup | `/actuator/health/liveness` |
| Liveness | `/actuator/health/liveness` |
| Readiness | `/actuator/health/readiness` |

---

## Logging

Both applications produce structured JSON logs with the following fields:

```json
{
  "@timestamp": "2026-04-07T22:08:09.546Z",
  "level": "INFO",
  "logger_name": "com.iprody.paymentserviceapp.service.PaymentServiceImpl",
  "traceId": "abc123",
  "spanId": "def456",
  "message": "Payment created: guid=..., status=PROCESSING",
  "app": "payment-service-app"
}
```

Logs are collected by Promtail, stored in Loki, and queryable in Grafana by `traceId`, `spanId`, `level`, or `app`.

---

## Project Structure

```
microservices-payment-app/
├── payment-service-app/          # REST API service
│   ├── src/main/java/
│   │   └── com/iprody/paymentserviceapp/
│   │       ├── controller/       # REST endpoints
│   │       ├── service/          # Business logic
│   │       ├── persistence/      # JPA entities, repositories, specifications
│   │       ├── async/            # Kafka sender/listener interfaces and implementations
│   │       ├── mapper/           # MapStruct mappers
│   │       ├── security/         # Keycloak JWT converter
│   │       └── exceptions/       # Global exception handler
│   ├── src/test/                 # Unit + integration tests (Testcontainers)
│   └── docker-compose.yml        # Local infrastructure
├── xpayment-adapter-app/         # Async adapter service
│   ├── src/main/java/
│   │   └── com/iprody/xpaymentadapterapp/
│   │       ├── async/            # Kafka listener, validation, DLT
│   │       ├── api/              # X Payment Provider gateway + OpenAPI client
│   │       └── checkstate/       # RabbitMQ delayed polling
│   └── docker-compose.yml        # X Payment Provider mock
├── k8s/                          # Kubernetes manifests and Helm values
│   ├── payment-service.yml
│   ├── xpayment-adapter-service.yml
│   ├── keycloak.yml
│   ├── kafka.yml
│   ├── rabbitmq.yml
│   ├── rabbitmq-Dockerfile
│   ├── loki-values.yml
│   └── promtail-values.yaml
└── pom.xml                       # Multi-module Maven root
```

---

## License

MIT
