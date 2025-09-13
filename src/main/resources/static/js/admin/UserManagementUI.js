/**
 * 회원 관리 UI 클래스
 */
class UserManagementUI {
    constructor() {
        this.elements = {
            searchType: document.getElementById('searchType'),
            searchKeyword: document.getElementById('searchKeyword'),
            roleFilter: document.getElementById('roleFilter'),
            searchBtn: document.getElementById('searchBtn'),
            resetBtn: document.getElementById('resetBtn'),
            userTableBody: document.getElementById('userTableBody'),
            totalCount: document.getElementById('totalCount'),
            pagination: document.getElementById('pagination'),
            searchLoading: document.getElementById('searchLoading'),
            noResults: document.getElementById('noResults'),
            searchResults: document.querySelector('.search-results'),
            userDetailModal: document.getElementById('userDetailModal'),
            userDetailContent: document.getElementById('userDetailContent'),
            closeModal: document.getElementById('closeModal'),
            upgradeRequestCount: document.getElementById('upgradeRequestCount')
        };
    }

    /**
     * 검색 결과 표시
     * @param {Object} data 검색 결과 데이터
     */
    displaySearchResults(data) {
        this.elements.totalCount.textContent = data.totalElements;

        if (data.content && data.content.length > 0) {
            this.elements.userTableBody.innerHTML = data.content.map(user => `
                <tr>
                    <td>${this.escapeHtml(user.name)}</td>
                    <td>${this.escapeHtml(user.nickname)}</td>
                    <td>${this.escapeHtml(user.email)}</td>
                    <td>${this.escapeHtml(user.phone || '-')}</td>
                    <td><span class="role-badge ${user.role.toLowerCase()}">${this.getRoleText(user.role)}</span></td>
                    <td>${this.formatDate(user.createdAt)}</td>
                    <td><span class="status-badge ${user.status === 'ACTIVE' ? 'active' : 'inactive'}">${user.status === 'ACTIVE' ? '활성' : '비활성'}</span></td>
                    <td>
                        <button type="button" class="detail-btn" data-user-id="${user.userId}">상세</button>
                    </td>
                </tr>
            `).join('');

            this.showSearchResults();
        } else {
            this.showNoResults();
        }
    }

