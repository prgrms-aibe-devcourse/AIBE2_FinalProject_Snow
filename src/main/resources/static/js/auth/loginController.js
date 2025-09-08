/**
 * 로그인 페이지 메인 컨트롤러
 * LoginApi, UI, Validator를 조합하여 로그인 플로우를 관리
 */
class LoginController {
    constructor() {
        this.loginApi = new LoginApi();
        this.ui = new LoginUI();

        this.init();
    }

    /**
     * 컨트롤러 초기화
     */
    init() {
        this.checkUrlParams();
        this.checkExistingToken();
        this.setupFormSubmission();
    }

    /**
     * URL 파라미터 확인 (에러, 로그아웃 메시지 등)
     */
    checkUrlParams() {
        const urlParams = new URLSearchParams(window.location.search);

        if (urlParams.get('error')) {
            this.ui.showAlert('아이디 또는 비밀번호를 확인해 주세요', 'error');
        }

        if (urlParams.get('logout')) {
            this.ui.showAlert('로그아웃되었습니다.', 'success');
        }

        if (urlParams.get('expired')) {
            this.ui.showAlert('세션이 만료되었습니다. 다시 로그인해주세요.', 'error');
        }

        if (urlParams.get('message')) {
            this.ui.showAlert(decodeURIComponent(urlParams.get('message')), 'info');
        }

        // URL 파라미터 정리
        if (urlParams.toString()) {
            window.history.replaceState({}, document.title, window.location.pathname);
        }
    }

    /**
     * 기존 토큰 확인 및 자동 리다이렉트
     */
    async checkExistingToken() {
        const token = this.loginApi.getStoredToken();

        if (token && !this.loginApi.isTokenExpired()) {
            const shouldRedirect = confirm('이미 로그인되어 있습니다. 메인 페이지로 이동하시겠습니까?');
            if (shouldRedirect) {
                window.location.href = '/';
            }
        } else if (token) {
            // 만료된 토큰 제거
            this.loginApi.removeToken();
            this.ui.showAlert('로그인이 만료되었습니다. 다시 로그인해주세요.', 'error');
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
     * @param {Event} e 폼 제출 이벤트
     */
    async handleSubmit(e) {
        e.preventDefault();

        // 클라이언트 사이드 검증
        if (!this.ui.validateForm()) {
            return;
        }

        const formData = this.ui.getFormData();
        const email = formData.get('email').trim();
        const password = formData.get('password');

        this.ui.toggleLoading(true);

        try {
            const response = await this.loginApi.login(email, password);

            if (response.accessToken) {
                // 토큰 저장
                this.loginApi.storeToken(response.accessToken, response);

                // 성공 메시지 표시
                this.ui.showAlert('로그인 성공! 메인 페이지로 이동합니다.', 'success');

                // 메인 페이지로 리다이렉트
                setTimeout(() => {
                    window.location.href = '/';
                }, 1500);
            } else {
                throw new Error('로그인 응답이 올바르지 않습니다.');
            }
        } catch (error) {
            console.error('로그인 실패:', error);
            this.ui.showAlert(
                error.message || '로그인에 실패했습니다. 다시 시도해주세요.',
                'error'
            );
        } finally {
            this.ui.toggleLoading(false);
        }
    }
}