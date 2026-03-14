/**
 * 공통 유틸리티 함수
 * Chart.js AJAX 로딩 패턴 공통화
 */

const KrEconoMon = {

    /**
     * 로딩 스피너 HTML 반환
     */
    loadingSpinner: function () {
        return `<div class="chart-placeholder">
                    <div class="text-center">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩 중...</span>
                        </div>
                        <div class="mt-2 text-muted small">데이터 로딩 중...</div>
                    </div>
                </div>`;
    },

    /**
     * 에러 메시지 HTML 반환
     */
    errorMessage: function (msg) {
        return `<div class="chart-error">
                    <i class="text-danger">&#9888;</i>
                    <span>${msg || '데이터를 불러올 수 없습니다.'}</span>
                </div>`;
    },

    /**
     * AJAX GET 요청 후 콜백 실행
     * @param {string} url - API 엔드포인트
     * @param {Function} onSuccess - 성공 콜백(data)
     * @param {Function} onError - 실패 콜백(error)
     */
    fetchData: function (url, onSuccess, onError) {
        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.json();
            })
            .then(data => onSuccess(data))
            .catch(error => {
                console.error('API 호출 실패:', url, error);
                if (onError) onError(error);
            });
    }
};

// ============================================================
// Chart.js 유틸리티
// ============================================================

/**
 * 멀티 라인 차트 생성.
 * @param {string} canvasId - canvas 엘리먼트 ID
 * @param {Object} chartData - { labels: [], datasets: [] } (서버 응답과 동일 구조)
 * @param {Object} options - Chart.js options 오버라이드 (선택)
 */
function createLineChart(canvasId, chartData, options = {}) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    if (Chart.getChart(canvasId)) {
        Chart.getChart(canvasId).destroy();
    }

    const defaultOptions = {
        responsive: true,
        interaction: { mode: 'index', intersect: false },
        plugins: {
            legend: { position: 'top' },
            tooltip: { enabled: true }
        },
        scales: {
            x: { ticks: { maxTicksLimit: 12 } },
            y: { ticks: { callback: (v) => v.toLocaleString() } }
        }
    };

    new Chart(ctx, {
        type: 'line',
        data: chartData,
        options: Object.assign({}, defaultOptions, options)
    });
}

/**
 * 막대 차트 생성 (양수=파랑, 음수=빨강 자동 적용).
 * @param {string} canvasId
 * @param {Object} chartData - { labels: [], datasets: [] }
 * @param {Object} options
 */
function createBarChart(canvasId, chartData, options = {}) {
    const ctx = document.getElementById(canvasId);
    if (!ctx) return;

    if (Chart.getChart(canvasId)) {
        Chart.getChart(canvasId).destroy();
    }

    chartData.datasets.forEach(dataset => {
        dataset.backgroundColor = dataset.data.map(v =>
            v >= 0 ? 'rgba(0, 123, 255, 0.7)' : 'rgba(220, 53, 69, 0.7)'
        );
        dataset.borderColor = dataset.data.map(v =>
            v >= 0 ? 'rgba(0, 123, 255, 1)' : 'rgba(220, 53, 69, 1)'
        );
        dataset.borderWidth = 1;
    });

    const defaultOptions = {
        responsive: true,
        plugins: { legend: { position: 'top' } },
        scales: {
            y: { ticks: { callback: (v) => v.toFixed(1) + '%' } }
        }
    };

    new Chart(ctx, {
        type: 'bar',
        data: chartData,
        options: Object.assign({}, defaultOptions, options)
    });
}

// ============================================================
// 섹션별 데이터 로드 함수
// ============================================================