    /**
     * 페이징 설정
     * @param {Object} data 페이징 데이터
     * @param {number} currentPage 현재 페이지
     * @param {Function} onPageClick 페이지 클릭 콜백
     */
    setupPagination(data, currentPage, onPageClick) {
        const totalPages = data.totalPages;

        if (totalPages <= 1) {
            this.elements.pagination.innerHTML = '';
            return;
        }

        let paginationHtml = '';

        // 이전 페이지
        if (currentPage > 1) {
            paginationHtml += `<button type="button" class="page-btn" data-page="${currentPage - 1}">이전</button>`;
        }

        // 페이지 번호들
        const startPage = Math.max(1, currentPage - 2);
        const endPage = Math.min(totalPages, currentPage + 2);

        for (let i = startPage; i <= endPage; i++) {
            paginationHtml += `<button type="button" class="page-btn ${i === currentPage ? 'active' : ''}" data-page="${i}">${i}</button>`;
        }

        // 다음 페이지
        if (currentPage < totalPages) {
            paginationHtml += `<button type="button" class="page-btn" data-page="${currentPage + 1}">다음</button>`;
        }

        this.elements.pagination.innerHTML = paginationHtml;

        // 페이지 버튼 이벤트 바인딩
        this.elements.pagination.querySelectorAll('.page-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const page = parseInt(e.target.dataset.page);
                onPageClick(page);
            });
        });
    }

    /**
     * 사용자 상세 정보 모달 표시
     * @param {Object} user 사용자 정보
     */
    showUserDetailModal(user) {
        this.elements.userDetailContent.innerHTML = `
            <div class="user-detail">
                <div class="detail-row">
                    <label>이름:</label>
                    <span>${this.escapeHtml(user.name)}</span>
                </div>
                <div class="detail-row">
                    <label>닉네임:</label>
                    <span>${this.escapeHtml(user.nickname)}</span>
                </div>
                <div class="detail-row">
                    <label>이메일:</label>
                    <span>${this.escapeHtml(user.email)}</span>
                </div>
                <div class="detail-row">
                    <label>전화번호:</label>
                    <span>${this.escapeHtml(user.phone || '-')}</span>
                </div>
                <div class="detail-row">
                    <label>역할:</label>
                    <span class="role-badge ${user.role.toLowerCase()}">${this.getRoleText(user.role)}</span>
                </div>
                <div class="detail-row">
                    <label>상태:</label>
                    <span class="status-badge ${user.status === 'ACTIVE' ? 'active' : 'inactive'}">${user.status === 'ACTIVE' ? '활성' : '비활성'}</span>
                </div>
                <div class="detail-row">
                    <label>가입일:</label>
                    <span>${this.formatDate(user.createdAt)}</span>
                </div>
                <div class="detail-row">
                    <label>최근 로그인:</label>
                    <span>${user.lastLoginAt ? this.formatDate(user.lastLoginAt) : '-'}</span>
                </div>
                ${user.interestTags && user.interestTags.length > 0 ? `
                <div class="detail-row">
                    <label>관심사:</label>
                    <div class="interest-tags">
                        ${user.interestTags.map(tag => `<span class="tag">${this.escapeHtml(tag)}</span>`).join('')}
                    </div>
                </div>
                ` : ''}
            </div>
        `;

        this.elements.userDetailModal.style.display = 'block';
    }

    /**
     * 모달 닫기
     */
    closeModal() {
        this.elements.userDetailModal.style.display = 'none';
    }

    /**
     * 로딩 표시
     */
    showLoading() {
        this.elements.searchLoading.style.display = 'block';
        this.elements.searchResults.style.display = 'none';
        this.elements.noResults.style.display = 'none';
    }

    /**
     * 로딩 숨김
     */
    hideLoading() {
        this.elements.searchLoading.style.display = 'none';
    }

    /**
     * 검색 결과 표시
     */
    showSearchResults() {
        this.elements.searchResults.style.display = 'block';
        this.elements.noResults.style.display = 'none';
    }

    /**
     * 검색 결과 없음 표시
     */
    showNoResults() {
        this.elements.searchResults.style.display = 'none';
        this.elements.noResults.style.display = 'block';
    }

    /**
     * 검색 폼 리셋
     */
    resetSearchForm() {
        this.elements.searchType.value = 'name';
        this.elements.searchKeyword.value = '';
        this.elements.roleFilter.value = '';
    }

    /**
     * 계정 전환 요청 개수 업데이트
     * @param {number} count 요청 개수
     */
    updateUpgradeRequestCount(count) {
        if (this.elements.upgradeRequestCount) {
            this.elements.upgradeRequestCount.textContent = count;
        }
    }

    /**
     * 에러 메시지 표시
     * @param {string} message 에러 메시지
     */
    showError(message) {
        alert(message);
    }

    /**
     * 성공 메시지 표시
     * @param {string} message 성공 메시지
     */
    showSuccess(message) {
        alert(message);
    }

    /**
     * 검색 파라미터 가져오기
     * @returns {Object} 검색 파라미터
     */
    getSearchParams() {
        return {
            searchType: this.elements.searchType.value,
            keyword: this.elements.searchKeyword.value.trim(),
            role: this.elements.roleFilter.value
        };
    }

    // 유틸리티 메서드들
    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    getRoleText(role) {
        const roleTexts = {
            'USER': '일반사용자',
            'PROVIDER': '제공자',
            'HOST': '호스트',
            'ADMIN': '관리자'
        };
        return roleTexts[role] || role;
    }

    formatDate(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR') + ' ' + date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
    }
}