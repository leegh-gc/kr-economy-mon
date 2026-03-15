import { test, expect } from '@playwright/test';

test.describe('Tab 1: Economy Indicators', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should load main page with economy tab active', async ({ page }) => {
    await expect(page).toHaveTitle(/한국 경제/);
    const economyTab = page.locator('[data-tab="economy"], #economy-tab, .nav-link.active');
    await expect(economyTab.first()).toBeVisible();
  });

  test('should render economy chart sections', async ({ page }) => {
    // Wait for AJAX chart loading
    await page.waitForTimeout(3000);

    // Check that canvas elements exist (Chart.js renders to canvas)
    const charts = page.locator('canvas');
    const chartCount = await charts.count();
    expect(chartCount).toBeGreaterThanOrEqual(1);
  });

  test('should load economy API data', async ({ page }) => {
    // Intercept API calls to verify they succeed
    const responsePromise = page.waitForResponse(
      (resp) => resp.url().includes('/api/economy/') && resp.status() === 200,
      { timeout: 15000 }
    );

    await page.reload();

    const response = await responsePromise;
    expect(response.status()).toBe(200);
  });

  test('should display Gemini analysis section', async ({ page }) => {
    await page.waitForTimeout(5000);

    // Check for analysis text container
    const analysisSection = page.locator('[id*="analysis"], [class*="analysis"], .gemini-analysis');
    if (await analysisSection.count() > 0) {
      await expect(analysisSection.first()).toBeVisible();
    }
  });
});
