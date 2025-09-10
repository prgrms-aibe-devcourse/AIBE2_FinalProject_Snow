/**
 * 유저 승격 요청 페이지 메인 컨트롤러
 */
class RoleUpgradeController {
    constructor() {
        this.api = new RoleUpgradeApi();
        this.ui = new RoleUpgradeUI();

        this.init();
    }

    /**
     * 컨트롤러 초기화
     */
    init() {
        this.checkUserAuth();
        this.setupFormSubmission();
    }

    /**
     * 사용자 인증 확인
     */
    async checkUserAuth() {
        const token = this.api.getStoredToken();

        if (!token) {
            this.ui.showAlert('로그인이 필요합니다.', 'error');
            setTimeout(() => {
                window.location.href = '/auth/login?redirect=' + encodeURIComponent(window.location.pathname);
            }, 2000);
            return;
        }

        // 기존 요청 확인
        try {
            await this.checkExistingRequest();
        } catch (error) {
            console.warn('기존 요청 확인 실패:', error);
        }
    }

    /**
     * 기존 승격 요청 확인
     */
    async checkExistingRequest() {
        try {
            const requests = await this.api.getMyUpgradeRequests();
            const pendingRequest = requests.find(req => req.status === 'PENDING');

            if (pendingRequest) {
                this.ui.showAlert('이미 제출된 승격 요청이 있습니다. 관리자 검토를 기다려주세요.', 'info');
                this.ui.elements.submitBtn.disabled = true;
                this.ui.elements.submitBtn.querySelector('.btn-text').textContent = '처리 대기 중';
            }
        } catch (error) {
            console.warn('기존 요청 확인 중 오류:', error);
        }
    }

    /**
     * 폼 제출 이벤트 설정
     */
    setupFormSubmission() {
        this.ui.elements.form.addEventListener('submit', (e) => {
            this.handleSubmit(e);
        });
    }

    /**
     * 폼 제출 처리
     */
    async handleSubmit(e) {
        e.preventDefault();

        // 클라이언트 검증
        if (!this.ui.validateForm()) {
            this.ui.showAlert('입력 정보를 확인해주세요.', 'error');
            return;
        }

        // 요청 데이터 준비
        const requestData = this.ui.getUpgradeRequestData();
        const selectedFile = this.ui.getSelectedFile();

        // 최종 검증
        if (!this.validateSubmissionData(requestData, selectedFile)) {
            return;
        }

        this.ui.toggleLoading(true);

        try {
            const response = await this.api.createUpgradeRequest(requestData, selectedFile);

            if (response.success) {
                this.ui.showAlert(response.message || '승격 요청이 성공적으로 제출되었습니다!', 'success');
                this.ui.resetForm();

                setTimeout(() => {
                    window.location.href = '/';
                }, 3000);
            } else {
                throw new Error(response.message || '승격 요청에 실패했습니다.');
            }
        } catch (error) {
            console.error('승격 요청 실패:', error);
            this.handleSubmissionError(error);
        } finally {
            this.ui.toggleLoading(false);
        }
    }

    /**
     * 제출 데이터 검증
     */
    validateSubmissionData(requestData, file) {
        if (!requestData.requestedRole) {
            this.ui.showAlert('요청할 역할을 선택해주세요.', 'error');
            return false;
        }

        return true;
    }

    /**
     * 제출 에러 처리
     */
    handleSubmissionError(error) {
        let errorMessage = error.message || '승격 요청 중 오류가 발생했습니다.';

        if (error.message.includes('이미 제출된')) {
            errorMessage = '이미 제출된 승격 요청이 있습니다. 관리자 검토를 기다려주세요.';
            this.ui.elements.submitBtn.disabled = true;
            this.ui.elements.submitBtn.querySelector('.btn-text').textContent = '처리 대기 중';
        } else if (error.message.includes('권한')) {
            errorMessage = '해당 역할로 승격할 권한이 없습니다.';
        } else if (error.message.includes('로그인')) {
            errorMessage = '로그인이 필요합니다. 로그인 페이지로 이동합니다.';
            setTimeout(() => {
                window.location.href = '/auth/login?redirect=' + encodeURIComponent(window.location.pathname);
            }, 2000);
        }

        this.ui.showAlert(errorMessage, 'error');
    }
}