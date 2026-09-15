# Domain Invariants

## User
- No public registration.
- Internal `userId` is UUIDv7.
- `matricula` is a company-scoped legacy identifier.
- `(companyId, matricula)` identifies the legacy record.
- First login requires password replacement.
- Manager resets forgotten access with a new temporary credential.

## Event
- Presencial only in MVP.
- May contain multiple sessions.
- May appear through multiple registered company websites.
- Eligibility is audience-controlled.
- Published changes notify affected users.
- Soft deletion is reversible.

## Session
- Own schedule, location, capacity, status.
- May be cancelled independently.
- Capacity is enforced atomically.

## Reservation
- Represents reserved seat only.
- Does not represent attendance.
- User may reserve multiple sessions.
- Schedule conflict warns but does not block.
- User may cancel with preset or custom reason.

## Waitlist
- Ordered.
- Chunk size is Manager-configured.
- Chunk notification does not reserve seats.
- First valid confirmations win available seats.
- Capacity check remains atomic.

## User Deactivation
- If still deactivated after 3 days, future reservations are cancelled.
- Restoration does not restore those reservations.

## Audience
Supports company-wide, site, include/exclude class, and include/exclude user rules. Empty include/exclude rules mean all otherwise eligible users.

## Excel Migration
- Preview is mandatory.
- Manager decides row actions.
- Null/default behavior is configurable before commit.
- Excel does not own internal IDs.
- Import is not continuous synchronization.
