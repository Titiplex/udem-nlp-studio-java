import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    plugins: [vue()],
    base: './',
    build: {
        outDir: 'dist',
        emptyOutDir: true,
    },
    test: {
        environment: 'jsdom',
        setupFiles: ['./src/test/setup.ts'],
        globals: true,
        include: [
            'src/**/*.spec.ts',
            'src/**/*.test.ts',
            'src/**/*.spec.tsx',
            'src/**/*.test.tsx',
            'src/**/*.spec.vue',
            'src/**/*.test.vue',
        ],
        exclude: [
            'e2e/**',
            'node_modules/**',
            'dist/**',
        ],
        coverage: {
            provider: 'v8',
            reporter: ['text', 'html', 'lcov'],
            reportsDirectory: './coverage',
            include: ['src/**/*.{ts,vue}'],
            exclude: [
                'src/test/**',
                'src/**/*.d.ts',
                'src/main.ts',
            ],
        },
    },
})