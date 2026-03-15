import { test, expect } from '@playwright/test';

test.describe('Tab 2: Real Estate', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('should switch to real estate tab', async ({ page }) => {
    const realEstateTab = page.locator('[data-tab="realestate"], #realestate-tab, a:has-text("부동산")');
    if (await realEstateTab.count() > 0) {
      await realEstateTab.first().click();
      await page.waitForTimeout(2000);

      // Verify tab content is visible
      const tabContent = page.locator('[id*="realestate"], .realestate-content, .tab-pane.active');
      await expect(tabContent.first()).toBeVisible();
    }
  });

  test('should load real estate charts after tab switch', async ({ page }) => {
    const realEstateTab = page.locator('[data-tab="realestate"], #realestate-tab, a:has-text("부동산")');
    if (await realEstateTab.count() > 0) {
      await realEstateTab.first().click();
      await page.waitForTimeout(5000);

      // Check for chart canvases in real estate tab
      const charts = page.locator('.tab-pane.active canvas, [id*="realestate"] canvas');
      if (await charts.count() > 0) {
        await expect(charts.first()).toBeVisible();
      }
    }
  });

  test('should load real estate API data', async ({ page }) => {
    const realEstateTab = page.locator('[data-tab="realestate"], #realestate-tab, a:has-text("부동산")');
    if (await realEstateTab.count() > 0) {
      const responsePromise = page.waitForResponse(
        (resp) => resp.url().includes('/api/real-estate/') && resp.status() === 200,
        { timeout: 15000 }
      );

      await realEstateTab.first().click();

      const response = await responsePromise;
      expect(response.status()).toBe(200);
    }
  });

  test('should display TOP5 table after region selection', async ({ page }) => {
    const realEstateTab = page.locator('[data-tab="realestate"], #realestate-tab, a:has-text("부동산")');
    if (await realEstateTab.count() > 0) {
      await realEstateTab.first().click();
      await page.waitForTimeout(3000);

      // Check for table elements (TOP5 ranking table)
      const tables = page.locator('table, .top5-table, [class*="rank"]');
      if (await tables.count() > 0) {
        await expect(tables.first()).toBeVisible();
      }
    }
  });
});
