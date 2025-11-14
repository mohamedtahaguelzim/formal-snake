import { useState, useEffect } from 'react'

function Welcome({ onStartGame, connected, isDarkMode, setIsDarkMode }) {
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
    return saved ? parseInt(saved) : 500
  })
  const [snakeStartSize, setSnakeStartSize] = useState(() => {
    const saved = localStorage.getItem('snakeStartSize')
    return saved ? parseInt(saved) : 1
  })
  const [isTurnBased, setIsTurnBased] = useState(() => {
    const saved = localStorage.getItem('snakeIsTurnBased')
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
    localStorage.setItem('snakeIsTurnBased', isTurnBased)
  }, [isTurnBased])

  // Update localStorage when dark mode changes
  useEffect(() => {
    localStorage.setItem('snakeIsDarkMode', isDarkMode)
  }, [isDarkMode])

  const incrementStartSize = () => {
    if (snakeStartSize < 10) {
      setSnakeStartSize(snakeStartSize + 1)
    }
  }

  const decrementStartSize = () => {
    if (snakeStartSize > 1) {
      setSnakeStartSize(snakeStartSize - 1)
    }
  }

  const getSpeedLevels = () => {
    return [5000, 2000, 1000, 750, 500, 350, 200, 150, 100, 50] // 10 levels from 5s to 50ms
  }

  const getCurrentSpeedIndex = () => {
    const levels = getSpeedLevels()
    return levels.findIndex(speed => speed === gameSpeed) !== -1 
      ? levels.findIndex(speed => speed === gameSpeed)
      : 4 // default to 500ms
  }

  const decrementSpeed = () => {
    const levels = getSpeedLevels()
    const currentIndex = getCurrentSpeedIndex()
    if (currentIndex > 0) {
      setGameSpeed(levels[currentIndex - 1]) // Go to slower speed
    }
  }

  const incrementSpeed = () => {
    const levels = getSpeedLevels()
    const currentIndex = getCurrentSpeedIndex()
    if (currentIndex < levels.length - 1) {
      setGameSpeed(levels[currentIndex + 1]) // Go to faster speed
    }
  }

  const handleStart = () => {
    onStartGame({
      gridWidth: parseInt(gridWidth),
      gridHeight: parseInt(gridHeight),
      gameSpeed: isTurnBased ? 0 : parseInt(gameSpeed),
      snakeStartSize: parseInt(snakeStartSize)
    })
  }

  return (
    <div 
      className="flex flex-col items-center justify-center min-h-screen py-2 px-4"
      style={{ 
        backgroundColor: isDarkMode ? '#1a1a1a' : '#8B9556',
        color: isDarkMode ? '#8B9556' : '#000'
      }}
    >
      <h1 className="text-4xl font-black mb-4 tracking-wider pixelated text-center" style={{ color: isDarkMode ? '#8B9556' : '#000' }}>SNAKE</h1>
      
      <div 
        className="border-4 p-4 mb-4 max-w-md w-full"
        style={{
          backgroundColor: isDarkMode ? '#000' : '#6B7A3D',
          borderColor: isDarkMode ? '#8B9556' : '#000',
          color: isDarkMode ? '#8B9556' : '#000'
        }}
      >
        <h2 className="text-xl font-black mb-6 text-center tracking-wide">CONFIGURATION</h2>
        
        <div className="space-y-4 mb-8">
          <div>
            <label className="block text-sm font-black mb-2 tracking-wide">GRID SIZE:</label>
            <div className="flex gap-2">
              <div className="flex-1">
                <label className="block text-xs font-bold mb-1">WIDTH:</label>
                <input
                  type="number"
                  min="3"
                  max="40"
                  value={gridWidth}
                  onChange={(e) => setGridWidth(e.target.value)}
                  className="w-full px-3 py-2 border-2 font-bold focus:outline-none"
                  style={{ backgroundColor: isDarkMode ? '#000' : '#8B9556', color: isDarkMode ? '#8B9556' : '#000', borderColor: isDarkMode ? '#8B9556' : '#000' }}
                />
              </div>
              <div className="flex-1">
                <label className="block text-xs font-bold mb-1">HEIGHT:</label>
                <input
                  type="number"
                  min="3"
                  max="40"
                  value={gridHeight}
                  onChange={(e) => setGridHeight(e.target.value)}
                  className="w-full px-3 py-2 border-2 font-bold focus:outline-none"
                  style={{ backgroundColor: isDarkMode ? '#000' : '#8B9556', color: isDarkMode ? '#8B9556' : '#000', borderColor: isDarkMode ? '#8B9556' : '#000' }}
                />
              </div>
            </div>
          </div>
          
          <div>
            <div className="flex items-center justify-between mb-2">
              <label className="text-sm font-black tracking-wide">SPEED MODE:</label>
              <div className="flex items-center gap-2">
                <span className="text-xs font-bold">REAL-TIME</span>
                <button
                  onClick={() => setIsTurnBased(!isTurnBased)}
                  className="w-12 h-6 border-2 relative focus:outline-none flex items-center p-0"
                  style={{ 
                    backgroundColor: isTurnBased ? (isDarkMode ? '#8B9556' : '#000') : (isDarkMode ? '#000' : '#8B9556'),
                    borderColor: isDarkMode ? '#8B9556' : '#000'
                  }}
                >
                  <div
                    className="w-4 h-4 border transition-transform absolute"
                    style={{ 
                      backgroundColor: isDarkMode ? '#000' : '#6B7A3D',
                      borderColor: isDarkMode ? '#8B9556' : '#000',
                      left: isTurnBased ? '6px' : '1px'
                    }}
                  />
                </button>
                <span className="text-xs font-bold">TURN-BASED</span>
              </div>
            </div>
            <div className="flex items-center gap-2 mb-2">
              <button
                onClick={decrementSpeed}
                disabled={isTurnBased || getCurrentSpeedIndex() === 0}
                className="w-8 h-8 border-2 font-black text-xl flex items-center justify-center hover:opacity-80 disabled:opacity-50"
                style={{ 
                  backgroundColor: (isTurnBased || getCurrentSpeedIndex() === 0) ? '#666' : (isDarkMode ? '#8B9556' : '#000'), 
                  color: isDarkMode ? '#000' : '#8B9556',
                  borderColor: isDarkMode ? '#8B9556' : '#000'
                }}
              >
                -
              </button>
              <div className="flex-1 flex items-center gap-1 p-2 border-2" style={{ 
                backgroundColor: isTurnBased ? '#666' : (isDarkMode ? '#000' : '#6B7A3D'), 
                minHeight: '40px',
                borderColor: isDarkMode ? '#8B9556' : '#000'
              }}>
                {isTurnBased ? (
                  <span className="text-xs font-bold text-center w-full" style={{ color: '#999' }}>TURN-BASED</span>
                ) : (
                  Array.from({ length: getCurrentSpeedIndex() + 1 }, (_, index) => (
                    <div
                      key={index}
                      className="w-6 h-6 border rounded-sm"
                      style={{ 
                        backgroundColor: isDarkMode ? '#8B9556' : '#000',
                        borderColor: isDarkMode ? '#8B9556' : '#000'
                      }}
                    />
                  ))
                )}
              </div>
              <button
                onClick={incrementSpeed}
                disabled={isTurnBased || getCurrentSpeedIndex() === getSpeedLevels().length - 1}
                className="w-8 h-8 border-2 font-black text-xl flex items-center justify-center hover:opacity-80 disabled:opacity-50"
                style={{ 
                  backgroundColor: (isTurnBased || getCurrentSpeedIndex() === getSpeedLevels().length - 1) ? '#666' : (isDarkMode ? '#8B9556' : '#000'), 
                  color: isDarkMode ? '#000' : '#8B9556',
                  borderColor: isDarkMode ? '#8B9556' : '#000'
                }}
              >
                +
              </button>
            </div>
            {!isTurnBased && (
              <p className="text-xs font-bold text-center">
                {gameSpeed >= 1000 ? `${(gameSpeed/1000).toFixed(1)}S` : `${gameSpeed}MS`} INTERVAL
              </p>
            )}
          </div>

          <div>
            <label className="block text-sm font-black mb-2 tracking-wide">START SIZE:</label>
            <div className="flex items-center gap-2 mb-2">
              <button
                onClick={decrementStartSize}
                disabled={snakeStartSize <= 1}
                className="w-8 h-8 border-2 border-black font-black text-xl flex items-center justify-center hover:opacity-80 disabled:opacity-50"
                style={{ 
                  backgroundColor: snakeStartSize <= 1 ? '#666' : (isDarkMode ? '#8B9556' : '#000'), 
                  color: isDarkMode ? '#000' : '#8B9556',
                  borderColor: isDarkMode ? '#8B9556' : '#000'
                }}
              >
                -
              </button>
              <div className="flex-1 flex items-center gap-1 p-2 border-2" style={{ 
                backgroundColor: isDarkMode ? '#000' : '#6B7A3D', 
                minHeight: '40px',
                borderColor: isDarkMode ? '#8B9556' : '#000'
              }}>
                {Array.from({ length: snakeStartSize }, (_, index) => (
                  <div
                    key={index}
                    className="w-6 h-6 border rounded-sm"
                    style={{ 
                      backgroundColor: isDarkMode ? '#8B9556' : '#000',
                      borderColor: isDarkMode ? '#6B7A3D' : '#000'
                    }}
                  />
                ))}
              </div>
              <button
                onClick={incrementStartSize}
                disabled={snakeStartSize >= 10}
                className="w-8 h-8 border-2 border-black font-black text-xl flex items-center justify-center hover:opacity-80 disabled:opacity-50"
                style={{ 
                  backgroundColor: snakeStartSize >= 10 ? '#666' : (isDarkMode ? '#8B9556' : '#000'), 
                  color: isDarkMode ? '#000' : '#8B9556',
                  borderColor: isDarkMode ? '#8B9556' : '#000'
                }}
              >
                +
              </button>
            </div>
            <p className="text-xs font-bold text-center">{snakeStartSize} SEGMENT{snakeStartSize !== 1 ? 'S' : ''}</p>
          </div>
        </div>

        <button
          onClick={handleStart}
          disabled={!connected}
          className={`w-full font-black py-3 px-4 border-4 text-lg tracking-wider ${
            connected 
              ? 'cursor-pointer hover:opacity-80' 
              : 'cursor-not-allowed opacity-50'
          }`}
          style={{ 
            backgroundColor: connected ? (isDarkMode ? '#8B9556' : '#000') : '#666', 
            color: connected ? (isDarkMode ? '#000' : '#8B9556') : '#999', 
            borderColor: isDarkMode ? '#8B9556' : '#000' 
          }}
        >
          {connected ? 'START GAME' : 'CONNECTING...'}
        </button>
      </div>

      <div className="mt-4 text-center">
        <div className="flex items-center justify-between text-xs px-2">
          <div className="flex items-center gap-2">
            <div 
              className="w-2 h-2 rounded-full" 
              style={{ 
                backgroundColor: connected ? '#4ade80' : '#ef4444' 
              }}
            />
            <span style={{ color: isDarkMode ? '#8B9556' : '#000' }}>
              {connected ? 'Connected' : 'Connecting...'}
            </span>
          </div>
          <div className="flex items-center gap-2">
            <span className="font-bold" style={{ color: isDarkMode ? '#8B9556' : '#000' }}>Theme:</span>
            <button
              onClick={() => setIsDarkMode(!isDarkMode)}
              className="w-10 h-5 border rounded-full relative focus:outline-none flex items-center p-0"
              style={{ 
                backgroundColor: isDarkMode ? '#000' : '#6B7A3D',
                borderColor: isDarkMode ? '#8B9556' : '#000'
              }}
            >
              <div
                className="w-3 h-3 rounded-full transition-all duration-200 absolute"
                style={{ 
                  backgroundColor: isDarkMode ? '#8B9556' : '#000',
                  left: isDarkMode ? '16px' : '2px'
                }}
              />
            </button>
            <span style={{ color: isDarkMode ? '#8B9556' : '#000' }}>{isDarkMode ? 'Dark' : 'Light'}</span>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Welcome
