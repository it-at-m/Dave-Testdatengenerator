import { dirname, resolve } from "node:path";
import { fileURLToPath, URL } from "node:url";

import VueI18nPlugin from "@intlify/unplugin-vue-i18n/vite";
import vue from "@vitejs/plugin-vue";
import UnpluginFonts from "unplugin-fonts/vite";
import { defineConfig } from "vite";
import vueDevTools from "vite-plugin-vue-devtools";
import vuetify, { transformAssetUrls } from "vite-plugin-vuetify";
import VueRouter from "vue-router/vite";

import { EncodeBracketsPlugin, extendRoute } from "./encode-brackets-plugin";

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  const isDevelopment = mode === "development";
  return {
    plugins: [
      VueRouter({
        routesFolder: {
          src: "src/routes",
        },
        dts: "./route-map.d.ts",
        ...(isDevelopment && { extendRoute }),
      }),
      vue({
        template: { transformAssetUrls },
        features: {
          optionsAPI: isDevelopment,
        },
      }),
      vuetify(),
      UnpluginFonts({
        inlineFontFace: true,
        fontsource: {
          families: [
            {
              name: "Roboto",
              weights: [100, 300, 400, 500, 700, 900],
              subset: "latin",
              fallback: {
                category: "sans-serif",
              },
            },
          ],
        },
      }),
      vueDevTools(),
      VueI18nPlugin({
        include: resolve(
          dirname(fileURLToPath(import.meta.url)),
          "./src/locales/*.json"
        ),
      }),
      {
        ...EncodeBracketsPlugin(),
        apply: "serve", // Ensures plugin is only applied during serve, see https://vite.dev/guide/using-plugins#conditional-application
      },
    ],
    server: {
      host: true,
      port: 8088,
      proxy: {
        // Frontend calls go to "/api/backend/..."; strip that prefix and forward to the
        // dave-testdata-generator backend (default local port 8087).
        "/api/backend": {
          target: 'http://127.0.0.1:8087',
          changeOrigin: true,
          rewrite: (path) => path.replace(/^\/api\/backend/, ""),
        },
        "/actuator": "http://localhost:8087",
      },
      allowedHosts: ["host.docker.internal"], // required to use frontend behind proxy (e.g. API Gateway)
      headers: {
        "x-frame-options": "SAMEORIGIN", // required to use devtools behind proxy (e.g. API Gateway)
      },
    },
    resolve: {
      alias: {
        "@": fileURLToPath(new URL("./src", import.meta.url)),
      },
    },
  };
});
