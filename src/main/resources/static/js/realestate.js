/**
 * 부동산 탭 전용 AJAX + Chart.js 로딩 로직
 */

let currentAreaType = 'UA04';

const REGION_CODES = {
    gangnam:  ['11680', '11650', '11710'],
    gangdong: ['11740', '11350', '11200'],
    gangseo:  ['11500', '11560', '11470'],
    gangbuk:  ['11110', '11440', '11170']
};

function loadKbIndexChart() {
    fetch('/krEconoMon/api/real-estate/kb-index')
        .then(res => {
            if (!res.ok) throw new Error('KB지수 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => {
            const container = document.getElementById('chart-kb-index');
            if (!container) return;
            container.innerHTML = '<canvas id="kbIndexChart" height="100"></canvas>';
            createLineChart('kbIndexChart', data);
        })
        .catch(err => {
            const container = document.getElementById('chart-kb-index');
            if (container) container.innerHTML = KrEconoMon.errorMessage('KB지수 데이터를 불러올 수 없습니다.');
            console.error(err);
        });
}

function buildTop5Table(rows, priceLabel) {
    if (!rows || rows.length === 0) {
        return '<p class="text-muted text-center py-3">데이터가 없습니다.</p>';
    }
    const header = `
        <table class="table table-sm table-hover mb-0">
          <thead class="table-light">
            <tr>
              <th>아파트</th>
              <th>동/연도</th>
              <th>${priceLabel} (만원)</th>
              <th>건수</th>
            </tr>
          </thead>
          <tbody>`;
    const rows_html = rows.map(r => `
            <tr>
              <td class="fw-semibold">${r.aptName || '-'}</td>
              <td class="text-muted small">${r.dongName || '-'}<br>${r.buildYear || '-'}년</td>
              <td>
                <div class="text-danger small">최고 ${(r.maxPrice || 0).toLocaleString()}</div>
                <div class="fw-bold">${(r.avgPrice || 0).toLocaleString()}</div>
                <div class="text-primary small">최저 ${(r.minPrice || 0).toLocaleString()}</div>
              </td>
              <td>${r.dealCount || 0}건</td>
            </tr>`).join('');
    return header + rows_html + '</tbody></table>';
}

function loadTop5Tables(regionId, sigunguCodes) {
    const areaType = currentAreaType;
    const codesParam = sigunguCodes.join(',');

    const tradeEl = document.getElementById('table-top5-trade-' + regionId);
    const leaseEl = document.getElementById('table-top5-lease-' + regionId);

    if (tradeEl) {
        tradeEl.innerHTML = KrEconoMon.loadingSpinner();
        fetch(`/krEconoMon/api/real-estate/top5/trade?codes=${codesParam}&areaType=${areaType}`)
            .then(res => res.json())
            .then(rows => { tradeEl.innerHTML = buildTop5Table(rows, '평균매매가'); })
            .catch(() => { tradeEl.innerHTML = KrEconoMon.errorMessage('TOP10 데이터를 불러올 수 없습니다.'); });
    }

    if (leaseEl) {
        leaseEl.innerHTML = KrEconoMon.loadingSpinner();
        fetch(`/krEconoMon/api/real-estate/top5/lease?codes=${codesParam}&areaType=${areaType}`)
            .then(res => res.json())
            .then(rows => { leaseEl.innerHTML = buildTop5Table(rows, '평균전세가'); })
            .catch(() => { leaseEl.innerHTML = KrEconoMon.errorMessage('TOP10 데이터를 불러올 수 없습니다.'); });
    }
}

function loadRealEstateAnalysis() {
    const container = document.getElementById('realestate-analysis-text');
    if (!container) return;

    container.innerHTML = `<div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div> 분석 중...`;

    fetch('/krEconoMon/api/gemini/realestate-analysis')
        .then(res => res.json())
        .then(data => {
            if (data.status === 'ok' && data.text) {
                container.innerHTML = `
                    <p class="mb-1">${data.text.replace(/\n/g, '<br>')}</p>
                    <small class="text-muted">${data.cached ? '(캐시된 분석)' : '(방금 생성된 분석)'}</small>
                `;
            } else {
                container.innerHTML = KrEconoMon.errorMessage('AI 분석을 불러올 수 없습니다.');
            }
        })
        .catch(() => {
            container.innerHTML = KrEconoMon.errorMessage('AI 분석 연결에 실패했습니다.');
        });
}

function loadRealEstateCartoon() {
    const container = document.getElementById('realestate-cartoon-container');
    if (!container) return;

    container.innerHTML = `<div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div><span class="text-muted">AI 컷툰 생성 중...</span>`;

    fetch('/krEconoMon/api/gemini/realestate-cartoon')
        .then(res => res.json())
        .then(data => {
            if (data.status === 'ok' && data.imageData) {
                container.innerHTML = `
                    <img src="data:image/png;base64,${data.imageData}" alt="AI 부동산 컷툰"
                         class="img-fluid rounded" style="max-height: 400px;">
                    <div class="mt-1"><small class="text-muted">${data.cached ? '(캐시된 컷툰)' : '(방금 생성된 컷툰)'}</small></div>
                `;
            } else {
                container.innerHTML = `<p class="text-muted small">컷툰을 불러올 수 없습니다.</p>`;
            }
        })
        .catch(() => {
            container.innerHTML = `<p class="text-muted small">컷툰 생성에 실패했습니다.</p>`;
        });
}

function loadRegionCharts(regionId, sigunguCodes) {
    const codesParam = sigunguCodes.join(',');
    const areaType = currentAreaType;

    const tradeContainer = document.getElementById('chart-trade-' + regionId);
    if (tradeContainer) tradeContainer.innerHTML = KrEconoMon.loadingSpinner();

    fetch(`/krEconoMon/api/real-estate/price?region=${regionId}&areaType=${areaType}&codes=${codesParam}`)
        .then(res => res.json())
        .then(data => {
            if (tradeContainer) {
                tradeContainer.innerHTML = `<canvas id="chart-trade-canvas-${regionId}" height="120"></canvas>`;
                createLineChart(`chart-trade-canvas-${regionId}`, data);
            }
        })
        .catch(err => {
            if (tradeContainer) tradeContainer.innerHTML = KrEconoMon.errorMessage();
            console.error('매매가 차트 로드 실패:', err);
        });

    const leaseContainer = document.getElementById('chart-lease-' + regionId);
    if (leaseContainer) leaseContainer.innerHTML = KrEconoMon.loadingSpinner();

    fetch(`/krEconoMon/api/real-estate/lease?region=${regionId}&areaType=${areaType}&codes=${codesParam}`)
        .then(res => res.json())
        .then(data => {
            if (leaseContainer) {
                leaseContainer.innerHTML = `<canvas id="chart-lease-canvas-${regionId}" height="120"></canvas>`;
                createLineChart(`chart-lease-canvas-${regionId}`, data);
            }
        })
        .catch(err => {
            if (leaseContainer) leaseContainer.innerHTML = KrEconoMon.errorMessage();
            console.error('전세가 차트 로드 실패:', err);
        });

    loadTop5Tables(regionId, sigunguCodes);
}

document.addEventListener('DOMContentLoaded', () => {
    const realEstateTab = document.getElementById('realestate-tab');
    if (!realEstateTab) return;

    function initRealEstateTab() {
        loadKbIndexChart();
        loadRegionCharts('gangnam', REGION_CODES['gangnam']);
        loadRealEstateAnalysis();
        loadRealEstateCartoon();
    }

    realEstateTab.addEventListener('shown.bs.tab', initRealEstateTab);

    document.querySelectorAll('#regionTabs button[data-bs-toggle="pill"]').forEach(btn => {
        btn.addEventListener('shown.bs.tab', () => {
            const regionId = btn.getAttribute('data-region');
            if (regionId && REGION_CODES[regionId]) {
                loadRegionCharts(regionId, REGION_CODES[regionId]);
            }
        });
    });

    const areaTypeSelect = document.getElementById('areaTypeSelect');
    if (areaTypeSelect) {
        areaTypeSelect.addEventListener('change', (e) => {
            currentAreaType = e.target.value;
            const activeRegionBtn = document.querySelector('#regionTabs button.active[data-region]');
            if (activeRegionBtn) {
                const regionId = activeRegionBtn.getAttribute('data-region');
                loadRegionCharts(regionId, REGION_CODES[regionId]);
            }
        });
    }
});
