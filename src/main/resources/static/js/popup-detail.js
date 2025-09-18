// 팝업 상세 페이지 매니저 (리뷰 기능 통합)
class PopupDetailManager {
    constructor(popupId) {
        this.popupId = popupId;
        this.popupData = null;
        this.isBookmarked = false;
        this.reviewManager = null;
    }

    // 페이지 초기화
    async initialize() {
        try {
            if (!document.getElementById('popup-detail-content')) {
                await this.renderHTML();
            }
            this.setupEventListeners();
            await this.loadPopupData();

            // 리뷰 매니저 초기화
            this.reviewManager = new ReviewManager(this.popupId);
            await this.reviewManager.initialize();
        } catch (error) {
            console.error('팝업 상세 페이지 초기화 실패:', error);
            this.showError();
        }
    }

    // HTML 렌더링
    async renderHTML() {
        const template = await TemplateLoader.load('pages/popup/popup-detail');
        document.getElementById('main-content').innerHTML = template;
        document.getElementById('page-title').textContent = 'POPIN - 팝업 상세';
    }

    // 이벤트 리스너 설정
    setupEventListeners() {
        // 공유 버튼
        const shareBtn = document.getElementById('share-btn');
        if (shareBtn) {
            shareBtn.addEventListener('click', () => this.handleShare());
        }

        // 북마크 버튼
        const bookmarkBtn = document.getElementById('bookmark-btn');
        if (bookmarkBtn) {
            bookmarkBtn.addEventListener('click', () => this.handleBookmark());
        }

        // 예약하기 버튼
        const reservationBtn = document.getElementById('reservation-btn');
        if (reservationBtn) {
            reservationBtn.addEventListener('click', () => this.handleReservation());
        }

        // 주소 복사 버튼
        const copyAddressBtn = document.getElementById('copy-address-btn');
        if (copyAddressBtn) {
            copyAddressBtn.addEventListener('click', () => this.handleCopyAddress());
        }

        // 리뷰 작성 버튼
        const writeReviewBtn = document.querySelector('.write-review-btn');
        if (writeReviewBtn) {
            writeReviewBtn.addEventListener('click', () => this.handleWriteReview());
        }

        // 더보기 버튼
        const loadMoreBtn = document.querySelector('.load-more-btn');
        if (loadMoreBtn) {
            loadMoreBtn.addEventListener('click', () => this.handleLoadMoreReviews());
        }

        // 유사한 팝업 클릭 이벤트
        const similarGrid = document.getElementById('similar-popups-grid');
        if (similarGrid) {
            similarGrid.addEventListener('click', (e) => {
                const card = e.target.closest('.similar-popup-card');
                if (card && card.dataset.id) {
                    goToPopupDetail(card.dataset.id);
                }
            });
        }
    }

    // 팝업 데이터 로드
    async loadPopupData() {
        this.showLoading();

        try {
            this.popupData = await apiService.getPopup(this.popupId);
            this.renderPopupInfo();
            this.renderLocationInfo();
            await this.loadSimilarPopups();
            this.showContent();
        } catch (error) {
            console.error('팝업 데이터 로드 실패:', error);
            this.showError();
        }
    }

    // 팝업 정보 렌더링
    renderPopupInfo() {
        if (!this.popupData) return;

        // 메인 이미지
        const mainImg = document.getElementById('popup-main-img');
        if (mainImg) {
            const defaultImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAwIiBoZWlnaHQ9IjMwMCIgdmlld0JveD0iMCAwIDYwMCAzMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSI2MDAiIGhlaWdodD0iMzAwIiBmaWxsPSIjNEI1QUU0Ii8+Cjx0ZXh0IHg9IjMwMCIgeT0iMTUwIiBmb250LWZhbWlseT0ic2Fucy1zZXJpZiIgZm9udC1zaXplPSI0OCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGRvbWluYW50LWJhc2VsaW5lPSJjZW50cmFsIj7wn46qPC90ZXh0Pgo8L3N2Zz4=';

            mainImg.src = this.popupData.thumbnailUrl || defaultImage;
            mainImg.alt = this.popupData.title;
        }

        // 제목
        const titleEl = document.getElementById('popup-title');
        if (titleEl) {
            titleEl.textContent = this.popupData.title;
        }

        // 기간
        const periodEl = document.getElementById('popup-period');
        if (periodEl) {
            const startDate = new Date(this.popupData.startDate).toLocaleDateString('ko-KR');
            const endDate = new Date(this.popupData.endDate).toLocaleDateString('ko-KR');
            periodEl.textContent = `${startDate} ~ ${endDate}`;
        }

        // 운영시간
        const hoursEl = document.getElementById('popup-hours');
        if (hoursEl && this.popupData.operatingHours) {
            hoursEl.textContent = this.popupData.operatingHours;
        }

        // 태그
        const tagsEl = document.getElementById('popup-tags');
        if (tagsEl && Array.isArray(this.popupData.tags)) {
            tagsEl.innerHTML = '';
            this.popupData.tags.forEach(tag => {
                const span = document.createElement('span');
                span.className = 'tag';
                span.textContent = `#${tag}`;
                span.addEventListener('click', () => searchByTag(String(tag)));
                tagsEl.appendChild(span);
            });
        }
    }

