
import { useEffect, useState } from 'react'

const initialValues = [10, 20, 30, 40]

const complexity = {
  Traverse: {
    time: 'O(n)',
    space: 'O(1)',
    description: 'Har node ko ek baar visit kiya jata hai.'
  },
  Insert: {
    time: 'O(n)',
    space: 'O(1)',
    description: 'End par insert karne se pehle last node tak traverse karna padta hai.'
  },
  Delete: {
    time: 'O(n)',
    space: 'O(1)',
    description: 'Value ko locate karke node ko list se remove kiya jata hai.'
  },
  Search: {
    time: 'O(n)',
    space: 'O(1)',
    description: 'Worst case mein har node ko check karna padta hai.'
  },
  Reverse: {
    time: 'O(n)',
    space: 'O(1)',
    description: 'Har node ke next pointer ko reverse kiya jata hai.'
  }
}

export default function LinkedListVisualizer() {
  const [values, setValues] = useState(initialValues)
  const [operation, setOperation] = useState('Traverse')
  const [inputValue, setInputValue] = useState('50')
  const [steps, setSteps] = useState([])
  const [stepIndex, setStepIndex] = useState(0)
  const [playing, setPlaying] = useState(false)

  const currentStep = steps[stepIndex]
  const selectedComplexity = complexity[operation]
  const shownValues = currentStep?.values ?? values

  function runOperation() {
    const number = Number(inputValue)
    let nextSteps = []
    let finalValues = [...values]

    if (inputValue.trim() === '' || !Number.isFinite(number)) {
      setSteps([{
        values: [...values],
        active: -1,
        message: 'Please enter a valid node value.',
        pointer: ''
      }])
      setStepIndex(0)
      setPlaying(false)
      return
    }

    if (operation === 'Insert') {
      nextSteps.push({
        values: [...values],
        active: values.length - 1,
        message: values.length
          ? `Last node ${values[values.length - 1]} tak pahunch rahe hain.`
          : 'List empty hai. Naya node HEAD banega.',
        pointer: 'HEAD'
      })

      finalValues = [...values, number]

      nextSteps.push({
        values: finalValues,
        active: finalValues.length - 1,
        message: `Node ${number} insert ho gaya. Naya node ab NULL ko point karta hai.`,
        pointer: 'NEW NODE'
      })
    }

    else if (operation === 'Delete') {
      const index = values.indexOf(number)

      if (index === -1) {
        nextSteps = values.map((value, i) => ({
          values: [...values],
          active: i,
          message: `Node ${value} check kiya. ${number} abhi nahi mila.`,
          pointer: 'SEARCH'
        }))

        nextSteps.push({
          values: [...values],
          active: -1,
          message: `Node ${number} list mein nahi mila. List unchanged hai.`,
          pointer: 'NOT FOUND'
        })
      } else {
        for (let i = 0; i <= index; i++) {
          nextSteps.push({
            values: [...values],
            active: i,
            message: `Node ${values[i]} check kar rahe hain${i === index ? '. Target mil gaya!' : '.'}`,
            pointer: 'SEARCH'
          })
        }

        finalValues = values.filter((_, i) => i !== index)

        nextSteps.push({
          values: finalValues,
          active: -1,
          message: `Node ${number} delete ho gaya. Previous node ab next node ko point karta hai.`,
          pointer: 'LINK UPDATED'
        })
      }
    }

    else if (operation === 'Search') {
      const index = values.indexOf(number)

      for (let i = 0; i < values.length; i++) {
        nextSteps.push({
          values: [...values],
          active: i,
          message: values[i] === number
            ? `Node ${number} index ${i} par mil gaya! Search complete.`
            : `Node ${values[i]} ≠ ${number}. Next node par ja rahe hain.`,
          pointer: 'CURRENT'
        })

        if (values[i] === number) break
      }

      if (index === -1) {
        nextSteps.push({
          values: [...values],
          active: -1,
          message: `${number} list mein nahi mila. Saare nodes check ho gaye.`,
          pointer: 'NOT FOUND'
        })
      }
    }

    else if (operation === 'Reverse') {
      if (values.length === 0) {
        nextSteps.push({
          values: [],
          active: -1,
          message: 'List empty hai. Reverse karne ke liye koi node nahi hai.',
          pointer: 'HEAD = NULL'
        })
      } else {
        let previous = null

        values.forEach((value, i) => {
          nextSteps.push({
            values: [...values],
            active: i,
            message: `Current = ${value}. Previous pointer ko ${previous === null ? 'NULL' : previous} se track kar rahe hain.`,
            pointer: 'CURRENT'
          })

          nextSteps.push({
            values: [...values],
            active: i,
            message: `${value} ka next pointer reverse karke ${previous === null ? 'NULL' : previous} ko point karaya.`,
            pointer: 'REVERSE LINK'
          })

          previous = value
        })

        finalValues = [...values].reverse()

        nextSteps.push({
          values: finalValues,
          active: -1,
          message: `Reverse complete! Naya HEAD ${finalValues[0]} hai.`,
          pointer: 'HEAD UPDATED'
        })
      }
    }

    else {
      values.forEach((value, i) => {
        nextSteps.push({
          values: [...values],
          active: i,
          message: `Node ${value} visit kar rahe hain (${i + 1}/${values.length}).`,
          pointer: 'CURRENT'
        })
      })

      if (values.length === 0) {
        nextSteps.push({
          values: [],
          active: -1,
          message: 'List empty hai. Traverse karne ke liye nodes nahi hain.',
          pointer: 'HEAD = NULL'
        })
      } else {
        nextSteps.push({
          values: [...values],
          active: -1,
          message: 'Traversal complete! Saare nodes visit ho gaye.',
          pointer: 'DONE'
        })
      }
    }

    setValues(finalValues)
    setSteps(nextSteps)
    setStepIndex(0)
    setPlaying(false)
  }

  useEffect(() => {
    if (!playing || steps.length === 0) return

    const timer = setInterval(() => {
      setStepIndex(index => {
        if (index >= steps.length - 1) {
          setPlaying(false)
          return index
        }
        return index + 1
      })
    }, 1200)

    return () => clearInterval(timer)
  }, [playing, steps])

  function reset() {
    setPlaying(false)
    setStepIndex(0)
  }

  function changeOperation(value) {
    setOperation(value)
    setPlaying(false)
    setSteps([])
    setStepIndex(0)
  }

  return (
    <section style={panelStyle}>
      <h2 style={{ fontSize: '28px', marginTop: 0 }}>
        Linked List Visualizer
      </h2>

      <p style={{ color: '#94a3b8' }}>
        Explore nodes, pointers, operations and complexity step by step.
      </p>

      <div style={toolbarStyle}>
        <select
          value={operation}
          onChange={e => changeOperation(e.target.value)}
          style={controlStyle}
        >
          <option>Traverse</option>
          <option>Insert</option>
          <option>Delete</option>
          <option>Search</option>
          <option>Reverse</option>
        </select>

        <input
          type="number"
          value={inputValue}
          onChange={e => setInputValue(e.target.value)}
          style={controlStyle}
          aria-label="Node value"
          placeholder="Node value"
        />

        <button style={buttonStyle} onClick={runOperation}>
          Run Operation
        </button>
      </div>

      <div style={complexityStyle}>
        <h3 style={{ marginTop: 0 }}>Complexity Analysis</h3>
        <div style={complexityGridStyle}>
          <div style={metricStyle}>
            <small>TIME COMPLEXITY</small>
            <strong style={metricValueStyle}>
              {selectedComplexity.time}
            </strong>
          </div>
          <div style={metricStyle}>
            <small>SPACE COMPLEXITY</small>
            <strong style={metricValueStyle}>
              {selectedComplexity.space}
            </strong>
          </div>
        </div>
        <p style={{ color: '#cbd5e1', marginBottom: 0 }}>
          {selectedComplexity.description}
        </p>
      </div>

      <div style={listStyle}>
        <div style={nodesStyle}>
          <strong style={{ color: '#34d399' }}>HEAD →</strong>

          {shownValues.map((value, index) => (
            <div
              key={`${index}-${value}`}
              style={{ display: 'flex', alignItems: 'center', gap: '10px' }}
            >
              <div style={{
                ...nodeStyle,
                borderColor: currentStep?.active === index ? '#34d399' : '#64748b',
                background: currentStep?.active === index ? '#064e3b' : '#1e293b',
                boxShadow: currentStep?.active === index
                  ? '0 0 18px #34d39955'
                  : 'none'
              }}>
                <strong style={{ fontSize: '20px' }}>{value}</strong>
                <small style={{ color: '#94a3b8' }}>DATA</small>
                <small style={{ color: '#34d399' }}>NEXT →</small>
              </div>
              <span style={{ color: '#34d399', fontSize: '22px' }}>→</span>
            </div>
          ))}

          <strong style={{ color: '#94a3b8' }}>NULL</strong>
        </div>
      </div>

      <div style={dryRunStyle}>
        <strong style={{ color: '#34d399' }}>DRY RUN / STEP EXPLANATION</strong>
        <p style={{ lineHeight: 1.7 }}>
          {currentStep?.message ||
            'Choose an operation and click Run Operation to begin.'}
        </p>
        <p style={{ color: '#94a3b8', marginBottom: 0 }}>
          Pointer: {currentStep?.pointer || '—'}
        </p>
        <small style={{ color: '#94a3b8' }}>
          Step {steps.length ? stepIndex + 1 : 0} / {steps.length}
        </small>
      </div>

      <div style={toolbarStyle}>
        <button
          style={buttonStyle}
          disabled={stepIndex === 0 || steps.length === 0}
          onClick={() => {
            setPlaying(false)
            setStepIndex(i => i - 1)
          }}
        >
          ← Previous
        </button>

        <button
          style={buttonStyle}
          disabled={steps.length === 0}
          onClick={() => {
            if (playing) {
              setPlaying(false)
            } else if (stepIndex >= steps.length - 1) {
              setStepIndex(0)
              setPlaying(true)
            } else {
              setPlaying(true)
            }
          }}
        >
          {playing ? 'Ⅱ Pause' : '▶ Play'}
        </button>

        <button
          style={buttonStyle}
          disabled={stepIndex >= steps.length - 1 || steps.length === 0}
          onClick={() => {
            setPlaying(false)
            setStepIndex(i => i + 1)
          }}
        >
          Next →
        </button>

        <button style={buttonStyle} onClick={reset}>
          Reset Steps
        </button>
      </div>
    </section>
  )
}

