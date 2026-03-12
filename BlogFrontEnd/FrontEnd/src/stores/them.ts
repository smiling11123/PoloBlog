import { ref, nextTick } from 'vue'
import { defineStore } from 'pinia'
import { dark, light } from '@/them/them'
import type { Them } from '@/them/them'

export const useThemStore = defineStore('them', () => {
    const themModel = ref(localStorage.getItem("themModel") || "dark")
    // 默认背景图
    const defaultHeaderImg = "http://122.51.102.21/files/myblog/cb90ee30-ff45-4406-9d82-b3404cd4cad2.png"//"http://localhost:9000/myblog/0100312000iznjmym62D5_W_2048_1536.jpg_.webp"
    const headerImgUrl = ref<string>(localStorage.getItem("headerImgUrl") || defaultHeaderImg)
    const them = ref<Them>(themModel.value === "dark" ? dark : light)

    /**
     * 核心动画函数：执行 View Transition 扩散效果
     * @param e 触发事件的对象（用于获取点击坐标）
     * @param callback 改变状态的回调函数（如切换主题或更换图片）
     */
    const runTransition = async (e: MouseEvent, callback: () => void) => {
        // 如果浏览器不支持 View Transition API，则直接执行逻辑
        if (!document.startViewTransition) {
            callback()
            return
        }

        const x = e.clientX
        const y = e.clientY
        const endRadius = Math.hypot(
            Math.max(x, window.innerWidth - x),
            Math.max(y, window.innerHeight - y)
        )

        const viewTransition = document.startViewTransition(async () => {
            callback()
            await nextTick()
        })

        viewTransition.ready.then(() => {
            const clipPath = [
                `circle(0px at ${x}px ${y}px)`,
                `circle(${endRadius}px at ${x}px ${y}px)`
            ]
            document.documentElement.animate(
                {
                    clipPath: clipPath
                },
                {
                    duration: 500,
                    easing: 'ease-in',
                    pseudoElement: '::view-transition-new(root)',
                }
            )
        })
    }

    // 切换暗黑/明亮模式
    const themChange = (e: MouseEvent) => {
        runTransition(e, () => {
            themModel.value = themModel.value === "dark" ? "light" : "dark"
            them.value = themModel.value === "dark" ? dark : light
            localStorage.setItem("themModel", themModel.value)
        })
    }

    // 更换壁纸
    const changeHeaderImg = (e: MouseEvent, url: string) => {
        runTransition(e, () => {
            headerImgUrl.value = url
            localStorage.setItem("headerImgUrl", url)
        })
    }

    return {
        them,
        headerImgUrl,
        themModel,
        themChange,
        changeHeaderImg
    }
})
