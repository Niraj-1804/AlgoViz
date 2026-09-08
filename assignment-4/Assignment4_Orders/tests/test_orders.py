import json, os, sys, threading
sys.path.insert(0, os.path.dirname(os.path.dirname(__file__)))
import app
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.request import urlopen, Request

class PaymentMock(BaseHTTPRequestHandler):
    calls = 0
    def do_POST(self):
        PaymentMock.calls += 1
        self.send_response(200); self.end_headers(); self.wfile.write(b'{"status":"APPROVED"}')
    def log_message(self,*a): pass

def start_services():
    pay = HTTPServer(('127.0.0.1', 0), PaymentMock)
    threading.Thread(target=pay.serve_forever, daemon=True).start()
    app.PAYMENTS_URL = f'http://127.0.0.1:{pay.server_port}'
    srv = HTTPServer(('127.0.0.1', 0), app.Handler)
    threading.Thread(target=srv.serve_forever, daemon=True).start()
    return srv, pay

def request(srv, method, path, body=None, headers=None):
    data = json.dumps(body).encode() if body is not None else None
    r = Request(f'http://127.0.0.1:{srv.server_port}{path}', data=data, method=method, headers=headers or {})
    try:
        x = urlopen(r); return x.status, x.headers, json.loads(x.read())
    except Exception as e:
        return e.code, e.headers, json.loads(e.read())

def setup_function():
    app.STORE = app.OrderStore()

def test_create_has_201_and_location():
    srv,pay=start_services();
    s,h,b=request(srv,'POST','/orders',{'customerId':'C1','item':'Thali','quantity':1,'amount':120},{'Content-Type':'application/json','Idempotency-Key':'k1'})
    assert s==201 and h['Location'].startswith('/orders/') and b['status']=='CONFIRMED'
    srv.server_close(); pay.server_close()

def test_idempotent_repeat_returns_original():
    srv,pay=start_services();
    headers={'Content-Type':'application/json','Idempotency-Key':'same'}; body={'customerId':'C1','item':'Thali','quantity':1,'amount':120}
    s1,h1,b1=request(srv,'POST','/orders',body,headers); s2,h2,b2=request(srv,'POST','/orders',body,headers)
    assert s1==s2==201 and b1['id']==b2['id'] and PaymentMock.calls >= 1
    srv.server_close(); pay.server_close()

def test_malformed_body_is_400():
    srv,pay=start_services()
    r=Request(f'http://127.0.0.1:{srv.server_port}/orders',data=b'{bad',method='POST',headers={'Content-Type':'application/json'})
    try: urlopen(r); assert False
    except Exception as e: assert e.code==400
    srv.server_close(); pay.server_close()

def test_unknown_id_is_404():
    srv,pay=start_services(); s,h,b=request(srv,'GET','/orders/ORD-NOTFOUND'); assert s==404 and b['status']==404; srv.server_close(); pay.server_close()
