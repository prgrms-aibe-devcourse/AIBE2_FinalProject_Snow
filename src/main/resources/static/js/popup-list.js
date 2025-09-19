// 팝업 리스트 페이지 전용 모듈
class PopupListManager {
    constructor() {
        this.currentPage = 0;
        this.isFetching = false;
        this.hasMore = true;

        // 필터 상태 관리
        this.currentFilterMode = 'latest'; // 'latest', 'featured', 'popularity', 'deadline', 'region-date'
        this.currentRegion = 'All';
        this.currentDateFilter = 'All'; // 'All', 'today', '7days', '14days'

        this.grid = null;
        this.loadingIndicator = null;
        this.regionDateFilterContainer = null;
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
                <button class="tab-item active" data-mode="latest">All</button>
                <button class="tab-item" data-mode="featured">추천</button>
                <button class="tab-item" data-mode="popularity">인기 팝업</button>
                <button class="tab-item" data-mode="deadline">마감임박</button>
                <button class="tab-item" data-mode="region-date">지역/날짜</button>
            </div>

            <div id="region-date-filters" class="region-date-filters" style="display: none;">
                <div class="sub-filter-section">
                    <h4 class="sub-filter-title">지역</h4>
                    <div class="sub-filter-tabs" id="region-filter-tabs">
                        <button class="sub-tab-item active" data-region="All">All</button>
                        <button class="sub-tab-item" data-region="서울">서울</button>
                        <button class="sub-tab-item" data-region="경기">경기</button>
                        <button class="sub-tab-item" data-region="인천">인천</button>
                        <button class="sub-tab-item" data-region="부산">부산</button>
                        <button class="sub-tab-item" data-region="대전">대전</button>
                    </div>
                </div>
                <div class="sub-filter-section">
                    <h4 class="sub-filter-title">날짜</h4>
                     <div class="sub-filter-tabs" id="date-filter-tabs">
                        <button class="sub-tab-item active" data-date="All">All</button>
                        <button class="sub-tab-item" data-date="today">오늘</button>
                        <button class="sub-tab-item" data-date="7days">+7</button>
                        <button class="sub-tab-item" data-date="14days">+14</button>
                        <button class="sub-tab-item" data-date="custom">직접입력</button>
                    </div>
                </div>
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
        this.regionDateFilterContainer = document.getElementById('region-date-filters');
    }

    // 이벤트 리스너 설정
    setupEventListeners() {
        // 메인 필터 탭 이벤트
        document.querySelector('.filter-tabs').addEventListener('click', (e) => {
            this.handleFilterClick(e);
        });

        // 지역/날짜 서브 필터 이벤트
        this.regionDateFilterContainer.addEventListener('click', (e) => {
            this.handleSubFilterClick(e);
        });

        // 무한 스크롤 이벤트
        this._onScroll = () => {
            if (this.isFetching || !this.hasMore) return;
            this.handlePageScroll();
        };
        window.addEventListener('scroll', this._onScroll, {passive: true});

        // 카드 클릭 위임
        this.grid.addEventListener('click', (e) => {
            const card = e.target.closest('.popup-card');
            if (card && card.dataset.id) {
                goToPopupDetail(card.dataset.id);
            }
        });

        // 이미지 로딩 실패 처리
        this.grid.addEventListener('error', (e) => {
            const img = e.target;
            if (img && img.matches('.card-image')) {
                img.onerror = null;
                img.src = img.dataset.fallbackSrc;
            }
        }, true);
    }

    // 메인 필터 클릭 처리
    handleFilterClick(e) {
        const selectedTab = e.target.closest('.tab-item');
        if (!selectedTab || this.isFetching) return;

        const newMode = selectedTab.dataset.mode;
        if (this.currentFilterMode === newMode && newMode !== 'region-date') return;

        // 활성 탭 UI 변경
        document.querySelectorAll('.filter-tabs .tab-item').forEach(tab =>
            tab.classList.remove('active')
        );
        selectedTab.classList.add('active');

        this.currentFilterMode = newMode;

        if (newMode === 'region-date') {
            // 지역/날짜 필터 섹션 토글
            const isVisible = this.regionDateFilterContainer.style.display === 'block';
            this.regionDateFilterContainer.style.display = isVisible ? 'none' : 'block';
        } else {
            // 다른 탭 선택 시 지역/날짜 필터 숨김
            this.regionDateFilterContainer.style.display = 'none';
            this.resetAndLoad();
        }
    }

