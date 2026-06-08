# Payment Consistency Baseline

This document records the narrow payment consistency safeguards added for the
dev/staging baseline. It intentionally does not introduce database schema
changes, migrations, or large transaction-boundary refactors.

## Scope

The current ticketing flow creates a `TempTicket` in `PENDING` status first.
KakaoPay `ready` then preempts stock by decreasing `amateur_rounds.total_ticket`
and stores the returned KakaoPay `tid` in `temp_ticket.kakao_tid`. KakaoPay
`approve` confirms the reservation and creates a `RealTicket`.

This baseline only blocks clear invalid requests and duplicate ready calls that
can safely be handled without changing the broader payment state machine.

## Fixed In This Baseline

- Reject `TempTicket` creation when `quantity <= 0`.
- Reject duplicate `/kakaoPay/ready` calls when the target `TempTicket` already
  has a `kakaoTid`.
- Ensure duplicate ready calls are rejected before stock is decreased.

## Deferred Follow-Ups

- Make `stopPayment` state transitions atomic.
- Prevent `cancel`, `fail`, and scheduler races from restoring stock more than
  once.
- Add a database-level unique constraint for `real_ticket.kakao_tid` after
  auditing existing duplicate data.
- Separate KakaoPay external API calls from long-running database transactions.
- Make `cancelTicket` idempotent and safe against concurrent duplicate cancel
  requests.
- Expand payment state transition tests for approve, cancel, fail, scheduler,
  and database-save failure scenarios.

## Verification

Run the focused tests:

```bash
./gradlew test --tests cc.backend.kakaoPay.service.KakaoPayBusinessServiceTest --no-daemon
./gradlew test --tests cc.backend.ticket.service.TempTicketServiceImplTest --no-daemon
```

Run compile and whitespace checks:

```bash
./gradlew compileJava --no-daemon
git diff --check
```
