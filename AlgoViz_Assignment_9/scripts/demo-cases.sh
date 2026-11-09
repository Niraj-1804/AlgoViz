#!/usr/bin/env bash
# Shows all 5 cases and saves sanitized responses (tokens must never be uploaded).
set -euo pipefail
API="${API:-http://localhost:8080/api}"
: "${STUDENT_PASSWORD:?Export STUDENT_PASSWORD before running}"
: "${ADMIN_PASSWORD:?Export ADMIN_PASSWORD before running}"
command -v jq >/dev/null || { echo 'Install jq first: brew install jq'; exit 1; }
mkdir -p demo/output
printf '\nCASE 1: no token -> 401\n'
curl -sS -i "$API/algorithms" | tee demo/output/01-no-token.txt
printf '\nCASE 2: admin login -> 200 and JWT (redacted in saved output)\n'
LOGIN=$(curl -sS -w '\n%{http_code}' -H 'Content-Type: application/json' -d "$(jq -n --arg p "$ADMIN_PASSWORD" '{username:"admin",password:$p}')" "$API/login")
STATUS=$(echo "$LOGIN" | tail -n 1)
BODY=$(echo "$LOGIN" | sed '$d')
TOKEN=$(echo "$BODY" | jq -r '.token // empty')
test -n "$TOKEN" || { echo 'Login failed'; exit 1; }
echo "HTTP $STATUS (token received; masked for safety)" | tee demo/output/02-login.txt
printf '\nCASE 3: token -> 200\n'
curl -sS -i -H "Authorization: Bearer $TOKEN" "$API/algorithms" | tee demo/output/03-auth-read.txt
printf '\nCASE 4: admin create -> 201\n'
curl -sS -i -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"algorithm":"bubble-sort","values":[5,2,4]}' "$API/visualizations" | tee demo/output/04-admin-write.txt
printf '\nCASE 5: student write -> 403\n'
STUDENT_TOKEN=$(curl -sS -H 'Content-Type: application/json' -d "$(jq -n --arg p "$STUDENT_PASSWORD" '{username:"student",password:$p}')" "$API/login" | jq -r '.token // empty')
test -n "$STUDENT_TOKEN" || { echo 'Student login failed'; exit 1; }
curl -sS -i -H "Authorization: Bearer $STUDENT_TOKEN" -H 'Content-Type: application/json' -d '{"algorithm":"bubble-sort","values":[9,3,1]}' "$API/visualizations" | tee demo/output/05-student-write.txt
printf '\nOPTIONAL CASE 6: student DELETE -> 403\n'
curl -sS -i -X DELETE -H "Authorization: Bearer $STUDENT_TOKEN" "$API/visualizations/1" | tee demo/output/06-student-delete.txt
printf '\nDone. Save screenshots of each case. Never show raw JWT/passwords in your video.\n'