    // 위치 정보 렌더링 메서드
    renderLocationInfo() {
        if (!this.popupData) return;

        const hasLocation = this.popupData.latitude && this.popupData.longitude;
        const hasVenue = this.popupData.venueName || this.popupData.venueAddress;

        if (!hasLocation && !hasVenue) return;

        const locationSection = document.getElementById('location-section');
        if (locationSection) {
            locationSection.style.display = 'block';
        }

        const venueNameEl = document.getElementById('venue-name');
        if (venueNameEl) {
            venueNameEl.textContent = this.popupData.venueName || '장소 정보 없음';
        }

        const venueAddressEl = document.getElementById('venue-address');
        if (venueAddressEl) {
            venueAddressEl.textContent = this.popupData.venueAddress || '주소 정보 없음';
        }

        // 주차 정보
        const parkingInfoEl = document.getElementById('parking-info');
        const parkingTextEl = document.getElementById('parking-text');
        if (parkingInfoEl && parkingTextEl && this.popupData.parkingAvailable !== undefined) {
            parkingInfoEl.style.display = 'flex';
            parkingTextEl.textContent = this.popupData.parkingAvailable ? '주차 가능' : '주차 불가';
            parkingInfoEl.className = this.popupData.parkingAvailable ? 'parking-info parking-available' : 'parking-info parking-unavailable';
        }

        if (hasLocation) {
            setTimeout(() => {
                this.initializeLocationMap();
            }, 0);
        } else {
            const mapContainer = document.querySelector('.map-container');
            if (mapContainer) {
                mapContainer.style.display = 'none';
            }
        }
    }

    // 지도 초기화
    initializeLocationMap() {
        console.log('[지도 초기화] 시작');
        const startTime = performance.now();

        const mapContainer = document.getElementById('popup-location-map');
        if (!mapContainer) {
            console.error('[지도 초기화] 맵 컨테이너를 찾을 수 없음');
            return;
        }

        console.log('[지도 초기화] 맵 컨테이너 발견:', mapContainer);

        // 카카오맵 API 로드 확인
        if (typeof kakao === 'undefined') {
            console.error('[지도 초기화] kakao 객체가 정의되지 않음');
            this.handleMapLoadError(mapContainer, '카카오맵 스크립트가 로드되지 않았습니다.');
            return;
        }

        if (!kakao.maps) {
            console.error('[지도 초기화] kakao.maps 객체가 정의되지 않음');
            this.handleMapLoadError(mapContainer, '카카오맵 API가 제대로 로드되지 않았습니다.');
            return;
        }

        console.log('[지도 초기화] 카카오맵 API 로드 확인됨');
        console.log('[지도 초기화] 좌표:', this.popupData.latitude, this.popupData.longitude);

        try {
            // 지도 옵션 설정
            const mapOption = {
                center: new kakao.maps.LatLng(this.popupData.latitude, this.popupData.longitude),
                level: 3
            };

            console.log('[지도 초기화] 지도 옵션:', mapOption);

            // 지도 생성 시작
            const mapCreateStart = performance.now();
            console.log('[지도 초기화] 지도 생성 시작');

            this.locationMap = new kakao.maps.Map(mapContainer, mapOption);

            const mapCreateEnd = performance.now();
            console.log(`[지도 초기화] 지도 생성 완료 (소요시간: ${mapCreateEnd - mapCreateStart}ms)`);

            // 마커 생성
            const markerCreateStart = performance.now();
            console.log('[지도 초기화] 마커 생성 시작');

            const marker = new kakao.maps.Marker({
                position: new kakao.maps.LatLng(this.popupData.latitude, this.popupData.longitude)
            });

            marker.setMap(this.locationMap);

            const markerCreateEnd = performance.now();
            console.log(`[지도 초기화] 마커 생성 완료 (소요시간: ${markerCreateEnd - markerCreateStart}ms)`);

            const totalTime = performance.now() - startTime;
            console.log(`[지도 초기화] 전체 완료 (총 소요시간: ${totalTime}ms)`);

            // 성능 임계치 확인
            if (totalTime > 3000) {
                console.warn(`[지도 초기화] 성능 주의: ${totalTime}ms 소요됨 (권장: 3초 미만)`);
            }

        } catch (error) {
            console.error('[지도 초기화] 오류 발생:', error);
            this.handleMapLoadError(mapContainer, `지도 생성 중 오류: ${error.message}`);
        }
    }

