# Contract Rules

## HTTP
Base version: `/v1`

REST only. OpenAPI required.

## Error Contract
Required:
- HTTP status
- `error_code`
- `error_message`
- `correlation_id`
- `timestamp`

Optional safe context:
- module
- operation
- resource identifier

Never expose stack traces, secrets, credentials, or provider tokens.

## Signal Contracts
Signals are synchronous module contracts. Expected examples: `CompanySignal`, `AuthSignal`, `NotificationSignal`.

Signals must use module-owned contract DTOs and must not leak internal entities or infrastructure types.

## Integration Events
Examples:
- EventCreated
- EventPublished
- EventUpdated
- EventCancelled
- SessionCancelled
- ReservationCreated
- ReservationCancelled
- WaitlistJoined
- WaitlistSlotOffered
- UserDeactivated

Consumers must tolerate retries. Handlers must be idempotent.
