# Ticket / Payment Validation Baseline

This document records the minimal validation and error-handling baseline for the
ticket reservation and KakaoPay request flow. It is intentionally narrow: it
does not change payment state transitions, transaction boundaries, database
schema, or external KakaoPay API call order.

## Scope

- `POST /tickets/{amateurShowId}/reserve`
- `POST /kakaoPay/ready`
- `PATCH /myTickets/{realTicketId}/cancel`
- Representative bad request handling for malformed body, missing request
  parameter, and type mismatch errors

## Baseline Added

- `TempTicketCreateRequestDTO.quantity` must be positive.
- `TempTicketServiceImpl.createTempTicket()` rejects `quantity <= 0` before
  repository access, so service-level callers cannot bypass controller
  validation.
- Reserve request IDs are validated as positive values:
  - `amateurShowId`
  - `amateurRoundId`
  - `amateurTicketId`
- KakaoPay ready request validates `tempTicketId` as a positive value.
- Ticket cancel request validates `realTicketId` as a positive value.
- KakaoPay cancel/fail redirect fallback logs exceptions through the logger
  instead of printing stack traces directly.

## Error Handling

The existing `ApiResponse` failure format is preserved. This baseline only adds
minimal mapping for common malformed request cases:

- Missing required request parameter
- Request parameter/path variable type mismatch
- Unreadable request body

DTO validation still uses the existing `MethodArgumentNotValidException`
handling path. Controller method validation uses existing `ErrorStatus` enum
names in constraint messages so `ConstraintViolationException` remains
compatible with the current handler.

## Out Of Scope

- Global exception contract redesign
- Applying validation to every DTO in the project
- Frontend error message mapping
- Database schema changes or migrations
- KakaoPay transaction boundary refactoring
- `stopPayment` / scheduler race-condition refactoring
- Payment status state-machine redesign

## Follow-Up Work

- Standardize the global exception response contract.
- Add controller-level security and validation regression tests with `MockMvc`.
- Expand request validation to board, comment, image, admin, and member APIs.
- Remove remaining direct `printStackTrace()` calls across the whole codebase.
- Document frontend error message mapping for ticket/payment failures.
