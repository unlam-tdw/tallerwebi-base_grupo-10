#!/usr/bin/env node
// dev.mjs — Local dev orchestrator: starts EVERYTHING with one command and
// shuts EVERYTHING down with Ctrl+C (Jetty, style watcher and MySQL).
// Usage: npm run dev  (from the project root, via the root package.json)

import { spawn } from "node:child_process";
import { existsSync } from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), "../../../..");
const FRONTEND = path.join(ROOT, "src/main/frontend");

const NPM = "npm";
const MVN = "mvn";

const children = [];
let shuttingDown = false;

// shell:true is mandatory in modern Node to resolve .cmd/.bat executables
// (npm/mvn) and avoid the EINVAL error. Commands are static, with no user
// input, so we build the line as a string (avoids the DEP0190 warning).
function run(command, args, cwd = ROOT) {
  const commandLine = `${command} ${args.join(" ")}`;
  const child = spawn(commandLine, { cwd, stdio: "inherit", shell: true });
  children.push(child);
  return child;
}

function runAndWait(command, args, cwd = ROOT) {
  return new Promise((resolve, reject) => {
    const child = run(command, args, cwd);
    child.on("error", (err) => reject(err));
    child.on("exit", (code) => {
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(`${command} exited with code ${code}`));
      }
    });
  });
}

// Waits for Jetty to answer before opening the BrowserSync proxy
// (avoids the 502 window at http://localhost:3000 during boot).
async function waitForJetty(url, timeoutMs = 90_000) {
  const end = Date.now() + timeoutMs;
  while (Date.now() < end) {
    try {
      const res = await fetch(url);
      if (res.ok) {
        return;
      }
    } catch {
      // Jetty is not up yet
    }
    await new Promise((r) => setTimeout(r, 1500));
  }
  console.warn("[dev] Jetty did not answer in time; BrowserSync may show 502 until it starts.");
}

function stopMysql() {
  console.log("\n==> Stopping MySQL (docker compose stop mysql)");
  return new Promise((resolve) => {
    const stop = spawn("docker compose stop mysql", { cwd: ROOT, stdio: "inherit", shell: true });
    stop.on("error", resolve);
    stop.on("exit", resolve);
  });
}

async function stopChildren() {
  for (const child of children) {
    if (child.exitCode === null && !child.killed) {
      try {
        child.kill("SIGTERM");
      } catch {
        // already finished
      }
    }
  }
}

async function shutdown(code = 130) {
  if (shuttingDown) {
    return;
  }
  shuttingDown = true;
  await stopChildren();
  await stopMysql();
  process.exit(code);
}

process.on("SIGINT", () => shutdown(130));
process.on("SIGTERM", () => shutdown(143));

main().catch((error) => {
  console.error(`\n[dev] Error: ${error.message}`);
  shutdown(1);
});

async function main() {
  console.log("==> 1/5 MySQL (docker compose up -d mysql)");
  await runAndWait("docker", ["compose", "up", "-d", "mysql"]);

  if (!existsSync(path.join(FRONTEND, "node_modules"))) {
    console.log("==> 2/5 Installing frontend dependencies (npm ci)");
    await runAndWait(NPM, ["ci"], FRONTEND);
  } else {
    console.log("==> 2/5 Frontend dependencies OK (node_modules present)");
  }

  console.log("==> 3/5 Initial CSS (npm run build)");
  await runAndWait(NPM, ["run", "build"], FRONTEND);

  console.log("==> 4/5 Live style watcher (vite build --watch)");
  const watch = run(NPM, ["run", "build", "--", "--watch"], FRONTEND);

  console.log("==> 5/5 Jetty + BrowserSync (auto browser reload)");
  const jetty = run(MVN, ["jetty:run"], ROOT);

  jetty.on("exit", (code) => {
    if (!shuttingDown) {
      console.log(`\n[dev] Jetty exited (code ${code}). Shutting everything down...`);
      shutdown(code ?? 1);
    }
  });

  await waitForJetty("http://localhost:8080/spring");

  const sync = run(
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

  console.log("\n[dev] Everything is up:");
  console.log("[dev]   Browser with auto-reload → http://localhost:3000/spring");
  console.log("[dev]   Jetty direct             → http://localhost:8080/spring");
  console.log("[dev] Edit CSS or templates and the browser updates itself.");
  console.log("[dev] Ctrl+C shuts everything down (watch, Jetty, BrowserSync and MySQL).\n");
}