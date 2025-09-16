// space-list.js 전체 파일 (검색 기능 포함)

// id 추출 (id / spaceId / space_id 호환)
function pickSpaceId(space) {
    return space?.id ?? space?.spaceId ?? space?.space_id ?? null;
}

// 이미지 없을 때 쓸 인라인 플레이스홀더 (파일 의존 없음)
const IMG_PLACEHOLDER =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent(
        `<svg xmlns="http://www.w3.org/2000/svg" width="160" height="120">
       <rect width="100%" height="100%" fill="#f2f2f2"/>
       <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle"
             fill="#888" font-size="14">no image</text>
     </svg>`
    );

// 공간 목록 페이지 전용
const SpaceListPage = {
    allSpaces: [], // 전체 공간 데이터 저장
    currentSpaces: [], // 현재 표시되는 공간 데이터
    isSearchMode: false, // 검색 모드 여부

    async init() {
        try {
            this.showLoading();
            const spaces = await apiService.listSpaces();
            this.allSpaces = spaces;
            this.currentSpaces = spaces;
            this.renderSpaces(spaces);
            this.initializeSearch(); // 검색 기능 초기화
        } catch (error) {
            console.error('Space List page initialization failed:', error);
            this.showError('공간 목록을 불러오는데 실패했습니다.');
        }
    },

    // 검색 기능 초기화
    initializeSearch() {
        const searchInput = document.getElementById('searchKeyword');
        const searchBtn = document.getElementById('searchBtn');
        const resetBtn = document.getElementById('resetBtn');
        const applyFilterBtn = document.getElementById('applyFilter');
        const clearSearchBtn = document.getElementById('clearSearch');
        const resetSearchBtn = document.getElementById('resetSearch');

        // 검색 버튼 클릭
        if (searchBtn) {
            searchBtn.addEventListener('click', () => this.performSearch());
        }

        // 엔터키로 검색
        if (searchInput) {
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.performSearch();
                }
            });
        }

        // 필터 적용
        if (applyFilterBtn) {
            applyFilterBtn.addEventListener('click', () => this.performSearch());
        }

        // 검색/필터 초기화
        if (resetBtn) {
            resetBtn.addEventListener('click', () => this.resetFilters());
        }

        // 전체보기 (검색 해제)
        if (clearSearchBtn) {
            clearSearchBtn.addEventListener('click', () => this.clearSearch());
        }

        // 검색 초기화 (검색 결과 없음 상태에서)
        if (resetSearchBtn) {
            resetSearchBtn.addEventListener('click', () => this.clearSearch());
        }
    },

    // 검색 실행
    async performSearch() {
        const keyword = document.getElementById('searchKeyword')?.value?.trim();
        const location = document.getElementById('locationFilter')?.value?.trim();
        const minArea = document.getElementById('minArea')?.value;
        const maxArea = document.getElementById('maxArea')?.value;

        // 검색 조건이 하나라도 있는지 확인
        const hasSearchCondition = keyword || location || minArea || maxArea;

        if (!hasSearchCondition) {
            alert('검색 조건을 하나 이상 입력해주세요.');
            return;
        }

        try {
            this.showLoading();

            // 검색 API 호출
            const searchParams = new URLSearchParams();
            if (keyword) searchParams.append('keyword', keyword);
            if (location) searchParams.append('location', location);
            if (minArea) searchParams.append('minArea', minArea);
            if (maxArea) searchParams.append('maxArea', maxArea);

            const searchResults = await apiService.get(`/spaces/search?${searchParams.toString()}`);

            this.currentSpaces = searchResults;
            this.isSearchMode = true;
            this.renderSpaces(searchResults);
            this.showSearchInfo(searchResults.length);

        } catch (error) {
            console.error('검색 실패:', error);
            this.showError('검색 중 오류가 발생했습니다.');
        }
    },

    // 필터 초기화
    resetFilters() {
        document.getElementById('searchKeyword').value = '';
        document.getElementById('locationFilter').value = '';
        document.getElementById('minArea').value = '';
        document.getElementById('maxArea').value = '';
    },

    // 검색 해제 (전체 목록으로 돌아가기)
    clearSearch() {
        this.resetFilters();
        this.isSearchMode = false;
        this.currentSpaces = this.allSpaces;
        this.renderSpaces(this.allSpaces);
        this.hideSearchInfo();
    },

    // 검색 정보 표시
    showSearchInfo(count) {
        const searchInfo = document.getElementById('searchInfo');
        const resultCount = document.getElementById('resultCount');

        if (searchInfo && resultCount) {
            resultCount.textContent = count;
            searchInfo.style.display = 'flex';
        }
    },

    // 검색 정보 숨기기
    hideSearchInfo() {
        const searchInfo = document.getElementById('searchInfo');
        if (searchInfo) {
            searchInfo.style.display = 'none';
        }
    },

    showLoading() {
        const loadingEl = document.getElementById('loading');
        const spaceListEl = document.getElementById('spaceList');
        const emptyStateEl = document.getElementById('emptyState');
        const noSearchResultEl = document.getElementById('noSearchResult');

        if (loadingEl) loadingEl.style.display = 'block';
        if (spaceListEl) spaceListEl.style.display = 'none';
        if (emptyStateEl) emptyStateEl.style.display = 'none';
        if (noSearchResultEl) noSearchResultEl.style.display = 'none';
    },

    hideLoading() {
        const loadingEl = document.getElementById('loading');
        if (loadingEl) loadingEl.style.display = 'none';
    },

    renderSpaces(spaces) {
        this.hideLoading();

        const spaceListEl = document.getElementById('spaceList');
        const emptyStateEl = document.getElementById('emptyState');
        const noSearchResultEl = document.getElementById('noSearchResult');

        // 검색 모드이고 결과가 없는 경우
        if (this.isSearchMode && (!spaces || spaces.length === 0)) {
            if (spaceListEl) spaceListEl.style.display = 'none';
            if (emptyStateEl) emptyStateEl.style.display = 'none';
            if (noSearchResultEl) noSearchResultEl.style.display = 'block';
            return;
        }

        // 전체 목록이 비어있는 경우
        if (!spaces || spaces.length === 0) {
            if (spaceListEl) spaceListEl.style.display = 'none';
            if (emptyStateEl) emptyStateEl.style.display = 'block';
            if (noSearchResultEl) noSearchResultEl.style.display = 'none';
            return;
        }

        // 정상적으로 결과가 있는 경우
        if (spaceListEl) {
            spaceListEl.style.display = 'block';
            spaceListEl.innerHTML = '';
            spaces.forEach(space => {
                const spaceCard = this.createSpaceCard(space);
                spaceListEl.appendChild(spaceCard);
            });
        }
        if (emptyStateEl) emptyStateEl.style.display = 'none';
        if (noSearchResultEl) noSearchResultEl.style.display = 'none';
    },

    createSpaceCard(space) {
        const id = pickSpaceId(space);
        const card = document.createElement('div');
        card.className = 'space-card';

        const imageUrl = this.getThumbUrl(space);
        const imageHtml = `<img class="thumb" src="${imageUrl}" alt="썸네일">`;

        card.innerHTML = `
      <div class="space-header">
        <div>
          <h4 class="space-title">${space.title || '(제목 없음)'}</h4>
          <div class="space-details">
            <div>등록자: ${space.ownerName || '-'}</div>
            <div>임대료: ${this.formatRentalFee(space.rentalFee)}</div>
            <div>주소: ${space.address || '-'}</div>
            <div>면적: ${space.areaSize || '-'} m²</div>
            <div class="actions-inline">
              <button class="link" data-act="detail" data-id="${id}">상세정보</button>
              <button class="link" data-act="inquire" data-id="${id}">문의하기</button>
              <button class="link" data-act="report"  data-id="${id}">신고</button>
            </div>
          </div>
        </div>
        ${imageHtml}
      </div>
      <div class="space-meta">
        <span>등록일: ${this.formatDate(space.createdAt)}</span>
        <div class="space-actions">
          ${space.mine ? `
            <button class="action-btn edit"   data-act="edit"   data-id="${id}">수정</button>
            <button class="action-btn delete" data-act="delete" data-id="${id}">삭제</button>
          ` : ''}
        </div>
      </div>
    `;

        // 이미지 실패 시 1회만 플레이스홀더로 대체 (무한 루프 방지)
        const imgEl = card.querySelector('.thumb');
        if (imgEl) {
            imgEl.onerror = function () {
                this.onerror = null;
                this.src = IMG_PLACEHOLDER;
            };
        }

        // 버튼 동작
        card.addEventListener('click', (e) => {
            const btn = e.target.closest('[data-act]');
            if (!btn) return;

            const act = btn.getAttribute('data-act');
            const targetId = btn.getAttribute('data-id');

            switch (act) {
                case 'detail':
                    location.assign(`../../templates/pages/space-detail.html?id=${targetId}`);
                    break;
                case 'edit':
                    location.assign(`../../templates/pages/space-edit.html?id=${targetId}`);
                    break;
                case 'delete':
                    this.deleteSpace(targetId);
                    break;
                case 'inquire':
                    this.inquireSpace(targetId);
                    break;
                case 'report':
                    this.reportSpace(targetId);
                    break;
            }
        });

        return card;
    },

    // 썸네일 URL 처리 (coverImageUrl → 절대 경로 변환 포함)
    getThumbUrl(space) {
        if (space.coverImageUrl) {
            return `${window.location.origin}${space.coverImageUrl}`;
        }
        if (space.coverImage) {
            return `${window.location.origin}${space.coverImage}`;
        }
        return IMG_PLACEHOLDER;
    },

    // API
    async deleteSpace(spaceId) {
        if (!confirm('정말로 이 공간을 삭제하시겠습니까?')) return;
        try {
            await apiService.deleteSpace(spaceId);
            alert('공간이 삭제되었습니다.');
            location.reload();
        } catch (error) {
            console.error('공간 삭제 실패:', error);
            alert('삭제에 실패했습니다.');
        }
    },
    async inquireSpace(spaceId) {
        try {
            await apiService.inquireSpace(spaceId);
        } catch (error) {
            console.error('문의 실패:', error);
            alert('문의 중 오류가 발생했습니다.');
        }
    },
    async reportSpace(spaceId) {
        try {
            await apiService.reportSpace(spaceId);
        } catch (error) {
            console.error('신고 실패:', error);
            alert('신고 중 오류가 발생했습니다.');
        }
    },

    // 유틸리티 함수들
    formatDate(dateString) {
        if (!dateString) return '날짜 정보 없음';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR');
    },

    formatRentalFee(amount) {
        if (!amount && amount !== 0) return '-';
        return `${amount} 만원`;
    },

    showError(message) {
        this.hideLoading();
        const spaceListEl = document.getElementById('spaceList');
        const emptyStateEl = document.getElementById('emptyState');
        if (spaceListEl) {
            spaceListEl.style.display = 'block';
            spaceListEl.innerHTML = `<div class="error-state"><p>${message}</p></div>`;
        }
        if (emptyStateEl) emptyStateEl.style.display = 'none';
    }
};

window.SpaceListPage = SpaceListPage;