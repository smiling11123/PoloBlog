<template>
    <div class="header-container" :style="{ backgroundImage: `url('${themStore.headerImgUrl}')` }">
        <div class="danmaku-layer" v-if="shouldRunDanmaku">
            <DanmakuItem v-for="msg in activeDanmakus" :key="msg.id" :content="msg.content" :top="msg.trackTop"
                :duration="msg.duration" :color="themStore.themModel === 'dark' ? '#f0f0f0' : '#ffffff'"
                @finished="removeDanmaku(msg.id)" />
        </div>

        <div class="content-wrapper" :style="{ opacity: opacity }">
            <div class="left-container">
                <h2 class="title">Welcome Back！</h2>
                <div class="mate-container">
                    <TextType :text="displaySlogans as string | string[]"
                        :typingSpeed="opacity === 0 ? 750 : 70" :pauseDuration="1500" :showCursor="true"
                        cursorCharacter="|" :text-colors="[themStore.them.textColor]" />
                </div>

                <div class="action-group">
                    <button class="publish-trigger-btn" @click="openPublishDialog">
                        <svg class="icon" viewBox="0 0 1024 1024" width="20" height="20">
                            <path
                                d="M860.8 116.8c-10.4-10.4-25.2-13.6-38.8-8.4L88.4 402.4c-14.8 5.6-24.4 20-24.4 35.6 0 15.6 9.6 30 24.4 35.6L412 612l238 238c5.6 14.8 20 24.4 35.6 24.4 15.6 0 30-9.6 35.6-24.4l310-783.6c5.2-13.6 2-28.4-8.4-38.8z"
                                fill="currentColor"></path>
                        </svg>
                        <span>发布留言条</span>
                    </button>
                </div>
            </div>

            <div class="right-container">
                <div class="auth-container">
                    <div class="pic-wrapper">
                        <img class="pic" src="../../public/管理员头像.jpg" alt="User" />
                    </div>
                    <div class="info-container">
                        <span class="username">{{ userStore.authInfo?.userName }}</span>
                    </div>
                </div>
            </div>
        </div>

        <Transition name="fade">
            <div v-if="appStore.isShowPublishMessageSlip" class="dialog-overlay" @click.self="closePublishDialog">
                <div class="publish-card">
                    <div class="card-header">
                        <h3>写下留言</h3>
                        <button class="close-icon" @click="closePublishDialog">×</button>
                    </div>
                    <textarea v-model="newMsgContent" placeholder="这一刻的想法..." maxlength="60"></textarea>
                    <div class="card-footer">
                        <span class="char-count">{{ newMsgContent.length }}/60</span>
                        <div class="btns">
                            <button class="btn-cancel" @click="closePublishDialog">取消</button>
                            <button class="btn-confirm" :disabled="!newMsgContent.trim() || isSubmitting"
                                @click="submitMessage">
                                {{ isSubmitting ? '发送中...' : '发布' }}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </Transition>
    </div>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted, watch } from 'vue';
import { useThemStore } from '../stores/them';
import { getMessageSlipToShow, publishMessageSlip } from '@/api/messageSlip';
import DanmakuItem from './DanmakuItem.vue';
import TextType from './TextType.vue';
import { useAppStore } from '@/stores/app';
import { useUserStore } from '@/stores/user';

const props = defineProps<{ opacity: number }>();
const themStore = useThemStore();
const appStore = useAppStore();
const userStore = useUserStore();

interface ActiveDanmaku {
    id: number;
    content: string;
    trackTop: number;
    duration: number;
}

interface TrackState {
    availableAt: number;
    lastLaunchAt: number;
}

const activeDanmakus = ref<ActiveDanmaku[]>([]);
const pendingDanmakus = ref<string[]>([]);
let launchTimer: any = null;
let refreshDataTimer: any = null;
let dispatchTimer: ReturnType<typeof setInterval> | null = null;
let trackStates: TrackState[] = [];
let measureCanvas: HTMLCanvasElement | null = null;
const isPageVisible = ref(typeof document === 'undefined' ? true : !document.hidden);
const displaySlogans = computed(() => {
    return userStore.displaySlogans.length > 0
        ? userStore.displaySlogans
        : ['热爱折腾的全栈开发者'];
});
const shouldRunDanmaku = computed(() => props.opacity > 0.3 && isPageVisible.value);

