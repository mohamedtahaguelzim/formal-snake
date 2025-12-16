# Formal Snake

A formally verified implementation of the Snake game, modeled as a state machine and verified using Stainless (Scala verification framework).

## Screenshots

### Welcome Screen
<img src="docs/screenshots/welcome.png" alt="Welcome Screen" width="400">

### Game in Progress
<img src="docs/screenshots/playing.png" alt="Playing" width="400">

### Game Over
<img src="docs/screenshots/gameover.png" alt="Game Over" width="400">

## About

This project formalizes the Snake game as a state machine to verify correctness properties including:
- Snake continuity and collision detection
- State transition validity
- Safety properties of game rules

The implementation consists of a Scala backend with formal verification and a React frontend for visualization.

## Setup

### Backend (Scala)

Requirements: JDK 11+, sbt 1.x

```bash
cd backend
sbt compile
sbt run
```

See [installation instructions](https://www.scala-sbt.org/1.x/docs/Installing-sbt-on-Linux.html) for sbt setup.


### Formal Verfication (Stainless)

Requirements: Stainless 0.9.9

```bash
cd backend
stainless src/main/scala/snake/core/*
```

See [installation instructions](https://epfl-lara.github.io/stainless/installation.html) for Stainless setup.

### Frontend (React + Vite)

Requirements: Node.js 16+

```bash
cd frontend
npm install
npm run dev
```

## Project Structure

- `backend/` - Scala backend with Akka HTTP and formal verification
- `frontend/` - React frontend with Vite
- `docs/` - Academic paper and documentation
