// 팝업 검색 페이지 전용 모듈
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
        this.debounceTimeout = null;
        this.autocompleteCache = new Map(); // 자동완성 캐시
        this.isLoadingSuggestions = false; // 자동완성 로딩 상태
        this.isShowingAlert = false; // alert 표시 중 플래그
    }

    // 페이지 초기화
    async initialize() {
        try {
            // HTML이 이미 있는지 확인
            if (!this.checkExistingHTML()) {
                await this.renderHTML();
            }

            this.setupElements();
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

    // HTML 렌더링
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

    // 이벤트 리스너 설정
    setupEventListeners() {
        // 버튼 클릭 이벤트 (중복 방지)
        this.searchButton.addEventListener('click', () => {
            if (this.isShowingAlert) return; // alert 표시 중이면 무시
            this.performSearch();
        });

        this.searchInput.addEventListener('keydown', this.handleKeyDown.bind(this));

        this.searchInput.addEventListener('input', this.handleInput.bind(this));

        this.searchInput.addEventListener('focus', () => {
            const query = this.searchInput.value.trim();
            if (query.length > 0) {
                this.loadAutocompleteSuggestions(query);
            }
        });

        this.searchInput.addEventListener('blur', (e) => {
            // 자동완성 항목 클릭을 위해 약간의 지연 추가
            setTimeout(() => {
                if (!e.relatedTarget || !e.relatedTarget.closest('.related-searches')) {
                    this.hideAutocomplete();
                }
            }, 150);
        });

        document.addEventListener('click', (e) => {
            if (!e.target.closest('.search-area')) {
                this.hideAutocomplete();
            }
        });

        // 자동완성 클릭 이벤트 (중복 방지)
        if (this.relatedSearches) {
            this.relatedSearches.addEventListener('click', (e) => {
                if (this.isShowingAlert) return; // alert 표시 중이면 무시

                const item = e.target.closest('.autocomplete-item');
                if (item) {
                    // data-suggestion 속성에서 정확한 텍스트 가져오기
                    const suggestionText = item.dataset.suggestion;
                    console.log('자동완성 클릭:', suggestionText); // 디버깅 로그
                    this.searchInput.value = suggestionText;
                    this.hideAutocomplete();

                    // 자동완성 선택시에는 길이 제한 없이 바로 검색
                    this.performSearchFromAutocomplete(suggestionText);
                }
            });
        }

        if (this.searchResults) {
            this.searchResults.addEventListener('click', (e) => {
                const card = e.target.closest('.popup-card');
                if (!card) return;
                const popupId = card.dataset.popupId;
                if (popupId) goToPopupDetail(popupId);
            });
        }
    }

    // 입력 처리를 위한 디바운싱 핸들러 (API 호출 버전)
    handleInput() {
        clearTimeout(this.debounceTimeout);
        this.debounceTimeout = setTimeout(() => {
            const query = this.searchInput.value.trim();
            if (query && query.length >= 1) {
                this.loadAutocompleteSuggestions(query);
            } else {
                this.hideAutocomplete();
                this.hideSearchResults();
            }
        }, 300); // 300ms 지연
    }

    // 서버에서 자동완성 제안 로드
    async loadAutocompleteSuggestions(query) {
        if (this.isLoadingSuggestions) return;

        try {
            this.isLoadingSuggestions = true;

            // 캐시 확인
            if (this.autocompleteCache.has(query)) {
                const cachedSuggestions = this.autocompleteCache.get(query);
                this.displayAutocompleteSuggestions(cachedSuggestions, query);
                return;
            }

            // API 호출
            const response = await apiService.getAutocompleteSuggestions(query);

            // 캐시에 저장 (최대 50개 항목)
            if (this.autocompleteCache.size >= 50) {
                const firstKey = this.autocompleteCache.keys().next().value;
                this.autocompleteCache.delete(firstKey);
            }
            this.autocompleteCache.set(query, response.suggestions);

            // 결과 표시
            this.displayAutocompleteSuggestions(response.suggestions, query);

        } catch (error) {
            console.error('자동완성 제안 로드 실패:', error);
            this.hideAutocomplete();
        } finally {
            this.isLoadingSuggestions = false;
        }
    }

    // 자동완성 제안 표시 (아이콘 제거, 단순화)
    displayAutocompleteSuggestions(suggestions, query) {
        if (!this.relatedSearches || !suggestions || suggestions.length === 0) {
            this.hideAutocomplete();
            return;
        }

        this.searchInput.closest('.search-input-wrapper').classList.add('autocomplete-active');
        this.searchInput.closest('.search-area').classList.add('active');

        this.relatedSearches.innerHTML = suggestions.map(suggestion => {
            return `
                <div class="autocomplete-item" data-suggestion="${this.escapeHtml(suggestion.text)}">
                    <svg class="autocomplete-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="11" cy="11" r="8"></circle>
                        <path d="m21 21-4.35-4.35"></path>
                    </svg>
                    <div class="autocomplete-text">${this.escapeHtml(suggestion.text)}</div>
                </div>`;
        }).join('');

        this.autocompleteItems = this.relatedSearches.querySelectorAll('.autocomplete-item');
        this.relatedSearches.classList.add('show');
        this.selectedIndex = -1;
    }

    // 키보드 이벤트 (중복 alert 방지)
    handleKeyDown(e) {
        console.log('키보드 이벤트:', e.key, '| alert 상태:', this.isShowingAlert); // 디버깅 로그

        // alert가 표시 중이면 이벤트 무시
        if (this.isShowingAlert) {
            console.log('alert 표시 중이므로 키보드 이벤트 무시');
            return;
        }

        const isAutocompleteVisible = this.relatedSearches && this.relatedSearches.classList.contains('show');

        switch (e.key) {
            case 'Enter':
                e.preventDefault();
                if (this.selectedIndex > -1 && this.autocompleteItems[this.selectedIndex]) {
                    // 자동완성에서 선택한 경우 - 길이 체크 없이 바로 검색
                    const suggestion = this.autocompleteItems[this.selectedIndex].dataset.suggestion;
                    console.log('자동완성 키보드 선택:', suggestion);
                    this.searchInput.value = suggestion;
                    this.hideAutocomplete();
                    this.performSearchFromAutocomplete(suggestion);
                } else {
                    // 직접 입력한 경우 - 길이 체크 수행
                    console.log('직접 입력 엔터 검색');
                    this.hideAutocomplete();
                    this.performSearch(); // 여기서 길이 체크가 수행됨
                }
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

    // 자동완성 숨김
    hideAutocomplete() {
        if (this.relatedSearches) {
            this.relatedSearches.classList.remove('show');
            this.searchInput.closest('.search-input-wrapper').classList.remove('autocomplete-active');
            this.searchInput.closest('.search-area').classList.remove('active');
        }
        this.autocompleteItems = [];
        this.selectedIndex = -1;
    }

    // 키보드로 자동완성 네비게이션
    navigateAutocomplete(direction) {
        if (this.autocompleteItems.length === 0) return;

        this.autocompleteItems[this.selectedIndex]?.classList.remove('selected');
        this.selectedIndex = (this.selectedIndex + direction + this.autocompleteItems.length) % this.autocompleteItems.length;
        this.autocompleteItems[this.selectedIndex].classList.add('selected');
        this.autocompleteItems[this.selectedIndex].scrollIntoView({ block: 'nearest' });
    }

    // 자동완성에서 선택했을 때의 검색 (길이 제한 없음, 중복 방지)
    async performSearchFromAutocomplete(searchQuery) {
        if (!searchQuery || this.isSearching || this.isShowingAlert) return;

        console.log('자동완성 검색 수행:', searchQuery);

        this.currentQuery = searchQuery;
        this.isSearching = true;

        this.hideAutocomplete();
        this.showLoading();
        this.hideSearchResults();

        try {
            const params = {
                query: searchQuery,
                page: 0,
                size: 20
            };
            const response = await apiService.searchPopups(params);
            this.displaySearchResults(response);
        } catch (error) {
            console.error('자동완성 검색 실패:', error);
            this.showSearchError();
        } finally {
            this.isSearching = false;
            this.hideLoading();
        }
    }

    // 검색 수행 (중복 alert 방지)
    async performSearch(searchParams = {}) {
        const searchQuery = searchParams.query || this.searchInput.value.trim();
        console.log('performSearch 호출 - 검색어:', searchQuery, '| alert 상태:', this.isShowingAlert);

        // 검색어 길이 체크 (최소 2글자)
        if (!searchQuery || searchQuery.length < 2) {
            console.log('검색어 길이 부족:', searchQuery);

            if (searchQuery.length === 1) {
                // alert 중복 방지
                if (this.isShowingAlert) {
                    console.log('이미 alert 표시 중 - 무시');
                    return;
                }

                console.log('alert 표시 시작');
                this.isShowingAlert = true;
                alert('검색어를 2글자 이상 입력해주세요.');

                // alert 닫힌 후 포커스 복원 및 상태 해제
                setTimeout(() => {
                    console.log('alert 상태 해제');
                    this.isShowingAlert = false;
                    this.searchInput.focus(); // 포커스 복원
                }, 200);
                return;
            } else if (!searchQuery) {
                // 빈 검색어일 때는 조용히 무시
                return;
            }
        }

        if (this.isSearching) return;

        console.log('정상 검색 수행:', searchQuery); // 디버깅 로그

        this.currentQuery = searchQuery;
        this.isSearching = true;

        // 검색 시작 시 자동완성 및 기타 UI 숨김
        this.hideAutocomplete();
        this.showLoading();
        this.hideSearchResults();

        try {
            const params = {
                query: searchQuery,
                page: searchParams.page || 0,
                size: searchParams.size || 20
            };
            console.log('검색 파라미터:', params); // 디버깅 로그
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

        // API 응답 구조에 맞게 'popups' 필드 사용
        if (!response || !response.popups || response.popups.length === 0) {
            this.showNoResults();
            return;
        }

        this.searchResults.innerHTML = `
            <div class="search-results-title">'${this.escapeHtml(this.currentQuery)}' 검색 결과 (${response.totalElements}개)</div>
            <div class="search-results-grid">
                ${response.popups.map(popup => this.createSearchResultCard(popup)).join('')}
            </div>`;
        this.showSearchResults();
    }

    // 검색 결과 카드 생성
    createSearchResultCard(popup) {
        const safeTitle = this.escapeHtml(popup?.title ?? '');
        const safeRegion = this.escapeHtml(popup?.region ?? '장소 미정');
        const safePeriod = this.escapeHtml(popup?.period ?? '');
        const imgSrc = this.escapeHtml(popup?.mainImageUrl || 'https://via.placeholder.com/150');
        const popupId = encodeURIComponent(String(popup?.id ?? ''));

        return `
            <div class="popup-card" data-popup-id="${popupId}">
                <div class="popup-image">
                    <img src="${imgSrc}" alt="${safeTitle}" loading="lazy">
                </div>
                <div class="popup-info">
                    <h3 class="popup-title">${safeTitle}</h3>
                    <p class="popup-region">${safeRegion}</p>
                    <p class="popup-period">${safePeriod}</p>
                </div>
            </div>`;
    }

    // 검색 결과 없음 표시
    showNoResults() {
        if (!this.searchResults) return;
        this.searchResults.innerHTML = `
            <div class="no-results">
                <div class="no-results-icon">🔍</div>
                <h3>검색 결과가 없습니다</h3>
                <p>'${this.escapeHtml(this.currentQuery)}'에 대한 검색 결과를 찾을 수 없습니다.</p>
                <p>다른 검색어로 다시 시도해보세요.</p>
            </div>`;
        this.showSearchResults();
    }

    // 검색 오류 표시
    showSearchError() {
        if (!this.searchResults) return;
        this.searchResults.innerHTML = `
            <div class="search-error">
                <div class="error-icon">⚠️</div>
                <h3>검색 중 오류가 발생했습니다</h3>
                <p>잠시 후 다시 시도해주세요.</p>
            </div>`;
        this.showSearchResults();
    }

    // 검색 결과 표시
    showSearchResults() {
        if (this.searchResults) this.searchResults.classList.add('show');
    }

    // 검색 결과 숨김
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

    escapeHtml(text) {
        if (typeof text !== 'string') return '';
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, (m) => map[m]);
    }
}

// 전역 인스턴스
window.PopupSearchManager = PopupSearchManager;