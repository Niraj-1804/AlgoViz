# AlgoViz — Sabse pehle yahan padhein

## Project run karne ka tarika

### 1. Zaroori software
- Java JDK 25
- Maven
- Node.js 20 ya uske baad ka version

### 2. Backend start karein
Terminal kholen:
```bash
cd AlgoViz_Project/backend
mvn spring-boot:run
```
Backend `http://localhost:8080` par chalega.

API test:
`http://localhost:8080/api/algorithms/bubble-sort?values=5,1,4,2,8`

### 3. Frontend start karein
Ek naya terminal kholen:
```bash
cd AlgoViz_Project/frontend
npm install
npm run dev
```
Terminal mein jo local URL aaye (aam taur par `http://localhost:5173`), browser mein kholen.

## Code ka simple explanation

### Backend ka flow
1. `AlgorithmController.java` URL se comma-separated numbers leta hai.
2. Controller numbers ko Java `List<Integer>` mein badalta hai.
3. `BubbleSortService.java` Bubble Sort run karta hai.
4. Har comparison/swap ke baad `AlgorithmStep` snapshot save hota hai.
5. `AlgorithmResponse` poore steps aur complexity ko JSON mein bhejta hai.

### Frontend ka flow
1. `App.jsx` API ko `fetch()` request bhejta hai.
2. API ka JSON `data` state mein save hota hai.
3. `stepIndex` batata hai ki kaunsa step dikhana hai.
4. Array values bars ki height aur labels banati hain.
5. Next/Previous `stepIndex` ko badalte/ghatate hain.
6. Play interval se agla step apne-aap dikhata hai.
7. `style.css` poori UI ka design banata hai.

## Common problem
- `mvn` not found: Maven install/PATH check karein.
- `java` version problem: `java -version` se Java 25 verify karein.
- Frontend API error: pehle backend terminal chal raha ho, phir frontend run karein.
- Port busy: 8080 ya 5173 use karne wali doosri process band karein.

## Dhyan dein
Yeh starter project hai. Is version mein Bubble Sort visualization implement hai. PDF mein diye gaye baaki algorithms, login, MySQL history ko next development phase mein add kiya ja sakta hai. MySQL is starter mein abhi configured nahin hai.
