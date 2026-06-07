#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${1:-}"
if [[ -z "$BASE_URL" ]]; then
  echo "Usage: $0 <base-url>" >&2
  echo "Example: $0 http://52.79.122.153" >&2
  echo "Example: $0 https://api-dev.example.com" >&2
  exit 2
fi

BASE_URL="${BASE_URL%/}"
TIMEOUT_SECONDS="${SMOKE_TIMEOUT_SECONDS:-10}"
FAILED=0

request_status() {
  local method="$1"
  local path="$2"
  local body_file="$3"

  if [[ -n "$body_file" ]]; then
    curl -sS -o "$body_file" -w "%{http_code}" \
      --connect-timeout "$TIMEOUT_SECONDS" \
      --max-time "$TIMEOUT_SECONDS" \
      -X "$method" \
      "$BASE_URL$path"
  else
    curl -sS -o /dev/null -w "%{http_code}" \
      --connect-timeout "$TIMEOUT_SECONDS" \
      --max-time "$TIMEOUT_SECONDS" \
      -X "$method" \
      "$BASE_URL$path"
  fi
}

expect_status() {
  local name="$1"
  local method="$2"
  local path="$3"
  local expected_regex="$4"
  local status

  set +e
  status="$(request_status "$method" "$path" "")"
  local curl_exit=$?
  set -e

  if [[ $curl_exit -ne 0 ]]; then
    echo "FAIL $name: curl exit=$curl_exit path=$method $path"
    FAILED=1
    return
  fi

  if [[ "$status" =~ ^($expected_regex)$ ]]; then
    echo "PASS $name: $status $method $path"
  else
    echo "FAIL $name: expected $expected_regex, got $status for $method $path"
    FAILED=1
  fi
}

expect_health_up() {
  local body
  body="$(mktemp)"

  set +e
  local status
  status="$(request_status "GET" "/actuator/health" "$body")"
  local curl_exit=$?
  set -e

  if [[ $curl_exit -ne 0 ]]; then
    echo "FAIL actuator health: curl exit=$curl_exit"
    FAILED=1
  elif [[ "$status" != "200" ]]; then
    echo "FAIL actuator health: expected 200, got $status"
    FAILED=1
  elif ! grep -q '"status":"UP"' "$body"; then
    echo "FAIL actuator health: response does not contain status UP"
    FAILED=1
  else
    echo "PASS actuator health: 200 and status UP"
  fi

  rm -f "$body"
}

echo "Running dev/staging smoke test against $BASE_URL"

# Public operational/read endpoints.
expect_health_up
expect_status "swagger ui is reachable" "GET" "/swagger-ui/index.html" "200"
expect_status "public ranking API is reachable" "GET" "/amateurs/ranking" "200"
expect_status "public boards API reaches validation" "GET" "/boards" "400"

# KakaoPay boundary: ready is a user action, callbacks are public redirects.
expect_status "kakaoPay ready blocks anonymous user" "POST" "/kakaoPay/ready?tempTicketId=1" "401|403"
expect_status "kakaoPay approve callback is public and reaches validation" "GET" "/kakaoPay/approve" "400"
expect_status "kakaoPay cancel callback is public and reaches validation" "GET" "/kakaoPay/cancel" "400"
expect_status "kakaoPay fail callback is public and reaches validation" "GET" "/kakaoPay/fail" "400"

# Private user/admin endpoints should reject anonymous requests.
expect_status "my tickets requires authentication" "GET" "/myTickets/list" "401|403"
expect_status "member myPage requires authentication" "GET" "/member/myPage" "401|403"
expect_status "admin dashboard requires authentication" "GET" "/admin/dashboard/approval" "401|403"

if [[ "$FAILED" -ne 0 ]]; then
  echo "Smoke test failed for $BASE_URL"
  exit 1
fi

echo "Smoke test passed for $BASE_URL"
