/**
 * 회원 관리 컨트롤러
 */
class UserManagementController {
    constructor() {
        this.api = new UserManagementApi();
        this.ui = new UserManagementUI();
        this.currentPage = 1;

        // 전역에서 접근 가능하도록 설정 (디버깅용)
        window.userController = this;

        // HTML에서 DOMContentLoaded로 초기화하므로 여기서는 바로 init 호출
        this.init();
    }

    /**
     * 초기화
     */
    init() {
        console.log('UserManagementController 초기화 시작');
        this.checkAdminAuth();
        this.bindEvents();
        this.loadInitialData();
        console.log('UserManagementController 초기화 완료');
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
        console.log('이벤트 바인딩 시작');

        // 검색 버튼 클릭
        if (this.ui.elements.searchBtn) {
            this.ui.elements.searchBtn.addEventListener('click', () => {
                this.performSearch(1);
            });
        }

        // 리셋 버튼 클릭
        if (this.ui.elements.resetBtn) {
            this.ui.elements.resetBtn.addEventListener('click', () => {
                this.resetSearch();
            });
        }

        // 엔터키 검색
        if (this.ui.elements.searchKeyword) {
            this.ui.elements.searchKeyword.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') {
                    this.performSearch(1);
                }
            });
        }

        // 모달 닫기
        if (this.ui.elements.closeModal) {
            this.ui.elements.closeModal.addEventListener('click', () => {
                console.log('모달 닫기 버튼 클릭됨');
                this.ui.closeModal();
            });
        }

        // 모달 배경 클릭으로 닫기
        if (this.ui.elements.userDetailModal) {
            this.ui.elements.userDetailModal.addEventListener('click', (e) => {
                if (e.target === this.ui.elements.userDetailModal) {
                    console.log('모달 배경 클릭됨');
                    this.ui.closeModal();
                }
            });
        }

        // 사용자 상세보기 버튼 클릭 (이벤트 위임)
        if (this.ui.elements.userTableBody) {
            this.ui.elements.userTableBody.addEventListener('click', (e) => {
                console.log('테이블 클릭 이벤트:', e.target);

                if (e.target.classList.contains('detail-button')) {
                    const userId = e.target.dataset.userId || e.target.getAttribute('data-user-id');
                    console.log('상세보기 버튼 클릭됨 - 사용자 ID:', userId);

                    if (userId) {
                        this.showUserDetail(userId);
                    } else {
                        console.error('사용자 ID를 찾을 수 없습니다.');
                        alert('사용자 ID를 찾을 수 없습니다.');
                    }
                }
            });
        } else {
            console.error('userTableBody 요소를 찾을 수 없습니다.');
        }

        console.log('이벤트 바인딩 완료');
    }

    /**
     * 초기 데이터 로드
     */
    async loadInitialData() {
        console.log('초기 데이터 로드 시작');
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
        console.log('검색 수행 - 페이지:', page);
        this.currentPage = page;
        const searchParams = this.ui.getSearchParams();
        console.log('검색 파라미터:', searchParams);

        this.ui.showLoading();

        try {
            const params = {
                ...searchParams,
                page: page - 1, // 백엔드는 0부터 시작
                size: 10
            };

            const data = await this.api.searchUsers(params);
            console.log('검색 결과:', data);

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
        console.log('검색 리셋');
        this.ui.resetSearchForm();
        this.performSearch(1);
    }

    /**
     * 사용자 상세 정보 표시
     * @param {string} userId 사용자 ID
     */
    async showUserDetail(userId) {
        console.log('사용자 상세 정보 조회 시작 - ID:', userId);

        try {
            const user = await this.api.getUserDetail(userId);
            console.log('사용자 상세 정보 조회 성공:', user);

            this.ui.showUserDetailModal(user);

        } catch (error) {
            console.error('사용자 상세 정보 조회 실패:', error);
            this.ui.showError('사용자 정보를 불러오는데 실패했습니다: ' + error.message);
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