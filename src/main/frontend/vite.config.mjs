import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";

const PATH_DIST = fileURLToPath(new URL("../webapp/resources/core/dist", import.meta.url));

// El frontend es SOLO estilos: Thymeleaf renderiza todo el HTML y toda la lógica
// vive en Java. Vite+Tailwind actúan únicamente en build-time para generar el CSS
// que las plantillas referencian (@{/dist/main.css}). No se emite JavaScript.
export default defineConfig({
  plugins: [tailwindcss()],
  build: {
    outDir: PATH_DIST,
    emptyOutDir: true,
    sourcemap: false,
    rollupOptions: {
      input: "src/styles/main.css",
      output: {
        assetFileNames: "[name][extname]",
      },
    },
  },
});
