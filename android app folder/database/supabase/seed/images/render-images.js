#!/usr/bin/env node
/**
 * Renders svg/*.svg to jpg/*.jpg (800x600) using headless Chrome/Edge, then compresses.
 *
 *   node generate-images.js && node render-images.js
 *
 * Needs Chrome or Edge installed (Windows paths below; set CHROME_PATH to override).
 */
const fs = require('fs');
const path = require('path');
const { execFileSync } = require('child_process');

const SVG = path.join(__dirname, 'svg'), PNG = path.join(__dirname, 'png'), JPG = path.join(__dirname, 'jpg');
[PNG, JPG].forEach(d => fs.mkdirSync(d, { recursive: true }));

const browser = [process.env.CHROME_PATH,
  'C:/Program Files/Google/Chrome/Application/chrome.exe',
  'C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe'].find(p => p && fs.existsSync(p));
if (!browser) { console.error('No Chrome/Edge found. Set CHROME_PATH.'); process.exit(1); }

for (const f of fs.readdirSync(SVG).filter(f => f.endsWith('.svg'))) {
  const name = f.replace('.svg', '');
  const html = path.join(PNG, `${name}.html`);
  fs.writeFileSync(html, `<!doctype html><html><body style="margin:0;background:#0b0908">${fs.readFileSync(path.join(SVG, f), 'utf8')}</body></html>`);
  execFileSync(browser, ['--headless=new', '--disable-gpu', '--hide-scrollbars', '--window-size=800,600',
    `--screenshot=${path.join(PNG, name + '.png')}`, 'file:///' + html.replace(/\\/g, '/')], { stdio: 'ignore', timeout: 60000 });
  fs.unlinkSync(html);
}

// PNG -> JPG (quality 86) with .NET System.Drawing (available on Windows PowerShell)
const ps = `Add-Type -AssemblyName System.Drawing
$enc = [System.Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() | Where-Object { $_.MimeType -eq 'image/jpeg' }
$p = New-Object System.Drawing.Imaging.EncoderParameters(1)
$p.Param[0] = New-Object System.Drawing.Imaging.EncoderParameter([System.Drawing.Imaging.Encoder]::Quality, [long]86)
Get-ChildItem '${PNG}' -Filter *.png | ForEach-Object {
  $img = [System.Drawing.Image]::FromFile($_.FullName)
  $img.Save((Join-Path '${JPG}' ($_.BaseName + '.jpg')), $enc, $p); $img.Dispose()
}`;
execFileSync('powershell', ['-NoProfile', '-Command', ps], { stdio: 'inherit' });
console.log('rendered', fs.readdirSync(JPG).length, 'images to', JPG);
