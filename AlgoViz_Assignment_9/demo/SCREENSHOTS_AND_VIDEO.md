# Submission demo — follow this checklist

Run `scripts/demo-cases.sh` after backend starts. Record a short screen capture showing:

1. `GET /api/algorithms` with **no token** -> **401**.
2. `POST /api/login` with admin credentials -> **200**, a JWT returned (blur/cover token).
3. `GET /api/algorithms` with valid Bearer token -> **200**.
4. `POST /api/visualizations` with ADMIN Bearer token -> **201**.
5. `POST /api/visualizations` with STUDENT Bearer token -> **403**.

Take **real** screenshots of these cases and put them in this folder or your Google Drive. Do not fake screenshots. Do not expose any complete token or passwords. Upload video/screenshots to Drive and paste the sharing link in README. GitHub repository URL and Google Classroom submission must also be completed by your team.

Optional additional verification: try `DELETE /api/visualizations/1` with a STUDENT Bearer token; expect **403**. The demo script saves this as `demo/output/06-student-delete.txt`.
