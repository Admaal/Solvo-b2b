# Captura screenshots para README (requiere app en http://localhost:4200)
param(
    [string]$BaseUrl = "http://localhost:4200"
)

$ErrorActionPreference = "Stop"
$OutDir = Join-Path (Split-Path $PSScriptRoot -Parent) "docs\screenshots"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

Write-Host "Instalando playwright en scripts/ (una vez)..."
Push-Location $PSScriptRoot
if (-not (Test-Path "node_modules/playwright")) {
  npm install --omit=dev 2>$null
}
npx playwright install chromium 2>$null
$env:SCREENSHOT_BASE_URL = $BaseUrl
node capture-screenshots.cjs
Remove-Item Env:SCREENSHOT_BASE_URL -ErrorAction SilentlyContinue
Pop-Location

Write-Host "Capturas guardadas en $OutDir"
