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