    // 주소 복사
    async handleCopyAddress() {
        if (!this.popupData.venueAddress) {
            this.showToast('복사할 주소가 없습니다.');
            return;
        }

        const success = await apiService.copyToClipboard(this.popupData.venueAddress);

        if (success) {
            this.showToast('주소가 클립보드에 복사되었습니다.');

            const copyBtn = document.getElementById('copy-address-btn');
            if (copyBtn) {
                copyBtn.classList.add('copied');
                setTimeout(() => copyBtn.classList.remove('copied'), 2000);
            }
        } else {
            this.showToast('주소 복사에 실패했습니다.');
        }
    }

    showToast(message) {
        let toast = document.getElementById('toast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'toast';
            toast.className = 'toast';
            document.body.appendChild(toast);
        }
        toast.textContent = message;
        toast.classList.add('show');
        setTimeout(() => toast.classList.remove('show'), 3000);
    }

    // 유사한 팝업 로드
    async loadSimilarPopups() {
        try {
            const similarPopups = await apiService.getSimilarPopups(this.popupId);
            this.renderSimilarPopups(similarPopups);
        } catch (error) {
            console.warn('유사한 팝업 로드 실패:', error);
            // 에러가 발생해도 계속 진행하도록 수정
            const gridEl = document.getElementById('similar-popups-grid');
            if (gridEl) {
                gridEl.innerHTML = '<p style="text-align: center; color: #6B7280; padding: 20px;">유사한 팝업을 불러올 수 없습니다.</p>';
            }
        }
    }

    // 유사한 팝업 렌더링
    renderSimilarPopups(popups) {
        const gridEl = document.getElementById('similar-popups-grid');
        if (!gridEl || !popups || popups.length === 0) return;

        gridEl.innerHTML = popups.map(popup => {
            const title = this.escapeHtml(popup.title ?? '');
            const thumb = (popup.thumbnailUrl && /^https?:/i.test(popup.thumbnailUrl))
              ? popup.thumbnailUrl
                : 'https://via.placeholder.com/200x150/4B5AE4/ffffff?text=%F0%9F%8E%AA';
            return `
              <div class="similar-popup-card" data-id="${popup.id}">
                <img src="${thumb}" alt="${title}" class="similar-popup-image">
                <div class="similar-popup-info">
                  <h3 class="similar-popup-title">${title}</h3>
                  <p class="similar-popup-period">${this.formatDateRange(popup.startDate, popup.endDate)}</p>
                </div>
              </div>`;
        }).join('');
    }

    // 날짜 범위 포맷
    formatDateRange(startDate, endDate) {
        const start = new Date(startDate).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
        const end = new Date(endDate).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
        return `${start} ~ ${end}`;
    }

    // 공유 처리
    async handleShare() {
        const shareData = {
            title: this.popupData?.title || '팝업 스토어',
            text: `${this.popupData?.title} - POPIN에서 확인하세요!`,
            url: window.location.href
        };

        if (navigator.share) {
            try {
                await navigator.share(shareData);
            } catch (error) {
                console.log('공유 취소됨');
            }
        } else {
            // Web Share API 미지원 시 클립보드에 복사
            try {
                await navigator.clipboard.writeText(window.location.href);
                alert('링크가 클립보드에 복사되었습니다.');
            } catch (error) {
                console.error('클립보드 복사 실패:', error);
                alert('링크 복사에 실패했습니다.');
            }
        }
    }

    // 북마크 처리
    async handleBookmark() {
        try {
            if (this.isBookmarked) {
                await apiService.removeBookmark(this.popupId);
                this.isBookmarked = false;
                this.updateBookmarkButton();
                alert('북마크가 해제되었습니다.');
            } else {
                await apiService.addBookmark(this.popupId);
                this.isBookmarked = true;
                this.updateBookmarkButton();
                alert('북마크에 추가되었습니다.');
            }
        } catch (error) {
            console.error('북마크 처리 실패:', error);
            alert('북마크 처리 중 오류가 발생했습니다.');
        }
    }

