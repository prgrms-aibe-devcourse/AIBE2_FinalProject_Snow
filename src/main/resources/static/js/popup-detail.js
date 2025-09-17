// 팝업 상세 페이지 매니저
class PopupDetailManager {
    constructor(popupId) {
        this.popupId = popupId;
        this.popupData = null;
        this.isBookmarked = false;

        this.fallbackImages = {
            main: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNjAwIiBoZWlnaHQ9IjMwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjNEI1QUU0Ii8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iMjQiIGZpbGw9IndoaXRlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+UG9wdXAgSW1hZ2U8L3RleHQ+PC9zdmc+',
            card: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMTUwIiBoZWlnaHQ9IjE1MCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjNEI1QUU0Ii8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtc2l6ZT0iMTYiIGZpbGw9IndoaXRlIiB0ZXh0LWFuY2hvcj0ibWlkZGxlIiBkeT0iLjNlbSI+Tm8gSW1hZ2U8L3RleHQ+PC9zdmc+',
            avatar: 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGNpcmNsZSBjeD0iMjAiIGN5PSIyMCIgcj0iMjAiIGZpbGw9IiM4YjVjZjYiLz48dGV4dCB4PSI1MCUiIHk9IjUwJSIgZm9udC1zaXplPSIxNCIgZmlsbD0id2hpdGUiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj5VPC90ZXh0Pjwvc3ZnPg=='
        };
    }

    // 페이지 초기화
    async initialize() {
        try {
            if (!document.getElementById('popup-detail-content')) {
                await this.renderHTML();
            }
            this.setupEventListeners();
            await this.loadPopupData();
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
            mainImg.src = this.popupData.thumbnailUrl || this.fallbackImages.main;
            mainImg.alt = this.popupData.title;
            mainImg.onerror = function() {
                this.onerror = null; // 무한루프 방지
                this.src = this.fallbackImages.main;
            }.bind(mainImg);
        }

        // 제목
        const titleEl = document.getElementById('popup-title');
        if (titleEl) {
            titleEl.textContent = this.popupData.title;
        }

        // 기간
        const periodEl = document.getElementById('popup-period');
        if (periodEl) {
            periodEl.textContent = this.popupData.period || this.formatPeriod(this.popupData.startDate, this.popupData.endDate);
        }

        // 운영시간
        const hoursEl = document.getElementById('popup-hours');
        if (hoursEl && this.popupData.hours) {
            hoursEl.innerHTML = this.formatHours(this.popupData.hours);
        }

        // 태그
        this.renderTags();

        // 예약 버튼 상태
        this.updateReservationButton();
    }

    // 태그 렌더링
    renderTags() {
        const tagsContainer = document.getElementById('popup-tags');
        if (!tagsContainer) return;

        const tags = [];

        // 카테고리 태그
        if (this.popupData.categoryName) {
            tags.push(`#${this.popupData.categoryName}`);
        }

        // 지역 태그
        if (this.popupData.region) {
            tags.push(`#${this.popupData.region}`);
        }

        // 더미 태그 (나중에 실제 태그 데이터로 교체)
        if (this.popupData.title.includes('요아정')) {
            tags.push('#요아정', '#롯데월드몰');
        }

        const tagsHTML = tags.map(tag =>
            `<button class="popup-tag" onclick="searchByTag('${tag}')">${tag}</button>`
        ).join('');

        tagsContainer.innerHTML = tagsHTML;
    }

    // 기간 포맷팅
    formatPeriod(startDate, endDate) {
        if (!startDate && !endDate) return '기간 미정';

        const formatDate = (date) => {
            if (!date) return '';
            return date.replace(/-/g, '.');
        };

        if (startDate && endDate) {
            return `${formatDate(startDate)} - ${formatDate(endDate)}`;
        } else if (startDate) {
            return `${formatDate(startDate)} -`;
        } else {
            return `- ${formatDate(endDate)}`;
        }
    }

    // 운영시간 포맷팅
    formatHours(hours) {
        if (!hours || hours.length === 0) {
            return '운영시간 미정';
        }

        const dayNames = ['월', '화', '수', '목', '금', '토', '일'];
        const groupedHours = {};

        // 요일별로 그룹화
        hours.forEach(hour => {
            const timeRange = `${hour.openTime || '미정'} ~ ${hour.closeTime || '미정'}`;
            if (!groupedHours[timeRange]) {
                groupedHours[timeRange] = [];
            }
            groupedHours[timeRange].push(dayNames[hour.dayOfWeek]);
        });

        // 포맷팅
        return Object.entries(groupedHours)
            .map(([timeRange, days]) => `${days.join(', ')} ${timeRange}`)
            .join('<br>');
    }

    // 예약 버튼 상태 업데이트
    updateReservationButton() {
        const reservationBtn = document.getElementById('reservation-btn');
        if (!reservationBtn) return;

        if (this.popupData.reservationAvailable) {
            reservationBtn.textContent = '예약하기';
            reservationBtn.disabled = false;
        } else if (this.popupData.waitlistAvailable) {
            reservationBtn.textContent = '대기열 등록';
            reservationBtn.disabled = false;
        } else {
            reservationBtn.textContent = '예약 불가';
            reservationBtn.disabled = true;
        }
    }

