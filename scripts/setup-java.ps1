# Instala JDK 21 para Solvo-B2B (solo necesario una vez)
$ErrorActionPreference = "Stop"

Write-Host "=== Setup JDK 21 para Solvo-B2B ===" -ForegroundColor Cyan

if (Get-Command java -ErrorAction SilentlyContinue) {
    $version = & java -version 2>&1 | Select-Object -First 1
    Write-Host "Java ya disponible: $version" -ForegroundColor Green
    if ($env:JAVA_HOME) {
        Write-Host "JAVA_HOME=$env:JAVA_HOME"
    }
    exit 0
}

if (Get-Command winget -ErrorAction SilentlyContinue) {
    Write-Host "Instalando Eclipse Temurin 21 via winget..."
    winget install --id EclipseAdoptium.Temurin.21.JDK -e --accept-source-agreements --accept-package-agreements
    Write-Host "Reinicia Cursor tras la instalación para que JAVA_HOME esté disponible en los hooks."
    exit 0
}

Write-Host @"

No se encontró Java ni winget. Opciones manuales:

1. Descarga JDK 21 desde https://adoptium.net/
2. Instálalo y define JAVA_HOME (ejemplo PowerShell):

   [Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot", "User")

3. O extrae un JDK portable en: backend\.jdk\jdk-21\

Luego verifica:
   cd backend
   .\mvnw.cmd test

"@ -ForegroundColor Yellow
