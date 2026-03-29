# Mercora Commerce

Mercora Commerce is a portfolio-ready microservices platform for enterprise e-commerce, dealer distribution, and CMS management. This repository starts with the Phase 1 foundation: centralized config, service discovery, API gateway, and authentication.

## Phase 1 Services

- `config-server`: Spring Cloud Config Server with native repository support
- `discovery-server`: Eureka server for service registration and discovery
- `api-gateway`: request routing, CORS, correlation IDs, Redis-backed rate limiting, and JWT validation
- `auth-service`: registration, login, refresh token flow, MongoDB persistence, Redis token storage, and audit logs
- `product-service`: catalog CRUD, brand/category hierarchy, flexible attributes, filters, pagination, and Kafka product events
- `inventory-service`: warehouse stock, batch tracking, reservation lifecycle, low-stock alerts, and order-driven stock allocation hooks
- `cart-service`: Redis-backed carts, coupon handling, checkout repricing, and payment-session initiation scaffolding
- `order-service`: order creation, lifecycle transitions, timeline history, Kafka integration, and WebSocket-ready status updates

## Planned Modules


```text
mercora-commerce/
├── api-gateway/
├── auth-service/
├── product-service/
├── inventory-service/
├── cart-service/
├── order-service/
├── payment-service/
├── dealer-service/
├── notification-service/
├── cms-service/
├── config-server/
├── discovery-server/
├── frontend/
├── docker-compose.yml
└── README.md
```

## Local Run Order

1. Start MongoDB and Redis.
2. Run `config-server`.
3. Run `discovery-server`.
4. Run `api-gateway`.
5. Run `auth-service`.

## Next Steps

- Build the Payment Service with gateway integration and webhook-safe processing.
- Add shared logging and correlation ID propagation across downstream services.
- Expand Docker Compose to run the remaining business services.
