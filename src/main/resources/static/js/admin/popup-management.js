// /js/admin/popup-management.js

class PopupManagement {
    constructor() {
        this.currentPage = 1;
        this.pageSize = 10;
        this.totalPages = 0;
        this.currentFilters = {};
        this.selectedPopupId = null;

        this.init();
    }

    init() {
        this.checkAdminAuth();
        this.bindEvents();
        this.loadPopups();
        this.loadStats();
    }

    // 관리자 권한 확인
    checkAdminAuth() {
        const token = localStorage.getItem('authToken');
        const userRole = localStorage.getItem('userRole');

        if (!token || userRole !== 'ADMIN') {
            alert('관리자만 접근할 수 있습니다.');
            window.location.href = '/templates/auth/login.html';
            return;
        }
    }

    // 이벤트 바인딩
    bindEvents() {
        // 검색 버튼
        document.getElementById('searchBtn').addEventListener('click', () => {
            this.search();
        });

        // 초기화 버튼
        document.getElementById('resetBtn').addEventListener('click', () => {
            this.resetFilters();
        });

        // Enter 키로 검색
        document.getElementById('searchKeyword').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.search();
            }
        });

        // 모달 관련 이벤트
        document.getElementById('modalCloseBtn').addEventListener('click', () => {
            this.closeModal();
        });

        document.getElementById('closeModalBtn').addEventListener('click', () => {
            this.closeModal();
        });

        document.getElementById('rejectModalCloseBtn').addEventListener('click', () => {
            this.closeRejectModal();
        });

        document.getElementById('cancelRejectBtn').addEventListener('click', () => {
            this.closeRejectModal();
        });

        // 액션 버튼들
        document.getElementById('approveBtn').addEventListener('click', () => {
            this.approvePopup();
        });

        document.getElementById('rejectBtn').addEventListener('click', () => {
            this.openRejectModal();
        });

        document.getElementById('suspendBtn').addEventListener('click', () => {
            this.suspendPopup();
        });

        document.getElementById('confirmRejectBtn').addEventListener('click', () => {
            this.confirmReject();
        });

        // 모달 외부 클릭 시 닫기
        window.addEventListener('click', (e) => {
            const detailModal = document.getElementById('detailModal');
            const rejectModal = document.getElementById('rejectModal');

            if (e.target === detailModal) {
                this.closeModal();
            }
            if (e.target === rejectModal) {
                this.closeRejectModal();
            }
        });
    }

    // 인증 토큰 가져오기
    getAuthToken() {
        return localStorage.getItem('authToken');
    }

    // API 요청 헤더 생성
    getAuthHeaders() {
        return {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${this.getAuthToken()}`
        };
    }

    // 통계 정보 로드
    // 통계 정보 로드 - 백엔드 응답에 맞게 수정
    async loadStats() {
        try {
            const response = await fetch('/api/admin/popups/stats', {
                headers: this.getAuthHeaders()
            });

            if (!response.ok) throw new Error('통계 로딩 실패');

            const stats = await response.json();

            // 백엔드에서 제공하는 필드명에 맞게 수정
            document.getElementById('totalCount').textContent = stats.total || 0;
            document.getElementById('pendingCount').textContent = stats.planning || 0;  // planning -> pendingCount
            document.getElementById('activeCount').textContent = stats.ongoing || 0;    // ongoing -> activeCount

        } catch (error) {
            console.error('통계 로딩 실패:', error);
        }
    }

    // 팝업 목록 로드
    async loadPopups() {
        try {
            this.showLoading();

            const params = new URLSearchParams({
                page: this.currentPage - 1, // 백엔드는 0부터 시작
                size: this.pageSize,
                ...this.currentFilters
            });

            const response = await fetch(`/api/admin/popups?${params}`, {
                headers: this.getAuthHeaders()
            });

            if (!response.ok) throw new Error('팝업 목록 로딩 실패');

            const data = await response.json();
            this.renderTable(data.content);
            this.renderPagination(data);

            // 통계 업데이트
            this.loadStats();

        } catch (error) {
            console.error('팝업 목록 로딩 실패:', error);
            this.showError('팝업 목록을 불러오는데 실패했습니다.');
        }
    }

    // 로딩 상태 표시
    showLoading() {
        document.getElementById('tableContainer').innerHTML =
            '<div class="loading">데이터를 불러오는 중...</div>';
    }

    // 에러 상태 표시
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
                        <th>ID</th>
                        <th>팝업명</th>
                        <th>주최자</th>
                        <th>카테고리</th>
                        <th>기간</th>
                        <th>상태</th>
                        <th>등록일</th>
                        <th>액션</th>
                    </tr>
                </thead>
                <tbody>
                    ${popups.map(popup => this.renderTableRow(popup)).join('')}
                </tbody>
            </table>
        `;

        document.getElementById('tableContainer').innerHTML = tableHTML;
    }

    // 테이블 행 렌더링
    renderTableRow(popup) {
        const statusClass = popup.status.toLowerCase();
        const statusText = this.getStatusText(popup.status);
        const categoryText = this.getCategoryText(popup.category);

        return `
            <tr>
                <td>${popup.id}</td>
                <td>
                    <div style="font-weight: 500;">${popup.title}</div>
                    <div style="font-size: 12px; color: #666;">${popup.location}</div>
                </td>
                <td>${popup.hostName}</td>
                <td><span class="category-badge">${categoryText}</span></td>
                <td>
                    <div>${this.formatDate(popup.startDate)} ~</div>
                    <div>${this.formatDate(popup.endDate)}</div>
                </td>
                <td><span class="status-badge ${statusClass}">${statusText}</span></td>
                <td>${this.formatDate(popup.createdAt)}</td>
                <td>
                    <div class="action-buttons">
                        <button class="btn btn-sm btn-primary" onclick="popupManagement.viewDetail(${popup.id})">
                            상세보기
                        </button>
                    </div>
                </td>
            </tr>
        `;
    }

    // 상태 텍스트 변환
    getStatusText(status) {
        const statusMap = {
            'PENDING': '대기중',
            'APPROVED': '승인됨',
            'REJECTED': '거부됨',
            'ACTIVE': '진행중',
            'COMPLETED': '완료됨',
            'CANCELLED': '취소됨'
        };
        return statusMap[status] || status;
    }

    // 카테고리 텍스트 변환
    getCategoryText(category) {
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

    // 날짜 포맷팅
    formatDate(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
    }

    // 페이지네이션 렌더링
    renderPagination(pageData) {
        const { totalPages, number, first, last } = pageData;
        this.totalPages = totalPages;

        if (totalPages <= 1) {
            document.getElementById('pagination').innerHTML = '';
            return;
        }

        let paginationHTML = '';

        // 이전 페이지 버튼
        paginationHTML += `
            <button ${first ? 'disabled' : ''} onclick="popupManagement.goToPage(${number})">
                이전
            </button>
        `;

        // 페이지 번호들
        const startPage = Math.max(0, number - 2);
        const endPage = Math.min(totalPages - 1, number + 2);

        for (let i = startPage; i <= endPage; i++) {
            paginationHTML += `
                <button class="${i === number ? 'active' : ''}" 
                        onclick="popupManagement.goToPage(${i + 1})">
                    ${i + 1}
                </button>
            `;
        }

        // 다음 페이지 버튼
        paginationHTML += `
            <button ${last ? 'disabled' : ''} onclick="popupManagement.goToPage(${number + 2})">
                다음
            </button>
        `;

        document.getElementById('pagination').innerHTML = paginationHTML;
    }

    // 페이지 이동
    goToPage(page) {
        this.currentPage = page;
        this.loadPopups();
    }

    // 검색
    search() {
        const filters = {
            status: document.getElementById('statusFilter').value,
            category: document.getElementById('categoryFilter').value,
            keyword: document.getElementById('searchKeyword').value.trim()
        };

        // 빈 값 제거
        this.currentFilters = Object.fromEntries(
            Object.entries(filters).filter(([key, value]) => value)
        );

        this.currentPage = 1;
        this.loadPopups();
    }

    // 필터 초기화
    resetFilters() {
        document.getElementById('statusFilter').value = '';
        document.getElementById('categoryFilter').value = '';
        document.getElementById('searchKeyword').value = '';

        this.currentFilters = {};
        this.currentPage = 1;
        this.loadPopups();
    }

    // 팝업 상세보기
    async viewDetail(popupId) {
        try {
            const response = await fetch(`/api/admin/popups/${popupId}`, {
                headers: this.getAuthHeaders()
            });

            if (!response.ok) throw new Error('팝업 상세 정보 로딩 실패');

            const popup = await response.json();
            this.showDetailModal(popup);

        } catch (error) {
            console.error('팝업 상세 정보 로딩 실패:', error);
            alert('팝업 상세 정보를 불러오는데 실패했습니다.');
        }
    }

    // 상세 모달 표시
    showDetailModal(popup) {
        this.selectedPopupId = popup.id;

        const modalBody = document.getElementById('modalBody');
        modalBody.innerHTML = this.createDetailHTML(popup);

        // 액션 버튼 표시/숨김
        this.updateActionButtons(popup.status);

        document.getElementById('detailModal').style.display = 'block';
    }

    // 상세 정보 HTML 생성
    createDetailHTML(popup) {
        return `
            <div class="popup-detail">
                <div class="detail-section">
                    <h3>기본 정보</h3>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <span class="detail-label">팝업 ID</span>
                            <span class="detail-value">${popup.id}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">팝업명</span>
                            <span class="detail-value">${popup.title}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">주최자</span>
                            <span class="detail-value">${popup.hostName} (${popup.hostEmail})</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">카테고리</span>
                            <span class="detail-value">${this.getCategoryText(popup.category)}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">상태</span>
                            <span class="detail-value">
                                <span class="status-badge ${popup.status.toLowerCase()}">
                                    ${this.getStatusText(popup.status)}
                                </span>
                            </span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">등록일</span>
                            <span class="detail-value">${this.formatDate(popup.createdAt)}</span>
                        </div>
                    </div>
                </div>

                <div class="detail-section">
                    <h3>팝업 상세</h3>
                    <div class="detail-grid">
                        <div class="detail-item">
                            <span class="detail-label">위치</span>
                            <span class="detail-value">${popup.location}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">상세 주소</span>
                            <span class="detail-value">${popup.address}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">시작일</span>
                            <span class="detail-value">${this.formatDate(popup.startDate)}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">종료일</span>
                            <span class="detail-value">${this.formatDate(popup.endDate)}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">운영시간</span>
                            <span class="detail-value">${popup.openTime} - ${popup.closeTime}</span>
                        </div>
                        <div class="detail-item">
                            <span class="detail-label">참가비</span>
                            <span class="detail-value">${popup.entryFee ? popup.entryFee.toLocaleString() + '원' : '무료'}</span>
                        </div>
                    </div>
                </div>

                <div class="detail-section">
                    <h3>설명</h3>
                    <div class="detail-value" style="white-space: pre-wrap; line-height: 1.6;">
                        ${popup.description || '설명이 없습니다.'}
                    </div>
                </div>

                ${popup.images && popup.images.length > 0 ? `
                    <div class="detail-section">
                        <h3>팝업 이미지</h3>
                        <div class="popup-images">
                            ${popup.images.map(image => `
                                <img src="${image.url}" alt="팝업 이미지" class="popup-image" 
                                     onclick="window.open('${image.url}', '_blank')">
                            `).join('')}
                        </div>
                    </div>
                ` : ''}

                ${popup.rejectReason ? `
                    <div class="detail-section">
                        <h3>거부 사유</h3>
                        <div class="detail-value" style="color: #dc3545; white-space: pre-wrap; line-height: 1.6;">
                            ${popup.rejectReason}
                        </div>
                    </div>
                ` : ''}
            </div>
        `;
    }

    // 액션 버튼 업데이트
    updateActionButtons(status) {
        const approveBtn = document.getElementById('approveBtn');
        const rejectBtn = document.getElementById('rejectBtn');
        const suspendBtn = document.getElementById('suspendBtn');

        // 모든 버튼 숨김
        approveBtn.style.display = 'none';
        rejectBtn.style.display = 'none';
        suspendBtn.style.display = 'none';

        // 상태에 따라 버튼 표시
        switch (status) {
            case 'PENDING':
                approveBtn.style.display = 'block';
                rejectBtn.style.display = 'block';
                break;
            case 'APPROVED':
            case 'ACTIVE':
                suspendBtn.style.display = 'block';
                break;
        }
    }

    // 팝업 승인
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

    // 팝업 거부 모달 열기
    openRejectModal() {
        document.getElementById('rejectReason').value = '';
        document.getElementById('rejectModal').style.display = 'block';
    }

    // 팝업 거부 확정
    async confirmReject() {
        const reason = document.getElementById('rejectReason').value.trim();

        if (!reason) {
            alert('거부 사유를 입력해주세요.');
            return;
        }

        if (!this.selectedPopupId) return;

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

    // 팝업 정지
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

    // 모달 닫기
    closeModal() {
        document.getElementById('detailModal').style.display = 'none';
        this.selectedPopupId = null;
    }

    // 거부 모달 닫기
    closeRejectModal() {
        document.getElementById('rejectModal').style.display = 'none';
    }
}

// 전역 변수로 인스턴스 생성
let popupManagement;

// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', function() {
    popupManagement = new PopupManagement();
});