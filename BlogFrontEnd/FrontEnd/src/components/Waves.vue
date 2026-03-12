<template>
  <div class="waves-area">
    <svg class="waves-svg" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink"
      viewBox="0 24 150 28" preserveAspectRatio="none" shape-rendering="auto">
      <defs>
        <path id="gentle-wave" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z" />
      </defs>
      <g class="parallax">
        <use class="wave wave-1" xlink:href="#gentle-wave" x="48" y="0" />
        <use class="wave wave-2" xlink:href="#gentle-wave" x="48" y="3" />
        <use class="wave wave-3" xlink:href="#gentle-wave" x="48" y="5" />
        <use class="wave wave-4" xlink:href="#gentle-wave" x="48" y="7" />
      </g>
    </svg>
  </div>
</template>
<script setup lang="ts">
</script>
<style scoped lang="scss">
.waves-area {
  position: absolute;
  top: -15vh;
  left: 0;
  width: 100%;
  height: 15vh;
  min-height: 100px;
  max-height: 150px;
  z-index: 1;
  pointer-events: none;
  /* 物理消除缝隙 */
  transform: translateY(1px); 
}

.waves-svg {
  width: 100%;
  height: 100%;
  display: block;
}

.wave-1 {
  fill: var(--wavesColor1);
}

.wave-2 {
  fill: var(--wavesColor2);
}

.wave-3 {
  fill: var(--wavesColor3);
}

.wave-4 {
  fill: var(--content-bg-solid);
}

.parallax>use {
  animation: move-forever 25s cubic-bezier(.55, .5, .45, .5) infinite;
}
.parallax>use:nth-child(1) { animation-delay: -2s; animation-duration: 7s; }
.parallax>use:nth-child(2) { animation-delay: -3s; animation-duration: 10s; }
.parallax>use:nth-child(3) { animation-delay: -4s; animation-duration: 13s; }
.parallax>use:nth-child(4) { animation-delay: -5s; animation-duration: 20s; }

@keyframes move-forever {
  0% { transform: translate3d(-90px, 0, 0); }
  100% { transform: translate3d(85px, 0, 0); }
}
</style>
