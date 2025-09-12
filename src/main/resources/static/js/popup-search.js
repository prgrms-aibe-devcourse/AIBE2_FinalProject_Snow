// 팝업 검색 페이지 전용 모듈 (수정된 버전)
class PopupSearchManager {
    constructor() {
        this.searchInput = null;
        this.searchButton = null;
        this.relatedSearches = null;
        this.searchResults = null;
        this.searchLoading = null;
        this.currentQuery = '';
        this.isSearching = false;
        this.selectedIndex = -1;
        this.autocompleteItems = [];
        this.allSuggestions = [];
        this.debounceTimeout = null;
    }

    // 페이지 초기화
    async initialize() {
        try {
            // HTML이 이미 있는지 확인
            if (!this.checkExistingHTML()) {
                await this.renderHTML();
            }

            this.setupElements();
            this.loadSuggestionData();
            this.setupEventListeners();
            this.hideAllResults();
        } catch (error) {
            console.error('팝업 검색 페이지 초기화 실패:', error);
            this.showError('페이지를 불러오는 중 오류가 발생했습니다.');
        }
    }

    // 기존 HTML 확인
    checkExistingHTML() {
        const searchInput = document.getElementById('popup-search-input');
        const searchContainer = document.querySelector('.popup-search-container');
        return searchInput && searchContainer;
    }

    // HTML 렌더링 (필요할 때만)
    async renderHTML() {
        try {
            const html = await TemplateLoader.load('pages/popup-search');
            document.getElementById('main-content').innerHTML = html;
        } catch (error) {
            console.warn('템플릿 로드 실패, 폴백 HTML 사용:', error);
            document.getElementById('main-content').innerHTML = `
                <div class="popup-search-container">
                    <div class="search-area">
                        <div class="search-input-wrapper">
                            <input type="text" id="popup-search-input" class="search-input" placeholder="검색어를 입력하세요" autocomplete="off">
                            <button class="search-button" id="popup-search-button">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <circle cx="11" cy="11" r="8"></circle>
                                    <path d="m21 21-4.35-4.35"></path>
                                </svg>
                            </button>
                        </div>
                        <div class="related-searches" id="popup-related-searches"></div>
                    </div>
                    <div id="popup-search-results" class="search-results"></div>
                    <div id="popup-search-loading" class="loading-container" style="display: none;">
                        <div class="loading"></div>
                    </div>
                </div>`;
        }
    }

    // DOM 요소 설정
    setupElements() {
        this.searchInput = document.getElementById('popup-search-input');
        this.searchButton = document.getElementById('popup-search-button');
        this.relatedSearches = document.getElementById('popup-related-searches');
        this.searchResults = document.getElementById('popup-search-results');
        this.searchLoading = document.getElementById('popup-search-loading');

        // 요소가 없으면 오류 발생
        if (!this.searchInput || !this.searchButton) {
            throw new Error('필수 DOM 요소를 찾을 수 없습니다.');
        }
    }

    // 검색 제안 데이터 로드
    loadSuggestionData() {
        // TODO: 추후 API를 통해 동적으로 가져올 수 있도록 수정
        this.allSuggestions = [
            '브런치 팝업', '브런치 카페', '브런치 스토어', '캐릭터 굿즈', '캐릭터 팝업스토어',
            '크레용신짱', '크레용신짱 팝업', '체험형 팝업', '체험 이벤트', '한정판 상품',
            '한정판 굿즈', '케이팝 팝업', 'K-POP 스토어', 'BTS 팝업', '포켓몬 센터',
            '포켓몬 굿즈', '디즈니 팝업', '디즈니 프린세스', '지브리 카페', '토토로 팝업',
            '마블 히어로즈', '아이언맨 팝업', '카카오프렌즈', '라이언 팝업', '팝업스토어'
        ];
    }

