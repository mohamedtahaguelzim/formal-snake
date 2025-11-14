import { useState, useEffect } from 'react'

function Welcome({ onStartGame, connected }) {
  const [gridWidth, setGridWidth] = useState(() => {
    const saved = localStorage.getItem('snakeGameWidth')
    return saved ? parseInt(saved) : 20
  })
  const [gridHeight, setGridHeight] = useState(() => {
    const saved = localStorage.getItem('snakeGameHeight')
    return saved ? parseInt(saved) : 20
  })
  const [gameSpeed, setGameSpeed] = useState(() => {
    const saved = localStorage.getItem('snakeGameSpeed')
    return saved ? parseInt(saved) : 200
  })
  const [snakeStartSize, setSnakeStartSize] = useState(() => {
    const saved = localStorage.getItem('snakeStartSize')
    return saved ? parseInt(saved) : 1
  })
  const [showDebugNumbers, setShowDebugNumbers] = useState(() => {
    const saved = localStorage.getItem('snakeShowDebugNumbers')
    return saved ? saved === 'true' : false
  })

  // Save config to localStorage when changed
  useEffect(() => {
    localStorage.setItem('snakeGameWidth', gridWidth)
  }, [gridWidth])

  useEffect(() => {
    localStorage.setItem('snakeGameHeight', gridHeight)
  }, [gridHeight])

  useEffect(() => {
    localStorage.setItem('snakeGameSpeed', gameSpeed)
  }, [gameSpeed])

  useEffect(() => {
    localStorage.setItem('snakeStartSize', snakeStartSize)
  }, [snakeStartSize])

  useEffect(() => {
    localStorage.setItem('snakeShowDebugNumbers', showDebugNumbers)
  }, [showDebugNumbers])

  const handleStart = () => {
    onStartGame({
      gridWidth: parseInt(gridWidth),
      gridHeight: parseInt(gridHeight),
      gameSpeed: parseInt(gameSpeed),
      snakeStartSize: parseInt(snakeStartSize),
      showDebugNumbers: showDebugNumbers
    })
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-black text-white p-4">
      <h1 className="text-6xl font-bold mb-8 text-green-400">Formal Snake</h1>
      
      <div className="bg-gray-800 p-8 rounded-lg shadow-lg">
        <h2 className="text-2xl font-semibold mb-6 text-center">Game Configuration</h2>
        
        <div className="space-y-4 mb-8">
          <div>
            <label className="block text-sm font-medium mb-2">Grid Width</label>
            <input
              type="number"
              min="10"
              max="40"
              value={gridWidth}
              onChange={(e) => setGridWidth(e.target.value)}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium mb-2">Grid Height</label>
            <input
              type="number"
              min="10"
              max="40"
              value={gridHeight}
              onChange={(e) => setGridHeight(e.target.value)}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-green-500"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium mb-2">Game Speed (ms)</label>
            <input
              type="number"
              min="0"
              max="500"
              step="50"
              value={gameSpeed}
              onChange={(e) => setGameSpeed(e.target.value)}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-green-500"
            />
            <p className="text-xs text-gray-400 mt-1">0 = Turn-based, Higher = Slower (0-500ms)</p>
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">Snake Start Size</label>
            <input
              type="number"
              min="1"
              max="10"
              value={snakeStartSize}
              onChange={(e) => setSnakeStartSize(e.target.value)}
              className="w-full px-3 py-2 bg-gray-700 border border-gray-600 rounded-md text-white focus:outline-none focus:ring-2 focus:ring-green-500"
            />
            <p className="text-xs text-gray-400 mt-1">Number of segments the snake starts with (1-10)</p>
          </div>
          
          <div className="flex items-center">
            <input
              type="checkbox"
              id="debugNumbers"
              checked={showDebugNumbers}
              onChange={(e) => setShowDebugNumbers(e.target.checked)}
              className="w-4 h-4 bg-gray-700 border-gray-600 rounded focus:ring-2 focus:ring-green-500"
            />
            <label htmlFor="debugNumbers" className="ml-2 text-sm font-medium">Show Debug Numbers</label>
          </div>
        </div>

        <button
          onClick={handleStart}
          disabled={!connected}
          className={`w-full font-bold py-3 px-6 rounded-md transition-colors duration-200 ${
            connected 
              ? 'bg-green-600 hover:bg-green-700 text-white cursor-pointer' 
              : 'bg-gray-600 text-gray-400 cursor-not-allowed'
          }`}
        >
          {connected ? 'Start Game' : 'Connecting to Server...'}
        </button>
      </div>

      <div className="mt-4 text-center">
        <p className={`text-sm ${connected ? 'text-green-400' : 'text-yellow-400'}`}>
          {connected ? '🟢 Connected to backend' : '🔴 Connecting to backend server...'}
        </p>
      </div>
    </div>
  )
}

export default Welcome
