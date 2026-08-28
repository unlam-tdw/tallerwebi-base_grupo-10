#!/usr/bin/env node
// dev.mjs — Orquestador de desarrollo local: prende TODO con un comando y
// apaga TODO con Ctrl+C (Jetty, watcher de estilos y MySQL).
// Uso: npm run dev  (desde la raíz del proyecto, via el package.json raíz)

import { spawn } from "node:child_process";
import { existsSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../../..");
const FRONTEND = path.join(ROOT, "src/main/frontend");

const NPM = "npm";
const MVN = "mvn";

const procesos = [];
let apagando = false;

// shell:true es obligatorio en Node moderno para resolver ejecutables .cmd/.bat
// (npm/mvn) y evitar el error EINVAL. Los comandos son estáticos, sin input de
// usuario, por eso armamos la línea como string (evita el warning DEP0190).
function correr(comando, args, cwd = ROOT) {
  const linea = `${comando} ${args.join(" ")}`;
  const hijo = spawn(linea, { cwd, stdio: "inherit", shell: true });
  procesos.push(hijo);
  return hijo;
}

function ejecutarYEsperar(comando, args, cwd = ROOT) {
  return new Promise((resolve, reject) => {
    const hijo = correr(comando, args, cwd);
    hijo.on("error", (err) => reject(err));
    hijo.on("exit", (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`${comando} terminó con código ${code}`));
      }
    });
  });
}

// Espera a que Jetty conteste antes de abrir el proxy de BrowserSync
// (evita la ventana de 502 en http://localhost:3000 durante el boot).
async function esperarJetty(url, timeoutMs = 90_000) {
  const fin = Date.now() + timeoutMs;
  while (Date.now() < fin) {
    try {
      const res = await fetch(url);
      if (res.ok) {
        return;
      }
    } catch {
      // Jetty todavia no esta arriba
    }
    await new Promise((r) => setTimeout(r, 1500));
  }
  console.warn("[dev] Jetty no respondió a tiempo; BrowserSync puede mostrar 502 hasta que arranque.");
}

function apagarMysql() {
  console.log("\n==> Apagando MySQL (docker compose stop mysql)");
  return new Promise((resolve) => {
    const stop = spawn("docker compose stop mysql", { cwd: ROOT, stdio: "inherit", shell: true });
    stop.on("error", resolve);
    stop.on("exit", resolve);
  });
}

async function detenerHijos() {
  for (const proceso of procesos) {
    if (proceso.exitCode === null && !proceso.killed) {
      try {
        proceso.kill("SIGTERM");
      } catch {
        // ya terminó
      }
    }
  }
}

async function salirTodo(codigo = 130) {
  if (apagando) {
    return;
  }
  apagando = true;
  await detenerHijos();
  await apagarMysql();
  process.exit(codigo);
}

process.on("SIGINT", () => salirTodo(130));
process.on("SIGTERM", () => salirTodo(143));

main().catch((error) => {
  console.error(`\n[dev] Error: ${error.message}`);
  salirTodo(1);
});

async function main() {
  console.log("==> 1/5 MySQL (docker compose up -d mysql)");
  await ejecutarYEsperar("docker", ["compose", "up", "-d", "mysql"]);

  if (!existsSync(path.join(FRONTEND, "node_modules"))) {
    console.log("==> 2/5 Instalando dependencias del frontend (npm ci)");
    await ejecutarYEsperar(NPM, ["ci"], FRONTEND);
  } else {
    console.log("==> 2/5 Dependencias del frontend OK (node_modules presente)");
  }

  console.log("==> 3/5 CSS inicial (npm run build)");
  await ejecutarYEsperar(NPM, ["run", "build"], FRONTEND);

  console.log("==> 4/5 Watcher de estilos en vivo (vite build --watch)");
  const watch = correr(NPM, ["run", "build", "--", "--watch"], FRONTEND);

  console.log("==> 5/5 Jetty + BrowserSync (auto-reload del browser)");
  const jetty = correr(MVN, ["jetty:run"], ROOT);

  jetty.on("exit", (code) => {
    if (!apagando) {
      console.log(`\n[dev] Jetty terminó (código ${code}). Apagando el resto...`);
      salirTodo(code ?? 1);
    }
  });

  await esperarJetty("http://localhost:8080/spring");

  const sync = correr(
    "npx",
    [
      "browser-sync",
      "start",
      "--proxy",
      "http://localhost:8080/spring",
      "--files",
      "src/main/webapp/resources/core/dist/**/*.css,src/main/webapp/WEB-INF/views/thymeleaf/**/*.html",
      "--no-open",
      "--port",
      "3000",
      "--reload-delay",
      "500",
    ],
    ROOT
  );

  console.log("\n[dev] Todo arriba:");
  console.log("[dev]   Browser con auto-reload → http://localhost:3000/spring");
  console.log("[dev]   Jetty directo            → http://localhost:8080/spring");
  console.log("[dev] Editá CSS o plantillas y el browser se actualiza solo.");
  console.log("[dev] Ctrl+C apaga todo (watch, Jetty, BrowserSync y MySQL).\n");
}