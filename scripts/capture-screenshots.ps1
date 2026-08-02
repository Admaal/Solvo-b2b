# Captura screenshots para README (requiere app en http://localhost:4200)
param(
    [string]$BaseUrl = "http://localhost:4200"
)

$ErrorActionPreference = "Stop"
$OutDir = Join-Path (Split-Path $PSScriptRoot -Parent) "docs\screenshots"
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

$shots = @(
    @{ Path = "01-login.png"; Url = "$BaseUrl/auth/login" },
    @{ Path = "02-tickets-grid.png"; Url = "$BaseUrl/tickets" },
    @{ Path = "03-ticket-detalle.png"; Url = "$BaseUrl/tickets" },
    @{ Path = "04-swagger.png"; Url = "http://localhost:8080/swagger-ui.html" }
)

Write-Host "Instalando playwright en scripts/ (una vez)..."
Push-Location $PSScriptRoot
if (-not (Test-Path "node_modules/playwright")) {
  npm install --omit=dev 2>$null
}
npx playwright install chromium 2>$null
node capture-screenshots.cjs
Pop-Location

Write-Host "Capturas guardadas en $OutDir"
