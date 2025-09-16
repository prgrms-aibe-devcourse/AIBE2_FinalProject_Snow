/**
 * 장소 관리 UI 클래스
 */
class SpaceManagementUI {
    constructor() {
        this.elements = {
            // 통계
            totalSpaces: document.getElementById('totalSpaces'),
            publicSpaces: document.getElementById('publicSpaces'),
            privateSpaces: document.getElementById('privateSpaces'),

            // 필터
            ownerFilter: document.getElementById('ownerFilter'),
            titleFilter: document.getElementById('titleFilter'),
            isPublicFilter: document.getElementById('isPublicFilter'),
            searchBtn: document.getElementById('searchBtn'),
            resetBtn: document.getElementById('resetBtn'),

            // 테이블
            spacesTableBody: document.getElementById('spacesTableBody'),
            pagination: document.getElementById('pagination'),
            searchLoading: document.getElementById('searchLoading'),
            noResults: document.getElementById('noResults'),

            // 모달
            spaceDetailModal: document.getElementById('spaceDetailModal'),
            spaceDetailContent: document.getElementById('spaceDetailContent'),
            closeModal: document.getElementById('closeModal'),

            confirmModal: document.getElementById('confirmModal'),
            confirmTitle: document.getElementById('confirmTitle'),
            confirmMessage: document.getElementById('confirmMessage'),
            confirmBtn: document.getElementById('confirmBtn'),
            cancelBtn: document.getElementById('cancelBtn'),
            confirmModalClose: document.getElementById('confirmModalClose')
        };

        this.currentAction = null;
        this.currentSpaceId = null;

        console.log('🎨 SpaceManagementUI 초기화 완료');
        console.log('📋 Elements:', this.elements);
    }

    /**
     * 통계 업데이트
     */
    updateStats(stats) {
        console.log('📊 통계 업데이트:', stats);
        if (this.elements.totalSpaces) {
            this.elements.totalSpaces.textContent = stats.totalSpaces || 0;
        }
        if (this.elements.publicSpaces) {
            this.elements.publicSpaces.textContent = stats.publicSpaces || 0;
        }
        if (this.elements.privateSpaces) {
            this.elements.privateSpaces.textContent = stats.privateSpaces || 0;
        }
    }

    /**
     * 장소 테이블 렌더링
     */
    renderSpacesTable(spacesData) {
        console.log('🏠 장소 테이블 렌더링 시작:', spacesData);

        const tbody = this.elements.spacesTableBody;
        if (!tbody) {
            console.error('❌ spacesTableBody 요소를 찾을 수 없습니다');
            return;
        }

        // 데이터 검증
        if (!spacesData || !spacesData.content || spacesData.content.length === 0) {
            console.log('📭 데이터가 없어서 "결과 없음" 표시');
            this.showNoResults();
            return;
        }

        console.log(`✅ ${spacesData.content.length}개의 장소 데이터 렌더링`);
        this.hideNoResults();

        tbody.innerHTML = '';

        spacesData.content.forEach((space, index) => {
            console.log(`🏠 장소 ${index + 1}:`, space);
            const row = this.createSpaceRow(space);
            tbody.appendChild(row);
        });

        // 페이지네이션 렌더링
        this.renderPagination(spacesData);
    }

    /**
     * 장소 행 생성
     */
    createSpaceRow(space) {
        const row = document.createElement('tr');

        const formatDate = (dateString) => {
            if (!dateString) return '-';
            return new Date(dateString).toLocaleDateString('ko-KR');
        };

        const formatPrice = (price) => {
            if (!price) return '-';
            return new Intl.NumberFormat('ko-KR').format(price) + '원';
        };

        // ✅ 목록 데이터에서는 SpaceListResponseDto 구조 사용
        const getOwnerName = (space) => {
            return space.ownerName || '-';  // SpaceListResponseDto에서는 ownerName이 직접 제공됨
        };

        const getLocation = (space) => {
            return space.address || '-';    // SpaceListResponseDto에서는 address가 직접 제공됨
        };

        row.innerHTML = `
        <td>${space.id}</td>
        <td>${space.title || '-'}</td>
        <td>${getOwnerName(space)}</td>
        <td>${getLocation(space)}</td>
        <td>${formatPrice(space.rentalFee)}</td>
        <td>
            <span class="status-badge ${space.isPublic ? 'public' : 'private'}">
                ${space.isPublic ? '공개' : '비공개'}
            </span>
        </td>
        <td>${formatDate(space.createdAt)}</td>
        <td>
            <div class="action-buttons">
                <button class="btn btn-info detail-btn" data-space-id="${space.id}">
                    상세보기
                </button>
                ${space.isPublic ?
            `<button class="btn btn-warning" data-space-id="${space.id}">
                        비활성화
                    </button>` :
            `<button class="btn btn-success" data-space-id="${space.id}">
                        활성화
                    </button>`
        }
            </div>
        </td>
    `;

        return row;
    }