const newMsgContent = ref('');
const isSubmitting = ref(false);
const DANMAKU_PADDING_PX = 56;
const DANMAKU_MEASURE_FONT = '500 14px "Segoe UI", system-ui, -apple-system, sans-serif';
const DANMAKU_TRAVEL_DISTANCE_FACTOR = 2.5;

const getTrackTops = () => {
    return window.innerWidth < 768
        ? [20, 32, 44, 56, 68, 80]
        : [15, 25, 35, 45, 55, 65, 75, 85];
};

const getTrackGapPx = () => {
    return window.innerWidth < 768 ? 72 : 96;
};

const getDanmakuSpeedPxPerSecond = () => {
    return window.innerWidth < 768 ? 120 : 165;
};

const getDanmakuDuration = () => {
    const travelDistance = window.innerWidth * DANMAKU_TRAVEL_DISTANCE_FACTOR;
    return Number((travelDistance / getDanmakuSpeedPxPerSecond()).toFixed(2));
};

const resetTrackStates = () => {
    trackStates = getTrackTops().map(() => ({
        availableAt: 0,
        lastLaunchAt: 0
    }));
};

const getMeasureContext = () => {
    if (!measureCanvas) {
        measureCanvas = document.createElement('canvas');
    }
    const context = measureCanvas.getContext('2d');
    if (context) {
        context.font = DANMAKU_MEASURE_FONT;
    }
    return context;
};

const estimateDanmakuWidth = (content: string) => {
    const context = getMeasureContext();
    if (!context) {
        return content.length * 16 + DANMAKU_PADDING_PX;
    }
    return Math.ceil(context.measureText(content).width) + DANMAKU_PADDING_PX;
};

const findAvailableTrackIndex = (contentWidth: number) => {
    const now = Date.now();
    const speed = getDanmakuSpeedPxPerSecond();
    const trackGap = getTrackGapPx();

    if (trackStates.length !== getTrackTops().length) {
        resetTrackStates();
    }

    const availableIndexes = trackStates
        .map((state, index) => ({ state, index }))
        .filter(({ state }) => state.availableAt <= now)
        .sort((a, b) => a.state.lastLaunchAt - b.state.lastLaunchAt)
        .map(({ index }) => index);

    if (availableIndexes.length === 0) {
        return -1;
    }

    const targetIndex = availableIndexes[0] as number;
    trackStates[targetIndex] = {
        availableAt: now + ((contentWidth + trackGap) / speed) * 1000,
        lastLaunchAt: now
    };
    return targetIndex;
};

/**
 * 按轨道可用时间发射弹幕，避免同轨追尾重叠
 */
const tryLaunchDanmaku = (content: string) => {
    const normalizedContent = content.trim();
    if (!normalizedContent || activeDanmakus.value.length > 20) {
        return false;
    }

    const trackIndex = findAvailableTrackIndex(estimateDanmakuWidth(normalizedContent));
    if (trackIndex < 0) {
        return false;
    }

    activeDanmakus.value.push({
        id: Date.now() + Math.random(),
        content: normalizedContent,
        trackTop: getTrackTops()[trackIndex] as number,
        duration: getDanmakuDuration()
    });
    return true;
};

const flushDanmakuQueue = () => {
    while (pendingDanmakus.value.length > 0) {
        const nextContent = pendingDanmakus.value[0];
        if (!nextContent || !tryLaunchDanmaku(nextContent)) {
            break;
        }
        pendingDanmakus.value.shift();
    }
};

const enqueueDanmaku = (content: string) => {
    const normalizedContent = content.trim();
    if (!normalizedContent) {
        return;
    }
    if (pendingDanmakus.value.length >= 40) {
        pendingDanmakus.value.shift();
    }
    pendingDanmakus.value.push(normalizedContent);
    flushDanmakuQueue();
};

const startDanmakuDispatcher = () => {
    if (dispatchTimer) {
        clearInterval(dispatchTimer);
    }
    const interval = window.innerWidth < 768 ? 420 : 320;
    dispatchTimer = setInterval(() => {
        if (!document.hidden) {
            flushDanmakuQueue();
        }
    }, interval);
};

const clearDanmakuState = () => {
    activeDanmakus.value = [];
    pendingDanmakus.value = [];
    resetTrackStates();
};

