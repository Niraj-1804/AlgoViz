import { useEffect, useState } from 'react'
import './TreeVisualizer.css'

function cloneTree(node) {
  if (!node) return null

  return {
    value: node.value,
    left: cloneTree(node.left),
    right: cloneTree(node.right),
  }
}

function makeNode(value) {
  return {
    value,
    left: null,
    right: null,
  }
}

function createFrames(values, type) {
  const frames = []
  let root = null

  frames.push({
    root: null,
    message: 'Ready! Press Next Step to begin insertion.',
    activeValue: null,
    insertedValue: null,
  })

  for (const value of values) {
    // BINARY SEARCH TREE INSERTION
    if (type === 'bst') {
      if (!root) {
        root = makeNode(value)

        frames.push({
          root: cloneTree(root),
          message: `${value} becomes the root node.`,
          activeValue: value,
          insertedValue: value,
        })

        continue
      }

      let current = root
      let inserted = false

      while (!inserted) {
        frames.push({
          root: cloneTree(root),
          message: `Compare ${value} with node ${current.value}.`,
          activeValue: current.value,
          insertedValue: null,
        })

        if (value === current.value) {
          frames.push({
            root: cloneTree(root),
            message: `${value} already exists. Duplicate values are skipped.`,
            activeValue: current.value,
            insertedValue: null,
          })

          inserted = true
        } else if (value < current.value) {
          if (!current.left) {
            current.left = makeNode(value)

            frames.push({
              root: cloneTree(root),
              message: `${value} < ${current.value}: inserted ${value} on the left.`,
              activeValue: value,
              insertedValue: value,
            })

            inserted = true
          } else {
            frames.push({
              root: cloneTree(root),
              message: `${value} < ${current.value}: move left.`,
              activeValue: current.value,
              insertedValue: null,
            })

            current = current.left
          }
        } else {
          if (!current.right) {
            current.right = makeNode(value)

            frames.push({
              root: cloneTree(root),
              message: `${value} > ${current.value}: inserted ${value} on the right.`,
              activeValue: value,
              insertedValue: value,
            })

            inserted = true
          } else {
            frames.push({
              root: cloneTree(root),
              message: `${value} > ${current.value}: move right.`,
              activeValue: current.value,
              insertedValue: null,
            })

            current = current.right
          }
        }
      }
    } else {
      // BINARY TREE: INSERT IN LEVEL ORDER
      if (!root) {
        root = makeNode(value)

        frames.push({
          root: cloneTree(root),
          message: `${value} becomes the root node.`,
          activeValue: value,
          insertedValue: value,
        })

        continue
      }

      const queue = [root]
      let inserted = false

      while (queue.length > 0 && !inserted) {
        const current = queue.shift()

        frames.push({
          root: cloneTree(root),
          message: `Checking node ${current.value} for an empty position.`,
          activeValue: current.value,
          insertedValue: null,
        })

        if (!current.left) {
          current.left = makeNode(value)

          frames.push({
            root: cloneTree(root),
            message: `Inserted ${value} as the left child of ${current.value}.`,
            activeValue: value,
            insertedValue: value,
          })

          inserted = true
        } else if (!current.right) {
          current.right = makeNode(value)

          frames.push({
            root: cloneTree(root),
            message: `Inserted ${value} as the right child of ${current.value}.`,
            activeValue: value,
            insertedValue: value,
          })

          inserted = true
        } else {
          queue.push(current.left)
          queue.push(current.right)
        }
      }
    }
  }

  frames.push({
    root: cloneTree(root),
    message: 'Insertion complete!',
    activeValue: null,
    insertedValue: null,
  })

  return frames
}

function TreeNodeView({ node, activeValue, insertedValue }) {
  if (!node) return null

  const isActive = node.value === activeValue
  const isInserted = node.value === insertedValue

  return (
    <div className="tree-node-wrapper">
      <div
        className={`tree-node ${isActive ? 'active-node' : ''} ${
          isInserted ? 'inserted-node' : ''
        }`}
      >
        {node.value}
      </div>

      {(node.left || node.right) && (
        <div
          className={`tree-children ${node.left ? 'has-left' : ''} ${
            node.right ? 'has-right' : ''
          }`}
        >
          <div className="tree-child">
            {node.left && (
              <TreeNodeView
                node={node.left}
                activeValue={activeValue}
                insertedValue={insertedValue}
              />
            )}
          </div>

          <div className="tree-child">
            {node.right && (
              <TreeNodeView
                node={node.right}
                activeValue={activeValue}
                insertedValue={insertedValue}
              />
            )}
          </div>
        </div>
      )}
    </div>
  )
}

