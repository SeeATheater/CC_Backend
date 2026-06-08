# Payment DB Constraint

This document records the database invariant for preventing duplicate
`RealTicket` rows for the same KakaoPay transaction id.

## Purpose

`KakaoPayBusinessService.completePayment()` already checks
`RealTicketRepository.existsByKakaoTid(kakaoTid)` before issuing a
`RealTicket`. That application-level guard handles normal retries, but it cannot
fully prevent race conditions when duplicate approve callbacks are processed at
the same time.

The database must enforce the final invariant:

- one non-null `real_ticket.kakao_tid` can map to at most one `RealTicket`

`kakao_tid` remains nullable in this PR because older or non-payment-created
rows may exist. MySQL unique indexes allow multiple `NULL` values.

## Pre-Apply Checks

Run these checks before applying the unique constraint in any environment:

```sql
SHOW CREATE TABLE real_ticket;

SHOW INDEX FROM real_ticket;

SELECT COUNT(*) AS total_real_ticket_count
FROM real_ticket;

SELECT COUNT(*) AS null_kakao_tid_count
FROM real_ticket
WHERE kakao_tid IS NULL;

SELECT kakao_tid, COUNT(*) AS cnt
FROM real_ticket
WHERE kakao_tid IS NOT NULL
GROUP BY kakao_tid
HAVING COUNT(*) > 1;
```

The duplicate check must return no rows before applying the constraint.

## Apply SQL

```sql
ALTER TABLE real_ticket
ADD CONSTRAINT uk_real_ticket_kakao_tid UNIQUE (kakao_tid);
```

Equivalent form:

```sql
CREATE UNIQUE INDEX uk_real_ticket_kakao_tid
ON real_ticket (kakao_tid);
```

Use only one of the two statements.

## Rollback SQL

```sql
ALTER TABLE real_ticket
DROP INDEX uk_real_ticket_kakao_tid;
```

## Verification SQL

```sql
SHOW INDEX FROM real_ticket
WHERE Key_name = 'uk_real_ticket_kakao_tid';

SELECT kakao_tid, COUNT(*) AS cnt
FROM real_ticket
WHERE kakao_tid IS NOT NULL
GROUP BY kakao_tid
HAVING COUNT(*) > 1;
```

## Application Behavior

After this constraint is applied, concurrent duplicate inserts for the same
`kakao_tid` will fail at the database level. The current application-level
`existsByKakaoTid` check should still remain because it avoids unnecessary
duplicate work for ordinary retries.

If duplicate approve races still surface as user-visible errors, add a follow-up
application fallback that catches duplicate key violations, re-queries the
existing `RealTicket` by `kakaoTid`, and treats the callback as an idempotent
success when the existing row is found.

## Out Of Scope

- `stopPayment` atomic state transition refactor
- cancel, fail, and scheduler race handling
- KakaoPay external API transaction-boundary redesign
- Flyway or Liquibase introduction
- production schema change execution
