/**
 * 장소 관리 컨트롤러 클래스
 */
class SpaceManagementController {
    constructor() {
        this.api = new SpaceManagementApi();
        this.ui = new SpaceManagementUI();
        this.currentPage = 0;
        this.pageSize = 20;
        this.currentFilters = {};

        this.init();
    }

    /**
     * 초기화
     */
    async init() {
        this.setupEventListeners();
        await this.loadInitialData();
    }

    /**
     * 이벤트 리스너 설정
     */
    setupEventListeners() {
        // 검색 버튼
        if (this.ui.elements.searchBtn) {
            this.ui.elements.searchBtn.addEventListener('click', () => {
                this.search();
            });
        }

        // 초기화 버튼
        if (this.ui.elements.resetBtn) {
            this.ui.elements.resetBtn.addEventListener('click', () => {
                this.reset();
            });
        }

        // 필터 입력 시 엔터키로 검색
        const filterInputs = [
            this.ui.elements.ownerFilter,
            this.ui.elements.titleFilter
        ];

        filterInputs.forEach(input => {
            if (input) {
                input.addEventListener('keypress', (e) => {
                    if (e.key === 'Enter') {
                        this.search();
                    }
                });
            }
        });

        // 모달 닫기 이벤트
        if (this.ui.elements.closeModal) {
            this.ui.elements.closeModal.addEventListener('click', () => {
                this.ui.closeModal();
            });
        }

        if (this.ui.elements.confirmModalClose) {
            this.ui.elements.confirmModalClose.addEventListener('click', () => {
                this.ui.closeModal();
            });
        }

        if (this.ui.elements.cancelBtn) {
            this.ui.elements.cancelBtn.addEventListener('click', () => {
                this.ui.closeModal();
            });
        }

        if (this.ui.elements.confirmBtn) {
            this.ui.elements.confirmBtn.addEventListener('click', () => {
                this.executeConfirmedAction();
            });
        }

        // 모달 배경 클릭 시 닫기
        [this.ui.elements.spaceDetailModal, this.ui.elements.confirmModal].forEach(modal => {
            if (modal) {
                modal.addEventListener('click', (e) => {
                    if (e.target === modal) {
                        this.ui.closeModal();
                    }
                });
            }
        });

        // ★ 상세보기 버튼 이벤트 처리 추가 (이벤트 위임 방식)
        if (this.ui.elements.spacesTableBody) {
            this.ui.elements.spacesTableBody.addEventListener('click', (e) => {
                // 상세보기 버튼 클릭
                if (e.target.classList.contains('detail-button') || e.target.closest('.detail-button')) {
                    const button = e.target.classList.contains('detail-button') ? e.target : e.target.closest('.detail-button');
                    const spaceId = button.dataset.spaceId;
                    if (spaceId) {
                        this.showSpaceDetail(spaceId);
                    }
                }

                // 활성화/비활성화 버튼 클릭
                if (e.target.classList.contains('button-warning') || e.target.closest('.button-warning')) {
                    const button = e.target.classList.contains('button-warning') ? e.target : e.target.closest('.button-warning');
                    const spaceId = button.dataset.spaceId || button.getAttribute('onclick')?.match(/\d+/)?.[0];
                    if (spaceId) {
                        this.hideSpace(spaceId);
                    }
                }

                if (e.target.classList.contains('button-success') || e.target.closest('.button-success')) {
                    const button = e.target.classList.contains('button-success') ? e.target : e.target.closest('.button-success');
                    const spaceId = button.dataset.spaceId || button.getAttribute('onclick')?.match(/\d+/)?.[0];
                    if (spaceId) {
                        this.showSpace(spaceId);
                    }
                }
            });
        }
    }

    /**
     * 초기 데이터 로드
     */
    async loadInitialData() {
        try {
            // 통계 데이터 로드
            await this.loadStats();

            // 장소 목록 로드
            await this.loadSpaces();
        } catch (error) {
            console.error('초기 데이터 로드 실패:', error);
            this.api.handleError(error);
        }
    }

    /**
     * 통계 데이터 로드
     */
    async loadStats() {
        try {
            const stats = await this.api.getStats();
            this.ui.updateStats(stats);
        } catch (error) {
            console.error('통계 로드 실패:', error);
            // 통계 로드 실패는 전체 페이지 동작을 막지 않음
        }
    }

