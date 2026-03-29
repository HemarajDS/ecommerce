# Mercora Commerce

Mercora Commerce is a portfolio-ready microservices platform for enterprise e-commerce, dealer distribution, and CMS management. The backend is organized as a Spring Boot microservices monorepo and currently includes the full backend service set from the project plan.

## Current Backend Scope

- `config-server`: Spring Cloud Config Server with native repository support
- `discovery-server`: Eureka server for service registration and discovery
- `api-gateway`: request routing, CORS, correlation IDs, Redis-backed rate limiting, and JWT validation
- `auth-service`: registration, login, refresh token flow, MongoDB persistence, Redis token storage, and audit logs
- `product-service`: catalog CRUD, brand/category hierarchy, flexible attributes, filters, pagination, and Kafka product events
- `inventory-service`: warehouse stock, batch tracking, reservation lifecycle, low-stock alerts, and order-driven stock allocation hooks
- `cart-service`: Redis-backed carts, coupon handling, checkout repricing, and payment-session initiation scaffolding
- `order-service`: order creation, lifecycle transitions, timeline history, Kafka integration, and WebSocket-ready status updates
- `payment-service`: payment intents, idempotent request handling, webhook processing, and payment lifecycle events
- `dealer-service`: dealer onboarding, PO validation, credit and quota checks, approvals, and ledger tracking
- `notification-service`: event-driven email, SMS, in-app notification history, and preference management
- `cms-service`: page, section, SEO, version history, and draft-to-publish workflow scaffolding

## Repository Layout

```text
.
|-- api-gateway/
|-- auth-service/
|-- cart-service/
|-- cms-service/
|-- config-server/
|-- dealer-service/
|-- discovery-server/
|-- frontend/
|-- inventory-service/
|-- notification-service/
|-- order-service/
|-- payment-service/
|-- product-service/
|-- .github/
|-- docker-compose.yml
|-- pom.xml
`-- README.md
```

## Verification Status

- Java 17 verified locally
- Apache Maven 3.9.6 installed locally for verification
- Multi-module backend `mvn -ntp test` completed successfully on March 29, 2026
- Current unit tests pass across all backend modules

## Local Run Order

1. Start infrastructure from `docker-compose.yml`.
2. Start `config-server`.
3. Start `discovery-server`.
4. Start `api-gateway`.
5. Start downstream services as needed.

## Notes

- The backend currently compiles and passes the existing unit tests.
- Several integrations are scaffolded and portfolio-ready, but some production-grade external integrations still need deeper implementation.
- The frontend is not built yet.

## Next Steps

- Build the React frontend for customer, admin, and dealer portals
- Add stronger integration tests and richer service-to-service verification
- Tighten shared event contracts and production hardening
