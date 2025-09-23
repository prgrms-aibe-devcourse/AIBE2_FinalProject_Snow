/**
 * 회원 관리 컨트롤러
 */
class UserManagementController {
    constructor() {
        this.api = new UserManagementApi();
        this.ui = new UserManagementUI();
        this.currentPage = 1;

        this.init();
    }

    /**
     * 초기화
     */
    init() {
        this.checkAdminAuth();
        this.bindEvents();
        this.loadInitialData();
    }

    /**
     * 관리자 권한 확인
     */
    checkAdminAuth() {
        const token = localStorage.getItem('authToken');
        const userRole = localStorage.getItem('userRole');

        if (!token || userRole !== 'ADMIN') {
            alert('관리자만 접근할 수 있습니다.');
            window.location.href = '/templates/pages/auth/login.html';
            return;
        }
    }

    /**
     * 이벤트 바인딩
     */
    bindEvents() {
        // 검색 버튼 클릭
        this.ui.elements.searchBtn.addEventListener('click', () => {
            this.performSearch(1);
        });

        // 리셋 버튼 클릭
        this.ui.elements.resetBtn.addEventListener('click', () => {
            this.resetSearch();
        });

        // 엔터키 검색
        this.ui.elements.searchKeyword.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.performSearch(1);
            }
        });

        // 모달 닫기
        this.ui.elements.closeModal.addEventListener('click', () => {
            this.ui.closeModal();
        });

        // 모달 배경 클릭으로 닫기
        this.ui.elements.userDetailModal.addEventListener('click', (e) => {
            if (e.target === this.ui.elements.userDetailModal) {
                this.ui.closeModal();
            }
        });

        // 사용자 상세보기 버튼 클릭 (이벤트 위임)
        this.ui.elements.userTableBody.addEventListener('click', (e) => {
            if (e.target.classList.contains('detail-button')) {
                const userId = e.target.dataset.userId;
                this.showUserDetail(userId);
            }
        });
    }

    /**
     * 초기 데이터 로드
     */
    async loadInitialData() {
        try {
            // 계정 전환 요청 개수 로드
            const upgradeRequestCount = await this.api.getUpgradeRequestCount();
            this.ui.updateUpgradeRequestCount(upgradeRequestCount);

            // 초기 검색 (전체 목록)
            this.performSearch(1);

        } catch (error) {
            console.error('초기 데이터 로드 실패:', error);
        }
    }

    /**
     * 검색 수행
     * @param {number} page 페이지 번호
     */
    async performSearch(page = 1) {
        this.currentPage = page;
        const searchParams = this.ui.getSearchParams();

        this.ui.showLoading();

        try {
            const params = {
                ...searchParams,
                page: page - 1, // 백엔드는 0부터 시작
                size: 10
            };

            const data = await this.api.searchUsers(params);

            this.ui.displaySearchResults(data);
            this.ui.setupPagination(data, page, (newPage) => {
                this.performSearch(newPage);
            });

        } catch (error) {
            console.error('검색 실패:', error);
            this.ui.showError('검색 중 오류가 발생했습니다.');
        } finally {
            this.ui.hideLoading();
        }
    }

    /**
     * 검색 리셋
     */
    resetSearch() {
        this.ui.resetSearchForm();
        this.performSearch(1);
    }

    /**
     * 사용자 상세 정보 표시
     * @param {string} userId 사용자 ID
     */
    async showUserDetail(userId) {
        try {
            const user = await this.api.getUserDetail(userId);
            this.ui.showUserDetailModal(user);

        } catch (error) {
            console.error('사용자 상세 정보 조회 실패:', error);
            this.ui.showError('사용자 정보를 불러오는데 실패했습니다.');
        }
    }

    /**
     * 페이지 새로고침
     */
    refresh() {
        this.loadInitialData();
    }

    /**
     * 통계 정보 조회
     */
    async getStatistics() {
        try {
            const [totalCount, roleStats] = await Promise.all([
                this.api.getTotalUserCount(),
                this.api.getUserCountByRole()
            ]);

            return {
                totalCount,
                roleStats
            };
        } catch (error) {
            console.error('통계 정보 조회 실패:', error);
            throw error;
        }
    }
}