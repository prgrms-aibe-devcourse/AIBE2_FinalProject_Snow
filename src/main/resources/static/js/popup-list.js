// 팝업 리스트 페이지 전용 모듈
class PopupListManager {
    constructor() {
        this.currentPage = 0;
        this.isFetching = false;
        this.hasMore = true;
        this.currentSortBy = 'latest';
        this.grid = null;
        this.loadingIndicator = null;
    }

    // 페이지 초기화
    async initialize() {
        try {
            await this.renderHTML();
            this.setupElements();
            this.setupEventListeners();
            await this.loadInitialData();
        } catch (error) {
            console.error('팝업 리스트 페이지 초기화 실패:', error);
            this.showError('페이지를 불러오는 중 오류가 발생했습니다.');
        }
    }

    // HTML 렌더링
    renderHTML() {
        document.getElementById('main-content').innerHTML = `
            <div class="announcement-banner">
                <span class="icon-speaker">🔊</span>
                <p>새로운 팝업스토어가 매주 업데이트됩니다!</p>
            </div>

            <div class="filter-tabs">
                <button class="tab-item active" data-sort="latest">All</button>
                <button class="tab-item" data-sort="featured">추천</button>
                <button class="tab-item" data-sort="popularity">인기 팝업</button>
                <button class="tab-item" data-sort="deadline">마감임박</button>
                <button class="tab-item" data-filter="region">지역/날짜</button>
            </div>

            <div id="popup-grid" class="popup-grid"></div>

            <div id="loading-indicator" class="loading-container" style="display: none;">
                <div class="loading"></div>
            </div>
        `;
    }

    // DOM 요소 설정
    setupElements() {
        this.grid = document.getElementById('popup-grid');
        this.loadingIndicator = document.getElementById('loading-indicator');
    }

    // 이벤트 리스너 설정
    setupEventListeners() {
        // 필터 탭 이벤트
        document.querySelector('.filter-tabs').addEventListener('click', (e) => {
            this.handleFilterClick(e);
        });

        this._onScroll = () => {
            if (this.isFetching || !this.hasMore) return;
            this.handlePageScroll();
        };
        window.addEventListener('scroll', this._onScroll, { passive: true });

        // 카드 클릭 위임
        this.grid.addEventListener('click', (e) => {
            const card = e.target.closest('.popup-card');
            if (card && card.dataset.id) {
                goToPopupDetail(card.dataset.id);
            }
        });
    }

    // 필터 클릭 처리
    handleFilterClick(e) {
        const selectedTab = e.target.closest('.tab-item');
        if (!selectedTab || this.isFetching) return;

        if (selectedTab.dataset.filter === 'region') {
            alert('지역/날짜 필터 기능은 준비 중입니다.');
            return;
        }

        // 활성 탭 변경
        document.querySelectorAll('.filter-tabs .tab-item').forEach(tab =>
            tab.classList.remove('active')
        );
        selectedTab.classList.add('active');

        // 필터 변경 및 새로 로드
        this.currentSortBy = selectedTab.dataset.sort || 'latest';
        this.resetAndLoad();
    }

    // 전체 페이지 스크롤 처리 (무한 스크롤)
    handlePageScroll() {
        // 페이지 하단에서 200px 이내일 때 다음 페이지 로드
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const windowHeight = window.innerHeight;
        const documentHeight = document.documentElement.scrollHeight;

        if (documentHeight - scrollTop - windowHeight < 200) {
            this.loadMore();
        }
    }

    // 팝업 카드 HTML 생성
    createPopupCard(popup) {
        const fallbackImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjNjY3ZWVhIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iMTYiIGZpbGw9IndoaXRlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+Tm8gSW1hZ2U8L3RleHQ+PC9zdmc+';
        const safeSrc = isSafeUrl(popup.mainImageUrl) ? popup.mainImageUrl : fallbackImage;

        return `
        <div class="popup-card" data-id="${popup.id}">
            <div class="card-image-wrapper">
                <img src="${safeSrc}" 
                     alt="${esc(popup.title)}" class="card-image" loading="lazy" decoding="async"
                     onerror="this.onerror=null; this.src='${fallbackImage}'">
            </div>
            <div class="card-content">
                <h3 class="card-title">${esc(popup.title)}</h3>
                <p class="card-info">${esc(popup.period)}</p>
                <p class="card-info location">${esc(popup.region || '장소 미정')}</p>
            </div>
        </div>
    `;
    }

    // 초기 데이터 로드
    async loadInitialData() {
        await this.fetchAndDisplayPopups(false);
    }

    // 더 많은 데이터 로드
    async loadMore() {
        await this.fetchAndDisplayPopups(true);
    }

    // 리셋 후 새로 로드
    async resetAndLoad() {
        this.currentPage = 0;
        this.hasMore = true;

        // 맨 위로 스크롤
        window.scrollTo({ top: 0, behavior: 'smooth' });

        await this.fetchAndDisplayPopups(false);
    }

    // 데이터 가져오기 및 표시 (수정된 부분)
    async fetchAndDisplayPopups(isLoadMore = false) {
        if (this.isFetching || !this.hasMore) return;

        this.isFetching = true;

        if (!isLoadMore) {
            this.grid.innerHTML = '';
            this.currentPage = 0;
            this.hasMore = true;
        }

        this.showLoading();

        try {
            const params = {
                page: this.currentPage,
                size: 10,
                sortBy: this.currentSortBy
            };

            const response = await apiService.getPopups(params);

            // API 응답 구조에 맞게 'popups' 필드 사용
            if (response.popups && response.popups.length > 0) {
                this.renderPopups(response.popups);
                this.currentPage++;
                this.hasMore = !response.last;
            } else {
                this.hasMore = false;
                if (!isLoadMore) {
                    this.showNoResults();
                }
            }
        } catch (error) {
            console.error('팝업 로드 실패:', error);
            if (!isLoadMore) {
                this.showError('팝업을 불러오는 중 오류가 발생했습니다.');
            }
        } finally {
            this.isFetching = false;
            this.hideLoading();
        }
    }

    // 팝업 목록 렌더링
    renderPopups(popups) {
        const cardsHTML = popups.map(popup => this.createPopupCard(popup)).join('');
        this.grid.insertAdjacentHTML('beforeend', cardsHTML);
    }

    // 로딩 표시
    showLoading() {
        this.loadingIndicator.style.display = 'flex';
    }

    // 로딩 숨기기
    hideLoading() {
        this.loadingIndicator.style.display = 'none';
    }

    // 결과 없음 표시
    showNoResults() {
        this.grid.innerHTML = '<p class="alert alert-info" style="grid-column: 1 / -1; text-align: center;">표시할 팝업이 없습니다.</p>';
    }

    // 에러 표시
    showError(message) {
        this.grid.innerHTML = `<p class="alert alert-error" style="grid-column: 1 / -1; text-align: center;">${message}</p>`;
    }

    // 컴포넌트 정리 (페이지 전환 시 호출)
    cleanup() {
        if (this._onScroll) {
            window.removeEventListener('scroll', this._onScroll);
            this._onScroll = null;
        }
    }
}

function esc(s) {
    return String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}
function isSafeUrl(url) {
    try { const u = new URL(url, window.location.origin); return u.protocol === 'http:' || u.protocol === 'https:'; } catch { return false; }
}

// 전역 인스턴스
window.PopupListManager = PopupListManager;