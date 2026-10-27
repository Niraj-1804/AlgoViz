# AlgoViz - Assignment 4: Progress Service

**CS543 Web Services | Group ID: 17**

| Member | Roll number | Team responsibility |
| --- | --- | --- |
| Anurag Tiwari | 20251651028 | Progress Service; Team Leader |
| Saurabh Singh | 20251651089 | Algorithm Service |
| Niraj Kumar Gupta | 20251651066 | Visualization Service |
| Mohd Rashid | 20251651057 | User Service |

## Scope, source contracts and interpretation

The supplied brief is written for CampusEats and explicitly lists Catalogue,
Orders or Delivery. This submission applies the requirements to **AlgoViz** and
implements its existing **Progress Service**, not Payments. This is a stated
project-specific substitution, not a claim that the original brief lists Progress.
If only the literal CampusEats choices are allowed, the instructor must accept
this adaptation; that restriction cannot be resolved by silently renaming a service.

Assignment 3 used the Visualization Service's executeAlgorithm() path and a
fictional external SOAP execution partner. This assignment implements a DIFFERENT
service: Assignment 2's Progress Service and its trackProgress() responsibility.
Progress still owns learning progress/history; this four-endpoint slice implements
learning progress only. Algorithm metadata remains with Algorithm Service; users
remain with User Service; execution sessions remain with Visualization Service.
No cross-service store or database is imported. The original service-contract PDF
and SQL schema are retained under ../assignment2_reference for traceability.

Assignment 2 gives operation-level inputs and errors, not JSON field names or URLs.
These REST bindings refine that contract. The error phrase "invalid progress"
becomes a 422 Problem when the referenced session is ineligible. A missing local
progress resource becomes 404. Dependency unavailability becomes 503 rather than
incorrectly claiming that a user does not exist. Additional infrastructure errors
are explicit REST contract extensions, not invented quotes from Assignment 2.

The implementation now uses the project's **Java 21 + Spring Boot** backend stack.
The handout specifies Python module names and pytest. This implementation uses
equivalent Java classes and JUnit 5/MockMvc to align with the project's technology
stack; the table below documents this tooling adaptation. The backend and the separate dependency
fixture are Java. Python appears only in optional contract/evidence tooling;
it is not required to build or run either Java service. In-memory storage is
permitted by the exercise; full-project MySQL/JPA persistence and JWT are outside
this four-endpoint service slice.

| Handout component | Java equivalent |
| --- | --- |
| app.py | ProgressApplication + ProgressController |
| models.py | ProgressRecord and immutable public DTO record |
| store.py | ProgressStore |
| errors.py | ApiException + ProblemAdvice.problem |
| request validation | Validation.body/key/filters |
| outbound client | VisualizationClient using JDK HttpClient |
| pytest tests | JUnit 5 + MockMvc + Mockito in ProgressTests |

The intended group-repository layout places this service beside the Tutorial 4
folder. Tutorial 3 and Assignment 3 artefacts are retained separately; README.md
describes the service, demo and reference-folder arrangement.

## Part A: from operations to resources

### A2-A3: operation-to-noun mapping

These are SOAP-style design starting points, not public URLs or a new WSDL:

| Starting operation | Durable noun / representation | REST operation |
| --- | --- | --- |
| trackProgress(userId, algorithmId, visualizationId) | progress records | POST /progress-records |
| getProgress(progressId) | one progress record | GET /progress-records/{id} |
| listProgress(userId, status) | a filtered collection of progress records | GET /progress-records?userId=28&status=IN_PROGRESS |
| completeProgress(progressId, visualizationId) | the completion of a progress record | PUT /progress-records/{id}/completion |

The read/list/complete operations decompose Assignment 2's broader trackProgress()
capability; they do not transfer ownership from another service. URLs contain nouns.

### A4: resource table