function getTraversal(root, order) {
  const result = []

  function visit(node) {
    if (!node) return

    if (order === 'preorder') {
      result.push(node.value)
    }

    visit(node.left)

    if (order === 'inorder') {
      result.push(node.value)
    }

    visit(node.right)

    if (order === 'postorder') {
      result.push(node.value)
    }
  }

  visit(root)
  return result
}

export default function TreeVisualizer() {
  const [type, setType] = useState('bst')
  const [input, setInput] = useState('3,1,6,10,14')
  const [frames, setFrames] = useState([])
  const [step, setStep] = useState(0)
  const [playing, setPlaying] = useState(false)
  const [speed, setSpeed] = useState(1000)
  const [error, setError] = useState('')

  function startInsertion() {
    const parts = input.split(',').map(value => value.trim())

    if (
      parts.length === 0 ||
      parts.some(value => value === '' || !/^-?\d+$/.test(value)) ||
      parts.length > 100
    ) {
      setError('Enter 1 to 100 integers separated by commas.')
      setPlaying(false)
      return
    }

    const values = parts.map(Number)

    if (values.some(value => !Number.isSafeInteger(value))) {
      setError('Please enter valid integers.')
      setPlaying(false)
      return
    }

    setError('')
    setPlaying(false)
    setStep(0)
    setFrames(createFrames(values, type))
  }

  useEffect(() => {
    if (!playing) return undefined

    const timer = setInterval(() => {
      setStep(current => {
        if (current >= frames.length - 1) {
          setPlaying(false)
          return current
        }

        return current + 1
      })
    }, speed)

    return () => clearInterval(timer)
  }, [playing, speed, frames.length])

  function reset() {
    setPlaying(false)
    setFrames([])
    setStep(0)
    setError('')
  }

  function changeType(newType) {
    setType(newType)
    reset()
  }

  const frame = frames[step]

  return (
    <section className="tree-page">
      <h2>Tree Visualizer</h2>
      <p>
        Watch each element being inserted into a Binary Tree or Binary Search
        Tree.
      </p>

      <div className="tree-controls">
        <label>
          Tree type
          <select
            value={type}
            onChange={event => changeType(event.target.value)}
          >
            <option value="binary">Binary Tree</option>
            <option value="bst">Binary Search Tree (BST)</option>
          </select>
        </label>

        <label>
          Values (comma-separated)
          <input
            value={input}
            onChange={event => setInput(event.target.value)}
            placeholder="3,1,6,10,14"
          />
        </label>

        <button onClick={startInsertion}>Start Insertion</button>

        <button
          onClick={() =>
            setStep(current => Math.max(0, current - 1))
          }
          disabled={!frames.length || step === 0 || playing}
        >
          Previous
        </button>

        <button
          onClick={() => {
            if (step < frames.length - 1) {
              setStep(current => current + 1)
            }
          }}
          disabled={!frames.length || step >= frames.length - 1 || playing}
        >
          Next Step
        </button>

        <button
          onClick={() => setPlaying(current => !current)}
          disabled={!frames.length || step >= frames.length - 1}
        >
          {playing ? 'Pause' : 'Play'}
        </button>

        <button className="tree-reset" onClick={reset}>
          Reset
        </button>
      </div>

      <label className="tree-speed">
        Animation speed
        <select
          value={speed}
          onChange={event => setSpeed(Number(event.target.value))}
        >
          <option value={1600}>Slow</option>
          <option value={1000}>Normal</option>
          <option value={450}>Fast</option>
        </select>
      </label>

      {error && (
        <p className="tree-message" role="alert">
          {error}
        </p>
      )}

      {frame && (
        <>
          <p className="tree-message" role="status">
            <strong>
              Step {step} / {frames.length - 1}:
            </strong>{' '}
            {frame.message}
          </p>

          <div className="tree-legend">
            <span>
              <i className="legend-active" /> Comparing node
            </span>

            <span>
              <i className="legend-inserted" /> Newly inserted
            </span>
          </div>

          <div className="tree-canvas">
            {frame.root ? (
              <TreeNodeView
                node={frame.root}
                activeValue={frame.activeValue}
                insertedValue={frame.insertedValue}
              />
            ) : (
              <p>Press Next Step or Play to begin.</p>
            )}
          </div>

          {frame.root && (
            <div className="tree-traversals">
              <h3>Traversals</h3>

              <p>
                <strong>Inorder:</strong>{' '}
                {getTraversal(frame.root, 'inorder').join(' → ')}
              </p>

              <p>
                <strong>Preorder:</strong>{' '}
                {getTraversal(frame.root, 'preorder').join(' → ')}
              </p>

              <p>
                <strong>Postorder:</strong>{' '}
                {getTraversal(frame.root, 'postorder').join(' → ')}
              </p>
            </div>
          )}
        </>
      )}
    </section>
  )
}