    // 북마크 버튼 업데이트
    updateBookmarkButton() {
        const bookmarkBtn = document.getElementById('bookmark-btn');
        if (bookmarkBtn) {
            const svg = bookmarkBtn.querySelector('svg');
            if (this.isBookmarked) {
                svg.setAttribute('fill', 'currentColor');
                bookmarkBtn.style.color = '#6366F1';
            } else {
                svg.setAttribute('fill', 'none');
                bookmarkBtn.style.color = '';
            }
        }
    }

    // 예약하기 처리
    handleReservation() {
        if (!this.popupData) {
            alert('팝업 정보를 불러오는 중입니다.');
            return;
        }

        if (this.popupData.reservationLink) {
            window.open(this.popupData.reservationLink, '_blank');
        } else {
            alert('예약 기능은 준비 중입니다.');
        }
    }

    // 리뷰 작성 처리
    async handleWriteReview() {
        // 사용자 로그인 체크
        const userId = await this.getOrFetchUserId();
        if (!userId) {
            alert('로그인이 필요한 서비스입니다.');
            window.location.href = '/login';
            return;
        }

        // 리뷰 작성 페이지로 이동
        window.location.href = `/reviews/popup/${this.popupId}/create`;
    }

    // 더 많은 리뷰 로드
    handleLoadMoreReviews() {
        // 전체 리뷰 목록 페이지로 이동
        window.location.href = `/reviews/popup/${this.popupId}`;
    }

    // 사용자 ID 확보
    async getOrFetchUserId() {
        try {
            const cached = localStorage.getItem('userId') || sessionStorage.getItem('userId');
            const parsed = cached ? parseInt(cached, 10) : NaN;
            if (!Number.isNaN(parsed)) return parsed;
        } catch (e) {
            console.warn('userId 캐시 확인 실패:', e);
        }

        if (!apiService.getStoredToken()) return null;
        try {
            const userInfo = await apiService.getCurrentUser();
            if (userInfo && userInfo.id) {
                try { localStorage.setItem('userId', String(userInfo.id)); }
                catch { sessionStorage.setItem('userId', String(userInfo.id)); }
                return userInfo.id;
            }
        } catch (e) {
            console.warn('사용자 정보 가져오기 실패:', e);
        }

        return null;
    }

    // 로딩 표시
    showLoading() {
        document.getElementById('popup-detail-loading').style.display = 'flex';
        document.getElementById('popup-detail-content').style.display = 'none';
        if (document.getElementById('popup-detail-error')) {
            document.getElementById('popup-detail-error').style.display = 'none';
        }
    }

    // 콘텐츠 표시
    showContent() {
        document.getElementById('popup-detail-loading').style.display = 'none';
        document.getElementById('popup-detail-content').style.display = 'block';
        if (document.getElementById('popup-detail-error')) {
            document.getElementById('popup-detail-error').style.display = 'none';
        }

        if (this.locationMap) {
            this.locationMap.relayout();

            const correctPosition = new kakao.maps.LatLng(this.popupData.latitude, this.popupData.longitude);
            this.locationMap.setCenter(correctPosition);
        }
    }

    // 에러 표시
    showError() {
        document.getElementById('popup-detail-loading').style.display = 'none';
        document.getElementById('popup-detail-content').style.display = 'none';
        if (document.getElementById('popup-detail-error')) {
            document.getElementById('popup-detail-error').style.display = 'flex';
        }
    }

    // 컴포넌트 정리
    cleanup() {
        if (this.reviewManager) {
            this.reviewManager.cleanup();
        }
    }
}

// 리뷰 관리 클래스
class ReviewManager {
    constructor(popupId) {
        this.popupId = popupId;
        this.currentRating = 0;
        this.currentPage = 0;
        this.hasMore = true;
    }

    // 초기화
    async initialize() {
        this.setupEventListeners();
        await this.loadReviewStats();
        await this.loadRecentReviews();
    }

    // 이벤트 리스너 설정
    setupEventListeners() {
    }

    // 리뷰 통계 로드
    async loadReviewStats() {
        try {
            const response = await fetch(`/api/reviews/popup/${this.popupId}/stats`);
            if (!response.ok) throw new Error('Failed to load review stats');

            const stats = await response.json();
            this.renderReviewStats(stats);
        } catch (error) {
            console.error('리뷰 통계 로드 실패:', error);
            // 기본값으로 설정
            this.renderReviewStats({ averageRating: 0, totalReviews: 0 });
        } finally {
            // 로딩 스피너 강제 숨김
            this.hideStatsLoading();
        }
    }

