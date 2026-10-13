# AlgoViz Assignment 4 - Group 17

**Java 21 + Spring Boot 3.5.0 | Maven | JUnit 5**

| Name | Roll | Service |
| --- | --- | --- |
| Anurag Tiwari | 20251651028 | Progress / Team Leader |
| Saurabh Singh | 20251651089 | Algorithm |
| Niraj Kumar Gupta | 20251651066 | Visualization |
| Mohd Rashid | 20251651057 | User |

This package replaces the earlier Flask implementation with the project's Java
backend stack. Read `assignment4_progress/NOTES.md` for all assignment answers,
the resource table, failure mapping and design decisions. `submission.pdf` is the
cover/verification guide. Reference files from Assignments 2 and 3 are preserved.

## Build and test

Install JDK 21, Maven 3.6.3+ and curl; verify `java -version` and `mvn -version`.
Maven needs internet on the first build to download dependencies.

```sh
cd assignment4_progress
mvn clean package
```

This runs the 14 JUnit tests and produces `target/progress-service-1.0.0.jar`.
Build outputs are excluded from the source ZIP. `bin/` and `target/` are ignored
by Git. Import `pom.xml` into IntelliJ or Eclipse and select JDK 21.
If an older JAR was already committed, `.gitignore` does not untrack it:
run `git rm --cached --ignore-unmatch bin/progress-service-1.0.0.jar` from the
package root, then commit the removal.

## Run (two terminals, from package root)

Terminal 1 - separate Visualization fixture, implemented using JDK HttpServer:

```sh
java demo_visualization/VisualizationDemo.java
```

Terminal 2 - Progress Service in Spring Boot:

```sh
export VISUALIZATION_BASE_URL=http://127.0.0.1:5051
java -jar assignment4_progress/target/progress-service-1.0.0.jar
```

After rebuilding, use `assignment4_progress/target/progress-service-1.0.0.jar`.
PowerShell users set `$env:VISUALIZATION_BASE_URL="http://127.0.0.1:5051"` instead.
Optional `VISUALIZATION_PORT` and `PROGRESS_PORT` change listening ports; the
consumer always requires `VISUALIZATION_BASE_URL`, with no embedded default.
Do not add a trailing resource path to that base URL.

```sh
curl -i -X POST http://127.0.0.1:5050/progress-records \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: demo-1' \
  --data '{"userId":28,"algorithmId":1,"visualizationId":101}'
```

POST and PUT require `Content-Type: application/json` (a charset parameter is allowed).
Other media types, including `text/plain`, return HTTP 415 with the standard Problem body.

Repeat the same request/key to get the original 201 response and Location.
GET the returned Location; list with `/progress-records?userId=28`;
PUT `{"visualizationId":101}` to the Location plus `/completion`.
Both services bind to localhost. Stop them with Ctrl+C.

## Reproduce evidence (optional Python tooling only)

Python is **not the backend**. Two small utilities validate the YAML contract and
launch the Java services/curl to collect reproducible transcripts. They do not
implement APIs, storage or business logic. The backend and test suite use Java.
From `assignment4_progress`, regenerate the complete evidence on the submission laptop:

```sh
python3 -m venv .venv
. .venv/bin/activate
pip install -r scripts/requirements-tools.txt
python scripts/regenerate_evidence.py
```

The evidence runner chooses free ports and injects two temporary 503s before
success. It checks 23 live scenarios, writes the actual curl headers/bodies and
checks received request counts to prove retries and no retry on 4xx.
`regenerate_evidence.py` runs Maven, the validator and all live scenarios, then
replaces the evidence files only when every command succeeds. It records actual
local paths and versions; no paths are fabricated or rewritten. On Windows, use
`py -m venv .venv`, `.venv\Scripts\Activate.ps1`, then the same pip/python commands.

Evidence provenance is recorded in `assignment4_progress/evidence/environment.txt`
and `PROVENANCE.md`: these identify the actual execution environment and generation
time. Regeneration replaces these records together with the successful test and
HTTP outputs. Laptop-generated evidence requires running the script on that laptop.
Prerequisites are JDK 21, Maven, curl, Python and internet access for dependencies.

Existing evidence includes Maven/JUnit output, OpenAPI validation, network checks,
server logs and Java/Maven environment versions. Tests alone use Mockito;
the live demonstration uses actual TCP HTTP between separate Java processes.

## Scope and handout adaptation

The handout uses CampusEats and explicitly names Python modules and pytest.
This submission implements AlgoViz's Progress Service using Java classes and
JUnit equivalents, documented in NOTES. The implementation follows the project's
Java/Spring Boot stack; this differs from the handout's Python/pytest tooling. In-memory storage is allowed by the exercise: MySQL/JPA and
JWT from the full project are not implemented in this assignment slice. React
frontend changes are not needed for these REST requirements. Restart loses data
and idempotency receipts; this is a single-process teaching implementation.

The intended group-repository layout places `assignment4_progress` beside the
Tutorial 4 folder, with `demo_visualization`, `assignment2_reference`, and
`assignment3_reference` at the same parent level. Tutorial 3 and Assignment 3
artefacts are retained separately. The handout deadline is before Lecture 6;
no calendar date is specified.
