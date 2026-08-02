# Levanta PostgreSQL para desarrollo local
param(
    [switch]$Down
)

$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent

Push-Location $root
try {
    if ($Down) {
        docker compose down
        Write-Host "PostgreSQL detenido." -ForegroundColor Green
    } else {
        docker compose up -d
        Write-Host "Esperando a que PostgreSQL esté listo..." -ForegroundColor Yellow
        $retries = 0
        while ($retries -lt 30) {
            $health = docker inspect --format='{{.State.Health.Status}}' helpdesk-postgres 2>$null
            if ($health -eq "healthy") {
                Write-Host "PostgreSQL listo en localhost:5432 (helpdesk/helpdesk)" -ForegroundColor Green
                exit 0
            }
            Start-Sleep -Seconds 1
            $retries++
        }
        Write-Host "Contenedor arrancado; verifica con: docker compose logs postgres" -ForegroundColor Yellow
    }
} finally {
    Pop-Location
}