    // 최근 리뷰 로드 (최대 2개)
    async loadRecentReviews() {
        try {
            const response = await fetch(`/api/reviews/popup/${this.popupId}/recent?limit=2`);
            if (!response.ok) throw new Error('Failed to load reviews');

            const reviews = await response.json();
            this.renderRecentReviews(reviews);

            // 더보기 버튼 표시 여부 결정
            const statsResponse = await fetch(`/api/reviews/popup/${this.popupId}/stats`);
            if (statsResponse.ok) {
                const stats = await statsResponse.json();
                const loadMoreBtn = document.getElementById('loadMoreBtn') || document.querySelector('.load-more-btn');
                if (loadMoreBtn && stats.totalReviews > 2) {
                    loadMoreBtn.style.display = 'block';
                }
            }
        } catch (error) {
            console.error('리뷰 로드 실패:', error);
            this.renderNoReviews();
        } finally {
            // 로딩 스피너 강제 숨김
            this.hideReviewsLoading();
        }
    }

    hideStatsLoading() {
        const statsLoading = document.querySelector('.stats-loading');
        if (statsLoading) {
            statsLoading.style.display = 'none';
        }
    }

    hideReviewsLoading() {
        const reviewsLoading = document.getElementById('reviewsLoading') || document.querySelector('.reviews-loading');
        if (reviewsLoading) {
            reviewsLoading.style.display = 'none';
        }
    }

    // 리뷰 통계 렌더링
    renderReviewStats(stats) {
        const statsContainer = document.getElementById('reviewStats');
        if (!statsContainer) {
            console.warn('reviewStats 요소를 찾을 수 없습니다.');
            return;
        }

        // 로딩 숨김
        const loadingEl = statsContainer.querySelector('.stats-loading');
        if (loadingEl) {
            loadingEl.style.display = 'none';
        }

        // 통계 HTML 생성
        const rating = stats.averageRating || 0;
        const count = stats.totalReviews || 0;

        statsContainer.innerHTML = `
        <div class="rating-display">
            <div class="stars">
                ${this.renderStars(rating)}
            </div>
            <span class="rating-text">${rating.toFixed(1)}</span>
        </div>
        <span class="review-count">(${count})</span>
    `;
    }

    // 최근 리뷰 렌더링
    renderRecentReviews(reviews) {
        const listEl = document.getElementById('reviewsList') || document.querySelector('.reviews-list');
        const loadingEl = document.getElementById('reviewsLoading') || document.querySelector('.loading-spinner');

        if (loadingEl) {
            loadingEl.style.display = 'none';
        }

        if (!reviews || reviews.length === 0) {
            this.renderNoReviews();
            return;
        }

        if (listEl) {
            listEl.innerHTML = reviews.map(review => this.renderReviewItem(review)).join('');
        }
    }

    // 리뷰 아이템 렌더링
    renderReviewItem(review) {
        const createdDate = new Date(review.createdAt).toLocaleDateString('ko-KR');

        return `
            <div class="review-item">
                <div class="review-header">
                    <div class="review-stars">
                        ${this.renderStars(review.rating)}
                    </div>
                    <span class="review-date">${createdDate}</span>
                </div>
                <p class="review-content">${this.escapeHtml(review.content)}</p>
                <div class="reviewer-info">
                    <span class="reviewer-name">${this.escapeHtml(review.userName || '익명')}</span>
                </div>
            </div>
        `;
    }

    // 별점 렌더링
    renderStars(rating) {
        let stars = '';
        for (let i = 1; i <= 5; i++) {
            if (i <= rating) {
                stars += '<span class="star">★</span>';
            } else {
                stars += '<span class="star empty">★</span>';
            }
        }
        return stars;
    }

    // 리뷰 없을 때 렌더링
    renderNoReviews() {
        const listEl = document.getElementById('reviewsList') || document.querySelector('.reviews-list');
        if (listEl) {
            listEl.innerHTML = `
                <div class="no-reviews">
                    <div class="no-reviews-icon">📝</div>
                    <p>아직 작성된 리뷰가 없습니다.<br>첫 번째 리뷰를 작성해보세요!</p>
                </div>
            `;
        }
    }

    // HTML 이스케이프
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = String(text ?? '');
        return div.innerHTML;
    }

    // 정리
    cleanup() {
    }
}

// 태그로 검색하는 함수
function searchByTag(tag) {
    console.log(`"${tag}" 태그로 검색`);

    // 태그에서 # 제거
    const cleanTag = tag.startsWith('#') ? tag.substring(1) : tag;

    window.location.href = `/popup/search?query=${encodeURIComponent(cleanTag)}`;
}

// 전역 등록
window.PopupDetailManager = PopupDetailManager;
window.ReviewManager = ReviewManager;
window.searchByTag = searchByTag;