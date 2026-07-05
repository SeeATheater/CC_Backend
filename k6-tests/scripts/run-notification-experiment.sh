#!/usr/bin/env bash
set -euo pipefail

variant="${VARIANT:-${1:-versionA}}"
dataset_users="${DATASET_USERS:-500}"
requests="${REQUESTS:-100}"
base_url="${BASE_URL:-http://localhost:8080}"

case "$variant" in
  versionA|versionB) ;;
  *)
    echo "VARIANT는 versionA 또는 versionB여야 합니다." >&2
    exit 1
    ;;
esac

script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
k6_dir="$(cd "$script_dir/.." && pwd)"
results_dir="$k6_dir/results"
mkdir -p "$results_dir"

max_run=0
for result_file in "$results_dir"/experiment1-"$variant"-run*.json; do
  [[ -e "$result_file" ]] || continue
  filename="$(basename "$result_file")"
  if [[ "$filename" =~ -run([0-9]+)\.json$ ]]; then
    run_number="${BASH_REMATCH[1]}"
    if (( run_number > max_run )); then
      max_run="$run_number"
    fi
  fi
done

next_run=$((max_run + 1))
summary_file="$results_dir/experiment1-$variant-run$next_run.json"

k6_args=(
  run
  -e "BASE_URL=$base_url"
  -e "VARIANT=$variant"
  -e "DATASET_USERS=$dataset_users"
  -e "REQUESTS=$requests"
  -e "SUMMARY_FILE=$summary_file"
)

if [[ -n "${ADMIN_TOKEN:-}" ]]; then
  k6_args+=(-e "ADMIN_TOKEN=$ADMIN_TOKEN")
elif [[ -n "${ADMIN_PASSWORD:-}" ]]; then
  k6_args+=(-e "ADMIN_PASSWORD=$ADMIN_PASSWORD")
elif [[ -n "${DEFAULT_PW:-}" ]]; then
  k6_args+=(-e "DEFAULT_PW=$DEFAULT_PW")
else
  echo "ADMIN_TOKEN, ADMIN_PASSWORD 또는 DEFAULT_PW를 환경변수로 설정해야 합니다." >&2
  exit 1
fi

if [[ -n "${ADMIN_EMAIL:-}" ]]; then
  k6_args+=(-e "ADMIN_EMAIL=$ADMIN_EMAIL")
fi

echo "결과 파일: $summary_file"
k6 "${k6_args[@]}" "$script_dir/notification-sync-vs-async-test.js"
