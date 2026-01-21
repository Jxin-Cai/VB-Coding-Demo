<template>
  <canvas ref="canvasRef" class="particles-canvas"></canvas>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  particleCount: {
    type: Number,
    default: 50
  },
  particleColor: {
    type: String,
    default: 'rgba(0, 255, 136, 0.6)'
  },
  particleSize: {
    type: Number,
    default: 2
  },
  particleSpeed: {
    type: Number,
    default: 0.5
  },
  enableMouseFollow: {
    type: Boolean,
    default: false
  }
})

const canvasRef = ref(null)
let animationId = null
let particles = []
let mouse = { x: null, y: null }

class Particle {
  constructor(canvas) {
    this.canvas = canvas
    this.x = Math.random() * canvas.width
    this.y = Math.random() * canvas.height
    this.vx = (Math.random() - 0.5) * props.particleSpeed
    this.vy = (Math.random() - 0.5) * props.particleSpeed
    this.size = Math.random() * props.particleSize + 1
    this.alpha = Math.random() * 0.5 + 0.3
  }

  update() {
    this.x += this.vx
    this.y += this.vy

    // 边界检测
    if (this.x < 0 || this.x > this.canvas.width) {
      this.vx = -this.vx
    }
    if (this.y < 0 || this.y > this.canvas.height) {
      this.vy = -this.vy
    }

    // 鼠标吸引力
    if (props.enableMouseFollow && mouse.x !== null && mouse.y !== null) {
      const dx = mouse.x - this.x
      const dy = mouse.y - this.y
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      if (distance < 150) {
        const force = (150 - distance) / 150
        this.vx += (dx / distance) * force * 0.1
        this.vy += (dy / distance) * force * 0.1
      }
    }

    // 限制速度
    const maxSpeed = props.particleSpeed * 2
    const speed = Math.sqrt(this.vx * this.vx + this.vy * this.vy)
    if (speed > maxSpeed) {
      this.vx = (this.vx / speed) * maxSpeed
      this.vy = (this.vy / speed) * maxSpeed
    }
  }

  draw(ctx) {
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fillStyle = props.particleColor.replace(/[\d.]+\)$/g, `${this.alpha})`)
    ctx.fill()
  }
}

const init = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  canvas.width = window.innerWidth
  canvas.height = window.innerHeight

  particles = []
  for (let i = 0; i < props.particleCount; i++) {
    particles.push(new Particle(canvas))
  }
}

const animate = () => {
  const canvas = canvasRef.value
  if (!canvas) return

  const ctx = canvas.getContext('2d')
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  // 更新和绘制粒子
  particles.forEach(particle => {
    particle.update()
    particle.draw(ctx)
  })

  // 绘制连线
  drawConnections(ctx)

  animationId = requestAnimationFrame(animate)
}

const drawConnections = (ctx) => {
  for (let i = 0; i < particles.length; i++) {
    for (let j = i + 1; j < particles.length; j++) {
      const dx = particles[i].x - particles[j].x
      const dy = particles[i].y - particles[j].y
      const distance = Math.sqrt(dx * dx + dy * dy)

      if (distance < 120) {
        ctx.beginPath()
        ctx.strokeStyle = props.particleColor.replace(/[\d.]+\)$/g, `${(1 - distance / 120) * 0.2})`)
        ctx.lineWidth = 0.5
        ctx.moveTo(particles[i].x, particles[i].y)
        ctx.lineTo(particles[j].x, particles[j].y)
        ctx.stroke()
      }
    }
  }
}

const handleResize = () => {
  init()
}

const handleMouseMove = (e) => {
  if (props.enableMouseFollow) {
    mouse.x = e.clientX
    mouse.y = e.clientY
  }
}

onMounted(() => {
  init()
  animate()
  window.addEventListener('resize', handleResize)
  window.addEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('mousemove', handleMouseMove)
})
</script>

<style scoped>
.particles-canvas {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
  opacity: 0.6;
}
</style>
