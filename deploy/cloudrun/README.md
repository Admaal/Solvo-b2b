# Cloud Run (no usado en la demo pública)

La demo pública de la **API** corre en **Render** (Docker + `/actuator/health`).
El frontend se sirve en **Vercel**. Postgres de prod está en **Supabase**.

Kubernetes (Helm) se usa bajo demanda en clúster local (`kind`) para demostrar manifiestos, no como hosting 24/7.

Guía de go-live: [docs/SETUP-GITHUB.md](../../docs/SETUP-GITHUB.md).
Blueprint: [render.yaml](../../render.yaml).
