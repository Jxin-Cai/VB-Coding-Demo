import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite配置
export default defineConfig({
  plugins: [vue()],
  build: {
    // 输出目录
    outDir: 'dist',
    // 清空输出目录
    emptyOutDir: true
  },
  server: {
    // 开发服务器端口
    port: 3000,
    // 代理配置（开发环境）
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true
      }
    }
  }
})
