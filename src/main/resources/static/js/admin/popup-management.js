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
        // 검색 및 필터
        document.getElementById('searchBtn').addEventListener('click', () => this.search());
        document.getElementById('resetBtn').addEventListener('click', () => this.resetFilters());
        document.getElementById('searchKeyword').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') this.search();
        });

        // 모달
        document.getElementById('modalCloseBtn').addEventListener('click', () => this.closeModal());
        document.getElementById('closeModalBtn').addEventListener('click', () => this.closeModal());
        document.getElementById('rejectModalCloseBtn').addEventListener('click', () => this.closeRejectModal());
        document.getElementById('cancelRejectBtn').addEventListener('click', () => this.closeRejectModal());

        // 액션 버튼
        document.getElementById('approveBtn').addEventListener('click', () => this.approvePopup());
        document.getElementById('rejectBtn').addEventListener('click', () => this.openRejectModal());
        document.getElementById('suspendBtn').addEventListener('click', () => this.suspendPopup());
        document.getElementById('confirmRejectBtn').addEventListener('click', () => this.confirmReject());

        // 모달 외부 클릭 시 닫기
        window.addEventListener('click', (e) => {
            if (e.target === document.getElementById('detailModal')) this.closeModal();
            if (e.target === document.getElementById('rejectModal')) this.closeRejectModal();
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
                this.categories = data.success && data.data ? data.data : this.getDefaultCategories();
            } else {
                this.categories = this.getDefaultCategories();
            }
        } catch (error) {
            console.error('카테고리 로드 실패:', error);
            this.categories = this.getDefaultCategories();
        }
        this.populateCategoryFilter();
    }

    getDefaultCategories() {
        return [
            {id: 1, name: '푸드', slug: 'FOOD'},
            {id: 2, name: '패션', slug: 'FASHION'},
            {id: 3, name: '뷰티', slug: 'BEAUTY'},
            {id: 4, name: '라이프스타일', slug: 'LIFESTYLE'},
            {id: 5, name: '문화/예술', slug: 'CULTURE'},
            {id: 6, name: '스포츠', slug: 'SPORTS'},
            {id: 7, name: '기술/IT', slug: 'TECH'},
            {id: 8, name: '기타', slug: 'OTHER'}
        ];
    }

    populateCategoryFilter() {
        const categoryFilter = document.getElementById('categoryFilter');

        // 기존 옵션 제거 (전체 옵션 제외)
        while (categoryFilter.children.length > 1) {
            categoryFilter.removeChild(categoryFilter.lastChild);
        }

        // 카테고리 옵션 추가
        this.categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.slug || category.name;
            option.textContent = category.name;
            categoryFilter.appendChild(option);
        });
    }

    // 데이터 로드
    async loadStats() {
        try {
            const response = await fetch('/api/admin/popups/stats', {
                headers: this.getAuthHeaders()
            });

            if (response.ok) {
                const stats = await response.json();
                document.getElementById('totalCount').textContent = stats.total || 0;
                document.getElementById('pendingCount').textContent = stats.pending || 0;
                document.getElementById('activeCount').textContent = stats.active || 0;
            }
        } catch (error) {
            console.error('통계 로드 실패:', error);
        }
    }

    async loadPopups() {
        try {
            this.showLoading();

            const params = new URLSearchParams({
                page: this.currentPage - 1,
                size: this.pageSize,
                ...this.currentFilters
            });

            const response = await fetch(`/api/admin/popups?${params}`, {
                headers: this.getAuthHeaders()
            });

            if (!response.ok) throw new Error('팝업 목록 로드 실패');

            const data = await response.json();
            this.renderTable(data.content);
            this.renderPagination(data);
            this.loadStats();

        } catch (error) {
            console.error('팝업 목록 로드 실패:', error);
            this.showError('팝업 목록을 불러오는데 실패했습니다.');
        }
    }

    showLoading() {
        document.getElementById('tableContainer').innerHTML =
            '<div class="loading">데이터를 불러오는 중...</div>';
    }

    showError(message) {
        document.getElementById('tableContainer').innerHTML =
            `<div class="no-data">${message}</div>`;
    }

    // 테이블 렌더링
    renderTable(popups) {
        if (!popups || popups.length === 0) {
            document.getElementById('tableContainer').innerHTML =
                '<div class="no-data">등록된 팝업이 없습니다.</div>';
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

        document.getElementById('tableContainer').innerHTML = tableHTML;
    }

// 페이지네이션
    renderPagination(pageInfo) {
        const {number, totalPages, first, last} = pageInfo;

        if (totalPages <= 1) {
            document.getElementById('pagination').innerHTML = '';
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

        document.getElementById('pagination').innerHTML = html;
    }


    goToPage(page) {
        this.currentPage = page;
        this.loadPopups();
    }

    // 검색 및 필터
    search() {
        const filters = {
            status: document.getElementById('statusFilter').value,
            category: document.getElementById('categoryFilter').value,
            keyword: document.getElementById('searchKeyword').value.trim()
        };

        this.currentFilters = Object.fromEntries(
            Object.entries(filters).filter(([key, value]) => value)
        );

        this.currentPage = 1;
        this.loadPopups();
    }

    resetFilters() {
        document.getElementById('statusFilter').value = '';
        document.getElementById('categoryFilter').value = '';
        document.getElementById('searchKeyword').value = '';

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
        document.getElementById('modalBody').innerHTML = this.buildDetailHTML(popup);
        this.updateActionButtons(popup.status);
        document.getElementById('detailModal').style.display = 'block';
    }

    buildDetailHTML(popup) {
        const locationText = popup.venueName || popup.venueAddress || '장소 정보 없음';

        return `
    <div class="popup-detail-content">
        <div class="detail-section">
            <h3>기본 정보</h3>
            <div class="detail-grid">
                <div class="detail-item">
                    <label>팝업 ID:</label>
                    <div class="detail-value">${popup.id}</div>
                </div>
                <div class="detail-item">
                    <label>팝업명:</label>
                    <div class="detail-value">${popup.title}</div>
                </div>
                <div class="detail-item">
                    <label>브랜드:</label>
                    <div class="detail-value">${popup.brandName || '미등록'}</div>
                </div>
                <div class="detail-item">
                    <label>주최자:</label>
                    <div class="detail-value">${popup.hostName || '정보 없음'}</div>
                </div>
                <div class="detail-item">
                    <label>카테고리:</label>
                    <div class="detail-value">${popup.categoryName || this.getCategoryText(popup.category) || '미분류'}</div>
                </div>
                <div class="detail-item">
                    <label>장소:</label>
                    <div class="detail-value">${locationText}</div>
                </div>
                <div class="detail-item">
                    <label>상태:</label>
                    <div class="detail-value">
                        <span class="status-badge ${popup.status.toLowerCase()}">
                            ${this.getStatusText(popup.status)}
                        </span>
                    </div>
                </div>
                <div class="detail-item">
                    <label>기간:</label>
                    <div class="detail-value">
                        ${this.formatDate(popup.startDate)} ~ ${this.formatDate(popup.endDate)}
                    </div>
                </div>
            </div>
        </div>

        <div class="detail-section">
            <h3>상세 설명</h3>
            <div class="detail-value" style="white-space: pre-wrap; : 1.6;">
                ${popup.description || '설명이 없습니다.'}
            </div>
        </div>

        ${popup.rejectReason ? `
            <div class="detail-section">
                <h3>거부 사유</h3>
                <div class="detail-value reject-reason">
                    ${popup.rejectReason}
                </div>
            </div>
        ` : ''}
    </div>
`;
    }

    updateActionButtons(status) {
        const buttons = {
            approveBtn: document.getElementById('approveBtn'),
            rejectBtn: document.getElementById('rejectBtn'),
            suspendBtn: document.getElementById('suspendBtn')
        };

        // 모든 버튼 숨김
        Object.values(buttons).forEach(btn => btn.style.display = 'none');

        // 상태별 버튼 표시
        if (status === 'PENDING') {
            buttons.approveBtn.style.display = 'block';
            buttons.rejectBtn.style.display = 'block';
        } else if (['APPROVED', 'ACTIVE'].includes(status)) {
            buttons.suspendBtn.style.display = 'block';
        }
    }

    // 팝업 관리 액션
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

    openRejectModal() {
        document.getElementById('rejectReason').value = '';
        document.getElementById('rejectModal').style.display = 'block';
    }

    async confirmReject() {
        const reason = document.getElementById('rejectReason').value.trim();

        if (!reason) {
            alert('거부 사유를 입력해주세요.');
            return;
        }

        try {
            const response = await fetch(`/api/admin/popups/${this.selectedPopupId}/reject`, {
                method: 'POST',
                headers: this.getAuthHeaders(),
                body: JSON.stringify({reason})
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
        document.getElementById('detailModal').style.display = 'none';
        this.selectedPopupId = null;
    }

    closeRejectModal() {
        document.getElementById('rejectModal').style.display = 'none';
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
document.addEventListener('DOMContentLoaded', async function () {
    popupManagement = new PopupManagement();
});