    /**
     * 페이지네이션 렌더링
     */
    renderPagination(data) {
        const pagination = this.elements.pagination;
        if (!pagination) return;

        const { number: currentPage, totalPages, first, last } = data;

        if (totalPages <= 1) {
            pagination.innerHTML = '';
            return;
        }

        let paginationHTML = '';

        // 이전 페이지 버튼
        if (!first) {
            paginationHTML += `
                <button onclick="spaceManagementController.loadPage(${currentPage - 1})" ${first ? 'disabled' : ''}>
                    이전
                </button>
            `;
        }

        // 페이지 번호들
        const startPage = Math.max(0, currentPage - 2);
        const endPage = Math.min(totalPages - 1, currentPage + 2);

        for (let i = startPage; i <= endPage; i++) {
            paginationHTML += `
                <button class="${i === currentPage ? 'active' : ''}" 
                        onclick="spaceManagementController.loadPage(${i})">
                    ${i + 1}
                </button>
            `;
        }

        // 다음 페이지 버튼
        if (!last) {
            paginationHTML += `
                <button onclick="spaceManagementController.loadPage(${currentPage + 1})" ${last ? 'disabled' : ''}>
                    다음
                </button>
            `;
        }

        pagination.innerHTML = paginationHTML;
    }

    /**
     * 장소 상세 모달 표시
     */

    showSpaceDetail(space) {
        const modal = this.elements.spaceDetailModal;
        const content = this.elements.spaceDetailContent;

        if (!modal || !content) return;

        const formatDate = (dateString) => {
            if (!dateString) return '-';
            return new Date(dateString).toLocaleString('ko-KR');
        };

        const formatPrice = (price) => {
            if (!price) return '-';
            return new Intl.NumberFormat('ko-KR').format(price) + '원';
        };

        // ✅ 수정된 부분: 올바른 속성명 사용
        const getOwnerName = (space) => {
            if (space.owner && space.owner.name) return space.owner.name;
            if (space.owner && space.owner.email) return space.owner.email;
            return '-';
        };

        const getLocation = (space) => {
            // venue 정보가 있는 경우
            if (space.venue) {
                if (space.venue.roadAddress) return space.venue.roadAddress;
                if (space.venue.jibunAddress) return space.venue.jibunAddress;
            }
            // 임시 호환 필드가 있는 경우
            if (space.address) return space.address;
            return '-';
        };

        content.innerHTML = `
        <div class="space-detail">
            <div class="detail-section">
                <h4>기본 정보</h4>
                <div class="detail-grid">
                    <div class="detail-item">
                        <span class="detail-label">ID</span>
                        <span class="detail-value">${space.id}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">제목</span>
                        <span class="detail-value">${space.title || '-'}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">상태</span>
                        <span class="detail-value">
                            <span class="status-badge ${space.isPublic ? 'public' : 'private'}">
                                ${space.isPublic ? '공개' : '비공개'}
                            </span>
                        </span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">소유자</span>
                        <span class="detail-value">${getOwnerName(space)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">위치</span>
                        <span class="detail-value">${getLocation(space)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">가격</span>
                        <span class="detail-value">${formatPrice(space.rentalFee)}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">등록일</span>
                        <span class="detail-value">${formatDate(space.createdAt)}</span>
                    </div>
                </div>
            </div>
            ${space.description ? `
            <div class="detail-section">
                <h4>설명</h4>
                <p>${space.description}</p>
            </div>
            ` : ''}
        </div>
    `;

        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }

    /**
     * 확인 모달 표시
     */
    showConfirm(title, message, action) {
        this.currentAction = action;

        if (this.elements.confirmTitle) {
            this.elements.confirmTitle.textContent = title;
        }
        if (this.elements.confirmMessage) {
            this.elements.confirmMessage.textContent = message;
        }
        if (this.elements.confirmModal) {
            this.elements.confirmModal.style.display = 'block';
            document.body.style.overflow = 'hidden';
        }
    }

    /**
     * 성공 메시지 표시
     */
    showSuccess(message) {
        alert(message); // 간단한 구현, 나중에 토스트 메시지로 교체 가능
    }

    /**
     * 모달 닫기
     */
    closeModal() {
        const modals = [this.elements.spaceDetailModal, this.elements.confirmModal];
        modals.forEach(modal => {
            if (modal) {
                modal.style.display = 'none';
            }
        });
        document.body.style.overflow = 'auto';
        this.currentAction = null;
    }

    /**
     * 로딩 표시/숨기기
     */
    showLoading(show = true) {
        console.log('🔄 로딩 상태:', show);

        if (show) {
            if (this.elements.searchLoading) {
                this.elements.searchLoading.style.display = 'flex';
                console.log('✅ 로딩 표시');
            } else {
                console.warn('⚠️ searchLoading 요소를 찾을 수 없음');
            }
            if (this.elements.spacesTableBody) {
                this.elements.spacesTableBody.innerHTML = '';
            }
            this.hideNoResults();
        } else {
            this.hideLoading();
        }
    }

    /**
     * 로딩 숨기기
     */
    hideLoading() {
        if (this.elements.searchLoading) {
            this.elements.searchLoading.style.display = 'none';
            console.log('✅ 로딩 숨김');
        }
    }

    /**
     * 검색 결과 없음 표시
     */
    showNoResults() {
        console.log('📭 "결과 없음" 표시');
        if (this.elements.noResults) {
            this.elements.noResults.style.display = 'block';
        }
        if (this.elements.pagination) {
            this.elements.pagination.innerHTML = '';
        }
    }

    /**
     * 검색 결과 없음 숨기기
     */
    hideNoResults() {
        if (this.elements.noResults) {
            this.elements.noResults.style.display = 'none';
        }
    }

    /**
     * 필터 초기화
     */
    resetFilters() {
        if (this.elements.ownerFilter) this.elements.ownerFilter.value = '';
        if (this.elements.titleFilter) this.elements.titleFilter.value = '';
        if (this.elements.isPublicFilter) this.elements.isPublicFilter.value = '';
    }
}