#!/usr/bin/env bash
set -e
export PAYMENTS_SERVICE_URL="${PAYMENTS_SERVICE_URL:-http://localhost:8090}"
export PORT="${PORT:-8080}"
python app.py
