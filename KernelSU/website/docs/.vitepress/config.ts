import { defineConfig, SiteConfig } from 'vitepress'
import locales from './locales'
import { readdir, writeFile } from 'fs/promises'
import { resolve } from 'path'

export default defineConfig( {
    title: 'ShizuSU',
    description: 'A kernel-based root solution for Android',
    lang: 'en-US',
    locales: locales.locales,
    head: [
        ['link', { rel: 'icon', href: '/favicon.ico' }],
        ['link', { rel: 'apple-touch-icon', href: '/app-icon.png' }],
        ['meta', { name: 'theme-color', content: '#4f7cff' }],
        ['meta', { property: 'og:site_name', content: 'ShizuSU' }],
    ],
    themeConfig: {
        logo: '/logo.png',
        siteTitle: 'ShizuSU',
    },
    buildEnd: async (config: SiteConfig) => {
        const templateDir = resolve(config.outDir, 'templates');
        const templateList = resolve(templateDir, "index.json");
        let files = [];
        try {
            files = await readdir(templateDir);
            files = files.filter(file => !file.startsWith('.'));
        } catch(e) {
            // ignore
        }
        await writeFile(templateList, JSON.stringify(files));
    }
})