    /**
     * 장소 목록 로드
     */
    async loadSpaces() {
        try {
            this.ui.showLoading(true);

            const filters = this.getFilters();
            const params = {
                ...filters,
                page: this.currentPage,
                size: this.pageSize
            };

            const data = await this.api.getSpaces(params);
            console.log('📦 API 응답 데이터:', data);

            // ★ 수정된 부분: renderSpaces → renderSpacesTable
            this.ui.renderSpacesTable(data);

            this.ui.showLoading(false);
        } catch (error) {
            console.error('장소 목록 로드 실패:', error);
            this.ui.showLoading(false);
            this.api.handleError(error);
        }
    }

    /**
     * ★ 장소 상세 정보 표시 (수정된 부분 - 로딩 제거)
     */
    async showSpaceDetail(spaceId) {
        try {
            // ❌ 제거: this.ui.showLoading(true); - 이것이 기존 목록을 사라지게 만드는 원인

            console.log('🔍 장소 상세 정보 로드 시작:', spaceId);

            const space = await this.api.getSpaceDetail(spaceId);
            console.log('✅ 장소 상세 정보 로드 완료:', space);

            this.ui.showSpaceDetail(space);

            // ❌ 제거: this.ui.showLoading(false); - 불필요한 로딩 해제
        } catch (error) {
            console.error('장소 상세 정보 로드 실패:', error);
            // ❌ 제거: this.ui.showLoading(false);
            this.api.handleError(error);
        }
    }

    /**
     * 검색
     */
    search() {
        this.currentPage = 0;
        this.loadSpaces();
    }

    /**
     * 초기화
     */
    reset() {
        // 필터 초기화
        if (this.ui.elements.ownerFilter) this.ui.elements.ownerFilter.value = '';
        if (this.ui.elements.titleFilter) this.ui.elements.titleFilter.value = '';
        if (this.ui.elements.isPublicFilter) this.ui.elements.isPublicFilter.value = '';

        this.currentPage = 0;
        this.loadSpaces();
    }

    /**
     * 페이지 로드
     */
    loadPage(page) {
        this.currentPage = page;
        this.loadSpaces();
    }

    /**
     * 필터 값 가져오기
     */
    getFilters() {
        const filters = {};

        if (this.ui.elements.ownerFilter?.value) {
            filters.owner = this.ui.elements.ownerFilter.value;
        }

        if (this.ui.elements.titleFilter?.value) {
            filters.title = this.ui.elements.titleFilter.value;
        }

        if (this.ui.elements.isPublicFilter?.value) {
            filters.isPublic = this.ui.elements.isPublicFilter.value === 'true';
        }

        return filters;
    }

    /**
     * 장소 숨기기 (비활성화)
     */
    async hideSpace(spaceId) {
        this.ui.showConfirm(
            '장소 비활성화',
            '이 장소를 비활성화하시겠습니까?',
            () => this.executeHideSpace(spaceId)
        );
    }

    /**
     * 장소 보이기 (활성화)
     */
    async showSpace(spaceId) {
        this.ui.showConfirm(
            '장소 활성화',
            '이 장소를 활성화하시겠습니까?',
            () => this.executeShowSpace(spaceId)
        );
    }

    /**
     * 장소 비활성화 실행
     */
    async executeHideSpace(spaceId) {
        try {
            await this.api.hideSpace(spaceId);
            this.ui.showSuccess('장소가 비활성화되었습니다.');
            this.loadSpaces();
            this.loadStats();
        } catch (error) {
            console.error('장소 비활성화 실패:', error);
            this.api.handleError(error);
        }
    }

    /**
     * 장소 활성화 실행
     */
    async executeShowSpace(spaceId) {
        try {
            await this.api.showSpace(spaceId);
            this.ui.showSuccess('장소가 활성화되었습니다.');
            this.loadSpaces();
            this.loadStats();
        } catch (error) {
            console.error('장소 활성화 실패:', error);
            this.api.handleError(error);
        }
    }

    /**
     * 확인된 액션 실행
     */
    executeConfirmedAction() {
        if (this.ui.currentAction) {
            this.ui.currentAction();
            this.ui.closeModal();
        }
    }
}

// 전역 참조를 위한 인스턴스 생성
let spaceManagementController;

// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', () => {
    spaceManagementController = new SpaceManagementController();
});

// 전역 함수로 노출 (onclick 이벤트용)
window.spaceManagementController = {
    showSpaceDetail: (spaceId) => spaceManagementController?.showSpaceDetail(spaceId),
    hideSpace: (spaceId) => spaceManagementController?.hideSpace(spaceId),
    showSpace: (spaceId) => spaceManagementController?.showSpace(spaceId),
    loadPage: (page) => spaceManagementController?.loadPage(page)
};