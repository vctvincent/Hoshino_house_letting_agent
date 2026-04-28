import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '')
  
  return {
    plugins: [
      vue(),
      {
        name: 'favicon-handler',
        configureServer(server) {
          server.middlewares.use((req, res, next) => {
            if (req.url === '/favicon.ico') {
              res.statusCode = 200;
              res.setHeader('Content-Type', 'image/svg+xml');
              res.end(`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><text y=".9em" font-size="90">🏠</text></svg>`);
            } else {
              next();
            }
          });
        }
      }
    ],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    },
    // Vue 特性标志配置
    define: {
      __VUE_PROD_HYDRATION_MISMATCH_DETAILS__: 'false'
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true
        },
        // 修复：/uploads 直接代理到后端根路径，不需要 /api 前缀
        '/uploads': {
          target: 'http://localhost:8080',  // ✅ 直接访问后端根路径
          changeOrigin: true,
          rewrite: (path) => path  // 保持路径不变
        }
      }
    }
  }
})