| Method | URL | What it does | Success code | Failure codes |
| --- | --- | --- | --- | --- |
| POST | /progress-records | Verify a visualization, then create IN_PROGRESS tracking for a user/algorithm pair | 201 + Location (also replay) | 400, 409, 413, 415, 422, 502, 503, 500 |
| GET | /progress-records/{id} | Read the current public record | 200 | 404, 500 |
| GET | /progress-records?userId=28&status=IN_PROGRESS | List by required userId and optional status | 200 | 400, 500 |
| PUT | /progress-records/{id}/completion | Verify a completed visualization, then mark progress COMPLETED | 200 | 400, 404, 409, 413, 415, 422, 502, 503, 500 |

400 denotes malformed JSON, missing/extra fields, bad types/IDs, invalid key or
invalid filters. 404 denotes an absent progress record. 409 denotes changed data
under a used key, a duplicate user/algorithm pair under another key, or a different
completion after completion. 422 denotes a syntactically valid request whose
visualization does not exist, does not match the user/algorithm, has failed, or is
not completed when completion is requested. 413 is the 16 KiB body limit; 415 denotes an unsupported request media type.
502 denotes a refused, malformed or unexpected dependency response; 503 denotes
exhausted transient/transport failure. 500 is a sanitized unexpected internal error.
Router-level unknown URLs and unsupported methods also use problem() (404/405).

No operation uses 202: work is completed synchronously before returning. Inventing
background work just to emit 202 would misrepresent the behaviour.

### A5: hardest mapping (four sentences)

Completion was the least natural mapping because completeProgress sounds like an
RPC verb. I represented it as /progress-records/{id}/completion and used PUT with
a visualization reference to establish the completed state. Repeating the same
completion returns the same representation, while a different visualization after
completion produces 409. I rejected /completeProgress because it exposes a verb,
and rejected a general unrestricted status patch because it would let callers
claim completion without the required visualization evidence.

## Part B: contract-first development

openapi.yaml was authored and successfully checked before the handler code was
written. It has four operations; every operation has parameters and/or a body and
multiple documented responses. All body representations use components.schemas
and $ref, with reusable component parameters and errors. Evidence is in
`evidence/openapi-validation.txt`; regenerate it using the command in README.
OpenAPI validation checks the document, NOT whether requests are automatically
validated by Spring MVC.

## Part C: implementation decisions

### Record versus public representation

ProgressRecord stores internalId, publicId, userId, algorithmId, visualizationId,
idempotencyKey, requestFingerprint, createdAt, status and completedAt.
representation() returns an immutable PublicRecord DTO, omitting internalId,
idempotencyKey and requestFingerprint. The public UUID is distinct from the
numeric internal ID. No credential is accepted or exposed. This local exercise
uses userId as a reference, not authentication; production requires identity and
per-user authorization.

### Manual validation and one problem shape

The POST and PUT mappings declare `consumes = application/json`. Spring rejects
unsupported media types before the controller runs; ProblemAdvice handles
HttpMediaTypeNotSupportedException through problem() with HTTP 415. JSON with
a charset parameter remains supported. Both operations document the reusable
E415 response in OpenAPI.

Validation.body(byte[], fields) parses JSON and checks the complete object before
controllers access fields. It rejects missing/extra fields, non-object JSON,
booleans, strings, trailing JSON values and IDs outside the positive int range.
Validation.key and Validation.filters check headers and queries. ProblemAdvice
routes domain, JSON parsing, router and unexpected errors through problem(),
returning application/problem+json with type/title/status/detail. Unexpected
exceptions produce a sanitized 500. The 16 KiB limit is checked before parsing
but after Spring reads the byte array; it is not a streaming memory limit.

### Idempotency and state

POST requires Idempotency-Key. A successful create stores the key with its record
and saves a separate immutable receipt: canonical request fingerprint, original
201 body and Location. A repeat with the same key and equivalent parsed JSON
returns the ORIGINAL body and Location without dependency calls or writes, even
after the record is completed or the dependency has gone down. A changed payload
with that key returns 409; JSON property ordering does not change the fingerprint.

