# 鸭鸭交友 - 核心UI设计规范 (v1.0)

> 本规范基于参考设计图 (`IMG_4307.PNG`) 提取，旨在统一“红小鸭相亲交友” APP 的视觉语言，保证前端项目在色彩、排版、圆角、阴影等维度的高度一致性。

## 1. 设计理念 (Design Philosophy)
*   **清爽透气**：大量留白与极浅的灰紫色背景，使内容更加突出，减少视觉疲劳。
*   **年轻活力**：使用高饱和度、高明度的“靛蓝紫”为主品牌色，搭配“珊瑚粉”作为情感和心动相关的点缀，营造年轻、潮流的交友氛围。
*   **圆润亲和**：全面使用大圆角（卡片、按钮、标签），消除锐利感，拉近用户距离。

---

## 2. 色彩系统 (Color System)

色彩系统分为品牌主色、情感辅助色、功能状态色、中性文本色及背景色。请严格使用全局 SCSS 变量，禁止在组件中硬编码 HEX 值。

### 2.1 品牌主色 (Primary)
代表品牌核心视觉，用于主要按钮、Tab 选中状态、核心高亮文字等。
*   **Primary (主色紫)**: `#6A61F8` (变量: `$uni-color-primary`)
*   **Primary Gradient (主色渐变)**: `linear-gradient(135deg, #9287FF 0%, #6A61F8 100%)` (变量: `$uni-color-primary-gradient`)
*   **Primary Light (浅紫底色)**: `#F2F3FF` (变量: `$uni-color-primary-light`) - *用于主色标签背景*

### 2.2 辅助情感色 (Accent)
用于心动、喜欢、红点提示、特殊徽章等强情感交互区域。
*   **Accent (珊瑚粉)**: `#FF5E7D` (变量: `$uni-color-accent`)
*   **Accent Light (浅粉底色)**: `#FFF0F3` (变量: `$uni-color-accent-light`)

### 2.3 状态色 (Status)
*   **Success (成功/在线)**: `#00D2A3` (变量: `$uni-color-success`)
*   **Warning (警告/特殊提示)**: `#FFB03A` (变量: `$uni-color-warning`)

### 2.4 中性文本色 (Text & Neutral)
*   **标题/主要文字**: `#1A1A24` (变量: `$uni-text-color`) - *极致的深灰，而非纯黑*
*   **常规段落**: `#4A4A5A` (变量: `$uni-text-color-regular`)
*   **次要说明**: `#8A8A99` (变量: `$uni-text-color-grey`)
*   **占位符/禁用**: `#C8C8D4` (变量: `$uni-text-color-placeholder`)

### 2.5 背景色 (Background)
*   **纯白卡片底色**: `#FFFFFF` (变量: `$uni-bg-color`)
*   **页面基础底色**: `#F7F8FA` (变量: `$uni-bg-color-page`) - *带有一丝蓝紫倾向的浅灰*
*   **卡片内次级标签底色**: `#EAEBF0` (变量: `$uni-border-color`)

---

## 3. 圆角规范 (Border Radius)

全面采用圆润的设计语言。
*   **小圆角 (Small)**: `8rpx` (变量: `$uni-border-radius-sm`) - *用于小标签、微小元素*
*   **基础圆角 (Base)**: `16rpx` (变量: `$uni-border-radius-base`) - *用于常规按钮、输入框*
*   **大圆角 (Large)**: `32rpx` (变量: `$uni-border-radius-lg`) - *用于所有主体卡片 (Card)*
*   **全圆角/胶囊 (Pill)**: `100rpx` (变量: `$uni-border-radius-pill`) - *用于主操作按钮、标签 Pill*

---

## 4. 阴影与层级 (Shadows & Elevation)

抛弃传统生硬的黑色阴影，采用大面积弥散阴影和“同色系呼吸发光”效果。
*   **基础卡片阴影**: `0 8rpx 24rpx rgba(0, 0, 0, 0.04)` (变量: `$uni-shadow-base`)
*   **大卡片/悬浮层阴影**: `0 16rpx 40rpx rgba(0, 0, 0, 0.06)` (变量: `$uni-shadow-lg`)
*   **主色调按钮呼吸发光**: `0 12rpx 32rpx rgba(106, 97, 248, 0.25)` (变量: `$uni-shadow-primary`)
*   **粉色按钮呼吸发光**: `0 12rpx 32rpx rgba(255, 94, 125, 0.25)` (变量: `$uni-shadow-accent`)

---

## 5. 开发落地要求
1.  所有样式开发必须优先使用 `uni.scss` 中定义的变量。
2.  严格遵循卡片 `32rpx`，胶囊按钮 `100rpx` 的圆角设定。
3.  不要使用绝对的 `#000000` 或 `#FFFFFF`（除卡片背景外），字体应使用 `$uni-text-color`。
