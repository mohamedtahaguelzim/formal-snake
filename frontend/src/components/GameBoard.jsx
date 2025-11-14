import { useEffect } from 'react'

function GameBoard({ gameState, onKeyPress, onBackToMenu, onRestart }) {
  const { snake = [], food = null, score = 0, gameOver = false, gameWon = false, gridWidth = 20, gridHeight = 20, gameStarted = false, showDebugNumbers = false } = gameState

  // Keyboard event listener
  useEffect(() => {
    const handleKeyDown = (e) => {
      // Prevent default behavior for arrow keys and space
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', ' '].includes(e.key)) {
        e.preventDefault()
        onKeyPress(e.key)
      }
      // Handle restart with R key
      if (e.key === 'r' || e.key === 'R') {
        e.preventDefault()
        onRestart()
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [onKeyPress, onRestart])

  // Calculate adaptive cell size based on grid dimensions
  const calculateCellSize = () => {
    const maxGridSize = 600 // Maximum container size in pixels
    const cellSizeByWidth = Math.floor(maxGridSize / gridWidth)
    const cellSizeByHeight = Math.floor(maxGridSize / gridHeight)
    return Math.min(cellSizeByWidth, cellSizeByHeight, 30) // Cap at 30px max
  }

  const cellSize = calculateCellSize()

  // Render game board
  const renderBoard = () => {
    const board = []
    for (let y = 0; y < gridHeight; y++) {
      for (let x = 0; x < gridWidth; x++) {
        const snakeSegmentIndex = snake.findIndex(segment => segment.x === x && segment.y === y)
        const isSnake = snakeSegmentIndex !== -1
        const isFood = food && food.x === x && food.y === y
        const isHead = snakeSegmentIndex === 0

        // Calculate aggressive fading for snake body
        let segmentColor = 'bg-gray-900'
        if (isHead) {
          segmentColor = 'bg-green-400'
        } else if (isSnake) {
          // Aggressive fading: fade from bright to very dark quickly
          const fadePercent = Math.pow(snakeSegmentIndex / snake.length, 0.5) // Square root for aggressive fade
          const brightness = Math.round(600 - fadePercent * 500) // From 600 (bright) to 100 (very dark)
          segmentColor = `bg-green-${Math.max(100, Math.min(900, brightness))}`
        } else if (isFood) {
          segmentColor = 'bg-red-500'
        }

        const fontSize = Math.max(8, cellSize * 0.5) // Scale font with cell size

        board.push(
          <div
            key={`${x}-${y}`}
            className={`border border-gray-700 ${segmentColor} flex items-center justify-center font-bold`}
            style={{
              width: `${cellSize}px`,
              height: `${cellSize}px`,
              fontSize: `${fontSize}px`,
              ...(isSnake && !isHead ? {
                backgroundColor: `rgb(${22 - snakeSegmentIndex * 2}, ${163 - snakeSegmentIndex * 15}, ${74 - snakeSegmentIndex * 7})`
              } : {})
            }}
          >
            {showDebugNumbers && isSnake && (
              <span className="text-white text-opacity-70">{snakeSegmentIndex}</span>
            )}
          </div>
        )
      }
    }
    return board
  }

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-black text-white p-4">
      <div className="flex items-center justify-between w-full max-w-2xl mb-4">
        <button
          onClick={onBackToMenu}
          className="bg-gray-600 hover:bg-gray-700 text-white px-4 py-2 rounded-md transition-colors duration-200"
        >
          ← Back to Menu
        </button>
        <h1 className="text-4xl font-bold">Snake Game</h1>
        <div className="text-xl">Score: {score}</div>
      </div>

      <div 
        className="grid gap-0 border-2 border-gray-600 mb-4"
        style={{ gridTemplateColumns: `repeat(${gridWidth}, 1fr)` }}
      >
        {renderBoard()}
      </div>

      <div className="text-center">
        {!gameStarted && !gameOver && (
          <div>
            <p className="text-lg mb-2">Waiting for backend connection...</p>
            <div className="animate-pulse text-yellow-400">⚡ Connecting to server</div>
          </div>
        )}
        
        {gameWon && (
          <div>
            <p className="text-xl text-yellow-400 mb-4">🎉 YOU WIN! 🎉</p>
            <p className="text-lg mb-2">Perfect Score: {score}</p>
            <p className="text-sm text-green-400 mb-4">You filled the entire grid!</p>
            <button
              onClick={onRestart}
              className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-8 rounded-md transition-colors duration-200"
            >
              Play Again (R)
            </button>
          </div>
        )}
        
        {gameOver && !gameWon && (
          <div>
            <p className="text-xl text-red-500 mb-4">Game Over!</p>
            <p className="text-lg mb-4">Final Score: {score}</p>
            <button
              onClick={onRestart}
              className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-8 rounded-md transition-colors duration-200"
            >
              Restart Game (R)
            </button>
          </div>
        )}

        {gameStarted && !gameOver && !gameWon && (
          <p className="text-sm text-gray-400">Use arrow keys to move • Press R to restart</p>
        )}
      </div>

      <div className="mt-4 text-sm text-gray-500 text-center">
        <p>Connection Status: {gameState.connected ? '🟢 Connected' : '🔴 Disconnected'}</p>
        <p>Game State: {gameStarted ? (gameWon ? 'Victory!' : gameOver ? 'Game Over' : 'Active') : 'Waiting'}</p>
        <p>Snake Length: {snake.length} / {gridWidth * gridHeight}</p>
        <p>Grid: {gridWidth} × {gridHeight}</p>
      </div>
    </div>
  )
}

export default GameBoard
