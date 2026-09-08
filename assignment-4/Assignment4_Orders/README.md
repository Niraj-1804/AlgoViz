# Assignment 4 — CampusEats Orders REST Service

## Run

```bash
export PAYMENTS_SERVICE_URL=http://localhost:8090
export PORT=8080
python app.py
```

The Orders service makes a real HTTP `POST /payments` call to the configured Payments service. The dependency URL is read from `PAYMENTS_SERVICE_URL`.

## Validate OpenAPI

In an environment with `openapi-spec-validator` installed:

```bash
openapi-spec-validator openapi.yaml
```

The included `validator_output.txt` records the local zero-error structural validation used while preparing the submission package.

## Run tests

```bash
pytest -q
```

Expected result: `4 passed`.

## Submission evidence

- `openapi.yaml` — REST contract
- `models.py`, `store.py`, `errors.py`, `app.py` — implementation
- `tests/` — four required tests
- `NOTES.md` — A4, A5, D3 and all five written answers
- `curl_transcript.txt` — required `curl -i` evidence
- `validator_output.txt` — validation evidence
- `pytest_output.txt` — test evidence
