# Kite Studio

基于 Vue 3 + TypeScript 的个人网站平台，前后台双门户架构。

## 技术栈

Vue 3 · TypeScript · Vite · Vue Router · Pinia · Element Plus · Axios · ECharts · Sass

## 源码结构

```text
src/
├── main.ts         # 应用入口
├── App.vue         # 根组件
├── core/           # 应用基础设施：API、配置、路由、权限等
│   └── plugins/    # 全局插件注册及 ECharts 初始化
├── layouts/        # 页面布局
├── modules/        # 按业务划分的功能模块
└── shared/         # 公共组件、样式、资源及工具
```

## 快速开始

```bash
npm install
npm run dev      # 开发服务器 (http://localhost:5173)
npm run build    # 生产构建
npm run preview  # 预览生产构建
```