One user/algorithm pair has one progress record, so a different key cannot create
a duplicate pair. That prevents duplicate dashboard progress and repeated network
work. A synchronized ProgressStore monitor covers the replay check, remote verification and create
so simultaneous retries are serialized. Failed dependency calls create no record
or successful receipt; the client can retry after recovery with the same key.
PUT completion is also idempotent for the same visualization and returns 200.

The storage and receipts last only for the process lifetime. Restart loses them;
multiple worker processes do not share them. A production system would use durable
transactions and unique constraints for both the key and user/algorithm pair.
The global lock trades throughput for simplicity; no production scalability is claimed.

## Part D: one real outbound HTTP call

Progress uses VisualizationClient.fetch() to GET
`{VISUALIZATION_BASE_URL}/visualizations/{visualizationId}`. The service fails fast
at startup if the environment variable is absent or invalid; no dependency URL is
hard-coded into the consumer. Public reference IDs are passed over HTTP. The
independent demo_visualization/VisualizationDemo.java implements the existing getVisualization()
boundary with seeded session records and fault injection. It is an executable
local stand-in, not a real production visualization engine, and imports no Progress
store. Live evidence uses two processes and TCP HTTP; JUnit mocks are used only
for isolated tests.

A create checks the session's userId/algorithmId before recording IN_PROGRESS.
A completion additionally requires the session status COMPLETED. Progress trusts
the owning service's references rather than querying its database. Future User or
Algorithm existence checks would likewise go through those services' contracts.

### D2: timeout and retry policy

- JDK HttpClient has a 500 ms connection timeout; each GET request has a one-second request timeout.
- Maximum three total attempts; retries only for connection/timeout exceptions
  and HTTP 502, 503 or 504 on this read-only GET.
- Sleeps before retries: 0.1 * 2^attempt + Uniform(0, 0.1) seconds, giving
  [0.1, 0.2] and [0.2, 0.3] second windows (integer milliseconds). Sleeps are logged.
- Every 4xx, including 429, is handled immediately and never retried. Dependency
  404 maps to 422; other 4xx map to 502 because the internal lookup was refused.
- Redirects are disabled; malformed successes and other unexpected statuses are
  502 without retries. No outbound POST/create exists, so no unsafe create retry
  is performed. Any future retried create must carry a stable idempotency key.
- Transport IO failures and request timeout exceptions share the bounded retry
  path; interruption restores the thread interrupt flag and aborts with 503.

### D3: fallback reasoning (three sentences)

If verification remains unavailable after bounded retries, return 503 and do not
create or complete a progress record. Degrading by accepting unverified completion
would fabricate learning progress, so failing is the correct choice. Existing
records remain readable and stored idempotency receipts remain replayable because
those paths do not require the dependency.

## Five comparison answers

### 1. WSDL versus OpenAPI line counts

The retained Assignment 3 `partner.wsdl` has **100 lines** and
`openapi.yaml` has **324 lines** (counting physical lines with splitlines).
OpenAPI is **224 lines longer** in these files; a shorter contract is not assumed.
The WSDL describes one trace operation, an XML credential header, array/step types,
four abstract messages, a portType, SOAP binding and service/port. OpenAPI describes
four REST operations, validation constraints, parameters, reusable Problem responses,
all failure statuses and the idempotency behaviour. Much of the extra length is
those additional operations and YAML formatting, not an inherent cost of REST.

Two WSDL declarations no longer needed are (1) the SOAPAction/document-literal
SOAP binding, because the HTTP method/resource URL identifies the REST operation,
and (2) the wsdl:message/part envelope mapping and portType abstraction, because
OpenAPI directly binds parameters and JSON request/response schemas to paths.
The new contract still needs an address (servers) and type schemas; those needs
have not disappeared.

### 2. Actual SOAP fault and its HTTP/problem counterpart

