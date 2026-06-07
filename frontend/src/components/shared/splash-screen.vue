<template>
  <div ref="rootRef" class="splash-screen" aria-hidden="true">
    <div class="splash-background">
      <span class="splash-grid"></span>
      <span class="splash-radial"></span>
      <span class="splash-sweep"></span>
    </div>

    <div class="splash-frame">
      <span v-for="corner in corners" :key="corner" :class="['splash-corner', `splash-corner-${corner}`]"></span>
    </div>

    <main class="splash-lockup">
      <div class="splash-emblem">
        <span class="splash-emblem-halo"></span>
        <span class="splash-emblem-ring splash-emblem-ring-outer"></span>
        <span class="splash-emblem-ring splash-emblem-ring-inner"></span>
        <img class="splash-logo" src="/logo.svg" alt="" />
      </div>

      <div class="splash-copy">
        <p class="splash-eyebrow">NEU STUDY ADMIN</p>
        <h1 class="splash-title" aria-label="NEUSTUDY">
          <span v-for="(char, index) in titleChars" :key="`${char}-${index}`" class="splash-title-char">{{ char }}</span>
        </h1>
        <div class="splash-status">
          <span class="splash-status-line"></span>
          <span>Console ready</span>
          <span class="splash-status-line"></span>
        </div>
      </div>

      <div class="splash-progress">
        <span class="splash-progress-fill"></span>
      </div>
    </main>
  </div>
</template>

<script lang="ts" setup>
import { animate, createTimeline, stagger } from 'animejs'
import { onBeforeUnmount, onMounted, ref } from 'vue'

const emit = defineEmits<{
  done: []
}>()

const rootRef = ref<HTMLElement | null>(null)
const titleChars = 'NEUSTUDY'.split('')
const corners = ['top-left', 'top-right', 'bottom-left', 'bottom-right']
const activeAnimations: Array<{ revert: () => unknown }> = []
let finished = false

const finish = () => {
  if (finished) return
  finished = true
  emit('done')
}

onMounted(() => {
  const root = rootRef.value
  if (!root) return

  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches

  if (prefersReducedMotion) {
    activeAnimations.push(
      animate(root, {
        opacity: [1, 0],
        duration: 220,
        delay: 480,
        ease: 'out(3)',
        onComplete: finish
      })
    )
    return
  }

  const select = (selector: string) => root.querySelector(selector) as HTMLElement
  const selectAll = (selector: string) => root.querySelectorAll(selector)
  const targets = {
    grid: select('.splash-grid'),
    radial: select('.splash-radial'),
    sweep: select('.splash-sweep'),
    frame: select('.splash-frame'),
    corners: selectAll('.splash-corner'),
    emblem: select('.splash-emblem'),
    halo: select('.splash-emblem-halo'),
    rings: selectAll('.splash-emblem-ring'),
    logo: select('.splash-logo'),
    eyebrow: select('.splash-eyebrow'),
    chars: selectAll('.splash-title-char'),
    status: select('.splash-status'),
    statusLines: selectAll('.splash-status-line'),
    progress: select('.splash-progress-fill')
  }

  activeAnimations.push(
    animate(targets.radial, {
      scale: [1, 1.08],
      opacity: [0.58, 0.82],
      duration: 2200,
      alternate: true,
      loop: true,
      ease: 'inOut(3)'
    }),
    animate(targets.rings, {
      rotate: ['0deg', '360deg'],
      duration: 6400,
      delay: stagger(480),
      loop: true,
      ease: 'linear'
    }),
    animate(targets.sweep, {
      translateX: ['-115%', '115%'],
      opacity: [0, 0.62, 0],
      duration: 1800,
      delay: 320,
      ease: 'inOut(2)'
    })
  )

  const timeline = createTimeline({
    defaults: {
      ease: 'out(4)'
    },
    onComplete: finish
  })

  timeline
    .add(root, { opacity: [0, 1], duration: 180 }, 0)
    .add(targets.grid, { opacity: [0, 0.56], scale: [1.04, 1], duration: 620 }, 0)
    .add(targets.radial, { opacity: [0, 0.7], scale: [0.88, 1], duration: 780 }, 80)
    .add(targets.frame, { opacity: [0, 1], scale: [0.985, 1], duration: 520 }, 120)
    .add(targets.corners, { opacity: [0, 1], scale: [0.82, 1], duration: 460, delay: stagger(48) }, 160)
    .add(targets.emblem, { opacity: [0, 1], translateY: [18, 0], scale: [0.88, 1], duration: 680 }, 360)
    .add(targets.halo, { opacity: [0, 1], scale: [0.65, 1.05], duration: 720 }, 420)
    .add(targets.logo, { opacity: [0, 1], scale: [0.86, 1], filter: ['blur(10px)', 'blur(0px)'], duration: 620 }, 500)
    .add(targets.rings, { opacity: [0, 1], scale: [0.82, 1], duration: 680, delay: stagger(90) }, 520)
    .add(targets.eyebrow, { opacity: [0, 1], translateY: [10, 0], duration: 360 }, 760)
    .add(targets.chars, {
      opacity: [0, 1],
      translateY: [24, 0],
      filter: ['blur(8px)', 'blur(0px)'],
      duration: 560,
      delay: stagger(36)
    }, 900)
    .add(targets.status, { opacity: [0, 1], translateY: [8, 0], duration: 340 }, 1240)
    .add(targets.statusLines, { scaleX: [0, 1], duration: 420, delay: stagger(80) }, 1300)
    .add(targets.progress, { scaleX: [0, 1], duration: 1180, ease: 'inOut(3)' }, 620)
    .add(targets.emblem, { scale: [1, 1.035, 1], duration: 560, ease: 'inOut(3)' }, 1540)
    .add(root, {
      opacity: [1, 0],
      scale: [1, 1.012],
      filter: ['blur(0px)', 'blur(8px)'],
      duration: 560,
      ease: 'in(3)'
    }, 2240)

  activeAnimations.push(timeline)
})

