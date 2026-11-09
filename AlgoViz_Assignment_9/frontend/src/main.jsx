import React, {useEffect, useState} from 'react';
import {createRoot} from 'react-dom/client';
import './style.css';
const API='http://localhost:8080/api';
function App(){
  const [username,setUsername]=useState('student');
  const [password,setPassword]=useState('');
  const [token,setToken]=useState('');
  const [role,setRole]=useState('');
  const [algorithm,setAlgorithm]=useState('bubble-sort');
  const [values,setValues]=useState('8, 3, 5, 1, 7');
  const [target,setTarget]=useState('5');
  const [result,setResult]=useState(null);
  const [step,setStep]=useState(0);
  const [playing,setPlaying]=useState(false);
  const [speed,setSpeed]=useState(850);
  const [message,setMessage]=useState('Sign in to start');
  async function request(path,options={}){
    const resp=await fetch(`${API}${path}`,{...options,headers:{'Content-Type':'application/json',...(token?{Authorization:`Bearer ${token}`}:{})}});
    const body=await resp.json().catch(()=>({}));
    if(!resp.ok) throw new Error(`${resp.status}: ${body.error||resp.statusText}`);
    return body;
  }
  async function login(e){
    e.preventDefault();
    try{
      const res=await request('/login',{method:'POST',body:JSON.stringify({username,password})});
      setToken(res.token);setRole(res.role);setMessage(`Logged in as ${res.username} (${res.role})`);
      setResult(null);setPlaying(false);
    }catch(e){setMessage(e.message);}
  }
  async function visualize(){
    try{
      const input=values.split(',').map(s=>Number(s.trim()));
      if(!values.trim()||input.some(x=>!Number.isFinite(x)))throw new Error('Enter comma-separated numbers');
      const payload={algorithm,values:input,...(algorithm==='linear-search'?{target:Number(target)}:{})};
      const res=await request('/visualizations',{method:'POST',body:JSON.stringify(payload)});
      setResult(res.visualization);setStep(0);setPlaying(false);setMessage(`Visualization #${res.id} saved`);
    }catch(e){setMessage(e.message);}
  }
  useEffect(()=>{
    if(!playing||!result)return;
    const timer=setInterval(()=>setStep(prev=>{
      if(prev>=result.steps.length-1){setPlaying(false);return prev;} return prev+1;
    }),speed);
    return ()=>clearInterval(timer);
  },[playing,result,speed]);
  const current=result?.steps[step];
  return <div className="app">
    <header><div className="logo">ALGOVIZ <span>DSA VISUALIZER</span></div><p>Watch algorithms work, one step at a time.</p></header>
    <section className="panel"><h2>Secure Login</h2><form onSubmit={login} className="inline">
      <label>Username<select value={username} onChange={e=>setUsername(e.target.value)}><option>student</option><option>admin</option></select></label>
      <label>Password<input type="password" value={password} onChange={e=>setPassword(e.target.value)} required placeholder="Password from .env"/></label>
      <button>Log in</button>{token&&<button type="button" className="secondary" onClick={()=>{setToken('');setRole('');setResult(null);setPlaying(false);setMessage('Logged out');}}>Log out</button>}</form>
      <p className="status">{message}</p>
    </section>
    <section className="panel"><h2>Algorithm Lab</h2><div className="inline">
      <label>Algorithm<select value={algorithm} onChange={e=>setAlgorithm(e.target.value)}><option value="bubble-sort">Bubble Sort</option><option value="linear-search">Linear Search</option></select></label>
      <label>Input array<input value={values} onChange={e=>setValues(e.target.value)}/></label>
      {algorithm==='linear-search'&&<label>Find<input value={target} onChange={e=>setTarget(e.target.value)} type="number"/></label>}
      <button onClick={visualize} disabled={role!=='ADMIN'}>Run & Save</button>
    </div><p className="hint">Only ADMIN can start and save runs (Assignment 9 role rule). STUDENT can read secured endpoints.</p>
    {result&&<><div className="bars">{current.values.map((v,i)=><div key={i} className={'bar '+(current.activeIndices.includes(i)?'active':'')}><strong>{v}</strong><small>{i}</small></div>)}</div>
      <p className="explanation"><b>Step {step+1}/{result.steps.length}</b> — {current.operation}: {current.explanation}</p>
      <div className="controls"><button className="secondary" onClick={()=>{setPlaying(false);setStep(0);}}>Reset</button><button className="secondary" onClick={()=>setStep(s=>Math.max(0,s-1))}>Previous</button><button onClick={()=>setPlaying(p=>!p)}>{playing?'Pause':'Play'}</button><button className="secondary" onClick={()=>setStep(s=>Math.min(result.steps.length-1,s+1))}>Next</button><label>Speed <input type="range" min="200" max="1500" value={1700-speed} onChange={e=>setSpeed(1700-Number(e.target.value))}/></label></div>
      <p className="hint">Time: {result.timeComplexity} · Space: {result.spaceComplexity}</p></>}
    </section><footer>React + Java 21 + Spring Boot + MySQL · JWT protected</footer>
  </div>;
}
createRoot(document.getElementById('root')).render(<App/>);