This is the retained Assignment 3 fault envelope, unchanged:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope
 xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
 xmlns:alg="https://api.labcompute.example/algorithms/v1">
 <soapenv:Body>
  <soapenv:Fault>
   <faultcode>soapenv:Server</faultcode>
   <faultstring>The partner execution engine is unavailable.</faultstring>
   <detail>
    <alg:ExecutionFault>
     <alg:requestId>av-run-0001</alg:requestId>
     <alg:code>ENGINE_UNAVAILABLE</alg:code>
     <alg:message>No execution worker is available; no trace was produced.</alg:message>
    </alg:ExecutionFault>
   </detail>
  </soapenv:Fault>
 </soapenv:Body>
</soapenv:Envelope>
```

The corresponding dependency-unavailable class is represented in this REST
service by the following actual 503 response (also in curl-transcript.txt):

```http
HTTP/1.1 503 Service Unavailable
Content-Type: application/problem+json

{
  "type": "https://algoviz.example/problems/dependency-unavailable",
  "title": "Service Unavailable",
  "status": 503,
  "detail": "Cannot verify learning progress right now. Please try again later."
}
```

Because this assignment intentionally uses a different service, this is a mapping
of failure semantics, NOT a claim that Progress directly invokes/parses that SOAP
partner. Assignment 3's executeAlgorithm execution failure becomes a verification
dependency failure at Progress's boundary; its 503 is explicitly documented in the
OpenAPI create/completion responses. No vendor code reaches the student.
Returning this inside 200 would signal success to generic HTTP clients, proxies,
health metrics and retry/circuit-breaker policies that do not parse application
JSON. A real 503 carries the failure in the protocol status; retries still need
the safety/idempotency policy, not blanket retrying of every error.

### 3. Publish, find and bind

All three roles remain, but this implementation has no UDDI registry. UDDI
provides publication and inquiry APIs; "bind" means the consumer uses the
resolved service information to invoke the provider, not that UDDI forwards calls.
The replacement for each role is explicit:

| Role | Classic SOAP/UDDI arrangement | AlgoViz implementation |
| --- | --- | --- |
| Publish | A provider registers a businessService, bindingTemplate/access point and references to technical tModels; WSDL describes the interface. | The Progress team supplies versioned openapi.yaml containing paths, schemas and responses. Publishing it in the team repository is the distribution step; a deployed catalogue is not part of this package. |
| Find | A consumer queries the registry to identify a suitable service and retrieve its binding information. | The developer identifies Visualization Service from the Assignment 2 contract and README; deployment supplies its location through VISUALIZATION_BASE_URL. There is no runtime registry query. |
| Bind | The consumer configures its SOAP client from the contract and endpoint, then invokes the provider. | VisualizationClient.fetch(id) constructs GET {VISUALIZATION_BASE_URL}/visualizations/{id}, sends HTTP and validates the JSON response. Spring MVC binds incoming Progress requests to controller methods. |

For example, creating progress requires verification of visualization 101. The
Progress service reads the configured Visualization base URL, calls
`GET /visualizations/101`, and checks ownership/status before storing progress.
The consumer uses Visualization's contract for that lookup; Progress's OpenAPI
file describes the API it offers to its own consumers. Neither an OpenAPI
`servers` entry nor an environment variable performs service discovery by itself.

What disappears is **UDDI-specific registration and lookup infrastructure**:
registry keys, tModel references and bindingTemplate records are not used at
runtime. WSDL bindings, SOAP envelopes and SOAPAction dispatch also disappear
from this REST slice. The practical trade-off is simpler setup but no automatic
registry-based endpoint selection or failover: an address change needs updated
configuration and service restart. Assignment 3 only described discovery on
paper, so this is an architectural comparison, not a claim that a deployed UDDI
server was removed. [R1]

### 4. Who enforces the request schema now?

The exact runtime function is `Validation.body(byte[], String...)`, called by
both mutating controller methods before reading JSON fields. Without it, Jackson
coercion or unchecked JsonNode access could accept unwanted types or turn a missing
visualizationId into an accidental 500. The validator instead rejects boolean IDs,
missing fields and wrong shapes with 400. XML Schema enforcement likewise needs
a configured SOAP validator; WSDL and OpenAPI documents alone do not execute
runtime validation.

### 5. Where SOAP would still be appropriate

I would choose SOAP for **Progress Service's completion acknowledgement** if an
institutional consumer required a signed, verifiable completion record. This is
the response produced by Progress after it validates completion evidence and
changes its own stored record to COMPLETED, currently exposed through
`PUT /progress-records/{id}/completion`. The chosen responsibility belongs to
Progress: it records learning progress and publishes that record's completed
state; it does not execute or visualize an algorithm.

For example, a completion acknowledgement could pass through an institutional
gateway before reaching an academic-records system. I would use a SOAP response
with **WS-Security XML Signature**, signed by Progress Service using a trusted
X.509 identity. The agreed policy would cover the exact response Body containing
the public progress ID, userId, algorithmId, visualizationId, COMPLETED status and
completedAt, together with a correlation ID and security timestamp. The consumer
would verify the signing identity and signature coverage and use only the
verified body. If a gateway changed the student ID or completed status, signature
verification would fail. [R2]

The precise guarantee is **authenticated origin and detection of changes to the
signed completion acknowledgement across intermediary hops**, assuming trusted
keys and correct verification. HTTPS protects individual connections; a message
signature remains verifiable after a gateway terminates TLS. The signature does
not prove that the student actually learned the algorithm: Progress must still
validate the underlying completion evidence before issuing its acknowledgement.

An institutional SOAP/WS-Security interoperability requirement would justify this
choice; REST can also support message signatures. Replay protection requires
freshness checks and persistent duplicate detection, while repeat completion
requests still require idempotent state handling. SOAP alone does not guarantee
confidentiality, authorization or exactly-once processing. This is a conditional
design alternative for Progress's own completion response; the submitted REST
implementation does not implement WS-Security or issue signed acknowledgements.

## Verification and evidence

`src/test/java/com/algoviz/progress/ProgressTests.java` contains 14 JUnit tests,
including four methods named required* for the mandatory scenarios. Additional
checks cover concurrency, original-response replay, retry timing, all tested 4xx
statuses, outage atomicity, malformed upstream responses and domain validation.
Two regression tests also check 415 for unsupported media types on both mutations,
no dependency calls or stored records on rejection, and JSON with a charset.
Evidence is from actual Maven/JUnit execution and two independent Java processes.
The live curl transcript covers all five mandatory scenarios plus completion,
filtering, 422, 429, malformed dependency responses, persistent 503 and outage replay.

## References

- Provided Assignment 4(1).pdf: Parts A-D and submission instructions.
- Provided AlgoViz_DSA_Visualizer_Project(1).pdf: modules and architecture.
- ../assignment2_reference/service-contract.pdf: Task 3 Progress Service contract.
- ../assignment3_reference/partner.wsdl and soap-fault.xml: preserved comparisons.
- Spring Boot 3.5 system requirements: https://docs.spring.io/spring-boot/3.5/system-requirements.html
- JDK HttpClient: https://docs.oracle.com/en/java/javase/21/docs/api/java.net.http/java/net/http/HttpClient.html
- OpenAPI 3.0.3: https://spec.openapis.org/oas/v3.0.3.html

[R1] OASIS, UDDI Version 3.0.2, data model and publication/inquiry APIs:
https://www.oasis-open.org/committees/uddi-spec/doc/spec/v3/uddi_v3.htm

[R2] OASIS, Web Services Security: SOAP Message Security Version 1.1.1,
message integrity, confidentiality and end-to-end message security:
https://docs.oasis-open.org/wss-m/wss/v1.1.1/os/wss-SOAPMessageSecurity-v1.1.1-os.pdf
