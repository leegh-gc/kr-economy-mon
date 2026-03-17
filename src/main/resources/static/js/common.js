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
     * Gemini 분석 텍스트를 HTML로 변환 (마크다운 기본 처리)
     * - **text** → <strong>text</strong>
     * - ## heading → <strong class="d-block mt-2">heading</strong>
     * - 빈 줄 → 단락 구분
     */
    renderAnalysis: function (text) {
        if (!text) return '';
        // HTML 특수문자 이스케이프
        let html = text
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
        // ## 헤딩 처리
        html = html.replace(/^#{1,3}\s*(.+)$/gm, '<strong class="d-block mt-2 mb-1">$1</strong>');
        // **bold** 처리
        html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
        // * 항목 처리 (* item)
        html = html.replace(/^\*\s+(.+)$/gm, '• $1');
        // 빈 줄 → 단락 구분
        html = html.replace(/\n{2,}/g, '</p><p class="mb-1">');
        // 나머지 개행
        html = html.replace(/\n/g, '<br>');
        return '<p class="mb-1">' + html + '</p>';
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
        maintainAspectRatio: false,
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
        maintainAspectRatio: false,
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

function showChartError(canvasId, msg) {
    const canvas = document.getElementById(canvasId);
    if (canvas && canvas.parentElement) {
        canvas.parentElement.innerHTML = KrEconoMon.errorMessage(msg);
    }
}

function loadInterestRateChart() {
    fetch('/krEconoMon/api/economy/interest-rate')
        .then(res => {
            if (!res.ok) throw new Error('금리 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => createLineChart('interestRateChart', data))
        .catch(err => { console.error(err); showChartError('interestRateChart', '금리 데이터를 불러올 수 없습니다.'); });
}

function loadGdpChart() {
    fetch('/krEconoMon/api/economy/gdp')
        .then(res => {
            if (!res.ok) throw new Error('GDP 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => createBarChart('gdpChart', data))
        .catch(err => { console.error('GDP 데이터 로드 실패:', err); showChartError('gdpChart', 'GDP 데이터를 불러올 수 없습니다.'); });
}

function loadGdpIncomeChart() {
    fetch('/krEconoMon/api/economy/gdp-income')
        .then(res => {
            if (!res.ok) throw new Error('1인당GDP 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => createLineChart('gdpIncomeChart', data))
        .catch(err => { console.error('1인당GDP 데이터 로드 실패:', err); showChartError('gdpIncomeChart', '1인당 GDP 데이터를 불러올 수 없습니다.'); });
}

function loadExchangeRateChart() {
    fetch('/krEconoMon/api/economy/exchange-rate')
        .then(res => {
            if (!res.ok) throw new Error('환율 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => {
            const options = {
                scales: {
                    y: { title: { display: true, text: '환율 (원)' } }
                }
            };
            createLineChart('exchangeRateChart', data, options);
        })
        .catch(err => { console.error('환율 데이터 로드 실패:', err); showChartError('exchangeRateChart', '환율 데이터를 불러올 수 없습니다.'); });
}

function loadPriceIndexChart() {
    fetch('/krEconoMon/api/economy/price-index')
        .then(res => {
            if (!res.ok) throw new Error('물가 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => createLineChart('priceIndexChart', data))
        .catch(err => { console.error('물가 데이터 로드 실패:', err); showChartError('priceIndexChart', '물가 데이터를 불러올 수 없습니다.'); });
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
        .catch(err => { console.error('무역 데이터 로드 실패:', err); showChartError('tradeChart', '무역 데이터를 불러올 수 없습니다.'); });
}

function loadEmploymentChart() {
    fetch('/krEconoMon/api/economy/employment')
        .then(res => {
            if (!res.ok) throw new Error('고용 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => {
            // yAxisID를 JS에서 명시 할당 (JSON 직렬화 타이밍 문제 방지)
            if (data.datasets?.[0]) data.datasets[0].yAxisID = 'y';   // 실업률
            if (data.datasets?.[1]) data.datasets[1].yAxisID = 'y1';  // 취업자수
            const options = {
                scales: {
                    x: { ticks: { maxTicksLimit: 12 } },
                    y: {
                        type: 'linear', display: true, position: 'left',
                        title: { display: true, text: '실업률 (%)' },
                        min: 0, suggestedMax: 8,
                        ticks: { callback: v => v.toFixed(1) + '%' }
                    },
                    y1: {
                        type: 'linear', display: true, position: 'right',
                        title: { display: true, text: '취업자수 (천명)' },
                        suggestedMin: 20000, suggestedMax: 32000,
                        grid: { drawOnChartArea: false },
                        ticks: { callback: v => v.toLocaleString() }
                    }
                }
            };
            createLineChart('employmentChart', data, options);
        })
        .catch(err => { console.error('고용 데이터 로드 실패:', err); showChartError('employmentChart', '고용 데이터를 불러올 수 없습니다.'); });
}

function loadLiquidityChart() {
    fetch('/krEconoMon/api/economy/liquidity')
        .then(res => {
            if (!res.ok) throw new Error('통화 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => createLineChart('liquidityChart', data))
        .catch(err => { console.error('통화 데이터 로드 실패:', err); showChartError('liquidityChart', '통화 데이터를 불러올 수 없습니다.'); });
}

function loadForexReserveChart() {
    fetch('/krEconoMon/api/economy/forex-reserve')
        .then(res => {
            if (!res.ok) throw new Error('외환보유액 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => createLineChart('forexReserveChart', data))
        .catch(err => { console.error('외환보유액 데이터 로드 실패:', err); showChartError('forexReserveChart', '외환보유액 데이터를 불러올 수 없습니다.'); });
}

function loadEconomyAnalysis() {
    const container = document.getElementById('economy-analysis-text');
    if (!container) return;

    container.innerHTML = `<div class="spinner-border spinner-border-sm text-primary me-2" role="status"></div> 분석 중...`;

    fetch('/krEconoMon/api/gemini/economy-analysis')
        .then(res => res.json())
        .then(data => {
            if (data.status === 'ok' && data.text) {
                const dateLabel = data.analysedAt
                    ? `<small class="text-muted d-block mb-2">🗓 분석일시: ${data.analysedAt}</small>`
                    : '';
                container.innerHTML = `
                    ${dateLabel}
                    <div class="analysis-text">${KrEconoMon.renderAnalysis(data.text)}</div>
                `;
            } else {
                container.innerHTML = KrEconoMon.errorMessage('AI 분석을 불러올 수 없습니다.');
            }
        })
        .catch(() => {
            container.innerHTML = KrEconoMon.errorMessage('AI 분석 연결에 실패했습니다.');
        });
}

function refreshEconomyAnalysis() {
    const refreshBtn = document.getElementById('btn-economy-refresh');
    if (refreshBtn) {
        refreshBtn.disabled = true;
        refreshBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status"></span>초기화 중...';
    }
    fetch('/krEconoMon/api/gemini/refresh', { method: 'POST' })
        .then(res => res.json())
        .then(() => loadEconomyAnalysis())
        .catch(err => {
            console.error('분석 새로고침 실패:', err);
            loadEconomyAnalysis();
        })
        .finally(() => {
            if (refreshBtn) {
                refreshBtn.disabled = false;
                refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise me-1"></i>새로고침';
            }
        });
}

function loadPopulationChart() {
    fetch('/krEconoMon/api/economy/population')
        .then(res => {
            if (!res.ok) throw new Error('인구 데이터 로드 실패: ' + res.status);
            return res.json();
        })
        .then(data => {
            // 고령인구비율(index=1)을 막대로 변경
            if (data.datasets && data.datasets[1]) {
                data.datasets[1].type = 'bar';
                data.datasets[1].backgroundColor = 'rgba(253, 126, 20, 0.5)';
                data.datasets[1].borderColor = '#fd7e14';
            }
            // 합계출산율(index=2)을 전용 y2 축으로 분리해 등락이 잘 보이게
            if (data.datasets && data.datasets[2]) {
                data.datasets[2].yAxisID = 'y2';
            }
            const options = {
                scales: {
                    y:  { type: 'linear', display: true, position: 'left',
                          title: { display: true, text: '총인구 (천명)' } },
                    y1: { type: 'linear', display: true, position: 'right',
                          title: { display: true, text: '고령인구비율 (%)' },
                          min: 0, max: 30,
                          grid: { drawOnChartArea: false } },
                    y2: { type: 'linear', display: true, position: 'right',
                          title: { display: true, text: '합계출산율' },
                          min: 0, max: 2.5,
                          grid: { drawOnChartArea: false },
                          ticks: { color: '#28a745' } }
                }
            };
            createLineChart('populationChart', data, options);
        })
        .catch(err => { console.error('인구 데이터 로드 실패:', err); showChartError('populationChart', '인구 데이터를 불러올 수 없습니다.'); });
}

function loadVisitorCount() {
    fetch('/krEconoMon/api/visitor')
        .then(res => res.json())
        .then(data => {
            const el = document.getElementById('visitor-count');
            if (el) {
                el.innerHTML = `<i class="bi bi-eye me-1"></i>오늘: ${data.today.toLocaleString()} | 누적: ${data.total.toLocaleString()}`;
            }
        })
        .catch(() => {});
}

document.addEventListener('DOMContentLoaded', () => {
    loadVisitorCount();
    const economyTab = document.getElementById('economy-tab');
    if (economyTab) {
        function loadAllEconomyCharts() {
            loadInterestRateChart();
            loadGdpChart();
            loadGdpIncomeChart();
            loadExchangeRateChart();
            loadPriceIndexChart();
            loadTradeChart();
            loadEmploymentChart();
            loadLiquidityChart();
            loadForexReserveChart();
            loadPopulationChart();
            loadEconomyAnalysis();
        }

        economyTab.addEventListener('shown.bs.tab', loadAllEconomyCharts);
        if (economyTab.classList.contains('active')) {
            loadAllEconomyCharts();
        }

        const refreshBtn = document.getElementById('btn-economy-refresh');
        if (refreshBtn) {
            refreshBtn.addEventListener('click', refreshEconomyAnalysis);
        }
    }
});