    // 이벤트 리스너 설정
    setupEventListeners() {
        this.searchButton.addEventListener('click', () => this.performSearch());

        this.searchInput.addEventListener('keydown', this.handleKeyDown.bind(this));

        this.searchInput.addEventListener('input', this.handleInput.bind(this));

        this.searchInput.addEventListener('focus', () => {
            if (this.searchInput.value.trim().length > 0) {
                this.showAutocomplete();
            }
        });

        document.addEventListener('click', (e) => {
            if (!e.target.closest('.search-area')) {
                this.hideAutocomplete();
            }
        });

        if (this.relatedSearches) {
            this.relatedSearches.addEventListener('click', (e) => {
                const item = e.target.closest('.autocomplete-item');
                if (item) {
                    this.searchInput.value = item.textContent.trim();
                    this.performSearch();
                }
            });
        }
    }

    // 입력 처리를 위한 디바운싱 핸들러
    handleInput() {
        clearTimeout(this.debounceTimeout);
        this.debounceTimeout = setTimeout(() => {
            const query = this.searchInput.value.trim();
            if (query) {
                this.showAutocomplete();
                this.hideSearchResults();
            } else {
                this.hideAutocomplete();
            }
        }, 300); // 300ms 지연
    }

    // 키보드 이벤트
    handleKeyDown(e) {
        const isAutocompleteVisible = this.relatedSearches && this.relatedSearches.classList.contains('show');

        switch (e.key) {
            case 'Enter':
                e.preventDefault();
                if (this.selectedIndex > -1 && this.autocompleteItems[this.selectedIndex]) {
                    this.searchInput.value = this.autocompleteItems[this.selectedIndex].textContent.trim();
                }
                this.performSearch();
                break;
            case 'ArrowDown':
                if (isAutocompleteVisible) {
                    e.preventDefault();
                    this.navigateAutocomplete(1);
                }
                break;
            case 'ArrowUp':
                if (isAutocompleteVisible) {
                    e.preventDefault();
                    this.navigateAutocomplete(-1);
                }
                break;
            case 'Escape':
                this.hideAutocomplete();
                break;
        }
    }

    // 자동완성 표시
    showAutocomplete() {
        if (!this.relatedSearches) return;

        const query = this.searchInput.value.trim();
        const filteredSuggestions = this.allSuggestions
            .filter(suggestion => suggestion.toLowerCase().includes(query.toLowerCase()))
            .slice(0, 8);

        if (filteredSuggestions.length === 0) {
            this.hideAutocomplete();
            return;
        }

        this.searchInput.closest('.search-input-wrapper').classList.add('autocomplete-active');
        this.searchInput.closest('.search-area').classList.add('active');

        this.relatedSearches.innerHTML = filteredSuggestions.map(suggestion => {
            const highlightedText = this.highlightText(suggestion, query);
            return `
                <div class="autocomplete-item">
                    <svg class="autocomplete-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="11" cy="11" r="8"></circle>
                        <path d="m21 21-4.35-4.35"></path>
                    </svg>
                    <div class="autocomplete-text">${highlightedText}</div>
                </div>`;
        }).join('');

        this.autocompleteItems = this.relatedSearches.querySelectorAll('.autocomplete-item');
        this.relatedSearches.classList.add('show');
        this.selectedIndex = -1;
    }

