from http.server import BaseHTTPRequestHandler, HTTPServer
import json
class H(BaseHTTPRequestHandler):
 def do_POST(self):
  if self.path != '/payments': self.send_response(404); self.end_headers(); return
  self.send_response(200); self.send_header('Content-Type','application/json'); self.end_headers(); self.wfile.write(json.dumps({'status':'APPROVED'}).encode())
 def log_message(self,*a): pass
HTTPServer(('127.0.0.1',8090),H).serve_forever()
