"""Run two independent local services and capture genuine curl -i responses.
Usage (from assignment4_progress): python scripts/capture_evidence.py
Requires curl on PATH. No external service is contacted.
"""
from pathlib import Path
import json
import os
import shlex
import socket
import subprocess
import sys
import time

SERVICE = Path(__file__).resolve().parents[1]
ROOT = SERVICE.parent
EVIDENCE = Path(os.environ.get('ALGOVIZ_EVIDENCE_DIR', str(SERVICE / 'evidence')))
EVIDENCE.mkdir(exist_ok=True)


def free_port():
    with socket.socket() as s:
        s.bind(('127.0.0.1', 0))
        return s.getsockname()[1]


def ready(port, proc):
    for _ in range(600):
        if proc.poll() is not None:
            raise RuntimeError('Service exited before becoming ready; inspect logs.')
        try:
            with socket.create_connection(('127.0.0.1', port), timeout=.1):
                return
        except OSError:
            time.sleep(.05)
    raise RuntimeError('Service did not start.')


def main():
    vp, pp = free_port(), free_port()
    while pp == vp:
        pp = free_port()
    env = dict(os.environ, VISUALIZATION_PORT=str(vp), PROGRESS_PORT=str(pp),
               VISUALIZATION_BASE_URL=f'http://127.0.0.1:{vp}', DEMO_FAIL_FIRST='2',
               PYTHONUNBUFFERED='1')
    processes = []
    with (EVIDENCE/'visualization.log').open('w') as vl, (EVIDENCE/'progress.log').open('w') as pl, (EVIDENCE/'curl-transcript.txt').open('w') as transcript:
        def call(label, method, path, status, body=None, key=None, raw=None, content_type="application/json"):
            args = ['curl','-i','-sS','--max-time','15','-X',method,f'http://127.0.0.1:{pp}{path}']
            if body is not None or raw is not None:
                args += ['-H','Content-Type: '+content_type,'--data',raw if raw is not None else json.dumps(body)]
            if key:
                args += ['-H','Idempotency-Key: '+key]
            transcript.write('\n## '+label+'\n$ '+shlex.join(args)+'\n')
            result = subprocess.run(args, capture_output=True, text=True, check=True)
            transcript.write(result.stdout+'\n')
            transcript.flush()
            lines = result.stdout.splitlines()
            assert int(lines[0].split()[1]) == status, (label, result.stdout)
            headers, content = result.stdout.split('\n\n',1)
            obj = json.loads(content)
            if status >= 400:
                assert set(obj) == {'type','title','status','detail'}
                assert obj['status'] == status
            return obj, headers
        try:
            v = subprocess.Popen(['java',str(ROOT/'demo_visualization/VisualizationDemo.java')], env=env, stdout=vl, stderr=subprocess.STDOUT)
            processes.append(v); ready(vp,v)
            p = subprocess.Popen(['java','-jar',str(SERVICE/'target/progress-service-1.0.0.jar')], env=env, stdout=pl, stderr=subprocess.STDOUT)
            processes.append(p); ready(pp,p)
            transcript.write('Actual local HTTP demonstration. Ephemeral ports are selected by this script.\n')
            transcript.write('The seeded Visualization demo injects two 503 responses before the first success.\n')
            call('POST rejects text/plain before creating a record','POST','/progress-records',415,{'userId':28,'algorithmId':1,'visualizationId':101},'wrong-media',content_type='text/plain')
            call('PUT rejects text/plain before looking up a record','PUT','/progress-records/missing/completion',415,{'visualizationId':101},content_type='text/plain')
            body = {'userId':28,'algorithmId':1,'visualizationId':101}
            first, headers = call('Create succeeds after two dependency retries','POST','/progress-records',201,body,'demo-1')
            location = '/progress-records/'+first['id']
            assert 'Location: '+location in headers
            replay, rh = call('Required: same request and key return original','POST','/progress-records',201,body,'demo-1')
            assert replay == first and 'Location: '+location in rh
            call('Required: malformed JSON rejected','POST','/progress-records',400,key='bad',raw='{"userId":')
            call('Required: missing record','GET','/progress-records/missing-id',404)
            call('Required: state conflict, duplicate user/algorithm with new key','POST','/progress-records',409,body,'demo-2')
            call('Single read','GET',location,200)
            call('Filtered list','GET','/progress-records?userId=28&status=IN_PROGRESS',200)
            done, _ = call('State-changing completion sub-resource','PUT',location+'/completion',200,{'visualizationId':101})
            assert done['status'] == 'COMPLETED'
            same, _ = call('Idempotent PUT completion repeat','PUT',location+'/completion',200,{'visualizationId':101})
            assert same == done
            call('Completion conflict: another visualization after completion','PUT',location+'/completion',409,{'visualizationId':104})
            replay, _ = call('POST replay preserves original even after completion','POST','/progress-records',201,body,'demo-1')
            assert replay == first
            call('Same key with changed payload','POST','/progress-records',409,dict(body,algorithmId=2),'demo-1')
            running, _ = call('Create another progress record','POST','/progress-records',201,dict(body,algorithmId=2,visualizationId=102),'running')
            call('Domain rejection: running visualization cannot complete progress','PUT','/progress-records/'+running['id']+'/completion',422,{'visualizationId':102})
            call('Missing referenced visualization: upstream 404 is not retried','POST','/progress-records',422,dict(body,algorithmId=3,visualizationId=999),'missing-session')
            call('Upstream 429 is not retried','POST','/progress-records',502,dict(body,algorithmId=3,visualizationId=429),'rate-limited')
            call('Malformed upstream data becomes 502','POST','/progress-records',502,dict(body,algorithmId=3,visualizationId=502),'malformed-remote')
            call('Persistent dependency 503 exhausts retry budget','POST','/progress-records',503,dict(body,algorithmId=3,visualizationId=503),'unavailable')
            v.terminate();v.wait(timeout=5)
            call('Connection refused: fallback after bounded retries','POST','/progress-records',503,dict(body,algorithmId=3,visualizationId=101),'disconnected')
            replay, _ = call('Stored idempotency result survives dependency outage','POST','/progress-records',201,body,'demo-1')
            assert replay == first
            final, _ = call('Failed dependency calls created no extra records','GET','/progress-records?userId=28',200)
            assert final['count'] == 2
            transcript.write('\nAll live scenario assertions passed.\n')
        finally:
            for proc in reversed(processes):
                if proc.poll() is None:
                    proc.terminate()
                    proc.wait(timeout=5)
    vlog = (EVIDENCE/'visualization.log').read_text()
    assert vlog.count('GET /visualizations/101 ') == 4  # 3 for create, 1 completion
    assert vlog.count('GET /visualizations/429 ') == 1
    assert vlog.count('GET /visualizations/999 ') == 1
    assert vlog.count('GET /visualizations/503 ') == 3
    print('PASS: live curl scenarios, real HTTP retries, no retry on 4xx, and outage fallback.')
    print('Saved evidence/curl-transcript.txt, progress.log and visualization.log')


if __name__ == '__main__':
    main()
