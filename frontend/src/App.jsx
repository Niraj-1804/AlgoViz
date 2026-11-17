import { useEffect, useMemo, useState } from 'react'
import LinkedListVisualizer from './LinkedListVisualizer'
import SearchingVisualizer from './SearchingVisualizer'
import TreeVisualizer from './TreeVisualizer'

export default function App() {
  const [input, setInput] = useState('5, 1, 4, 2, 8')
  const [algorithm, setAlgorithm] = useState('bubble')
  const [page, setPage] = useState('sorting')

  const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

  const API = `${API_BASE_URL}/api/algorithms/${algorithm}-sort`

  const [data, setData] = useState(null)
  const [stepIndex, setStepIndex] = useState(0)
  const [speed, setSpeed] = useState(700)
  const [playing, setPlaying] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  async function runAlgorithm() {
    setLoading(true)
    setPlaying(false)
    setError('')

    try {
      const response = await fetch(
        `${API}?values=${encodeURIComponent(input.replaceAll(' ', ''))}`
      )

      const result = await response.json()

      if (!response.ok) {
        throw new Error(result.error || 'Could not run algorithm')
      }

      setData(result)
      setStepIndex(0)
    } catch (e) {
      setError(
        `${e.message}. Is the Spring Boot backend running on port 8080?`
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (page === 'sorting') {
      runAlgorithm()
    }
    // Initial load for the sorting page only.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (!playing || !data) return

    const timer = setInterval(() => {
      setStepIndex(current => {
        if (current >= data.steps.length - 1) {
          setPlaying(false)
          return current
        }

        return current + 1
      })
    }, speed)

    return () => clearInterval(timer)
  }, [playing, data, speed])

  const step = data?.steps?.[stepIndex]

  const max = useMemo(
    () => Math.max(1, ...(step?.values || []).map(value => Math.abs(value))),
    [step]
  )

  const percent = data?.steps?.length
    ? Math.round(((stepIndex + 1) / data.steps.length) * 100)
    : 0

  function changeStep(next) {
    if (!data) return

    setPlaying(false)
    setStepIndex(
      Math.max(0, Math.min(data.steps.length - 1, next))
    )
  }

  function handleAlgorithmChange(value) {
    setAlgorithm(value)
    setPlaying(false)
    setData(null)
    setStepIndex(0)
    setError('')
  }

  function handlePageChange(value) {
    setPlaying(false)
    setPage(value)
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#">
          <span className="brand-mark">A</span> AlgoViz
        </a>

        <nav>
          <button
            className={`nav-link ${page === 'sorting' ? 'active' : ''}`}
            onClick={() => handlePageChange('sorting')}
          >
            Sorting
          </button>

          <button
            className={`nav-link ${page === 'linkedlist' ? 'active' : ''}`}
            onClick={() => handlePageChange('linkedlist')}
          >
            Linked List
          </button>

          <button
            className={`nav-link ${page === 'searching' ? 'active' : ''}`}
            onClick={() => handlePageChange('searching')}
          >
            Searching
          </button>

          <button
            className={`nav-link ${page === 'tree' ? 'active' : ''}`}
            onClick={() => handlePageChange('tree')}
          >
            Tree Visualizer
          </button>

          <a href="#learn">Learn</a>

          <span className="status">
            <i /> DSA Playground
          </span>
        </nav>
      </header>

      <main>
        <section className="hero">
          <div className="eyebrow">
            <span /> INTERACTIVE DSA LAB
          </div>

          <h1>
            See algorithms <em>come to life.</em>
          </h1>

          <p>
            Learn algorithms one step at a time. Explore comparisons,
            operations, and how each algorithm works.
          </p>

          <div className="hero-tags">
            <span>✦ Step-by-step</span>
            <span>⌘ Beginner friendly</span>
            <span>↗ Complexity insights</span>
          </div>
        </section>

        {page === 'sorting' ? (
          <section id="visualizer" className="workspace">
            <div className="section-heading">
              <div>
                <span className="section-kicker">WORKSPACE / 01</span>
                <h2>Algorithm visualizer</h2>
              </div>

              <span className="live-pill">
                <i /> LIVE PLAYGROUND
              </span>
            </div>

            <div className="panel">
              <div className="toolbar">
                <label className="field-label">
                  ALGORITHM

                  <select
                    value={algorithm}
                    onChange={e => handleAlgorithmChange(e.target.value)}
                  >
                    <option value="bubble">Bubble Sort</option>
                    <option value="selection">Selection Sort</option>
                  </select>
                </label>

                <label className="field-label input-field">
                  YOUR ARRAY

                  <input
                    value={input}
                    onChange={e => setInput(e.target.value)}
                    placeholder="5, 1, 4, 2, 8"
                  />
                </label>

                <button
                  className="primary-btn"
                  onClick={runAlgorithm}
                  disabled={loading}
                >
                  {loading ? 'Running…' : '▶ Visualize'}
                </button>
              </div>

              {error && <div className="error">{error}</div>}

              <div className="viz-header">
                <div>
                  <span className="mini-dot" /> ARRAY VIEW
                  <span className="muted">
                    {' '}· {step?.values?.length || 0} elements
                  </span>
                </div>

                <span className="step-counter">
                  STEP <b>{data ? stepIndex + 1 : 0}</b> /{' '}
                  {data?.steps?.length || 0}
                </span>
              </div>

              <div className="chart-area">
                {step?.values?.map((value, i) => {
                  const active =
                    i === step.activeIndex ||
                    i === step.compareIndex

                  const swapped = step.operation === 'SWAP' && active

                  const height = Math.max(
                    12,
                    (Math.abs(value) / max) * 185
                  )

                  return (
                    <div
                      className="bar-column"
                      key={`${i}-${value}`}
                    >
                      <span
                        className={`bar-value ${
                          active ? 'value-active' : ''
                        }`}
                      >
                        {value}
                      </span>

                      <div
                        className={`bar ${
                          active ? 'bar-active' : ''
                        } ${swapped ? 'bar-swapped' : ''}`}
                        style={{ height: `${height}px` }}
                      >
                        <span>{String(i).padStart(2, '0')}</span>
                      </div>

                      <small>INDEX {i}</small>
                    </div>
                  )
                })}

                {!step && (
                  <p className="muted">
                    Start the backend and click Visualize.
                  </p>
                )}
              </div>

              <div className="progress-row">
                <span>PROGRESS</span>

                <div className="progress-track">
                  <div style={{ width: `${percent}%` }} />
                </div>

                <b>{percent}%</b>
              </div>

              <div className="controls">
                <button
                  className="control-btn"
                  onClick={() => changeStep(0)}
                  title="Reset"
                >
                  ↺ <span>Reset</span>
                </button>

                <div className="play-controls">
                  <button
                    className="icon-btn"
                    onClick={() => changeStep(stepIndex - 1)}
                    disabled={!data || stepIndex === 0}
                    aria-label="Previous step"
                  >
                    ←
                  </button>

                  <button
                    className="play-btn"
                    onClick={() => {
                      if (stepIndex >= (data?.steps.length || 1) - 1) {
                        setStepIndex(0)
                      }

                      setPlaying(!playing)
                    }}
                    disabled={!data}
                  >
                    {playing ? 'Ⅱ Pause' : '▶ Play'}
                  </button>

                  <button
                    className="icon-btn"
                    onClick={() => changeStep(stepIndex + 1)}
                    disabled={
                      !data ||
                      stepIndex >= data.steps.length - 1
                    }
                    aria-label="Next step"
                  >
                    →
                  </button>
                </div>

                <label className="speed-control">
                  SPEED

                  <select
                    value={speed}
                    onChange={e => setSpeed(Number(e.target.value))}
                  >
                    <option value={1200}>0.5×</option>
                    <option value={700}>1×</option>
                    <option value={300}>2×</option>
                  </select>
                </label>
              </div>
            </div>

            <div className="insight-grid">
              <article className="insight-card explanation-card">
                <div className="card-top">
                  <span className="card-icon">✳</span>
                  <span className="step-type">
                    {step?.operation || 'READY'}
                  </span>
                </div>

                <h3>What’s happening?</h3>

                <p>
                  {step?.explanation ||
                    'Run the algorithm to see a step-by-step explanation.'}
                </p>

                <div className="tip">
                  💡{' '}
                  <span>
                    Tip: Use Next to study each operation at your own pace.
                  </span>
                </div>
              </article>

              <article className="insight-card">
                <div className="card-top">
                  <span className="card-icon purple">⌁</span>
                  <span className="complexity-label">PERFORMANCE</span>
                </div>

                <h3>Complexity analysis</h3>

                <div className="complexity-row">
                  <span>Time complexity</span>
                  <b>{data?.timeComplexity || 'O(n²)'}</b>
                </div>

                <div className="complexity-row">
                  <span>Space complexity</span>
                  <b>{data?.spaceComplexity || 'O(1)'}</b>
                </div>

                <p className="tiny-note">
                  {algorithm === 'bubble'
                    ? 'Bubble Sort compares adjacent values repeatedly and swaps them when they are in the wrong order.'
                    : 'Selection Sort repeatedly finds the smallest value in the unsorted section and places it at the beginning.'}
                </p>
              </article>
            </div>
          </section>
        ) : page === 'linkedlist' ? (
          <section className="workspace">
            <div className="section-heading">
              <div>
                <span className="section-kicker">WORKSPACE / 02</span>
                <h2>Linked List Visualizer</h2>
              </div>
            </div>

            <LinkedListVisualizer />
          </section>
        ) : page === 'tree' ? (
          <section className="workspace">
            <div className="section-heading">
              <div>
                <span className="section-kicker">WORKSPACE / 04</span>
                <h2>Binary Tree &amp; BST Visualizer</h2>
              </div>
            </div>

            <TreeVisualizer />
          </section>
        ) : (
          <section className="workspace">
            <div className="section-heading">
              <div>
                <span className="section-kicker">WORKSPACE / 03</span>
                <h2>Searching Visualizer</h2>
              </div>
            </div>

            <SearchingVisualizer />
          </section>
        )}

        <section id="learn" className="learn-section">
          <div>
            <span className="section-kicker">THE BIG IDEA / 02</span>

            <h2>
              Small steps. <em>Big understanding.</em>
            </h2>

            <p>
              The Java backend creates sorting steps; React turns those
              steps into a visual story.
            </p>
          </div>

          <div className="flow-cards">
            <div>
              <span>01</span>
              <b>Input</b>
              <small>Enter numbers</small>
            </div>

            <i>→</i>

            <div>
              <span>02</span>
              <b>API</b>
              <small>Java + Spring Boot</small>
            </div>

            <i>→</i>

            <div>
              <span>03</span>
              <b>Visualize</b>
              <small>React animations</small>
            </div>
          </div>
        </section>

        <footer>
          <a className="brand" href="#">
            <span className="brand-mark">A</span> AlgoViz
          </a>

          <span>
            Built by Niraj Kumar · DSA made visual
            <br />
            <a href="mailto:guptaniraj18042000@gmail.com">
              guptaniraj18042000
            </a>
          </span>

          <span>React + Spring Boot</span>
        </footer>
      </main>
    </div>
  )
}