    // 텍스트 하이라이트
    highlightText(text, query) {
        if (!query) return text;
        const regex = new RegExp(`(${query.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
        return text.replace(regex, '<span class="highlight">$1</span>');
    }

    // 키보드로 자동완성 네비게이션
    navigateAutocomplete(direction) {
        if (this.autocompleteItems.length === 0) return;

        this.autocompleteItems[this.selectedIndex]?.classList.remove('selected');
        this.selectedIndex = (this.selectedIndex + direction + this.autocompleteItems.length) % this.autocompleteItems.length;
        this.autocompleteItems[this.selectedIndex].classList.add('selected');
        this.autocompleteItems[this.selectedIndex].scrollIntoView({ block: 'nearest' });
    }

    // 검색 수행
    async performSearch() {
        const searchQuery = this.searchInput.value.trim();
        if (!searchQuery || this.isSearching) return;

        this.currentQuery = searchQuery;
        this.isSearching = true;

        this.hideAutocomplete();
        this.showLoading();
        this.hideSearchResults();

        try {
            const params = { query: searchQuery, page: 0, size: 20 };
            const response = await apiService.searchPopups(params);
            this.displaySearchResults(response);
        } catch (error) {
            console.error('팝업 검색 실패:', error);
            this.showSearchError();
        } finally {
            this.isSearching = false;
            this.hideLoading();
        }
    }

    // 검색 결과 표시
    displaySearchResults(response) {
        if (!this.searchResults) return;

        if (!response || !response.popups || response.popups.length === 0) {
            this.showNoResults();
            return;
        }

        this.searchResults.innerHTML = `
            <div class="search-results-title">'${this.currentQuery}' 검색 결과 (${response.totalElements}개)</div>
            <div class="search-results-grid">
                ${response.popups.map(popup => this.createSearchResultCard(popup)).join('')}
            </div>`;
        this.showSearchResults();
    }

    // 검색 결과 카드 생성
    createSearchResultCard(popup) {
        const startDate = popup.startDate?.replaceAll('-', '.') || '미정';
        const endDate = popup.endDate?.replaceAll('-', '.') || '미정';
        return `
            <div class="popup-card" onclick="goToPopupDetail('${popup.id}')">
                <div class="card-image-wrapper">
                    <img src="${popup.thumbnailUrl || 'https://via.placeholder.com/150'}" 
                         alt="${popup.title}" class="card-image" 
                         onerror="this.src='https://via.placeholder.com/150'">
                </div>
                <div class="card-content">
                    <h3 class="card-title">${popup.title}</h3>
                    <p class="card-info">${startDate} ~ ${endDate}</p>
                    <p class="card-info location">${popup.region || popup.location || '장소 미정'}</p>
                </div>
            </div>`;
    }

    // 결과 없음 표시
    showNoResults() {
        if (!this.searchResults) return;

        const safeQuery = this.escapeHtml(this.currentQuery);

        this.searchResults.innerHTML = `
            <div class="no-results">
                <svg class="no-results-icon" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8"></circle><path d="m21 21-4.35-4.35"></path></svg>
                <div class="no-results-title">'${safeQuery}'에 대한 검색 결과가 없습니다</div>
                <div class="no-results-desc">다른 검색어를 시도해보세요</div>
            </div>`;
        this.showSearchResults();
    }

    // 검색 오류 표시
    showSearchError() {
        if (!this.searchResults) return;

        this.searchResults.innerHTML = `
            <div class="no-results">
                <div class="no-results-title">검색 중 오류가 발생했습니다</div>
                <div class="no-results-desc">잠시 후 다시 시도해주세요</div>
            </div>`;
        this.showSearchResults();
    }

    // UI 상태 관리
    hideAutocomplete() {
        if (!this.relatedSearches) return;

        this.relatedSearches.classList.remove('show');
        this.selectedIndex = -1;

        const wrapper = this.searchInput.closest('.search-input-wrapper');
        const area = this.searchInput.closest('.search-area');
        if (wrapper) wrapper.classList.remove('autocomplete-active');
        if (area) area.classList.remove('active');
    }

    showSearchResults() {
        if (this.searchResults) this.searchResults.classList.add('show');
    }

    hideSearchResults() {
        if (this.searchResults) this.searchResults.classList.remove('show');
    }

    hideAllResults() {
        this.hideAutocomplete();
        this.hideSearchResults();
    }

    showLoading() {
        if (this.searchLoading) this.searchLoading.style.display = 'flex';
    }

    hideLoading() {
        if (this.searchLoading) this.searchLoading.style.display = 'none';
    }

    showError(message) {
        const mainContent = document.getElementById('main-content');
        if (mainContent) {
            const div = document.createElement('div');
            div.className = 'alert alert-error';
            div.textContent = String(message);
            mainContent.innerHTML = '';
            mainContent.appendChild(div);
        }
    }
}

// 전역 인스턴스
window.PopupSearchManager = PopupSearchManager;