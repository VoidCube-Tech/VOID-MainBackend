# Architecture Rules

## Style
- Modular monolith.
- Lightweight hexagonal architecture.
- High cohesion.
- Low coupling.
- Explicit module contracts.
- No circular dependencies.

## Layers
`domain`: business rules, entities, value objects, invariants.

`application`: use cases, orchestration, transaction boundaries, required ports.

`api`: REST controllers, request/response DTOs, OpenAPI surface.

`infrastructure`: PostgreSQL, Redis, RabbitMQ, Cloudflare, external providers.

## Module Boundary
A module may expose Signals, publish integration events, and own tables in its schema.

A module must not call internal classes of another module, write another module's tables, or depend on another module's repository implementation.

## Signal
Signal is a synchronous module-facing contract. Its implementation may be in-process now or remote later. Consumers do not change when transport changes.

## Event
Use asynchronous events when immediate response is not required. Critical publication uses Transactional Outbox.

## Persistence
PostgreSQL, one logical database, domain schemas, Flyway. Cross-schema read only when justified. No cross-schema writes outside ownership.

## Runtime
Stateless application containers. Redis for ephemeral shared state. RabbitMQ for durable asynchronous work. OpenTelemetry for telemetry.
