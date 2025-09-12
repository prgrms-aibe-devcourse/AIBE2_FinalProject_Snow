/**
 * 유저 승격 요청 페이지 메인 컨트롤러
 */
class RoleUpgradeController {
    constructor() {
        // API와 UI 인스턴스 생성 전에 존재 여부 확인
        this.api = typeof RoleUpgradeApi !== 'undefined' ? new RoleUpgradeApi() : null;
        this.ui = typeof RoleUpgradeUI !== 'undefined' ? new RoleUpgradeUI() : null;

        this.init();
    }

    /**
     * 컨트롤러 초기화
     */
    init() {
        // API와 UI가 로드되지 않았다면 에러 처리
        if (!this.api || !this.ui) {
            console.error('RoleUpgradeApi 또는 RoleUpgradeUI 클래스를 찾을 수 없습니다.');
            this.showSimpleAlert('페이지 로딩 중 오류가 발생했습니다. 새로고침해주세요.');
            return;
        }

        this.checkUserAuth();
        this.setupFormSubmission();
    }

    /**
     * 사용자 인증 확인
     */
    async checkUserAuth() {
        if (!this.api) return;

        const token = this.api.getStoredToken();

        if (!token) {
            this.showSimpleAlert('로그인이 필요합니다.');
            setTimeout(() => {
                window.location.href = '/templates/pages/auth/login.html';
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
        if (!this.api || !this.ui) return;

        try {
            const requests = await this.api.getMyUpgradeRequests();
            const pendingRequest = requests.find(req => req.status === 'PENDING');

            if (pendingRequest) {
                this.ui.showAlert('이미 제출된 승격 요청이 있습니다. 관리자 검토를 기다려주세요.', 'info');

                // UI 요소가 존재할 때만 접근
                if (this.ui.elements && this.ui.elements.submitBtn) {
                    this.ui.elements.submitBtn.disabled = true;
                    const btnText = this.ui.elements.submitBtn.querySelector('.btn-text');
                    if (btnText) {
                        btnText.textContent = '처리 대기 중';
                    }
                }
            }
        } catch (error) {
            console.warn('기존 요청 확인 중 오류:', error);
        }
    }

    /**
     * 폼 제출 이벤트 설정
     */
    setupFormSubmission() {
        if (!this.ui || !this.ui.elements || !this.ui.elements.form) {
            console.warn('폼을 찾을 수 없습니다.');
            return;
        }

        this.ui.elements.form.addEventListener('submit', (e) => {
            this.handleSubmit(e);
        });
    }

    /**
     * 폼 제출 처리
     */
    async handleSubmit(e) {
        e.preventDefault();

        if (!this.ui || !this.api) {
            this.showSimpleAlert('시스템 오류가 발생했습니다.');
            return;
        }

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
                    window.location.href = '/templates/pages/main.html';
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
            if (this.ui) {
                this.ui.showAlert('요청할 역할을 선택해주세요.', 'error');
            } else {
                this.showSimpleAlert('요청할 역할을 선택해주세요.');
            }
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

            if (this.ui && this.ui.elements && this.ui.elements.submitBtn) {
                this.ui.elements.submitBtn.disabled = true;
                const btnText = this.ui.elements.submitBtn.querySelector('.btn-text');
                if (btnText) {
                    btnText.textContent = '처리 대기 중';
                }
            }
        } else if (error.message.includes('권한')) {
            errorMessage = '해당 역할로 승격할 권한이 없습니다.';
        } else if (error.message.includes('로그인')) {
            errorMessage = '로그인이 필요합니다. 로그인 페이지로 이동합니다.';
            setTimeout(() => {
                window.location.href = '/templates/pages/auth/login.html';
            }, 2000);
        }

        if (this.ui) {
            this.ui.showAlert(errorMessage, 'error');
        } else {
            this.showSimpleAlert(errorMessage);
        }
    }

    /**
     * 간단한 알림 표시 (UI 클래스 없을 때 사용)
     */
    showSimpleAlert(message) {
        // 기존 알림이 있다면 제거
        const existingAlert = document.querySelector('.simple-alert');
        if (existingAlert) {
            existingAlert.remove();
        }

        // 새 알림 생성
        const alertDiv = document.createElement('div');
        alertDiv.className = 'simple-alert';
        alertDiv.textContent = message;
        alertDiv.style.cssText = `
            position: fixed;
            top: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: #ff4757;
            color: white;
            padding: 12px 24px;
            border-radius: 8px;
            z-index: 9999;
            font-size: 14px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        `;

        document.body.appendChild(alertDiv);

        // 3초 후 제거
        setTimeout(() => {
            alertDiv.remove();
        }, 3000);
    }
}

// 전역에서 사용할 수 있도록 window 객체에 추가 (필요한 경우)
if (typeof window !== 'undefined') {
    window.RoleUpgradeController = RoleUpgradeController;
}