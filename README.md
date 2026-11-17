<<<<<<< HEAD
Project Team Member - 

leader name : Anurag Tiwari(20251651028)
Mohd Rashid(20251651057)
Niraj Kumar Gupta(20251651066)
Saurabh Singh(20251651089)
=======
# AlgoViz — DSA Visualizer

A beginner-friendly full-stack project based on your project proposal.

## Stack
- Frontend: React + Vite
- Backend: Java 25 + Spring Boot REST API
- Database: MySQL (optional for this starter; algorithm API works without it)

## Features in this starter
- Bubble Sort visualization steps
- Linear Search visualization steps
- Play / Pause, Next / Previous, Reset, speed control
- Step-by-step explanation and complexity
- React UI connected to Spring Boot API

## Run the backend
Install Java 25 and Maven, then in `backend/` run:

```bash
mvn spring-boot:run
```

API: `http://localhost:8080/api/algorithms/bubble-sort?values=5,1,4,2,8`

## Run the frontend
Install Node.js 20+ then in `frontend/` run:

```bash
npm install
npm run dev
```

Open the local URL printed by Vite (usually http://localhost:5173).

## How the flow works
1. React sends the array to `GET /api/algorithms/bubble-sort?values=...`.
2. Spring Boot runs Bubble Sort and creates a list of snapshots.
3. API returns JSON containing each snapshot, highlighted indexes, operation, explanation, and complexity.
4. React displays the selected snapshot as bars. Next/Previous and Play/Pause only change the displayed step.

## Beginner learning path
1. Start with `backend/src/main/java/com/algoviz/controller/AlgorithmController.java`.
2. Read `BubbleSortService.java` to see comparisons and swaps.
3. Read `frontend/src/App.jsx` to see how API data becomes bars.
4. Change the example array and inspect the JSON in the browser.

## Notes
- This is a learning starter, not a production deployment. Authentication, saved history, validation hardening, and MySQL persistence can be added next.
- If the browser blocks a request, check that the backend is running on port 8080.
>>>>>>> 8aadde6 (Initial commit)
