import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      // 如果还有以下常用别名，一并配置
      '~': resolve(__dirname, 'src'),
      'assets': resolve(__dirname, 'src/assets'),
      'components': resolve(__dirname, 'src/components'),
    }
  },
  
  define: {
    'process.env.NODE_HOST': JSON.stringify('http://localhost:8888')
  },
  
  server: {
    port: 5173,
    // 如果后端 API 有跨域问题，可以配置代理
    proxy: {
      '/api': {
        target: 'http://localhost:8888',
        changeOrigin: true
      }
    }
  }
})