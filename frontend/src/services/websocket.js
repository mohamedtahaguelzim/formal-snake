class WebSocketService {
  constructor() {
    this.ws = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 3000
    this.listeners = {}
  }

  connect(url = 'ws://localhost:8080/ws') {
    try {
      console.log('Connecting to WebSocket:', url)
      this.ws = new WebSocket(url)
      
      this.ws.onopen = () => {
        console.log('WebSocket connected successfully!')
        this.reconnectAttempts = 0
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

      this.ws.onclose = () => {
        console.log('WebSocket disconnected')
        this.emit('connected', false)
        this.attemptReconnect()
      }

      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error)
        this.emit('error', error)
      }

    } catch (error) {
      console.error('Failed to connect to WebSocket:', error)
      this.emit('error', error)
    }
  }

  attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++
      console.log(`Attempting to reconnect... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      
      setTimeout(() => {
        this.connect()
      }, this.reconnectInterval)
    } else {
      console.log('Max reconnection attempts reached')
      this.emit('maxReconnectAttemptsReached')
    }
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
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
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
}

export default new WebSocketService()
