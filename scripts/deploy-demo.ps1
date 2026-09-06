# Despliegue de la API en Render. El frontend va en Vercel (NG_APP_API_URL).
# Este script no llama a ninguna nube: imprime el orden de go-live.
# Cuentas (Supabase, Render, Vercel) se crean en los paneles.

$ErrorActionPreference = "Continue"

Write-Host "Orden de go-live (detalle: docs/SETUP-GITHUB.md)"
Write-Host "  1. SQL Editor de Supabase: docs/supabase-schema.sql"
Write-Host "     Session pooler puerto 5432, no 6543."
Write-Host "  2. Render: Blueprint render.yaml o Web Service, rootDir=backend"
Write-Host "     Health: /actuator/health"
Write-Host "     Env: DATABASE_*, JWT_SECRET (>=32, no el default), CORS_ALLOWED_ORIGINS, HELPDESK_DEMO_DATA=true"
Write-Host "  3. Vercel: Root Directory=frontend, build env NG_APP_API_URL=https://TU-SERVICIO.onrender.com"
Write-Host "  4. GitHub variable DEMO_API_HEALTH_URL=https://TU-SERVICIO.onrender.com/actuator/health"
Write-Host "  5. Pega la URL de Vercel en README.md y docs/PORTFOLIO.md"
Write-Host ""
Write-Host "Seed: HELPDESK_DEMO_DATA=true solo si la BD está vacía (no duplica en redeploy)."