    // 서브 필터 클릭 처리 (지역, 날짜)
    handleSubFilterClick(e) {
        const selectedSubTab = e.target.closest('.sub-tab-item');
        if (!selectedSubTab || this.isFetching) return;

        const region = selectedSubTab.dataset.region;
        const date = selectedSubTab.dataset.date;

        if (region) {
            this.currentRegion = region;
            document.querySelectorAll('#region-filter-tabs .sub-tab-item').forEach(tab =>
                tab.classList.toggle('active', tab.dataset.region === region)
            );
        }

        if (date) {
            if (date === 'custom') {
                alert('직접 입력 기능은 준비 중입니다.');
                return;
            }
            this.currentDateFilter = date;
            document.querySelectorAll('#date-filter-tabs .sub-tab-item').forEach(tab =>
                tab.classList.toggle('active', tab.dataset.date === date)
            );
        }

        this.resetAndLoad();
    }

    // 전체 페이지 스크롤 처리 (무한 스크롤)
    handlePageScroll() {
        const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
        if (scrollHeight - scrollTop - clientHeight < 200) {
            this.loadMore();
        }
    }

    // 팝업 카드 HTML 생성
    createPopupCard(popup) {
        const fallbackImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjNjY3ZWVhIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iMTYiIGZpbGw9IndoaXRlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+Tm8gSW1hZ2U8L3RleHQ+PC9zdmc+';
        const safeSrc = isSafeUrl(popup.mainImageUrl) ? popup.mainImageUrl : fallbackImage;
        const popupId = encodeURIComponent(String(popup?.id ?? ''));

        return `
            <div class="popup-card" data-id="${popupId}">
                <div class="card-image-wrapper">
                    <img src="${safeSrc}"
                         data-fallback-src="${fallbackImage}"
                         alt="${esc(popup.title)}"
                         class="card-image"
                         loading="lazy"
                         decoding="async">
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
        window.scrollTo({ top: 0, behavior: 'smooth' });
        await this.fetchAndDisplayPopups(false);
    }

    // 데이터 가져오기 및 표시
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
                size: 10
            };
            let response;

            switch (this.currentFilterMode) {
                case 'latest':
                    response = await apiService.getPopups(params);
                    break;
                case 'featured': // 백엔드에서 AI 추천이 인기팝업으로 연결되어 있음
                    response = await apiService.getAIRecommendedPopups({ ...params, status: 'ONGOING' });
                    break;
                case 'popularity':
                    response = await apiService.getPopularPopups(params);
                    break;
                case 'deadline':
                    response = await apiService.getDeadlineSoonPopups(params);
                    break;
                case 'region-date':
                    const regionDateParams = { ...params };
                    if (this.currentRegion !== 'All') {
                        regionDateParams.region = this.currentRegion;
                    }
                    if (this.currentDateFilter !== 'All') {
                        regionDateParams.dateFilter = this.currentDateFilter;
                    }
                    response = await apiService.getPopupsByRegionAndDate(regionDateParams);
                    break;
                default:
                    response = await apiService.getPopups(params);
            }

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

    showLoading() { this.loadingIndicator.style.display = 'flex'; }
    hideLoading() { this.loadingIndicator.style.display = 'none'; }
    showNoResults() { this.grid.innerHTML = '<p class="alert alert-info" style="grid-column: 1 / -1; text-align: center;">표시할 팝업이 없습니다.</p>'; }
    showError(message) { this.grid.innerHTML = `<p class="alert alert-error" style="grid-column: 1 / -1; text-align: center;">${message}</p>`; }

    // 컴포넌트 정리 (페이지 전환 시 호출)
    cleanup() {
        if (this._onScroll) {
            window.removeEventListener('scroll', this._onScroll);
            this._onScroll = null;
        }
    }
}

function esc(s) { return String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }
function isSafeUrl(url) { try { const u = new URL(url, window.location.origin); return u.protocol === 'http:' || u.protocol === 'https:'; } catch { return false; } }

window.PopupListManager = PopupListManager;