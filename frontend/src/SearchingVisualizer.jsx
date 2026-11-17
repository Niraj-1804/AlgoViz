import { useEffect, useState } from 'react'

export default function SearchingVisualizer() {
  const [input, setInput] = useState('10, 20, 30, 40, 50')
  const [target, setTarget] = useState('30')
  const [algorithm, setAlgorithm] = useState('linear')
  const [data, setData] = useState(null)
  const [stepIndex, setStepIndex] = useState(0)
  const [playing, setPlaying] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

  async function search() {
    setPlaying(false)
    setLoading(true)
    setError('')
    setData(null)
    setStepIndex(0)

    try {
      const parts = input.split(',').map(value => value.trim())

      if (
        !input.trim() ||
        parts.some(value => value === '') ||
        parts.length > 40
      ) {
        throw new Error('Array mein 1 se 40 numbers enter karo.')
      }

      const values = parts.map(Number)
      const number = Number(target)

      if (
        values.some(value => !Number.isInteger(value)) ||
        target.trim() === '' ||
        !Number.isInteger(number)
      ) {
        throw new Error('Array aur target mein valid integers enter karo.')
      }

      if (
        algorithm === 'binary' &&
        values.some((value, i) => i > 0 && values[i - 1] > value)
      ) {
        throw new Error(
          'Binary Search ke liye array ascending order mein sort karo.'
        )
      }

      const response = await fetch(
        `${API_BASE_URL}/api/search/${algorithm}?values=${encodeURIComponent(
          values.join(',')
        )}&target=${encodeURIComponent(number)}`
      )

      const result = await response.json()

      if (!response.ok) {
        throw new Error(result.error || 'Search API request failed.')
      }

      setData(result)
      setStepIndex(0)
    } catch (err) {
      setError(
        err.message ||
          'Backend se connect nahi ho pa raha. Backend status check karo.'
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!playing || !data?.steps?.length) return

    const timer = setInterval(() => {
      setStepIndex(current => {
        if (current >= data.steps.length - 1) {
          setPlaying(false)
          return current
        }

        return current + 1
      })
    }, 900)

    return () => clearInterval(timer)
  }, [playing, data])

  function changeInput(setter, value) {
    setter(value)
    setPlaying(false)
    setData(null)
    setStepIndex(0)
    setError('')
  }

  function resetSearch() {
    setPlaying(false)
    setStepIndex(0)
  }

  function togglePlay() {
    if (!data?.steps?.length) return

    if (stepIndex >= data.steps.length - 1) {
      setStepIndex(0)
      setPlaying(true)
      return
    }

    setPlaying(current => !current)
  }

  const currentStep = data?.steps?.[stepIndex]

  return (
    <div className="panel">
      <div className="toolbar">
        <label className="field-label">
          ALGORITHM
          <select
            value={algorithm}
            onChange={e => changeInput(setAlgorithm, e.target.value)}
          >
            <option value="linear">Linear Search</option>
            <option value="binary">Binary Search</option>
          </select>
        </label>

        <label className="field-label input-field">
          ARRAY
          <input
            value={input}
            onChange={e => changeInput(setInput, e.target.value)}
            placeholder="10, 20, 30, 40, 50"
          />
        </label>

        <label className="field-label">
          TARGET
          <input
            value={target}
            onChange={e => changeInput(setTarget, e.target.value)}
            placeholder="30"
          />
        </label>

        <button
          className="primary-btn"
          onClick={search}
          disabled={loading}
        >
          {loading ? 'Searching...' : 'Search'}
        </button>
      </div>

      {error && (
        <p className="error" role="alert">
          {error}
        </p>
      )}

      <div className="insight-card">
        <h3>Complexity Analysis</h3>
        <p>
          <strong>Algorithm:</strong>{' '}
          {data?.algorithm ||
            (algorithm === 'linear' ? 'Linear Search' : 'Binary Search')}
        </p>
        <p>
          <strong>Best Case T.C.:</strong>{' '}
          {data?.bestTimeComplexity || '—'}
        </p>
        <p>
          <strong>Average Case T.C.:</strong>{' '}
          {data?.averageTimeComplexity || '—'}
        </p>
        <p>
          <strong>Worst Case T.C.:</strong>{' '}
          {data?.worstTimeComplexity || '—'}
        </p>
        <p>
          <strong>Space Complexity:</strong>{' '}
          {data?.spaceComplexity || '—'}
        </p>
      </div>

      {currentStep && (
        <>
          <div className="viz-header">
            <div>
              <span className="mini-dot" /> SEARCHING VISUALIZATION
              <span className="muted">
                {' '}· {currentStep.values.length} elements
              </span>
            </div>

            <span className="step-counter">
              STEP <b>{stepIndex + 1}</b> / {data.steps.length}
            </span>
          </div>

          <div className="chart-area">
            {currentStep.values.map((value, index) => {
              const active = index === currentStep.activeIndex
              const found =
                active && currentStep.operation === 'FOUND'

              return (
                <div className="bar-column" key={index}>
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
                    } ${found ? 'bar-swapped' : ''}`}
                    style={{ height: '90px' }}
                  >
                    <span>{String(index).padStart(2, '0')}</span>
                  </div>

                  <small>INDEX {index}</small>

                  {active && (
                    <small>
                      {found ? 'FOUND' : 'CHECKING'}
                    </small>
                  )}
                </div>
              )
            })}
          </div>

          <div className="insight-card">
            <div className="card-top">
              <span className="card-icon">✳</span>
              <span className="step-type">
                {currentStep.operation}
              </span>
            </div>

            <h3>Current Step</h3>
            <p>{currentStep.explanation}</p>

            <p>
              Step {stepIndex + 1} of {data.steps.length}
            </p>

            {stepIndex === data.steps.length - 1 && (
              <p role="status">
                {data.found
                  ? `Target ${data.target} found at index ${data.resultIndex}.`
                  : `Target ${data.target} was not found in the array.`}
              </p>
            )}
          </div>

          <div className="controls">
            <button
              className="control-btn"
              onClick={resetSearch}
            >
              ↺ Reset
            </button>

            <div className="play-controls">
              <button
                className="icon-btn"
                onClick={() => {
                  setPlaying(false)
                  setStepIndex(current => Math.max(0, current - 1))
                }}
                disabled={stepIndex === 0}
                aria-label="Previous step"
              >
                ←
              </button>

              <button
                className="play-btn"
                onClick={togglePlay}
                disabled={loading}
              >
                {playing ? 'Ⅱ Pause' : '▶ Play'}
              </button>

              <button
                className="icon-btn"
                onClick={() => {
                  setPlaying(false)
                  setStepIndex(current =>
                    Math.min(data.steps.length - 1, current + 1)
                  )
                }}
                disabled={playing || stepIndex >= data.steps.length - 1}
                aria-label="Next step"
              >
                →
              </button>
            </div>
          </div>
        </>
      )}

      {!data && !error && !loading && (
        <p className="muted">
          Array aur target enter karke Search par click karo.
        </p>
      )}
    </div>
  )
}
