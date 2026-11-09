#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ ! -f .env ]]; then echo 'Missing .env: cp .env.example .env and configure it'; exit 1; fi
set -a
source .env
set +a
: "${JWT_SECRET:?set JWT_SECRET in .env}"
: "${STUDENT_PASSWORD:?set STUDENT_PASSWORD in .env}"
: "${ADMIN_PASSWORD:?set ADMIN_PASSWORD in .env}"
cd backend
mvn spring-boot:run
