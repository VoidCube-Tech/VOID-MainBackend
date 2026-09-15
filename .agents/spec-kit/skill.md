# Backend Events — Agent Skill

## Project
Modular monolith for VoidCube event management.

Primary scope:
- Authentication.
- Events.
- Event Website.
- Notifications integration.
- Import/export for legacy Excel workflows.

Do not implement microservices now.

## Business Context
A client company buys the Events + Event Website capability from VoidCube. VoidCube registers the company and its websites. Company Managers manage company users and events. Internal users do not self-register.

Core flow:
1. Company purchases Events + Event Website.
2. Company Managers are configured in VoidCube.
3. Managers create internal users.
4. User receives temporary credentials.
5. First login requires password replacement.
6. Managers create, edit, publish, cancel, archive, restore, or delete events.
7. Users access company websites, browse eligible events, reserve sessions, cancel reservations, and join waitlists.
8. Notifications are delivered through VoidCube messaging.

## Identity
- `userId` is an internal UUIDv7.
- Legacy `matricula` is never the technical primary identifier.
- Legacy identity scope is company-local.
- `(companyId, matricula)` identifies a legacy user within a company.
- Same matricula in different companies means different users.
- User may access multiple sites within the same company.
- Site access is controlled independently from technical identity.

## Company Integration
Company administration belongs to the broader VoidCube platform. Events must not duplicate the full company domain. Use public contracts/Signals for company, authentication, permission, and support data.

## Roles
Events treats Owner and Manager as equivalent operational authorities. Detailed permission configuration is managed through VoidCube.

## Events
Current Events scope is presencial only.

Lifecycle:
- DRAFT
- PUBLISHED
- CANCELLED
- FINISHED
- ARCHIVED

Soft deletion applies globally. Deleted aggregates must be restorable with their internal state.

An Event:
- belongs to a company;
- may be exposed through one or more company websites;
- may have multiple sessions;
- may restrict visibility/eligibility by include/exclude audience rules;
- may be featured manually or automatically;
- may be edited after publication;
- notifies users about published-event changes.

## Audience Rules
A user may belong to multiple custom classes.

Eligibility supports:
- whole company;
- included classes;
- excluded classes;
- included users;
- excluded users;
- site membership restrictions.

If include and exclude rules are empty, all otherwise eligible company users may access the event.

## Sessions
A Session belongs to one Event and may define start, end, location, capacity, and status. Each session has independent capacity. A user may reserve multiple sessions from the same event. Schedule conflicts do not block reservation; warn the user. A session may be cancelled independently and must notify affected users.

## Reservations
Reservation means seat reservation, not physical attendance. Capacity must be enforced atomically. If one seat remains and multiple users reserve concurrently, only one may succeed. Others may be offered waitlist entry.

Users may cancel their reservation. Managers may cancel reservations when authorized. Cancellation supports Manager-defined presets and a custom user reason. Reasons are queryable and usable in metrics.

## Waitlist
Waitlist is ordered. When capacity becomes available, notify a Manager-configured chunk of waiting users. The first confirmations consume available seats. Notification does not reserve a seat. Users may leave the waitlist. Managers may inspect and manage the queue.

## User Deactivation
If a user remains deactivated for 3 days, future reservations are cancelled automatically. Restoring the user later does not restore cancelled reservations.

## User Provisioning
No public registration. Manager creates users manually or through Excel migration/import. Legacy fields include name, matricula, and classes. First login requires password replacement. Forgotten access is reset by Manager using a new temporary credential.

## Excel Migration
Excel is a migration/import interface, not continuous synchronization.

Flow:
1. Upload.
2. Map source columns.
3. Configure null/default handling.
4. Resolve by company + matricula.
5. Compare against current state.
6. Show preview per row.
7. Manager selects per-row action.
8. Confirm.
9. Apply.

Possible actions: CREATE, UPDATE, KEEP, DISABLE, SOFT_DELETE. Never replace internal UUIDs with legacy identifiers. Excel is authoritative only for the confirmed migration operation.

## Event Website
One Events domain may feed multiple websites in the same company. Each website may have independent visual identity, texts, featured-event configuration, domain, and client/audience scope.

