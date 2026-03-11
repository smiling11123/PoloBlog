// stores/them.ts

export interface Them {
    // --- 基础背景 ---
    hBgColor: string;
    contentBgSolid: string;
    contentBgTrans: string;
    wavesColor1: string;
    wavesColor2: string;
    wavesColor3: string;

    textColor: string;        // 标题 (一级)
    textSecondary: string;    // 摘要 (二级)
    textTertiary: string;     // 元数据 (三级)

    cardBg: string;           // 默认背景
    cardBgHover: string;      // 悬停背景
    cardBorder: string;       // 默认边框
    cardBorderHover: string;  // 悬停边框
    cardShadow: string;       // 默认阴影
    cardShadowHover: string;  // 悬停阴影

    accentColor: string;      // 强调色
    widgetBg: string;         // 侧边栏背景

    headerTextColor: string;   // 头部大标题文字颜色 
    headerTextShadow: string;  // 头部文字阴影/光晕
    headerInfoBg: string;      // 右侧用户信息框背景

    menuBgHover: string;    // 菜单悬停背景

    articleBg: string; // 文章内容背景
}

export const dark: Them = {
    hBgColor: "rgba(30, 30, 30, 0.3)",
    contentBgSolid: "#121212",
    contentBgTrans: "rgba(18, 18, 18, 0.5)",

    wavesColor1: "rgba(30, 30, 30, 0.7)",
    wavesColor2: "rgba(30, 30, 30, 0.5)",
    wavesColor3: "rgba(30, 30, 30, 0.3)",

    textColor: "#f3f4f6",
    textSecondary: "#9ca3af",
    textTertiary: "#6b7280",

    cardBg: "rgba(30, 30, 30, 0.5)",
    cardBgHover: "rgba(45, 45, 45, 0.8)",
    cardBorder: "rgba(255, 255, 255, 0.1)",
    cardBorderHover: "rgba(255, 255, 255, 0.3)",

    cardShadow: "0 8px 16px -4px rgba(0, 0, 0, 0.5)",
    cardShadowHover: "0 20px 30px -5px rgba(0, 0, 0, 0.6)",

    accentColor: "#1c1c1e",
    widgetBg: "rgba(30, 30, 30, 0.5)",

    headerTextColor: "#ffffff",
    headerTextShadow: "0 2px 4px rgba(0, 0, 0, 0.6)",
    headerInfoBg: "rgba(0, 0, 0, 0.3)",

    menuBgHover: "rgba(255, 255, 255, 0.2)",

    articleBg: "rgba(30, 30, 30, 0.8)"
}

export const light: Them = {
    hBgColor: "rgba(177, 177, 177, 0.2)",
    contentBgSolid: "#f9fafb",
    contentBgTrans: "rgba(255, 255, 255, 0.1)",

    wavesColor1: "rgba(255, 255, 255, 0.5)",
    wavesColor2: "rgba(255, 255, 255, 0.3)",
    wavesColor3: "rgba(255, 255, 255, 0.1)",

    textColor: "#000000ff",
    textSecondary: "#374151",
    textTertiary: "#374151",

    cardBg: "rgba(177, 177, 177, 0.45)",
    cardBgHover: "rgba(185, 185, 185, 0.75)",

    cardBorder: "rgba(255, 255, 255, 0.8)",
    cardBorderHover: "#ffffff",

    cardShadow: "0 4px 15px rgba(148, 163, 184, 0.1)",
    cardShadowHover: "0 20px 40px rgba(148, 163, 184, 0.25)",

    accentColor: "#565656ff",
    widgetBg: "rgba(255, 255, 255, 0.45)",

    headerTextColor: "#000000ff", // 近乎纯黑，对比度最高
    headerTextShadow: "0 2px 4px rgba(0, 0, 0, 0.6)", // 白色光晕
    headerInfoBg: "rgba(255, 255, 255, 0.6)", // 用户框背景

    menuBgHover: "rgba(0, 0, 0, 0.3)",

    articleBg: "rgba(177, 177, 177, 0.8)"
}
