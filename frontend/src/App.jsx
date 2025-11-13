import { useState, useEffect } from 'react'
import Welcome from './components/Welcome'
import GameBoard from './components/GameBoard'
import websocketService from './services/websocket'

function App() {
  const [currentView, setCurrentView] = useState('welcome') // 'welcome' | 'game'
  const [gameState, setGameState] = useState({
    snake: [],
    food: null,
    score: 0,
    gameOver: false,
    gameStarted: false,
    gridWidth: 20,
    gridHeight: 20,
    connected: false
  })

  // Initialize WebSocket connection
  useEffect(() => {
    // Set up WebSocket event listeners
    websocketService.on('connected', (connected) => {
      setGameState(prev => ({ ...prev, connected }))
    })

    websocketService.on('GAME_STATE', (newGameState) => {
      setGameState(prev => ({ ...prev, ...newGameState }))
    })

    websocketService.on('GAME_STARTED', (data) => {
      setGameState(prev => ({ ...prev, gameStarted: true, ...data }))
    })

    websocketService.on('GAME_OVER', (data) => {
      setGameState(prev => ({ ...prev, gameOver: true, ...data }))
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

  const handleStartGame = (config) => {
    setCurrentView('game')
    setGameState(prev => ({ 
      ...prev, 
      gridWidth: config.gridWidth, 
      gridHeight: config.gridHeight 
    }))
    
    // Send start game request to backend
    websocketService.startGame(config)
  }

  const handleKeyPress = (key) => {
    // Send input to backend instead of handling locally
    websocketService.sendInput(key)
  }

  const handleBackToMenu = () => {
    setCurrentView('welcome')
    // Reset game state
    setGameState(prev => ({ 
      ...prev,
      snake: [],
      food: null,
      score: 0,
      gameOver: false,
      gameStarted: false
    }))
  }

  return (
    <>
      {currentView === 'welcome' && (
        <Welcome onStartGame={handleStartGame} />
      )}
      
      {currentView === 'game' && (
        <GameBoard 
          gameState={gameState}
          onKeyPress={handleKeyPress}
          onBackToMenu={handleBackToMenu}
        />
      )}
    </>
  )
}

export default App