    // 유사한 팝업 로드
    async loadSimilarPopups() {
        try {
            if (!this.popupData.categoryId) {
                console.warn('카테고리 ID가 없어 유사한 팝업을 로드할 수 없습니다.');
                return;
            }

            const response = await apiService.getPopups({
                page: 0,
                size: 6,
                categoryIds: [this.popupData.categoryId]
            });

            // 현재 팝업 제외
            const similarPopups = response.popups.filter(p => p.id !== this.popupData.id);
            this.renderSimilarPopups(similarPopups.slice(0, 4)); // 최대 4개만 표시
        } catch (error) {
            console.error('유사한 팝업 로드 실패:', error);
        }
    }

    // 유사한 팝업 렌더링
    renderSimilarPopups(popups) {
        const grid = document.getElementById('similar-popups-grid');
        if (!grid) return;

        if (!popups || popups.length === 0) {
            grid.innerHTML = '<p class="alert alert-info" style="grid-column: 1 / -1; text-align: center;">유사한 팝업이 없습니다.</p>';
            return;
        }

        const esc = (s) => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));

        const cardsHTML = popups.map(popup => `
            <div class="similar-popup-card" data-id="${popup.id}">
                <img src="${popup.mainImageUrl || popup.thumbnailUrl || this.fallbackImages.card}" 
                     alt="${esc(popup.title)}" class="similar-card-image"
                     onerror="this.onerror=null; this.src='${this.fallbackImages.card}'">
                <div class="similar-card-content">
                    <h3 class="similar-card-title">${esc(popup.title)}</h3>
                    <p class="similar-card-info">${esc(popup.region)}</p>
                </div>
            </div>
        `).join('');

        grid.innerHTML = cardsHTML;
    }

    // 공유 처리
    handleShare() {
        if (navigator.share) {
            navigator.share({
                title: this.popupData?.title || '팝업 스토어',
                text: this.popupData?.summary || '흥미로운 팝업 스토어를 확인해보세요!',
                url: window.location.href
            }).catch(console.error);
        } else {
            // 폴백: 클립보드에 복사
            navigator.clipboard.writeText(window.location.href).then(() => {
                alert('링크가 클립보드에 복사되었습니다!');
            }).catch(() => {
                alert('링크 복사에 실패했습니다.');
            });
        }
    }

    // 북마크 처리
    handleBookmark() {
        this.isBookmarked = !this.isBookmarked;
        const bookmarkBtn = document.getElementById('bookmark-btn');

        if (this.isBookmarked) {
            bookmarkBtn.classList.add('bookmarked');
            alert('북마크에 추가되었습니다!');
        } else {
            bookmarkBtn.classList.remove('bookmarked');
            alert('북마크에서 제거되었습니다!');
        }

        // TODO: 실제 북마크 API 호출
    }

    // 예약 처리
    handleReservation() {
        if (!this.popupData.reservationAvailable && !this.popupData.waitlistAvailable) {
            alert('현재 예약이 불가능합니다.');
            return;
        }

        if (this.popupData.reservationAvailable) {
            window.location.href = `/popup/${this.popupId}/reservation`;
        } else if (this.popupData.waitlistAvailable) {
            // 대기열 등록 기능 (추후 구현)
            alert('대기열 등록 기능은 준비 중입니다.');
        } else if (this.popupData.reservationLink) {
            // 외부 예약 링크가 있는 경우
            window.open(this.popupData.reservationLink, '_blank');
        }
    }

    // 리뷰 작성 처리
    handleWriteReview() {
        alert('리뷰 작성 기능은 준비 중입니다.');
        // TODO: 리뷰 작성 모달 또는 페이지로 이동
    }

    // 더 많은 리뷰 로드
    handleLoadMoreReviews() {
        alert('더 많은 리뷰 로드 기능은 준비 중입니다.');
        // TODO: 추가 리뷰 로드 API 호출
    }

    // 로딩 표시
    showLoading() {
        document.getElementById('popup-detail-loading').style.display = 'flex';
        document.getElementById('popup-detail-content').style.display = 'none';
        document.getElementById('popup-detail-error').style.display = 'none';
    }

    // 콘텐츠 표시
    showContent() {
        document.getElementById('popup-detail-loading').style.display = 'none';
        document.getElementById('popup-detail-content').style.display = 'block';
        document.getElementById('popup-detail-error').style.display = 'none';
    }

    // 에러 표시
    showError() {
        document.getElementById('popup-detail-loading').style.display = 'none';
        document.getElementById('popup-detail-content').style.display = 'none';
        document.getElementById('popup-detail-error').style.display = 'flex';
    }

    // 컴포넌트 정리
    cleanup() {
        // 현재는 특별한 정리 작업 없음
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
window.searchByTag = searchByTag;