const stopDanmakuRuntime = (clearState = false) => {
    if (launchTimer) {
        clearInterval(launchTimer);
        launchTimer = null;
    }
    if (dispatchTimer !== null) {
        clearInterval(dispatchTimer);
        dispatchTimer = null;
    }
    if (refreshDataTimer) {
        clearInterval(refreshDataTimer);
        refreshDataTimer = null;
    }
    if (clearState) {
        clearDanmakuState();
    }
};

const startDanmakuRuntime = () => {
    stopDanmakuRuntime(true);
    startDanmakuDispatcher();
    initDanmakuSystem();
    refreshDataTimer = setInterval(() => {
        if (shouldRunDanmaku.value) {
            initDanmakuSystem();
        }
    }, 120000);
};

/**
 * 初始化系统与定时器控制
 */
const initDanmakuSystem = async () => {
    try {
        const res = await getMessageSlipToShow(30);
        const messages = res.data;
        if (!messages || messages.length === 0) return;

        let currentIndex = 0;
        if (launchTimer) clearInterval(launchTimer);

        const interval = window.innerWidth < 768 ? 3500 : 2800;
        launchTimer = setInterval(() => {
            // 如果页面不可见，则不执行逻辑（虽然监听器已处理，但这层判断更稳健）
            if (document.hidden) return;

            if (currentIndex >= messages.length) currentIndex = 0;
            enqueueDanmaku(messages[currentIndex].content);
            currentIndex++;
        }, interval);
    } catch (error) { console.error("Danmaku Error:", error); }
};

/**
 * 页面可见性处理：防止离开页面后回来弹幕爆发
 */
const handleVisibilityChange = () => {
    isPageVisible.value = !document.hidden;
};

const removeDanmaku = (id: number) => {
    activeDanmakus.value = activeDanmakus.value.filter(d => d.id !== id);
};

const openPublishDialog = () => { appStore.isShowPublishMessageSlip = true; };
const closePublishDialog = () => { appStore.isShowPublishMessageSlip = false; newMsgContent.value = ''; };

const submitMessage = async () => {
    if (!newMsgContent.value.trim()) return;
    isSubmitting.value = true;
    try {
        await publishMessageSlip({ content: newMsgContent.value, userId: 1001 });
        enqueueDanmaku(newMsgContent.value);
        closePublishDialog();
    } catch (err) {
        console.error(err);
    } finally {
        isSubmitting.value = false;
    }
};

const ensureHeaderData = async () => {
    await Promise.allSettled([
        userStore.loadAuthInfo(),
        userStore.loadDisplaySlogans()
    ]);
};

watch(shouldRunDanmaku, (visible, previousVisible) => {
    if (visible === previousVisible) {
        return;
    }

    if (visible) {
        startDanmakuRuntime();
    } else {
        stopDanmakuRuntime(true);
    }
});

onMounted(() => {
    void ensureHeaderData();
    if (shouldRunDanmaku.value) {
        startDanmakuRuntime();
    }
    document.addEventListener('visibilitychange', handleVisibilityChange);
});

onUnmounted(() => {
    stopDanmakuRuntime();
    document.removeEventListener('visibilitychange', handleVisibilityChange);
});
</script>

<style scoped lang="scss">
$card-bg: v-bind('themStore.them.widgetBg');
$card-border: v-bind('themStore.them.cardBorder');
$header-text-color: v-bind('themStore.them.headerTextColor');
$bg-color: v-bind('themStore.them.hBgColor');

.header-container {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    z-index: -1;
    background-size: cover;
    background-position: center;
    overflow: hidden;

    &::before {
        content: '';
        position: absolute;
        inset: 0;
        background-color: $bg-color;
        z-index: -1;
        transition: background-color 0.5s cubic-bezier(0.4, 0, 0.2, 1);
    }
}

.danmaku-layer {
    position: absolute;
    inset: 0;
    pointer-events: none;
    z-index: 1;
}

.content-wrapper {
    position: relative;
    max-width: 1400px;
    height: 100vh;
    margin: 0 auto;
    padding: 0 40px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    z-index: 10;
    transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
    will-change: transform, padding, gap;

    @media (max-width: 768px) {
        flex-direction: column;
        justify-content: center;
        gap: 40px;
        padding: 0 20px;
    }
}

