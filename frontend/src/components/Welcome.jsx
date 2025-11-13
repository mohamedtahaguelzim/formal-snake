import { useState } from 'react'

function Welcome({ onStartGame }) {
  const [gridWidth, setGridWidth] = useState(20)
  const [gridHeight, setGridHeight] = useState(20)

  const handleStart = () => {
    onStartGame({
      gridWidth: parseInt(gridWidth),
      gridHeight: parseInt(gridHeight)
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
        </div>

        <button
          onClick={handleStart}
          className="w-full bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-md transition-colors duration-200"
        >
          Start Game
        </button>
      </div>

      <p className="mt-4 text-gray-400 text-center">
        Configure your snake game and connect to the backend server
      </p>
    </div>
  )
}

export default Welcome
