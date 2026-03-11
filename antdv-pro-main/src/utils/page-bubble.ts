// 页面气泡效果
const bubble: {
  width: number
  height: number
  dpr: number
  canvas: HTMLCanvasElement | null
  ctx: CanvasRenderingContext2D | null
  circles: Circle[]
  animate: boolean
  requestId: any
} = {
  width: 0,
  height: 0,
  dpr: 1,
  canvas: null,
  ctx: null,
  circles: [],
  animate: true,
  requestId: null,
}

export function init(canvasInstance: HTMLCanvasElement) {
  if (!bubble || !canvasInstance)
    throw new Error('no canvasInstance')
  removeListeners()
  bubble.width = window.innerWidth
  bubble.height = window.innerHeight
  bubble.canvas = canvasInstance
  resizeCanvas()
  rebuildCircles()
  updateAnimationState()
  animate()
  addListeners()
}

function getParticleCount() {
  return Math.min(140, Math.max(40, Math.floor(bubble.width / 18)))
}

function resizeCanvas() {
  if (!bubble.canvas)
    return

  bubble.dpr = Math.min(window.devicePixelRatio || 1, 1.5)
  bubble.canvas.width = Math.floor(bubble.width * bubble.dpr)
  bubble.canvas.height = Math.floor(bubble.height * bubble.dpr)
  bubble.canvas.style.width = `${bubble.width}px`
  bubble.canvas.style.height = `${bubble.height}px`
  bubble.ctx = bubble.canvas.getContext('2d') as CanvasRenderingContext2D
  bubble.ctx?.setTransform(bubble.dpr, 0, 0, bubble.dpr, 0, 0)
}

function rebuildCircles() {
  bubble.circles = Array.from({ length: getParticleCount() }, () => new Circle())
}

function updateAnimationState() {
  const scrollTop = window.scrollY || document.documentElement.scrollTop || document.body.scrollTop || 0
  bubble.animate = document.visibilityState !== 'hidden' && scrollTop <= bubble.height
}

function scrollCheck() {
  updateAnimationState()
}

function resize() {
  bubble.width = window.innerWidth
  bubble.height = window.innerHeight
  resizeCanvas()
  rebuildCircles()
  updateAnimationState()
}

function animate() {
  if (bubble.animate) {
    bubble.ctx!.clearRect(0, 0, bubble.width, bubble.height)
    for (const circle of bubble.circles)
      circle.draw()
  }
  bubble.requestId = requestAnimationFrame(animate)
}

const palette = [
  '22,119,255',
  '0,194,168',
  '87,181,231',
  '88,101,242',
]

class Circle {
  pos: {
    x: number
    y: number
  }

  alpha: number
  scale: number
  velocity: number
  color: string
  draw: () => void
  constructor() {
    this.pos = { x: 0, y: 0 }
    this.alpha = 0
    this.scale = 0
    this.velocity = 0
    this.color = palette[0]
    this.reset(true)
    this.draw = function () {
      if (!bubble.ctx)
        return

      this.pos.y -= this.velocity
      this.alpha -= 0.0008
      if (this.alpha <= 0 || this.pos.y < -24) {
        this.reset()
        return
      }
      bubble.ctx!.beginPath()
      bubble.ctx!.arc(
        this.pos.x,
        this.pos.y,
        this.scale * 10,
        0,
        2 * Math.PI,
        false,
      )
      bubble.ctx!.fillStyle = `rgba(${this.color},${this.alpha})`
      bubble.ctx!.fill()
    }
  }

  reset(initial = false) {
    this.pos = {
      x: Math.random() * bubble.width,
      y: initial ? bubble.height + Math.random() * (bubble.height * 0.35) : bubble.height + Math.random() * 120,
    }
    this.alpha = 0.08 + Math.random() * 0.2
    this.scale = 0.18 + Math.random() * 0.36
    this.velocity = 0.35 + Math.random() * 0.95
    this.color = palette[Math.floor(Math.random() * palette.length)]
  }
}

function addListeners() {
  window.addEventListener('scroll', scrollCheck)
  window.addEventListener('resize', resize)
  document.addEventListener('visibilitychange', updateAnimationState)
}

export function removeListeners() {
  window.removeEventListener('scroll', scrollCheck)
  window.removeEventListener('resize', resize)
  document.removeEventListener('visibilitychange', updateAnimationState)
  cancelAnimationFrame(bubble.requestId)
  bubble.requestId = null
}

export default {
  init,
  removeListeners,
}