.left-container {
    flex: 1;
    color: $header-text-color;
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);

    @media (max-width: 768px) {
        flex: none;
        align-items: center;
        transform: translateY(-10px);
    }

    .title {
        font-size: 2.5rem;
        font-weight: 700;
        margin-bottom: 10px;
        transition: font-size 0.4s ease;

        @media (max-width: 768px) {
            font-size: 1.8rem;
            text-align: center;
        }
    }

    .mate-container {
        font-size: 1.7rem;
        font-weight: 900;
        min-height: 2.5rem;
        transition: font-size 0.4s ease;

        @media (max-width: 768px) {
            font-size: 1.3rem;
            text-align: center;
        }
    }

    .action-group {
        margin-top: 35px;

        .publish-trigger-btn {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 12px 28px;
            background: rgba(255, 255, 255, 0.15);
            border: 1px solid rgba(255, 255, 255, 0.2);
            border-radius: 50px;
            color: white;
            backdrop-filter: blur(10px);
            cursor: pointer;
            transition: all 0.3s ease;

            &:hover {
                background: rgba(255, 255, 255, 0.3);
                transform: translateY(-3px) scale(1.02);
            }

            @media (max-width: 768px) {
                padding: 10px 22px;
                font-size: 14px;
            }
        }
    }
}

.right-container {
    flex: 1;
    display: flex;
    justify-content: flex-end;
    transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);

    @media (max-width: 768px) {
        flex: none;
        justify-content: center;
        transform: translateY(10px);
    }
}

.auth-container {
    width: 280px;
    padding: 20px;
    background: $card-bg;
    border: 1px solid $card-border;
    backdrop-filter: blur(12px);
    border-radius: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 15px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
    transition: all 0.5s cubic-bezier(0.25, 1, 0.5, 1);

    &:hover {
        transform: translateY(-5px);
        box-shadow: 0 15px 40px rgba(0, 0, 0, 0.25);
    }

    @media (max-width: 768px) {
        width: 300px;
        flex-direction: row;
        padding: 15px 20px;
        gap: 15px;
    }

    .pic-wrapper {
        width: 110px;
        height: 110px;
        border-radius: 50%;
        overflow: hidden;
        border: 2px solid rgba(255, 255, 255, 0.2);
        transition: all 0.5s ease;

        @media (max-width: 768px) {
            width: 54px;
            height: 54px;
        }

        .pic {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }
    }

    .info-container {
        width: 100%;
        background: v-bind('themStore.them.headerInfoBg');
        border-radius: 12px;
        padding: 12px;
        display: flex;
        flex-direction: column;
        align-items: center;
        color: $header-text-color;
        transition: all 0.5s ease;

        @media (max-width: 768px) {
            background: transparent;
            padding: 0;
            align-items: flex-start;
            flex: 1;
        }

        .username {
            font-size: 1.15rem;
            font-weight: bold;

            @media (max-width: 768px) {
                font-size: 1rem;
            }
        }
    }
}

.dialog-overlay {
    position: fixed;
    inset: 0;
    //background: rgba(0, 0, 0, 0.75);
    backdrop-filter: blur(10px);
    z-index: 2000;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
}

.publish-card {
    background: $card-bg;
    border: 1px solid $card-border;
    border-radius: 24px;
    width: 100%;
    max-width: 420px;
    padding: 25px;

    .card-header {
        display: flex;
        justify-content: space-between;
        margin-bottom: 15px;

        h3 {
            color: $header-text-color;
            font-size: 1.1rem;
            margin: 0;
        }

        .close-icon {
            background: none;
            border: none;
            color: $header-text-color;
            font-size: 24px;
            cursor: pointer;
        }
    }

    textarea {
        width: 100%;
        height: 100px;
        background: rgba(255, 255, 255, 0.05);
        border: 1px solid $card-border;
        border-radius: 12px;
        padding: 12px;
        color: $header-text-color;
        resize: none;
        outline: none;
    }

    .card-footer {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-top: 15px;

        .char-count {
            font-size: 0.8rem;
            opacity: 0.5;
            color: $header-text-color;
        }

        .btns {
            display: flex;
            gap: 10px;

            button {
                padding: 8px 18px;
                border-radius: 10px;
                cursor: pointer;
                border: none;
                font-size: 14px;
            }

            .btn-confirm {
                background: #20B0E3;
                color: white;
            }
        }
    }
}

.fade-enter-active,
.fade-leave-active {
    transition: all 0.4s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
    transform: scale(0.9);
}
</style>
