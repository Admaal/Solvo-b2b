const fs = require('fs');
const path = require('path');

if (process.env.VERCEL && !process.env.NG_APP_API_URL) {
  console.error('NG_APP_API_URL es obligatorio en Vercel (URL de Cloud Run, sin slash final).');
  process.exit(1);
}

const raw = process.env.NG_APP_API_URL || 'http://localhost:8080';
const origin = raw.replace(/\/$/, '');
const apiUrl = origin.endsWith('/api/v1') ? origin : `${origin}/api/v1`;
const file = path.join(__dirname, 'src', 'environments', 'environment.ts');

fs.writeFileSync(
  file,
  `export const environment = {\n  production: true,\n  apiUrl: '${apiUrl}',\n};\n`
);
console.log('API URL de producción:', apiUrl);
