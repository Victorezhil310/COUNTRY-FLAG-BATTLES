const fs = require("fs");
const path = require("path");

const src = path.join(__dirname, "..", "index.html");
const dst = path.join(__dirname, "..", "android", "app", "src", "main", "assets", "index.html");
let html = fs.readFileSync(src, "utf8");
const before = html;

function sub(needle, replacement, label) {
  if (!html.includes(needle)) throw new Error("PATCH FAILED: could not find " + label);
  html = html.split(needle).join(replacement);
  console.log("ok:", label);
}

// 1. self-hosted Montserrat instead of the Google Fonts stylesheet
sub(
`<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@600;800;900&display=swap" rel="stylesheet">`,
`<style>@font-face{font-family:"Montserrat";font-style:normal;font-weight:100 900;font-display:swap;src:url("fonts/montserrat-var.woff2") format("woff2")}</style>`,
"local Montserrat @font-face");

// 2. flags come from assets/flags, so the app needs no INTERNET permission
sub(
`  const img = new Image(); img.crossOrigin = "anonymous";`,
`  const img = new Image();`,
"drop crossOrigin");
sub(
`  img.src = \`https://flagcdn.com/w160/\${ct.c}.png\`;`,
`  img.src = \`flags/\${ct.c}.png\`;`,
"local flag src");

// 3. Android TextToSpeech bridge, used when the Web Speech API has no voices
sub(
`const speechQueue = [];`,
`const NativeTts = window.AndroidTts || null;
function nativeTtsReady() { try { return !!(NativeTts && NativeTts.isReady()); } catch (e) { return false; } }
const speechQueue = [];`,
"native TTS helper");

sub(
`function processSpeechQueue() {
  if (activeSpeech || !speechQueue.length) return;
  const item = speechQueue.shift();`,
`function processSpeechQueue() {
  if (activeSpeech || !speechQueue.length) return;
  if (nativeTtsReady()) {
    const item = speechQueue.shift();
    let ok = false;
    try { ok = NativeTts.speak(item.text); } catch (e) { ok = false; }
    if (!ok) { processSpeechQueue(); if (item.cb) item.cb(); return; }
    activeSpeech = { native: true };
    setTimeout(() => { activeSpeech = null; processSpeechQueue(); if (item.cb) item.cb(); },
      Math.min(20000, 900 + item.text.length * 62));
    return;
  }
  const item = speechQueue.shift();`,
"native branch in speech queue");

sub(
`  if (!soundOn || !("speechSynthesis" in window)) {
    setTimeout(() => { if (cb) cb(); }, 1600 + text.length * 50);`,
`  if (!soundOn || (!("speechSynthesis" in window) && !nativeTtsReady())) {
    setTimeout(() => { if (cb) cb(); }, 1600 + text.length * 50);`,
"speak() gate");

sub(
`  if (!soundOn || !("speechSynthesis" in window)) return;
  const list = (emotion && EMOTION_CALLS[emotion]) || ELIMINATION_CALLS;`,
`  if (!soundOn || (!("speechSynthesis" in window) && !nativeTtsReady())) return;
  const list = (emotion && EMOTION_CALLS[emotion]) || ELIMINATION_CALLS;`,
"announceFlag() gate");

sub(
`  speechQueue.length = 0;
  if ("speechSynthesis" in window) speechSynthesis.cancel();
  activeSpeech = null;`,
`  speechQueue.length = 0;
  if ("speechSynthesis" in window) speechSynthesis.cancel();
  try { if (NativeTts) NativeTts.stop(); } catch (e) {}
  activeSpeech = null;`,
"winner speech reset");

sub(
`function togglePause() { paused = !paused; if ("speechSynthesis" in window) paused ? speechSynthesis.pause() : speechSynthesis.resume(); }`,
`function togglePause() {
  paused = !paused;
  if (paused) { try { if (NativeTts) NativeTts.stop(); } catch (e) {} }
  if ("speechSynthesis" in window) paused ? speechSynthesis.pause() : speechSynthesis.resume();
}`,
"pause stops native speech");

sub(
`  if (!soundOn && "speechSynthesis" in window) { speechQueue.length = 0; speechSynthesis.cancel(); activeSpeech = null; }`,
`  if (!soundOn) {
    try { if (NativeTts) NativeTts.stop(); } catch (e) {}
    if ("speechSynthesis" in window) { speechQueue.length = 0; speechSynthesis.cancel(); activeSpeech = null; }
  }`,
"sound-off stops native speech");

if (html === before) throw new Error("nothing changed");
fs.writeFileSync(dst, html);
console.log("\nwrote", dst, (html.length / 1024).toFixed(1) + " KB");
console.log("remaining remote URLs:", (html.match(/https?:\/\/[^"'`\s]+/g) || []).filter(u => !u.includes("www.w3.org")).join(" ") || "none");
