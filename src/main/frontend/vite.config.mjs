import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import tailwindcss from "@tailwindcss/vite";

const PATH_DIST = fileURLToPath(new URL("../webapp/resources/core/dist", import.meta.url));

// The frontend is STYLES ONLY: Thymeleaf renders all HTML and all logic lives
// in Java. Vite+Tailwind only run at build-time to generate the CSS the
// templates reference (@{/dist/main.css}). No JavaScript is emitted.
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
