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
}

document.addEventListener('DOMContentLoaded', () => {
    const realEstateTab = document.getElementById('realestate-tab');
    if (!realEstateTab) return;

    function initRealEstateTab() {
        loadKbIndexChart();
        loadRegionCharts('gangnam', REGION_CODES['gangnam']);
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
