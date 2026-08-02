# Resetea Postgres local y vuelve a levantar el contenedor (seed demo en próximo arranque del backend)
param(
    [switch]$NoStart
)

$ErrorActionPreference = "Stop"
Push-Location (Split-Path $PSScriptRoot -Parent)

Write-Host "Deteniendo contenedores y eliminando volumen helpdesk_pgdata..."
docker compose down -v

if (-not $NoStart) {
    Write-Host "Levantando Postgres..."
    docker compose up -d
    Write-Host "Listo. Arranca el backend para sembrar 9 tickets demo."
}

Pop-Location
