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
  console.log("==> 1/4 MySQL (docker compose up -d mysql)");
  await ejecutarYEsperar("docker", ["compose", "up", "-d", "mysql"]);

  if (!existsSync(path.join(FRONTEND, "node_modules"))) {
    console.log("==> 2/4 Instalando dependencias del frontend (npm ci)");
    await ejecutarYEsperar(NPM, ["ci"], FRONTEND);
  } else {
    console.log("==> 2/4 Dependencias del frontend OK (node_modules presente)");
  }

  console.log("==> 3/4 CSS inicial (npm run build)");
  await ejecutarYEsperar(NPM, ["run", "build"], FRONTEND);

  console.log("==> 4/4 Jetty con hot-reload (Java/Thymeleaf)");
  const jetty = correr(MVN, ["jetty:run"], ROOT);

  jetty.on("exit", (code) => {
    if (!apagando) {
      console.log(`\n[dev] Jetty terminó (código ${code}). Apagando el resto...`);
      salirTodo(code ?? 1);
    }
  });

  console.log("\n[dev] Todo arriba → http://localhost:8080/spring  |  Ctrl+C apaga todo.");
  console.log("[dev] Estilos en vivo (opcional): en otra terminal → cd src/main/frontend && npm run build -- --watch\n");
}