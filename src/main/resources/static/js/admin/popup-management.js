// /js/admin/popup-management.js

class PopupManagement {
    constructor() {
        this.currentPage = 1;
        this.pageSize = 10;
        this.currentFilters = {};
        this.selectedPopupId = null;
        this.categories = [];

        this.init();
    }

    init() {
        this.checkAdminAuth();
        this.bindEvents();
        this.loadCategories();
        this.loadPopups();
        this.loadStats();
    }

    checkAdminAuth() {
        const token = localStorage.getItem('authToken');
        const userRole = localStorage.getItem('userRole');

        if (!token || userRole !== 'ADMIN') {
            alert('관리자만 접근할 수 있습니다.');
            window.location.href = '/templates/auth/login.html';
        }
    }

    bindEvents() {
        // DOM 요소 존재 확인 후 이벤트 리스너 추가
        const searchBtn = document.getElementById('searchBtn');
        if (searchBtn) {
            searchBtn.addEventListener('click', () => this.search());
        }

        const resetBtn = document.getElementById('resetBtn');
        if (resetBtn) {
            resetBtn.addEventListener('click', () => this.resetFilters());
        }

        const searchKeyword = document.getElementById('searchKeyword');
        if (searchKeyword) {
            searchKeyword.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') this.search();
            });
        }

        // 모달 관련 이벤트
        const modalCloseBtn = document.getElementById('modalCloseBtn');
        if (modalCloseBtn) {
            modalCloseBtn.addEventListener('click', () => this.closeModal());
        }

        const closeModalBtn = document.getElementById('closeModalBtn');
        if (closeModalBtn) {
            closeModalBtn.addEventListener('click', () => this.closeModal());
        }

        const rejectModalCloseBtn = document.getElementById('rejectModalCloseBtn');
        if (rejectModalCloseBtn) {
            rejectModalCloseBtn.addEventListener('click', () => this.closeRejectModal());
        }

        const cancelRejectBtn = document.getElementById('cancelRejectBtn');
        if (cancelRejectBtn) {
            cancelRejectBtn.addEventListener('click', () => this.closeRejectModal());
        }

        // 액션 버튼
        const approveBtn = document.getElementById('approveBtn');
        if (approveBtn) {
            approveBtn.addEventListener('click', () => this.approvePopup());
        }

        const rejectBtn = document.getElementById('rejectBtn');
        if (rejectBtn) {
            rejectBtn.addEventListener('click', () => this.openRejectModal());
        }

        const suspendBtn = document.getElementById('suspendBtn');
        if (suspendBtn) {
            suspendBtn.addEventListener('click', () => this.suspendPopup());
        }

        const confirmRejectBtn = document.getElementById('confirmRejectBtn');
        if (confirmRejectBtn) {
            confirmRejectBtn.addEventListener('click', () => this.confirmReject());
        }

        // 모달 외부 클릭 시 닫기
        window.addEventListener('click', (e) => {
            const detailModal = document.getElementById('detailModal');
            const rejectModal = document.getElementById('rejectModal');

            if (detailModal && e.target === detailModal) {
                this.closeModal();
            }
            if (rejectModal && e.target === rejectModal) {
                this.closeRejectModal();
            }
        });
    }

    getAuthHeaders() {
        return {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`
        };
    }

    // 카테고리 로드 및 설정
    async loadCategories() {
        try {
            const response = await fetch('/api/auth/categories', {
                headers: {'Content-Type': 'application/json'}
            });

            if (response.ok) {
                const data = await response.json();
                this.categories = data.success && data.data ? data.data : [];
                this.renderCategoryFilter();
            }
        } catch (error) {
            console.error('카테고리 로드 실패:', error);
            this.categories = [];
        }
    }

    renderCategoryFilter() {
        const categoryFilter = document.getElementById('categoryFilter');
        if (!categoryFilter) return;

        let options = '<option value="">전체 카테고리</option>';
        this.categories.forEach(category => {
            options += `<option value="${category.slug}">${category.name}</option>`;
        });
        categoryFilter.innerHTML = options;
    }

    // 통계 로드
    async loadStats() {
        try {
            const response = await fetch('/api/admin/popups/stats', {
                headers: this.getAuthHeaders()
            });

            if (response.ok) {
                const stats = await response.json();
                this.updateStatsDisplay(stats);
            }
        } catch (error) {
            console.error('통계 로드 실패:', error);
        }
    }

    updateStatsDisplay(stats) {
        const statsContainer = document.getElementById('statsContainer');
        if (statsContainer && stats) {
            const total = stats.total || 0;
            const pending = stats.pending || 0;
            const approved = stats.approved || 0;
            const active = stats.active || 0;

            statsContainer.innerHTML = `
                <div class="stat-item">
                    <span class="stat-value">${total}</span>
                    <span class="stat-label">전체 팝업</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">${pending}</span>
                    <span class="stat-label">대기중</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">${approved}</span>
                    <span class="stat-label">승인됨</span>
                </div>
                <div class="stat-item">
                    <span class="stat-value">${active}</span>
                    <span class="stat-label">진행중</span>
                </div>
            `;
        }
    }

    // 팝업 목록 로드
    async loadPopups() {
        try {
            this.showLoading();

            const queryParams = new URLSearchParams({
                page: this.currentPage - 1,
                size: this.pageSize,
                ...this.currentFilters
            });

            const response = await fetch(`/api/admin/popups?${queryParams}`, {
                headers: this.getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`팝업 목록 로드 실패: ${response.status}`);
            }

            const data = await response.json();
            this.renderTable(data.content || []);
            this.renderPagination(data);

        } catch (error) {
            console.error('팝업 목록 로드 실패:', error);
            this.showError('팝업 목록을 불러오는데 실패했습니다.');
        }
    }

    showLoading() {
        const tableContainer = document.getElementById('tableContainer');
        if (tableContainer) {
            tableContainer.innerHTML = '<div class="loading">데이터를 불러오는 중...</div>';
        }
    }

    showError(message) {
        const tableContainer = document.getElementById('tableContainer');
        if (tableContainer) {
            tableContainer.innerHTML = `<div class="no-data">${message}</div>`;
        }
    }

    // 테이블 렌더링
    renderTable(popups) {
        const tableContainer = document.getElementById('tableContainer');
        if (!tableContainer) return;

        if (!popups || popups.length === 0) {
            tableContainer.innerHTML = '<div class="no-data">등록된 팝업이 없습니다.</div>';
            return;
        }

        const tableHTML = `
            <table class="popup-table">
                <thead>
                    <tr>
                        <th>ID</th><th>팝업명</th><th>브랜드</th><th>카테고리</th>
                        <th>기간</th><th>상태</th><th>등록일</th><th>액션</th>
                    </tr>
                </thead>
                <tbody>
                    ${popups.map(popup => `
                        <tr>
                            <td>${popup.id}</td>
                            <td>
                                <div style="font-weight: 500;">${popup.title}</div>
                                <div style="font-size: 12px; color: #666;">${popup.venueName || popup.venueAddress || ''}</div>
                            </td>
                            <td>${popup.brandName || '미등록'}</td>
                            <td><span class="category-badge">${popup.categoryName || this.getCategoryText(popup.category) || '미분류'}</span></td>
                            <td>
                                <div>${this.formatDate(popup.startDate)} ~</div>
                                <div>${this.formatDate(popup.endDate)}</div>
                            </td>
                            <td><span class="status-badge ${popup.status.toLowerCase()}">${this.getStatusText(popup.status)}</span></td>
                            <td>${this.formatDate(popup.createdAt)}</td>
                            <td>
                                <button class="button button-sm button-primary" onclick="popupManagement.viewDetail(${popup.id})">
                                    상세보기
                                </button>
                            </td>
                        </tr>
                    `).join('')}
                </tbody>
            </table>
        `;

        tableContainer.innerHTML = tableHTML;
    }

    // 페이지네이션
    renderPagination(pageInfo) {
        const paginationContainer = document.getElementById('pagination');
        if (!paginationContainer) return;

        const { number, totalPages, first, last } = pageInfo;

        if (totalPages <= 1) {
            paginationContainer.innerHTML = '';
            return;
        }

        const startPage = Math.max(0, number - 2);
        const endPage = Math.min(totalPages - 1, number + 2);

        let html = `<button class="page-button" ${first ? 'disabled' : ''} onclick="popupManagement.goToPage(${number})">이전</button>`;

        for (let i = startPage; i <= endPage; i++) {
            const activeClass = i === number ? 'active' : '';
            html += `<button class="page-button ${activeClass}" onclick="popupManagement.goToPage(${i + 1})">${i + 1}</button>`;
        }

        html += `<button class="page-button" ${last ? 'disabled' : ''} onclick="popupManagement.goToPage(${number + 2})">다음</button>`;

        paginationContainer.innerHTML = html;
    }

    goToPage(page) {
        this.currentPage = page;
        this.loadPopups();
    }

    // 검색 및 필터
    search() {
        const statusFilter = document.getElementById('statusFilter');
        const categoryFilter = document.getElementById('categoryFilter');
        const searchKeyword = document.getElementById('searchKeyword');

        const filters = {
            status: statusFilter ? statusFilter.value : '',
            category: categoryFilter ? categoryFilter.value : '',
            keyword: searchKeyword ? searchKeyword.value.trim() : ''
        };

        this.currentFilters = Object.fromEntries(
            Object.entries(filters).filter(([key, value]) => value)
        );

        this.currentPage = 1;
        this.loadPopups();
    }

    resetFilters() {
        const statusFilter = document.getElementById('statusFilter');
        const categoryFilter = document.getElementById('categoryFilter');
        const searchKeyword = document.getElementById('searchKeyword');

        if (statusFilter) statusFilter.value = '';
        if (categoryFilter) categoryFilter.value = '';
        if (searchKeyword) searchKeyword.value = '';

        this.currentFilters = {};
        this.currentPage = 1;
        this.loadPopups();
    }

    // 상세보기 및 모달
    async viewDetail(popupId) {
        try {
            const response = await fetch(`/api/admin/popups/${popupId}`, {
                headers: this.getAuthHeaders()
            });

            if (!response.ok) throw new Error('팝업 상세 정보 로드 실패');

            const popup = await response.json();
            this.showDetailModal(popup);

        } catch (error) {
            console.error('팝업 상세 정보 로드 실패:', error);
            alert('팝업 상세 정보를 불러오는데 실패했습니다.');
        }
    }

    showDetailModal(popup) {
        this.selectedPopupId = popup.id;

        const detailModal = document.getElementById('detailModal');
        if (!detailModal) return;

        // 모달 내용 업데이트
        const modalContent = detailModal.querySelector('.modal-body');
        if (modalContent) {
            modalContent.innerHTML = this.generateModalContent(popup);
        }

        detailModal.style.display = 'block';
    }

    generateModalContent(popup) {
        return `
            <div class="popup-detail">
                <h3>${popup.title}</h3>
                <div class="detail-grid">
                    <div class="detail-item">
                        <strong>브랜드:</strong> ${popup.brandName || '미등록'}
                    </div>
                    <div class="detail-item">
                        <strong>카테고리:</strong> ${this.getCategoryText(popup.category)}
                    </div>
                    <div class="detail-item">
                        <strong>기간:</strong> ${this.formatDate(popup.startDate)} ~ ${this.formatDate(popup.endDate)}
                    </div>
                    <div class="detail-item">
                        <strong>상태:</strong> ${this.getStatusText(popup.status)}
                    </div>
                    <div class="detail-item">
                        <strong>장소:</strong> ${popup.venueName || '미정'}
                    </div>
                    <div class="detail-item">
                        <strong>주소:</strong> ${popup.venueAddress || '미정'}
                    </div>
                    <div class="detail-item full-width">
                        <strong>설명:</strong><br>
                        ${popup.description || '설명이 없습니다.'}
                    </div>
                </div>
            </div>
        `;
    }

    openRejectModal() {
        const rejectModal = document.getElementById('rejectModal');
        if (rejectModal) {
            rejectModal.style.display = 'block';
        }
    }

    async approvePopup() {
        if (!this.selectedPopupId) return;
        if (!confirm('이 팝업을 승인하시겠습니까?')) return;

        try {
            const response = await fetch(`/api/admin/popups/${this.selectedPopupId}/approve`, {
                method: 'POST',
                headers: this.getAuthHeaders()
            });

            if (!response.ok) throw new Error('팝업 승인 실패');

            alert('팝업이 승인되었습니다.');
            this.closeModal();
            this.loadPopups();

        } catch (error) {
            console.error('팝업 승인 실패:', error);
            alert('팝업 승인에 실패했습니다.');
        }
    }

    async confirmReject() {
        if (!this.selectedPopupId) return;

        const rejectReasonTextarea = document.getElementById('rejectReason');
        const reason = rejectReasonTextarea ? rejectReasonTextarea.value.trim() : '';

        if (!reason) {
            alert('거부 사유를 입력해주세요.');
            return;
        }

        try {
            const response = await fetch(`/api/admin/popups/${this.selectedPopupId}/reject`, {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify({ reason })
            });

            if (!response.ok) throw new Error('팝업 거부 실패');

            alert('팝업이 거부되었습니다.');
            this.closeRejectModal();
            this.closeModal();
            this.loadPopups();

        } catch (error) {
            console.error('팝업 거부 실패:', error);
            alert('팝업 거부에 실패했습니다.');
        }
    }

    async suspendPopup() {
        if (!this.selectedPopupId) return;
        if (!confirm('이 팝업을 정지하시겠습니까?')) return;

        try {
            const response = await fetch(`/api/admin/popups/${this.selectedPopupId}/suspend`, {
                method: 'POST',
                headers: this.getAuthHeaders()
            });

            if (!response.ok) throw new Error('팝업 정지 실패');

            alert('팝업이 정지되었습니다.');
            this.closeModal();
            this.loadPopups();

        } catch (error) {
            console.error('팝업 정지 실패:', error);
            alert('팝업 정지에 실패했습니다.');
        }
    }

    closeModal() {
        const detailModal = document.getElementById('detailModal');
        if (detailModal) {
            detailModal.style.display = 'none';
        }
        this.selectedPopupId = null;
    }

    closeRejectModal() {
        const rejectModal = document.getElementById('rejectModal');
        if (rejectModal) {
            rejectModal.style.display = 'none';
        }
    }

    // 유틸리티 함수
    getStatusText(status) {
        const statusMap = {
            'PLANNED': '계획중',
            'ACTIVE': '진행중',
            'COMPLETED': '완료됨',
            'CANCELLED': '취소됨',
            'PENDING': '대기중',
            'APPROVED': '승인됨',
            'REJECTED': '거부됨'
        };
        return statusMap[status] || status;
    }

    getCategoryText(category) {
        const found = this.categories.find(cat =>
            cat.slug === category || cat.name === category
        );

        if (found) return found.name;

        const categoryMap = {
            'FOOD': '푸드',
            'FASHION': '패션',
            'BEAUTY': '뷰티',
            'LIFESTYLE': '라이프스타일',
            'CULTURE': '문화/예술',
            'SPORTS': '스포츠',
            'TECH': '기술/IT',
            'OTHER': '기타'
        };
        return categoryMap[category] || category;
    }

    formatDate(dateString) {
        if (!dateString) return '-';

        return new Date(dateString).toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
    }
}

// 전역 변수로 인스턴스 생성
let popupManagement;

// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', function() {
    popupManagement = new PopupManagement();
});