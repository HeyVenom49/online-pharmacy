# Online Pharmacy (Microservices) — Spring Boot 3 / Java 17

Backend platform for an online pharmacy, implemented as Spring Boot microservices with Spring Cloud, JWT auth, RabbitMQ events, and Docker Compose.

- **Deep technical documentation**: `docs/TECHNICAL_DOCUMENTATION.md`
- **Endpoints reference**: `docs/API_ENDPOINTS.md`
- **Swagger testing guide**: `docs/SWAGGER_TESTING.md`
- **Example request data**: `docs/ENDPOINTS_TEST_DATA.json`

## Architecture (high level)

```
Client → Gateway (JWT + rate limiting)
             ├─ Identity (auth/users/JWT/notifications)
             ├─ Catalog (medicines/prescriptions/inventory)
             ├─ Orders  (cart/checkout/orders)
             └─ Admin   (dashboard KPIs)

Async events: Orders/Catalog/Identity → RabbitMQ → Notifications → (Identity internal + email)
```

## Services & ports (local)

| Service | Port | URL |
|---|---:|---|
| Gateway | 8080 | `http://localhost:8080` |
| Identity | 8081 | `http://localhost:8081` |
| Catalog | 8082 | `http://localhost:8082` |
| Orders | 8083 | `http://localhost:8083` |
| Admin | 8084 | `http://localhost:8084` |
| Notifications | 8085 | `http://localhost:8085` |
| Eureka | 8761 | `http://localhost:8761` |
| Config Server | 8888 | `http://localhost:8888` |

### Infrastructure (Docker Compose)

| Component | Default |
|---|---|
| PostgreSQL | `localhost:5432` (auth_db, catalog_db, order_db, admin_db) |
| Redis | `localhost:6379` |
| RabbitMQ | `localhost:5672` + UI `http://localhost:15672` (guest/guest) |
| Prometheus | `http://localhost:9090` (scrapes Actuator Prometheus metrics) |
| Grafana | `http://localhost:3000` |
| Zipkin | `http://localhost:9411` |
| Mailpit (dev/demo email) | configured in `docker-compose.yml` |

## What’s implemented

### `pharmacy-gateway`
- JWT validation (public path allowlist for auth + docs)
- Rate limiting filter
- Resilience4j circuit breakers on Feign calls (fault tolerance on inter-service HTTP)
- Routes to services under `/api/*`
- Unified OpenAPI aggregation + per-service OpenAPI proxy under `/docs/*`

### `pharmacy-identity`
- Signup/login/logout + JWT issuing
- JWT blacklist (Redis)
- User profile (`/auth/me`)
- Addresses CRUD
- In-app notifications (list/unread/read)
- Internal endpoints under `/internal/*` used by `pharmacy-notifications`

### `pharmacy-catalog`
- Categories, medicines
- Inventory batches + stock checks
- Prescription upload + admin review (approve/reject)

### `pharmacy-orders`
- Cart + cart items
- Checkout flow + order lifecycle
- Publishes order events to RabbitMQ

### `pharmacy-admin`
- Dashboard KPIs (orders/prescriptions/inventory signals)

### `pharmacy-notifications`
- RabbitMQ listeners for domain events
- Creates in-app notification rows via Identity internal endpoint
- Sends email via SMTP (Mailpit for demo)

### Cross-cutting observability & resilience
- **SLF4J logging** across services (`lombok.extern.slf4j.Slf4j`)
- **Metrics + Prometheus**: services expose `/actuator/prometheus` for scraping
- **Tracing + Zipkin**: spans are sent to Zipkin (`ZIPKIN_URL`)
- **Resilience4j**: circuit breakers + retry around Feign clients (`@CircuitBreaker`/`@Retry` on Feign interfaces)

## Setup (recommended: Docker Compose)

### Prerequisites
- Docker + Docker Compose
- Java 17 (for building/testing locally)
- Maven 3.8+

### 1) Create `.env`
This repo uses a local `.env` for Docker Compose (never commit it). Start from:

```bash
cp .env.example .env
```

**Important:** set a strong `JWT_SECRET` (same value is used across all services).

### 2) Build jars

```bash
mvn clean package -DskipTests
```

### 3) Start everything

```bash
docker compose up -d
```

### 4) Health checks

```bash
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8081/actuator/health
curl -s http://localhost:8082/actuator/health
curl -s http://localhost:8083/actuator/health
curl -s http://localhost:8084/actuator/health
curl -s http://localhost:8085/actuator/health
```

## Swagger / testing

### Gateway Swagger (recommended)
Open:
- `http://localhost:8080/swagger-ui.html`

Login (public):
- `POST /api/auth/login`

Then click **Authorize** and paste:
```
Bearer <token>
```

### Per-service docs via gateway
- `GET http://localhost:8080/docs/identity/v3/api-docs`
- `GET http://localhost:8080/docs/catalog/v3/api-docs`
- `GET http://localhost:8080/docs/orders/v3/api-docs`
- `GET http://localhost:8080/docs/admin/v3/api-docs`

### Direct service ports (optional)
Admin/Orders can authenticate either:
- via gateway headers (`X-User-Id`, `X-User-Email`, `X-User-Role`), or
- via `Authorization: Bearer <JWT>` directly (useful for Swagger on `8083/8084`).

## Demo users (dev)

Identity has a Flyway seed migration for demo accounts:
- **Admin**: `admin@pharmacy.com` / `admin123`
- **Customer**: `demo.customer@example.com` / `password123`

If these don’t work, restart Identity and confirm migrations ran.

## Tests

Run everything:

```bash
mvn test
```

Run selected modules (example):

```bash
mvn -pl pharmacy-common,pharmacy-identity,pharmacy-notifications -am test
```

## SonarQube (lint / static analysis)

This repo includes a SonarQube container in `docker-compose.yml`.

### 1) Start SonarQube

```bash
docker compose up -d sonarqube
```

Open SonarQube:
- `http://localhost:9000`

### 2) Create a token (UI)
In SonarQube UI → your profile → **Security** → generate a token.

### 3) Run analysis from the repo root

```bash
mvn -DskipTests sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<YOUR_SONAR_TOKEN>
```

If you also want coverage in Sonar, run tests first (JaCoCo reports are generated under each module):

```bash
mvn test
mvn sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dsonar.login=<YOUR_SONAR_TOKEN>
```

## Scripts
- `scripts/test-all-endpoints.sh`: gateway smoke checks

## Repo structure

```
online-pharmacy/
├── pharmacy-parent/           # Parent POM
├── pharmacy-common/           # Shared DTOs/events/exceptions/utilities
├── pharmacy-eureka/           # Service registry
├── pharmacy-config/           # Spring Cloud Config (native repo under config-repo/)
├── pharmacy-gateway/          # API Gateway
├── pharmacy-identity/         # Auth/users/addresses/notifications
├── pharmacy-catalog/          # Medicines/prescriptions/inventory
├── pharmacy-orders/           # Cart/orders
├── pharmacy-admin/            # Admin KPIs
├── pharmacy-notifications/    # Rabbit listeners + email dispatch
├── docs/
├── scripts/
├── docker-compose.yml
└── README.md
```

## Troubleshooting

- **403 on direct `8084/8083`**: almost always `JWT_SECRET` mismatch. Ensure `.env` sets `JWT_SECRET` and config-repo uses `${JWT_SECRET:...}` consistently.
- **`curl: (52) Empty reply from server`** right after restart: the service is still booting; retry after ~10–20 seconds.
