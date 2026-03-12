export type ImageVariant = 'sm' | 'md'

const VARIANT_SUFFIX: Record<ImageVariant, string> = {
  sm: '__sm',
  md: '__md',
}

const VARIANT_SIZE_HINT: Record<ImageVariant, number> = {
  sm: 480,
  md: 960,
}

const ASSET_EXTENSION_PATTERN = /\.(jpe?g|png|webp|avif)$/i
const STORAGE_ASSET_PATTERN = /\/(myblog|travel)\//i

const splitUrl = (url: string) => {
  const hashIndex = url.indexOf('#')
  const queryIndex = url.indexOf('?')
  const endIndex = [queryIndex, hashIndex].filter((index) => index >= 0).sort((left, right) => left - right)[0] ?? url.length
  return {
    base: url.slice(0, endIndex),
    suffix: url.slice(endIndex),
  }
}

export const getOptimizedImageUrl = (url?: string, variant: ImageVariant = 'md') => {
  if (!url) {
    return ''
  }
  if (/^(data:|blob:)/.test(url)) {
    return url
  }

  const { base, suffix } = splitUrl(url)
  if (!ASSET_EXTENSION_PATTERN.test(base) || !STORAGE_ASSET_PATTERN.test(base) || /__(sm|md)$/.test(base.replace(ASSET_EXTENSION_PATTERN, ''))) {
    return url
  }

  return base.replace(ASSET_EXTENSION_PATTERN, `${VARIANT_SUFFIX[variant]}$&`) + suffix
}

export const buildImageSrcSet = (url?: string) => {
  if (!url) {
    return ''
  }

  const srcSetEntries = (Object.keys(VARIANT_SUFFIX) as ImageVariant[])
    .map((variant) => `${getOptimizedImageUrl(url, variant)} ${VARIANT_SIZE_HINT[variant]}w`)
  if (new Set(srcSetEntries.map((entry) => entry.split(' ')[0])).size === 1 && srcSetEntries[0]?.startsWith(url)) {
    return ''
  }
  return srcSetEntries.concat(`${url} 1440w`).join(', ')
}

export const fallbackToOriginalImage = (event: Event, originalUrl?: string) => {
  const target = event.target as HTMLImageElement | null
  if (!target || !originalUrl || target.dataset.originalFallbackApplied === 'true') {
    return
  }

  target.dataset.originalFallbackApplied = 'true'
  target.removeAttribute('srcset')
  target.removeAttribute('sizes')
  target.src = originalUrl
}