function loadInterestRateChart() {
    fetch('/krEconoMon/api/economy/interest-rate')
        .then(res => {
            if (!res.ok) throw new Error('금리 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => createLineChart('interestRateChart', data))
        .catch(err => console.error(err));
}

function loadGdpChart() {
    fetch('/krEconoMon/api/economy/gdp')
        .then(res => res.json())
        .then(data => createBarChart('gdpChart', data))
        .catch(err => console.error('GDP 데이터 로드 실패:', err));
}

function loadExchangeRateChart() {
    fetch('/krEconoMon/api/economy/exchange-rate')
        .then(res => res.json())
        .then(data => {
            const options = {
                scales: {
                    y: {
                        type: 'linear', display: true, position: 'left',
                        title: { display: true, text: 'USD/EUR/CNY (원)' }
                    },
                    y1: {
                        type: 'linear', display: true, position: 'right',
                        title: { display: true, text: 'JPY 100엔 (원)' },
                        grid: { drawOnChartArea: false }
                    }
                }
            };
            createLineChart('exchangeRateChart', data, options);
        })
        .catch(err => console.error('환율 데이터 로드 실패:', err));
}

function loadPriceIndexChart() {
    fetch('/krEconoMon/api/economy/price-index')
        .then(res => res.json())
        .then(data => createLineChart('priceIndexChart', data))
        .catch(err => console.error('물가 데이터 로드 실패:', err));
}

function loadTradeChart() {
    fetch('/krEconoMon/api/economy/trade')
        .then(res => {
            if (!res.ok) throw new Error('무역 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => {
            const currentAccountDataset = { ...data.datasets[0], type: 'bar' };
            const tradeData = {
                labels: data.labels,
                datasets: [currentAccountDataset, data.datasets[1], data.datasets[2]]
            };
            const options = {
                scales: {
                    y:  { type: 'linear', display: true, position: 'left',
                          title: { display: true, text: '경상수지 (백만달러)' } },
                    y1: { type: 'linear', display: true, position: 'right',
                          title: { display: true, text: '수출/수입 (백만달러)' },
                          grid: { drawOnChartArea: false } }
                }
            };
            createLineChart('tradeChart', tradeData, options);
        })
        .catch(err => console.error('무역 데이터 로드 실패:', err));
}

function loadEmploymentChart() {
    fetch('/krEconoMon/api/economy/employment')
        .then(res => {
            if (!res.ok) throw new Error('고용 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => {
            const options = {
                scales: {
                    y:  { type: 'linear', display: true, position: 'left',
                          title: { display: true, text: '실업률 (%)' } },
                    y1: { type: 'linear', display: true, position: 'right',
                          title: { display: true, text: '취업자수 (천명)' },
                          grid: { drawOnChartArea: false } }
                }
            };
            createLineChart('employmentChart', data, options);
        })
        .catch(err => console.error('고용 데이터 로드 실패:', err));
}

function loadLiquidityChart() {
    fetch('/krEconoMon/api/economy/liquidity')
        .then(res => {
            if (!res.ok) throw new Error('통화 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => {
            const options = {
                scales: {
                    y:  { type: 'linear', display: true, position: 'left',
                          title: { display: true, text: 'M2 (조원)' } },
                    y1: { type: 'linear', display: true, position: 'right',
                          title: { display: true, text: '외환보유액 (억달러)' },
                          grid: { drawOnChartArea: false } }
                }
            };
            createLineChart('liquidityChart', data, options);
        })
        .catch(err => console.error('통화 데이터 로드 실패:', err));
}

function loadPopulationChart() {
    fetch('/krEconoMon/api/economy/population')
        .then(res => {
            if (!res.ok) throw new Error('인구 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => {
            const options = {
                scales: {
                    y:  { type: 'linear', display: true, position: 'left',
                          title: { display: true, text: '인구 (천명)' } },
                    y1: { type: 'linear', display: true, position: 'right',
                          title: { display: true, text: '비율 / 출산율' },
                          grid: { drawOnChartArea: false } }
                }
            };
            createLineChart('populationChart', data, options);
        })
        .catch(err => console.error('인구 데이터 로드 실패:', err));
}

document.addEventListener('DOMContentLoaded', () => {
    const economyTab = document.getElementById('economy-tab');
    if (economyTab) {
        economyTab.addEventListener('shown.bs.tab', () => {
            loadInterestRateChart();
            loadGdpChart();
            loadExchangeRateChart();
            loadPriceIndexChart();
            loadTradeChart();
            loadEmploymentChart();
            loadLiquidityChart();
            loadPopulationChart();
        });
        if (economyTab.classList.contains('active')) {
            loadInterestRateChart();
            loadGdpChart();
            loadExchangeRateChart();
            loadPriceIndexChart();
            loadTradeChart();
            loadEmploymentChart();
            loadLiquidityChart();
            loadPopulationChart();
        }
    }
});