const panelStyle = {
  color: '#e5e7eb',
  background: '#0b1220',
  padding: '28px',
  borderRadius: '16px',
  margin: '24px auto',
  maxWidth: '1100px',
  border: '1px solid #263449'
}

const toolbarStyle = {
  display: 'flex',
  flexWrap: 'wrap',
  gap: '12px',
  alignItems: 'center',
  margin: '20px 0'
}

const controlStyle = {
  background: '#111c2e',
  color: '#e5e7eb',
  border: '1px solid #334155',
  borderRadius: '8px',
  padding: '12px'
}

const buttonStyle = {
  background: '#24354d',
  color: '#ffffff',
  border: '1px solid #475569',
  borderRadius: '8px',
  padding: '12px 16px',
  cursor: 'pointer'
}

const complexityStyle = {
  margin: '22px 0',
  padding: '18px',
  borderRadius: '12px',
  background: '#111c2e',
  border: '1px solid #334155'
}

const complexityGridStyle = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
  gap: '12px'
}

const metricStyle = {
  display: 'flex',
  flexDirection: 'column',
  gap: '8px',
  padding: '14px',
  background: '#172337',
  borderRadius: '10px'
}

const metricValueStyle = {
  color: '#34d399',
  fontSize: '28px'
}

const listStyle = {
  overflowX: 'auto',
  padding: '24px 8px',
  background: '#080e1a',
  borderRadius: '12px'
}

const nodesStyle = {
  display: 'flex',
  alignItems: 'center',
  gap: '10px',
  minWidth: 'max-content'
}

const nodeStyle = {
  border: '2px solid #64748b',
  borderRadius: '10px',
  width: '76px',
  minHeight: '82px',
  display: 'flex',
  flexDirection: 'column',
  alignItems: 'center',
  justifyContent: 'center',
  gap: '3px',
  transition: 'all 0.2s ease'
}

const dryRunStyle = {
  marginTop: '22px',
  padding: '18px',
  borderRadius: '10px',
  background: '#172337',
  minHeight: '90px'
}
