import { useState, useEffect } from 'react'
import Welcome from './components/Welcome'
import GameBoard from './components/GameBoard'
import websocketService from './services/websocket'
import soundService from './services/sound'

function App() {
  const [currentView, setCurrentView] = useState('welcome') // 'welcome' | 'game'
  const [isMuted, setIsMuted] = useState(false)
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const saved = localStorage.getItem('snakeIsDarkMode')
    return saved ? saved === 'true' : false
  })
  const [gameState, setGameState] = useState({
    snake: [],
    food: null,
    gameOver: false,
    gameWon: false,
    gameStarted: false,
    gridWidth: 6,
    gridHeight: 4,
    connected: false
  })

  // Initialize WebSocket connection
  useEffect(() => {
    // Set up WebSocket event listeners
    websocketService.on('connected', (connected) => {
      setGameState(prev => ({ ...prev, connected }))
    })

    websocketService.on('GAME_STATE', (newGameState) => {
      setGameState(prev => ({ 
        ...prev, 
        ...newGameState,
        // Keep debug mode from local config
        showDebugNumbers: prev.showDebugNumbers
      }))
    })

    websocketService.on('error', (error) => {
      console.error('WebSocket error:', error)
      // Could show error notification to user
    })

    websocketService.on('maxReconnectAttemptsReached', () => {
      console.log('Could not connect to backend server')
      // Could show offline mode or error message
    })

    // Try to connect to WebSocket server
    websocketService.connect()

    // Cleanup on unmount
    return () => {
      websocketService.disconnect()
    }
  }, [])

  // Sync dark mode with Welcome component
  useEffect(() => {
    const handleStorageChange = () => {
      const saved = localStorage.getItem('snakeIsDarkMode')
      setIsDarkMode(saved ? saved === 'true' : false)
    }
    window.addEventListener('storage', handleStorageChange)
    handleStorageChange() // Check on mount
    return () => window.removeEventListener('storage', handleStorageChange)
  }, [])

  const handleStartGame = async (config) => {
    // Send config to backend first
    websocketService.sendConfig(config)
    
    // Store config locally only for UI purposes (debug mode)
    setGameState(prev => ({ 
      ...prev,
      showDebugNumbers: config.showDebugNumbers
    }))
    
    // Switch to game view immediately
    setCurrentView('game')
    
    // Send start game command - let backend handle all state
    websocketService.startGame()
  }

  const handleKeyPress = (key) => {
    // Send input to backend instead of handling locally
    websocketService.sendInput(key)
  }

  const handleRestart = () => {
    // Send restart command to backend - let backend handle state
    websocketService.restartGame()
  }

  const handleBackToMenu = () => {
    // Send quit to backend to stop the game
    websocketService.quitGame()
    setCurrentView('welcome')
  }

  const toggleMute = () => {
    setIsMuted(prev => {
      const newMuted = !prev
      soundService.setMuted(newMuted)
      return newMuted
    })
  }

  const toggleDebug = () => {
    setGameState(prev => ({ 
      ...prev, 
      showDebugNumbers: !prev.showDebugNumbers 
    }))
  }

  const toggleDarkMode = () => {
    setIsDarkMode(prev => {
      const newDarkMode = !prev
      localStorage.setItem('snakeIsDarkMode', newDarkMode.toString())
      return newDarkMode
    })
  }

  return (
    <>
      {currentView === 'welcome' && (
        <Welcome 
          onStartGame={handleStartGame} 
          connected={gameState.connected}
          isDarkMode={isDarkMode}
          setIsDarkMode={setIsDarkMode}
        />
      )}
      
      {currentView === 'game' && (
        <GameBoard 
          gameState={gameState}
          onKeyPress={handleKeyPress}
          onBackToMenu={handleBackToMenu}
          onRestart={handleRestart}
          isMuted={isMuted}
          onToggleMute={toggleMute}
          onToggleDebug={toggleDebug}
          isDarkMode={isDarkMode}
          onToggleDarkMode={toggleDarkMode}
        />
      )}
    </>
  )
}

export default App
