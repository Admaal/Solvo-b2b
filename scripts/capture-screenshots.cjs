const path = require('path');
const { chromium } = require('playwright');

const baseUrl = process.env.SCREENSHOT_BASE_URL || 'http://localhost:4200';
const apiUrl = process.env.SCREENSHOT_API_URL || 'http://localhost:8080';

const root = path.join(__dirname, '..');
const outDir = path.join(root, 'docs', 'screenshots');

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage({ viewport: { width: 1280, height: 800 } });

  await page.goto(`${baseUrl}/auth/login`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(outDir, '01-login.png'), fullPage: true });

  await page.getByRole('button', { name: /Entrar como Gestor/i }).click();
  await page.waitForURL('**/tickets**');
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(outDir, '02-tickets-grid.png'), fullPage: true });

  const detalle = page.getByRole('button', { name: /Ver detalle/i }).first();
  if (await detalle.count()) {
    await detalle.click();
    await page.waitForURL('**/tickets/**');
    await page.waitForTimeout(2000);
    await page.screenshot({ path: path.join(outDir, '03-ticket-detalle.png'), fullPage: true });
  }

  await page.goto(`${apiUrl}/swagger-ui.html`);
  await page.waitForTimeout(2000);
  await page.screenshot({ path: path.join(outDir, '04-swagger.png'), fullPage: true });

  await browser.close();
  console.log('Capturas guardadas en', outDir);
})();
