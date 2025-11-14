class WebSocketService {
  constructor() {
    this.ws = null
    this.reconnectAttempts = 0
    this.reconnectInterval = 5000
    this.listeners = {}
    this.isConnecting = false
    this.reconnectTimeout = null
  }

  connect(url = 'ws://localhost:8080/ws') {
    // Prevent multiple simultaneous connection attempts
    if (this.isConnecting || (this.ws && this.ws.readyState === WebSocket.OPEN)) {
      console.log('Already connected or connecting')
      return
    }

    // Close existing connection if in bad state
    if (this.ws && this.ws.readyState !== WebSocket.CLOSED) {
      try {
        this.ws.close()
      } catch (e) {
        console.warn('Error closing previous connection:', e)
      }
    }

    try {
      this.isConnecting = true
      console.log('Connecting to WebSocket:', url)
      this.ws = new WebSocket(url)
      
      this.ws.onopen = () => {
        console.log('WebSocket connected successfully!')
        this.isConnecting = false
        this.reconnectAttempts = 0
        if (this.reconnectTimeout) {
          clearTimeout(this.reconnectTimeout)
          this.reconnectTimeout = null
        }
        this.emit('connected', true)
      }

      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          console.log('Received from server:', data)
          
          // Backend sends game state directly as JSON
          if (data.snake !== undefined) {
            this.emit('GAME_STATE', data)
            
            if (data.gameStarted && !data.gameOver) {
              this.emit('GAME_STARTED', data)
            }
            
            if (data.gameOver) {
              this.emit('GAME_OVER', data)
            }
          }
          
          this.emit('message', data)
        } catch (error) {
          console.error('Error parsing WebSocket message:', error)
        }
      }

      this.ws.onclose = (event) => {
        console.log('WebSocket disconnected', event.code, event.reason)
        this.isConnecting = false
        this.emit('connected', false)
        this.attemptReconnect()
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error)
        this.isConnecting = false
        this.emit('error', error)
      }

    } catch (error) {
      console.error('Failed to connect to WebSocket:', error)
      this.isConnecting = false
      this.emit('error', error)
      this.attemptReconnect()
    }
  }

  attemptReconnect() {
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout)
    }
    
    this.reconnectAttempts++
    console.log(`Attempting to reconnect... (attempt ${this.reconnectAttempts})`)
    
    this.reconnectTimeout = setTimeout(() => {
      this.connect()
    }, this.reconnectInterval)
  }

  send(key) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      const message = JSON.stringify({ key })
      this.ws.send(message)
      console.log('Sent to server:', message)
    } else {
      console.warn('WebSocket is not connected')
    }
  }
  
  sendConfig(config) {
    return new Promise((resolve, reject) => {
      const attemptSend = (retries = 5) => {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
          const message = JSON.stringify(config)
          this.ws.send(message)
          console.log('Sent config to server:', message)
          resolve()
        } else if (retries > 0) {
          console.warn(`WebSocket not ready, retrying... (${retries} attempts left)`)
          setTimeout(() => attemptSend(retries - 1), 50)
        } else {
          console.error('Failed to send config: WebSocket is not connected')
          reject(new Error('WebSocket not connected'))
        }
      }
      attemptSend()
    })
  }

  // Event listener methods
  on(event, callback) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(callback)
  }

  off(event, callback) {
    if (this.listeners[event]) {
      this.listeners[event] = this.listeners[event].filter(cb => cb !== callback)
    }
  }

  emit(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => callback(data))
    }
  }

  disconnect() {
    if (this.reconnectTimeout) {
      clearTimeout(this.reconnectTimeout)
      this.reconnectTimeout = null
    }
    
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    
    this.isConnecting = false
  }

  // Game-specific methods
  startGame() {
    // Backend starts game on spacebar press
    this.send(' ')
  }

  sendInput(key) {
    // Send the key directly (ArrowUp, ArrowDown, etc.)
    this.send(key)
  }

  restartGame() {
    this.send('r')
  }
  
  quitGame() {
    this.send('q')
  }
}

export default new WebSocketService()
