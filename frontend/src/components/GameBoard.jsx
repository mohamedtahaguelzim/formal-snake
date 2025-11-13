import { useEffect } from 'react'

function GameBoard({ gameState, onKeyPress, onBackToMenu }) {
  const { snake = [], food = null, score = 0, gameOver = false, gridWidth = 20, gridHeight = 20, gameStarted = false } = gameState

  // Keyboard event listener
  useEffect(() => {
    const handleKeyDown = (e) => {
      // Prevent default behavior for arrow keys
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', ' '].includes(e.key)) {
        e.preventDefault()
        onKeyPress(e.key)
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [onKeyPress])

  // Render game board
  const renderBoard = () => {
    const board = []
    for (let y = 0; y < gridHeight; y++) {
      for (let x = 0; x < gridWidth; x++) {
        const isSnake = snake.some(segment => segment.x === x && segment.y === y)
        const isFood = food && food.x === x && food.y === y
        const isHead = snake[0]?.x === x && snake[0]?.y === y

        board.push(
          <div
            key={`${x}-${y}`}
            className={`w-4 h-4 border border-gray-700 ${
              isHead 
                ? 'bg-green-400' 
                : isSnake 
                ? 'bg-green-600' 
                : isFood 
                ? 'bg-red-500' 
                : 'bg-gray-900'
            }`}
          />
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
        
        {gameOver && (
          <div>
            <p className="text-xl text-red-500 mb-2">Game Over!</p>
            <p className="text-lg mb-2">Final Score: {score}</p>
            <p className="text-lg">Backend will handle restart</p>
          </div>
        )}

        {gameStarted && !gameOver && (
          <p className="text-sm text-gray-400">Game controlled by backend - Use arrow keys</p>
        )}
      </div>

      <div className="mt-4 text-sm text-gray-500 text-center">
        <p>Connection Status: {gameState.connected ? '🟢 Connected' : '🔴 Disconnected'}</p>
        <p>Game State: {gameStarted ? (gameOver ? 'Game Over' : 'Active') : 'Waiting'}</p>
        <p>Snake Length: {snake.length}</p>
        <p>Grid: {gridWidth} × {gridHeight}</p>
      </div>
    </div>
  )
}

export default GameBoard
