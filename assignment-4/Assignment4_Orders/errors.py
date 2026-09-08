import json

def problem(status, title, detail):
    return {"type": "https://campuseats.example.com/problems/" + title.lower().replace(" ", "-"),
            "title": title, "status": status, "detail": detail}

def send_json(handler, status, body, headers=None):
    raw = json.dumps(body).encode()
    handler.send_response(status)
    handler.send_header("Content-Type", "application/json")
    handler.send_header("Content-Length", str(len(raw)))
    for k, v in (headers or {}).items(): handler.send_header(k, v)
    handler.end_headers()
    handler.wfile.write(raw)