onBeforeUnmount(() => {
  activeAnimations.forEach(animation => animation.revert())
})
</script>

<style scoped>
.splash-screen {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: grid;
  place-items: center;
  overflow: hidden;
  color: #f8fff9;
  background:
    radial-gradient(circle at 50% 46%, rgba(32, 240, 139, 0.16), transparent 36%),
    linear-gradient(135deg, #03100a 0%, #071711 44%, #000 100%);
  isolation: isolate;
}

.splash-background,
.splash-grid,
.splash-radial,
.splash-sweep,
.splash-frame {
  position: absolute;
  inset: 0;
}

.splash-grid {
  background-image:
    linear-gradient(rgba(32, 240, 139, 0.08) 1px, transparent 1px),
    linear-gradient(90deg, rgba(32, 240, 139, 0.08) 1px, transparent 1px);
  background-size: 46px 46px;
  mask-image: radial-gradient(circle at 50% 50%, #000 0 58%, transparent 82%);
  opacity: 0;
}

.splash-radial {
  width: min(640px, 78vw);
  aspect-ratio: 1;
  inset: 50% auto auto 50%;
  border: 1px solid rgba(32, 240, 139, 0.2);
  border-radius: 50%;
  background:
    radial-gradient(circle, rgba(32, 240, 139, 0.2), transparent 52%),
    conic-gradient(from 150deg, transparent, rgba(32, 240, 139, 0.16), transparent 32%);
  filter: blur(1px);
  transform: translate(-50%, -50%);
  opacity: 0;
}

.splash-sweep {
  width: 46%;
  background: linear-gradient(90deg, transparent, rgba(248, 255, 249, 0.18), rgba(32, 240, 139, 0.24), transparent);
  transform: translateX(-115%) skewX(-12deg);
  mix-blend-mode: screen;
  opacity: 0;
}

.splash-frame {
  width: min(820px, calc(100vw - 44px));
  height: min(520px, calc(100vh - 44px));
  inset: 50% auto auto 50%;
  border: 1px solid rgba(248, 255, 249, 0.1);
  transform: translate(-50%, -50%);
  opacity: 0;
}

.splash-corner {
  position: absolute;
  width: 42px;
  height: 42px;
  border-color: rgba(32, 240, 139, 0.72);
  opacity: 0;
}

.splash-corner-top-left {
  top: -1px;
  left: -1px;
  border-top: 1px solid;
  border-left: 1px solid;
}

.splash-corner-top-right {
  top: -1px;
  right: -1px;
  border-top: 1px solid;
  border-right: 1px solid;
}

.splash-corner-bottom-left {
  bottom: -1px;
  left: -1px;
  border-bottom: 1px solid;
  border-left: 1px solid;
}

.splash-corner-bottom-right {
  right: -1px;
  bottom: -1px;
  border-right: 1px solid;
  border-bottom: 1px solid;
}

.splash-lockup {
  position: relative;
  z-index: 2;
  display: grid;
  justify-items: center;
  gap: 22px;
  width: min(620px, calc(100vw - 40px));
  text-align: center;
}

.splash-emblem {
  position: relative;
  display: grid;
  place-items: center;
  width: 124px;
  aspect-ratio: 1;
  opacity: 0;
}

.splash-emblem-halo,
.splash-emblem-ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
}

.splash-emblem-halo {
  background: radial-gradient(circle, rgba(32, 240, 139, 0.34), transparent 68%);
  filter: blur(6px);
  opacity: 0;
}

.splash-emblem-ring {
  opacity: 0;
}

.splash-emblem-ring-outer {
  border: 1px solid rgba(32, 240, 139, 0.48);
  border-top-color: rgba(248, 255, 249, 0.78);
}

.splash-emblem-ring-inner {
  inset: 14px;
  border: 1px solid rgba(248, 255, 249, 0.18);
  border-right-color: rgba(32, 240, 139, 0.74);
}

.splash-logo {
  position: relative;
  width: 72px;
  height: 72px;
  filter: hue-rotate(-68deg) saturate(1.7) brightness(1.16) contrast(1.06);
  opacity: 0;
}

.splash-copy {
  display: grid;
  gap: 8px;
}

.splash-eyebrow {
  margin: 0;
  color: rgba(32, 240, 139, 0.88);
  font-size: var(--text-xs);
  line-height: 1.4;
  font-weight: var(--weight-bold);
  letter-spacing: 0;
  opacity: 0;
}

.splash-title {
  display: flex;
  justify-content: center;
  margin: 0;
  font-size: clamp(2.6rem, 7.4vw, 5.2rem);
  line-height: 0.98;
  font-weight: 900;
  letter-spacing: 0;
}

.splash-title-char {
  display: inline-block;
  color: #f8fff9;
  opacity: 0;
  text-shadow:
    0 0 22px rgba(32, 240, 139, 0.34),
    0 0 46px rgba(32, 240, 139, 0.16);
}

.splash-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 14px;
  color: rgba(248, 255, 249, 0.76);
  font-size: var(--text-sm);
  line-height: 1.45;
  font-weight: var(--weight-medium);
  opacity: 0;
}

