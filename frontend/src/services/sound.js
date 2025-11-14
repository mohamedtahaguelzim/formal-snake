class SoundService {
  constructor() {
    this.audioContext = null
    this.isMuted = false
    this.sounds = {}
    this.initAudioContext()
  }

  initAudioContext() {
    try {
      // Create AudioContext on user interaction
      if (typeof AudioContext !== 'undefined') {
        this.audioContext = new AudioContext()
      } else if (typeof window.webkitAudioContext !== 'undefined') {
        this.audioContext = new window.webkitAudioContext()
      }
    } catch (error) {
      console.warn('Audio not supported:', error)
    }
  }

  // Generate beep sound for eating food
  generateEatSound() {
    if (!this.audioContext || this.isMuted) return
    
    const oscillator = this.audioContext.createOscillator()
    const gainNode = this.audioContext.createGain()
    
    oscillator.connect(gainNode)
    gainNode.connect(this.audioContext.destination)
    
    oscillator.frequency.setValueAtTime(800, this.audioContext.currentTime)
    oscillator.frequency.exponentialRampToValueAtTime(1200, this.audioContext.currentTime + 0.1)
    
    gainNode.gain.setValueAtTime(0.1, this.audioContext.currentTime)
    gainNode.gain.exponentialRampToValueAtTime(0.001, this.audioContext.currentTime + 0.2)
    
    oscillator.start(this.audioContext.currentTime)
    oscillator.stop(this.audioContext.currentTime + 0.2)
  }

  // Generate sound for game over
  generateGameOverSound() {
    if (!this.audioContext || this.isMuted) return
    
    const oscillator = this.audioContext.createOscillator()
    const gainNode = this.audioContext.createGain()
    
    oscillator.connect(gainNode)
    gainNode.connect(this.audioContext.destination)
    
    oscillator.frequency.setValueAtTime(400, this.audioContext.currentTime)
    oscillator.frequency.exponentialRampToValueAtTime(200, this.audioContext.currentTime + 0.5)
    
    gainNode.gain.setValueAtTime(0.1, this.audioContext.currentTime)
    gainNode.gain.exponentialRampToValueAtTime(0.001, this.audioContext.currentTime + 0.5)
    
    oscillator.start(this.audioContext.currentTime)
    oscillator.stop(this.audioContext.currentTime + 0.5)
  }

  // Generate sound for winning
  generateWinSound() {
    if (!this.audioContext || this.isMuted) return
    
    const notes = [523, 659, 784, 1047] // C5, E5, G5, C6
    
    notes.forEach((freq, index) => {
      setTimeout(() => {
        const oscillator = this.audioContext.createOscillator()
        const gainNode = this.audioContext.createGain()
        
        oscillator.connect(gainNode)
        gainNode.connect(this.audioContext.destination)
        
        oscillator.frequency.setValueAtTime(freq, this.audioContext.currentTime)
        
        gainNode.gain.setValueAtTime(0.1, this.audioContext.currentTime)
        gainNode.gain.exponentialRampToValueAtTime(0.001, this.audioContext.currentTime + 0.3)
        
        oscillator.start(this.audioContext.currentTime)
        oscillator.stop(this.audioContext.currentTime + 0.3)
      }, index * 100)
    })
  }

  // Generate move sound (subtle)
  generateMoveSound() {
    if (!this.audioContext || this.isMuted) return
    
    const oscillator = this.audioContext.createOscillator()
    const gainNode = this.audioContext.createGain()
    
    oscillator.connect(gainNode)
    gainNode.connect(this.audioContext.destination)
    
    oscillator.frequency.setValueAtTime(150, this.audioContext.currentTime)
    
    gainNode.gain.setValueAtTime(0.02, this.audioContext.currentTime)
    gainNode.gain.exponentialRampToValueAtTime(0.001, this.audioContext.currentTime + 0.1)
    
    oscillator.start(this.audioContext.currentTime)
    oscillator.stop(this.audioContext.currentTime + 0.1)
  }

  // Resume audio context after user interaction
  resumeAudioContext() {
    if (this.audioContext && this.audioContext.state === 'suspended') {
      this.audioContext.resume()
    }
  }

  toggleMute() {
    this.isMuted = !this.isMuted
    return this.isMuted
  }

  getMuted() {
    return this.isMuted
  }

  setMuted(muted) {
    this.isMuted = muted
  }

  // Play methods that match what components expect
  playEatSound() {
    this.generateEatSound()
  }

  playGameOverSound() {
    this.generateGameOverSound()
  }

  playWinSound() {
    this.generateWinSound()
  }

  playMoveSound() {
    this.generateMoveSound()
  }
}

export default new SoundService()