import { useEffect, useRef } from 'react'
import soundService from '../services/sound'

function GameBoard({ gameState, onKeyPress, onBackToMenu, onRestart, isMuted, onToggleMute, onToggleDebug, isDarkMode, onToggleDarkMode }) {
  const {
    snake = [],
    food = null,
    gameOver = false,
    gameWon = false,
    gridWidth = 20,
    gridHeight = 20,
    gameStarted = false,
    showDebugNumbers = false,
  } = gameState

  // Score is derived from snake length: 10 points per eaten segment.
  const score = 10 * Math.max(0, (snake ? snake.length : 0) - 1)

  const prevGameState = useRef({})

  // Initialize audio context on first user interaction
  useEffect(() => {
    const handleFirstInteraction = () => {
      soundService.resumeAudioContext()
      document.removeEventListener('click', handleFirstInteraction)
      document.removeEventListener('keydown', handleFirstInteraction)
    }

    document.addEventListener('click', handleFirstInteraction)
    document.addEventListener('keydown', handleFirstInteraction)

    return () => {
      document.removeEventListener('click', handleFirstInteraction)
      document.removeEventListener('keydown', handleFirstInteraction)
    }
  }, [])

  // Sound effects based on game state changes
  useEffect(() => {
    const prevScore = prevGameState.current.score || 0
    // Check for score increase (food eaten)
    if (score > prevScore) {
      soundService.playEatSound()
    }

    // Check for game over
    if (gameOver && !prevGameState.current.gameOver) {
      soundService.playGameOverSound()
    }

    // Check for game won
    if (gameWon && !prevGameState.current.gameWon) {
      soundService.playWinSound()
    }

    // Update previous state
    prevGameState.current = { score, gameOver, gameWon }
  }, [snake, gameOver, gameWon])

  // Keyboard event listener
  useEffect(() => {
    const handleKeyDown = (e) => {
      // Prevent default behavior for arrow keys and space
      if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', ' '].includes(e.key)) {
        e.preventDefault()
        // Play move sound for arrow keys
        if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
          soundService.playMoveSound()
        }
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

  // Calculate adaptive cell size with better scaling for small grids
  const calculateCellSize = () => {
    const viewportWidth = window.innerWidth * 0.5 // 50% of screen width
    const viewportHeight = window.innerHeight * 0.6 // Consider height too

    const cellSizeByWidth = Math.floor(viewportWidth / gridWidth)
    const cellSizeByHeight = Math.floor(viewportHeight / gridHeight)
    const calculatedSize = Math.min(cellSizeByWidth, cellSizeByHeight)

    // More aggressive scaling for small grids
    const minCellSize = Math.max(25, Math.floor(300 / Math.max(gridWidth, gridHeight)))
    const maxCellSize = 60

    return Math.max(minCellSize, Math.min(calculatedSize, maxCellSize))
  }

  const cellSize = calculateCellSize()



  const boardWidth = cellSize * gridWidth
  const boardHeight = cellSize * gridHeight

  // Color helpers for gradient from head (darker) to tail (lighter)
  const hexToRgb = (hex) => {
    const h = hex.replace('#', '')
    const full = h.length === 3 ? h.split('').map(c => c + c).join('') : h
    const int = parseInt(full, 16)
    return { r: (int >> 16) & 255, g: (int >> 8) & 255, b: int & 255 }
  }

  const rgbToHex = ({ r, g, b }) => {
    const toHex = (v) => v.toString(16).padStart(2, '0')
    return `#${toHex(r)}${toHex(g)}${toHex(b)}`
  }

  const lerp = (a, b, t) => Math.round(a + (b - a) * t)

  const lerpColor = (hexA, hexB, t) => {
    const A = hexToRgb(hexA)
    const B = hexToRgb(hexB)
    return rgbToHex({ r: lerp(A.r, B.r, t), g: lerp(A.g, B.g, t), b: lerp(A.b, B.b, t) })
  }

  const headColor = isDarkMode ? '#3b4220' : '#000000'
  const tailColor = isDarkMode ? '#bfc98a' : '#6B7A3D'

  return (
    <div className="flex flex-col items-center justify-center min-h-screen p-4" style={{
      backgroundColor: isDarkMode ? '#1a1a1a' : '#8B9556',
      color: isDarkMode ? '#8B9556' : '#000'
    }}>
      <div className="flex gap-2 mb-4">
        <button
          onClick={onBackToMenu}
          className="px-4 py-2 border-2 font-black tracking-wide hover:opacity-80"
          style={{
            backgroundColor: isDarkMode ? '#000' : '#000',
            color: isDarkMode ? '#8B9556' : '#8B9556',
            borderColor: isDarkMode ? '#8B9556' : '#000'
          }}
        >
          ← MENU
        </button>
        <button
          onClick={onToggleMute}
          className="px-4 py-2 border-2 font-black tracking-wide hover:opacity-80"
          style={{
            backgroundColor: isDarkMode ? '#000' : '#000',
            color: isDarkMode ? '#8B9556' : '#8B9556',
            borderColor: isDarkMode ? '#8B9556' : '#000'
          }}
          title={isMuted ? "Unmute" : "Mute"}
        >
          {isMuted ? "🔇" : "🔊"}
        </button>
        <button
          onClick={onToggleDebug}
          className="px-4 py-2 border-2 font-black tracking-wide hover:opacity-80"
          style={{
            backgroundColor: isDarkMode ? '#000' : '#000',
            color: isDarkMode ? '#8B9556' : '#8B9556',
            borderColor: isDarkMode ? '#8B9556' : '#000'
          }}
          title="Toggle Debug Mode"
        >
          {showDebugNumbers ? "🐛" : "⚙️"}
        </button>
        <button
          onClick={() => onToggleDarkMode && onToggleDarkMode()}
          className="px-4 py-2 border-2 font-black tracking-wide hover:opacity-80"
          style={{
            backgroundColor: isDarkMode ? '#000' : '#000',
            color: isDarkMode ? '#8B9556' : '#8B9556',
            borderColor: isDarkMode ? '#8B9556' : '#000'
          }}
          title="Toggle Dark Mode"
        >
          {isDarkMode ? '☀️' : '🌙'}
        </button>
      </div>

      <div
        className="relative border-4 shadow-lg mb-6"
        style={{
          width: `${boardWidth + 8}px`,
          height: `${boardHeight + 8}px`,
          backgroundColor: isDarkMode ? '#000' : '#6B7A3D',
          borderColor: isDarkMode ? '#8B9556' : '#000',
          padding: '4px'
        }}
      >
        {/* Snake segments */}
        {snake.map((segment, index) => {
          const segmentPadding = Math.floor(cellSize * 0.15) // Slightly more padding for better spacing
          // determine gradient position (0 = head, 1 = tail)
          const t = snake && snake.length > 1 ? index / (snake.length - 1) : 0
          const segColor = lerpColor(headColor, tailColor, t)

          return (
            <div
              key={`${segment.x}-${segment.y}-${index}`}
              className="absolute rounded-lg"
              style={{
                left: `${segment.x * cellSize + segmentPadding}px`,
                top: `${segment.y * cellSize + segmentPadding}px`,
                width: `${cellSize - (segmentPadding * 2)}px`,
                height: `${cellSize - (segmentPadding * 2)}px`,
                backgroundColor: segColor,
                borderColor: isDarkMode ? '#6B7A3D' : '#000'
              }}
            >
              {showDebugNumbers && (
                <div className="text-xs font-bold flex items-center justify-center h-full"
                  style={{ color: isDarkMode ? '#000' : '#8B9556' }}>
                  {index}
                </div>
              )}
            </div>
          )
        })}

        {/* Food (+) */}
        {food && (
          <div
            className="absolute flex items-center justify-center font-black animate-spin"
            style={{
              left: `${food.x * cellSize}px`,
              top: `${food.y * cellSize}px`,
              width: `${cellSize}px`,
              height: `${cellSize}px`,
              fontSize: `${cellSize * 0.8}px`,
              color: isDarkMode ? '#A5B663' : '#000',
              textShadow: isDarkMode ? '0 0 3px #8B9556' : 'none',
              animationDuration: '2s'
            }}
          >
            +
          </div>
        )}

        {/* Start Game Overlay */}
        {!gameStarted && !gameOver && !gameWon && (
          <div className="absolute inset-0 flex items-center justify-center" style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}>
            <div className="p-8 border-4 shadow-lg" style={{
              backgroundColor: isDarkMode ? '#1a1a1a' : '#8B9556',
              color: isDarkMode ? '#8B9556' : '#000',
              borderColor: isDarkMode ? '#8B9556' : '#000'
            }}>
              <h2 className="text-3xl font-black mb-4 tracking-wider text-center">SNAKE GAME</h2>
              <p className="text-xl font-bold mb-4 text-center">PRESS SPACE TO START</p>
            </div>
          </div>
        )}

        {/* Game Over Overlay */}
        {gameOver && (
          <div className="absolute inset-0 flex items-center justify-center" style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}>
            <div className="p-8 border-4 shadow-lg" style={{
              backgroundColor: isDarkMode ? '#000' : '#8B9556',
              color: isDarkMode ? '#8B9556' : '#000',
              borderColor: isDarkMode ? '#8B9556' : '#000'
            }}>
              <h2 className="text-3xl font-black mb-4 tracking-wider text-center">GAME OVER</h2>
              <p className="text-xl font-bold mb-4 text-center">SCORE: {score}</p>
              <div className="space-y-4 text-center">
                <div className="text-sm font-bold">PRESS R TO RESTART</div>
                <button
                  onClick={onRestart}
                  className="font-black py-3 px-8 border-2 hover:opacity-80"
                  style={{
                    backgroundColor: isDarkMode ? '#8B9556' : '#000',
                    color: isDarkMode ? '#000' : '#8B9556',
                    borderColor: isDarkMode ? '#000' : '#000'
                  }}
                >
                  RESTART
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Game Won Overlay */}
        {gameWon && (
          <div className="absolute inset-0 flex items-center justify-center" style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}>
            <div className="p-8 border-4 shadow-lg" style={{
              backgroundColor: isDarkMode ? '#1a1a1a' : '#8B9556',
              color: isDarkMode ? '#8B9556' : '#000',
              borderColor: isDarkMode ? '#8B9556' : '#000'
            }}>
              <h2 className="text-3xl font-black mb-4 tracking-wider text-center">YOU WON!</h2>
              <p className="text-xl font-bold mb-4 text-center">PERFECT SCORE: {score}</p>
              <div className="space-y-4 text-center">
                <div className="text-sm font-bold">PRESS R TO PLAY AGAIN</div>
                <button
                  onClick={onRestart}
                  className="font-black py-3 px-8 border-2 hover:opacity-80"
                  style={{
                    backgroundColor: isDarkMode ? '#8B9556' : '#000',
                    color: isDarkMode ? '#000' : '#8B9556',
                    borderColor: isDarkMode ? '#000' : '#000'
                  }}
                >
                  PLAY AGAIN
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Start Game Overlay */}
      {!gameStarted && !gameOver && !gameWon && (
        <div className="absolute inset-0 flex items-center justify-center" style={{ backgroundColor: 'rgba(0,0,0,0.7)' }}>
          <div className="p-8 border-4 shadow-lg" style={{
            backgroundColor: isDarkMode ? '#1a1a1a' : '#8B9556',
            color: isDarkMode ? '#8B9556' : '#000',
            borderColor: isDarkMode ? '#8B9556' : '#000'
          }}>
            <h2 className="text-3xl font-black mb-4 tracking-wider text-center">SNAKE GAME</h2>
            <p className="text-xl font-bold mb-4 text-center">PRESS SPACE TO START</p>
            <div className="space-y-4 text-center">
              <div className="flex gap-2 justify-center">
                <button
                  onClick={onBackToMenu}
                  className="px-4 py-2 border-2 font-black tracking-wide hover:opacity-80"
                  style={{
                    backgroundColor: isDarkMode ? '#000' : '#000',
                    color: isDarkMode ? '#8B9556' : '#8B9556',
                    borderColor: isDarkMode ? '#8B9556' : '#000'
                  }}
                >
                  ← MENU
                </button>
                <button
                  onClick={onToggleMute}
                  className="px-4 py-2 border-2 font-black tracking-wide hover:opacity-80"
                  style={{
                    backgroundColor: isDarkMode ? '#000' : '#000',
                    color: isDarkMode ? '#8B9556' : '#8B9556',
                    borderColor: isDarkMode ? '#8B9556' : '#000'
                  }}
                  title={isMuted ? "Unmute" : "Mute"}
                >
                  {isMuted ? "🔇" : "🔊"}
                </button>
                <button
                  onClick={onToggleDebug}
                  className="px-4 py-2 border-2 font-black tracking-wide hover:opacity-80"
                  style={{
                    backgroundColor: isDarkMode ? '#000' : '#000',
                    color: isDarkMode ? '#8B9556' : '#8B9556',
                    borderColor: isDarkMode ? '#8B9556' : '#000'
                  }}
                  title="Toggle Debug Mode"
                >
                  {showDebugNumbers ? "🐛" : "⚙️"}
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="text-center">
        {gameStarted && !gameOver && !gameWon && (
          <p className="text-sm font-bold text-center">USE ARROWS • PRESS R TO RESTART</p>
        )}
      </div>

      <div className="flex justify-between items-center mt-6 font-black border-2 p-3" style={{
        width: `${Math.max(boardWidth + 8, 400)}px`,
        backgroundColor: isDarkMode ? '#000' : '#6B7A3D',
        color: isDarkMode ? '#8B9556' : '#000',
        borderColor: isDarkMode ? '#8B9556' : '#000'
      }}>
        <div>SCORE: {score}</div>
        <div>SNAKE</div>
        <div>LEVEL: 1</div>
      </div>
    </div>
  )
}

export default GameBoard
