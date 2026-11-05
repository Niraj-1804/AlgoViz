ALGOVIZ - CS543 ASSIGNMENT 3 - GROUP 17

Team Leader: Anurag Tiwari (20251651028)
Saurabh Singh (20251651089) - Algorithm Service
Niraj Kumar Gupta (20251651066) - Visualization Service
Mohd Rashid (20251651057)

Required deliverables included:
  integration.pdf
  partner.wsdl
  soap-request.xml
  soap-response.xml
  soap-fault.xml

This final revision replaces the earlier assumed SMS/OTP edge with an external
algorithm-execution partner and maps its fault to Assignment 2 Task 4's
executeAlgorithm() -> Execution Failed. The supplied Assignment 2 defines no
OTP operation and no concrete REST route, status or JSON error representation.
The report clearly distinguishes its binding choices from that source contract.

Project-specific interpretation:
The assignment brief names CampusEats payment/delivery/SMS partners. This
submission explicitly substitutes an algorithm-execution provider for AlgoViz.
If your instructor requires one of those three gateway categories literally,
that restriction must be resolved before using this project-specific adaptation.

Provider and .example addresses are fictional; no live service has been run.
Local validation checks XML/XSD payloads, WSDL references, SOAPAction, endpoint,
request/response correlation and the example Bubble Sort trace. It is not a
live interoperability test. Demo credentials must not be used in production.

Submission:
Extract the ZIP and place the five required files in your AlgoViz repository.
Commit and push, then submit the repository link and upload this ZIP as leader.
The wording "three documents" in the brief conflicts with its five-file list;
all five explicitly named deliverables are included. README is supplementary.
No repository has been connected, committed or pushed by this preparation.
