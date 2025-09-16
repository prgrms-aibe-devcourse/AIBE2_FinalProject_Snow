/**
 * 회원가입 페이지 메인 컨트롤러
 * SignupApi, SignupUI, SignupValidator를 조합하여 회원가입 플로우를 관리
 */
class SignupController {
    constructor() {
        this.signupApi = new SignupApi();
        this.ui = new SignupUI();

        // 전역 참조 설정 (UI에서 사용)
        window.signupUI = this.ui;

        this.init();
    }

    /**
     * 컨트롤러 초기화
     */
    init() {
        this.setupFormSubmission();
        this.setupInitialState();
    }

    /**
     * 초기 상태 설정
     */
    async setupInitialState() {
        try {
            // UI 초기화가 완료될 때까지 대기
            await this.ui.loadCategories();

            // 제출 버튼 초기 상태
            this.ui.updateSubmitButton();

            console.log('회원가입 컨트롤러 초기화 완료');
        } catch (error) {
            console.error('회원가입 컨트롤러 초기화 실패:', error);
            this.ui.showAlert('페이지 초기화 중 오류가 발생했습니다.', 'error');
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

        console.log('회원가입 폼 제출 시작');

        // 클라이언트 사이드 검증
        if (!this.ui.validateForm()) {
            this.ui.showAlert('입력 정보를 확인해주세요.', 'error');
            return;
        }

        // 중복확인 상태 체크
        if (!this.validateDuplicateChecks()) {
            return;
        }

        const formData = this.ui.getFormData();
        const selectedTags = this.ui.getSelectedTags();

        console.log('선택된 관심사 태그:', selectedTags);

        // 회원가입 데이터 준비
        const signupData = this.signupApi.prepareSignupData(formData, selectedTags);

        console.log('회원가입 데이터:', {
            ...signupData,
            password: '***', // 보안상 로그에서 제외
            passwordConfirm: '***'
        });

        // 최종 검증
        if (!this.validateSignupData(signupData)) {
            return;
        }

        this.ui.toggleLoading(true);

        try {
            const response = await this.signupApi.signup(signupData);

            console.log('회원가입 응답:', response);

            if (response.success) {
                // 성공 메시지 표시
                this.ui.showAlert('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.', 'success');

                // 폼 리셋
                this.ui.resetForm();

                // 로그인 페이지로 리다이렉트
                setTimeout(() => {
                    window.location.href = '/auth/login?signup=success';
                }, 2000);
            } else {
                throw new Error(response.message || '회원가입에 실패했습니다.');
            }
        } catch (error) {
            console.error('회원가입 실패:', error);
            this.handleSignupError(error);
        } finally {
            this.ui.toggleLoading(false);
        }
    }

    /**
     * 중복확인 상태 검증
     * @returns {boolean} 검증 결과
     */
    validateDuplicateChecks() {
        const formData = this.ui.getFormData();
        const email = formData.get('email').trim();
        const nickname = formData.get('nickname').trim();

        if (email && !this.ui.validationStates.email) {
            this.ui.showAlert('이메일 중복확인을 완료해주세요.', 'error');
            this.ui.elements.emailInput.focus();
            return false;
        }

        if (nickname && !this.ui.validationStates.nickname) {
            this.ui.showAlert('닉네임 중복확인을 완료해주세요.', 'error');
            this.ui.elements.nicknameInput.focus();
            return false;
        }

        return true;
    }

    /**
     * 회원가입 데이터 최종 검증
     * @param {Object} signupData 회원가입 데이터
     * @returns {boolean} 검증 결과
     */
    validateSignupData(signupData) {
        // 필수 필드 체크
        const requiredFields = ['name', 'nickname', 'email', 'password', 'passwordConfirm', 'phone'];

        for (const field of requiredFields) {
            if (!signupData[field] || signupData[field].trim() === '') {
                this.ui.showAlert(`${this.getFieldDisplayName(field)}을(를) 입력해주세요.`, 'error');
                return false;
            }
        }

        // 비밀번호 일치 확인
        if (signupData.password !== signupData.passwordConfirm) {
            this.ui.showAlert('비밀번호가 일치하지 않습니다.', 'error');
            this.ui.elements.passwordConfirmInput.focus();
            return false;
        }

        // 이메일 형식 재검증
        const emailValidation = SignupValidator.validateEmail(signupData.email);
        if (!emailValidation.isValid) {
            this.ui.showAlert(emailValidation.message, 'error');
            this.ui.elements.emailInput.focus();
            return false;
        }

        // 비밀번호 강도 재검증
        const passwordValidation = SignupValidator.validatePassword(signupData.password);
        if (!passwordValidation.isValid) {
            this.ui.showAlert(passwordValidation.message, 'error');
            this.ui.elements.passwordInput.focus();
            return false;
        }

        // 핸드폰 번호 형식 재검증
        const phoneValidation = SignupValidator.validatePhone(signupData.phone);
        if (!phoneValidation.isValid) {
            this.ui.showAlert(phoneValidation.message, 'error');
            this.ui.elements.phoneInput.focus();
            return false;
        }

        // 관심사 검증 (선택사항이지만 개수 제한)
        const interestsValidation = SignupValidator.validateInterests(signupData.interests);
        if (!interestsValidation.isValid) {
            this.ui.showAlert(interestsValidation.message, 'error');
            return false;
        }

        return true;
    }

    /**
     * 필드명을 사용자 친화적 이름으로 변환
     * @param {string} fieldName 필드명
     * @returns {string} 표시용 필드명
     */
    getFieldDisplayName(fieldName) {
        const displayNames = {
            name: '이름',
            nickname: '닉네임',
            email: '이메일',
            password: '비밀번호',
            passwordConfirm: '비밀번호 확인',
            phone: '핸드폰 번호'
        };
        return displayNames[fieldName] || fieldName;
    }

    /**
     * 회원가입 에러 처리
     * @param {Error} error 에러 객체
     */
    handleSignupError(error) {
        let errorMessage = error.message || '회원가입 중 오류가 발생했습니다.';

        // 특정 에러에 대한 사용자 친화적 메시지
        if (errorMessage.includes('duplicate') || errorMessage.includes('중복')) {
            errorMessage = '이미 존재하는 정보입니다. 이메일이나 닉네임을 확인해주세요.';
        } else if (errorMessage.includes('validation') || errorMessage.includes('형식')) {
            errorMessage = '입력 정보의 형식을 확인해주세요.';
        } else if (errorMessage.includes('network') || errorMessage.includes('네트워크')) {
            errorMessage = '네트워크 연결을 확인하고 다시 시도해주세요.';
        } else if (errorMessage.includes('server') || errorMessage.includes('500')) {
            errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
        }

        this.ui.showAlert(errorMessage, 'error');

        // 로그에는 원본 에러 기록
        console.error('회원가입 상세 에러:', error);
    }

    /**
     * 카테고리 데이터 다시 로드
     */
    async reloadCategories() {
        try {
            await this.ui.loadCategories();
            this.ui.showAlert('카테고리 정보를 다시 불러왔습니다.', 'success');
        } catch (error) {
            console.error('카테고리 재로드 실패:', error);
            this.ui.showAlert('카테고리 정보 재로드에 실패했습니다.', 'error');
        }
    }

    /**
     * 디버깅용 메서드 - 현재 상태 출력
     */
    debugCurrentState() {
        const formData = this.ui.getFormData();
        const selectedTags = this.ui.getSelectedTags();
        const validationStates = this.ui.validationStates;

        console.log('=== 회원가입 현재 상태 ===');
        console.log('폼 데이터:', Object.fromEntries(formData));
        console.log('선택된 태그:', selectedTags);
        console.log('검증 상태:', validationStates);
        console.log('카테고리 수:', this.ui.categories.length);
        console.log('폼 유효성:', this.ui.validateForm());
        console.log('========================');
    }
}

// 전역에서 디버깅 메서드 사용 가능하도록 설정
window.debugSignup = () => {
    if (window.signupController) {
        window.signupController.debugCurrentState();
    } else {
        console.log('SignupController가 초기화되지 않았습니다.');
    }
};