.splash-status-line {
  width: min(104px, 20vw);
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(32, 240, 139, 0.82));
  transform: scaleX(0);
  transform-origin: right;
}

.splash-status-line:last-child {
  background: linear-gradient(90deg, rgba(32, 240, 139, 0.82), transparent);
  transform-origin: left;
}

.splash-progress {
  width: min(360px, 68vw);
  height: 3px;
  overflow: hidden;
  background: rgba(248, 255, 249, 0.1);
}

.splash-progress-fill {
  display: block;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, #f8fff9, #20f08b, #18a058);
  box-shadow: 0 0 18px rgba(32, 240, 139, 0.5);
  transform: scaleX(0);
  transform-origin: left;
}

@media (max-width: 640px) {
  .splash-frame {
    width: calc(100vw - 28px);
    height: calc(100vh - 28px);
  }

  .splash-emblem {
    width: 104px;
  }

  .splash-logo {
    width: 62px;
    height: 62px;
  }

  .splash-status {
    font-size: var(--text-xs);
  }
}

@media (prefers-reduced-motion: reduce) {
  .splash-screen,
  .splash-grid,
  .splash-radial,
  .splash-frame,
  .splash-emblem,
  .splash-logo,
  .splash-eyebrow,
  .splash-title-char,
  .splash-status,
  .splash-progress-fill {
    opacity: 1;
    filter: none;
    transform: none;
  }
}
</style>