Primary pages:
- main page with featured events;
- My Events;
- event search.

Search/filter supports name, date, class/audience, availability, status, reserved events, and waitlisted events.

## Featured Events
Manual featured configuration has priority when configured. Automatic ranking may consider temporal proximity, audience size, and remaining capacity. Exact weights are Manager-configurable.

## Metrics
At minimum expose event views, capacity, confirmed reservations, remaining seats, waitlist size, cancellations, cancellation reasons, occupancy rate, future events, finished events, cancelled events, total reservations, and average occupancy. Reservation is not attendance.

## Notifications
Messaging is unilateral: Manager/system -> users. Notifications may target company, site, event, session, class, reserved users, waitlisted users, or selected users. Notification state: unread, read, archived.

## Architecture
Use a modular monolith with lightweight hexagonal architecture.

Module shape:
- domain/
- application/
- api/
- infrastructure/

Domain code must not depend directly on Spring Web, persistence implementation, Redis, RabbitMQ, Cloudflare, or external providers.

## Module Communication
`Signal` is the official synchronous module contract. The caller depends on the Signal contract, never callee internals. Local implementation may call application code now; a future remote adapter may replace it without changing callers.

Use asynchronous events when immediate response is not required.

## Async Infrastructure
Use RabbitMQ for durable asynchronous processing. Use Transactional Outbox for critical integration events. Retry with backoff. Use dead-letter queues where applicable. External integrations require timeout and circuit-breaker behavior.

## Persistence
Primary DB: PostgreSQL. Use one database with domain schemas. Cross-schema read is allowed only when justified. Cross-domain write is forbidden; use module contracts. Use Flyway migrations. Use expand -> migrate -> contract for destructive schema evolution.

## IDs
Use UUIDv7 for internal externally relevant entity IDs. Never use matricula, Excel row, or external legacy ID as technical primary keys.

## Concurrency
Use temporary edit locks for mutable Event/Session editing. When one Manager is editing, another Manager cannot enter editing mode. Lock must expire automatically and must not rely on an open HTTP connection. Also use optimistic versioning as secondary protection. Reservation capacity must use transactional/atomic concurrency protection.

## API
REST only. Version prefix: `/v1/...`. OpenAPI is mandatory. API errors include HTTP status, `error_code`, `error_message`, `correlation_id`, and timestamp. Never expose stack traces or secrets.

## Authentication
Use short-lived JWT access token + rotating opaque refresh token. JWT contains minimal stable identity/context. Do not embed mutable class/permission state. First login with temporary password forces credential replacement.

## MFA
Use TOTP + recovery codes for privileged VoidCube roles where applicable. Do not depend solely on SMS MFA.

## Cache
Use Redis for cache, rate limiting, distributed locks, and short-lived state. PostgreSQL remains persistent source of truth.

## Observability
Use structured logs, correlation IDs, OpenTelemetry, metrics, traces, and health checks. Audit trail is separate from normal logs.

## Audit
Audit critical actions: event create/publish/cancel/delete/restore, session cancellation, administrative reservation cancellation, credential reset, import confirmation, permission-sensitive changes, support/emergency actions.

## Soft Delete
Soft delete is default. General purge policy is 6 months unless retention rules require longer. Do not apply generic purge to audit, legal, or financial records.

## Stateless Runtime
Application instances must be stateless. Required state belongs in PostgreSQL, Redis for ephemeral shared state, or object storage where applicable.

## Deployment
Initial deployment: Docker on VPS, development + production, GitHub Actions CI/CD. Do not introduce Kubernetes without a concrete need.

## Tests
Use unit, integration, contract, architecture, migration, and critical E2E tests. Tests do not replace safe migration design.

## Out of Scope
- paid events;
- tickets;
- event payment gateway;
- QR code/check-in;
- physical attendance tracking;
- online events;
- hybrid events;
- user-to-user chat;
- public self-registration;
- public third-party API.

Attendance/check-in remains a future-compatible extension point.

## Extraction Rule
Do not extract microservices now. If extracted later: preserve Signal/event contracts, isolate ownership, replace local Signal adapter with remote transport, preserve domain/application behavior, and split physical persistence only when justified.
