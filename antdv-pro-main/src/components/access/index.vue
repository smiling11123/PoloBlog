<script setup lang="ts">
import type { AccessEnum } from '~/utils/constant'

const props = defineProps<{
  access: string | number | (string | number)[] | AccessEnum
}>()
const { hasAccess } = useAccess()
const normalizedAccess = computed(() => {
  if (Array.isArray(props.access))
    return props.access.map(item => String(item))

  return String(props.access)
})
</script>

<template>
  <slot v-if="hasAccess(normalizedAccess)" />
</template>
