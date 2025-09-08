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
    setupInitialState() {
        // 첫 번째 카테고리 활성화
        this.ui.switchCategory('fashion');

        // 제출 버튼 초기 상태
        this.ui.updateSubmitButton();
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
            this.ui.showAlert('입력 정보를 확인해주세요.', 'error');
            return;
        }

        // 중복확인 상태 체크
        if (!this.validateDuplicateChecks()) {
            return;
        }

        const formData = this.ui.getFormData();
        const selectedTags = this.ui.getSelectedTags();

        // 회원가입 데이터 준비
        const signupData = this.signupApi.prepareSignupData(formData, selectedTags);

        // 최종 검증
        if (!this.validateSignupData(signupData)) {
            return;
        }

        this.ui.toggleLoading(true);

        try {
            const response = await this.signupApi.signup(signupData);

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

        // 서버 에러 메시지 파싱
        if (error.message) {
            if (error.message.includes('이메일')) {
                this.ui.showFieldError('email', error.message);
                this.ui.validationStates.email = false;
                this.ui.elements.emailInput.focus();
            } else if (error.message.includes('닉네임')) {
                this.ui.showFieldError('nickname', error.message);
                this.ui.validationStates.nickname = false;
                this.ui.elements.nicknameInput.focus();
            } else if (error.message.includes('비밀번호')) {
                this.ui.showFieldError('password', error.message);
                this.ui.elements.passwordInput.focus();
            } else if (error.message.includes('핸드폰') || error.message.includes('전화')) {
                this.ui.showFieldError('phone', error.message);
                this.ui.elements.phoneInput.focus();
            }
        }

        // 일반적인 에러 메시지 처리
        if (errorMessage.includes('중복')) {
            errorMessage = '이미 사용 중인 정보입니다. 다른 정보로 시도해주세요.';
        } else if (errorMessage.includes('네트워크')) {
            errorMessage = '네트워크 연결을 확인하고 다시 시도해주세요.';
        } else if (errorMessage.includes('서버')) {
            errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
        }

        this.ui.showAlert(errorMessage, 'error');
        this.ui.updateSubmitButton();
    }

    /**
     * 페이지 새로고침 또는 뒤로가기 시 경고
     */
    setupBeforeUnloadWarning() {
        let hasChanges = false;

        // 입력 필드 변경 감지
        const inputs = this.ui.elements.form.querySelectorAll('input');
        inputs.forEach(input => {
            input.addEventListener('input', () => {
                hasChanges = true;
            });
        });

        // 관심사 선택 변경 감지
        const originalToggleTag = this.ui.toggleTag.bind(this.ui);
        this.ui.toggleTag = function(tag) {
            hasChanges = true;
            return originalToggleTag(tag);
        };

        // 페이지 이탈 경고
        window.addEventListener('beforeunload', (e) => {
            if (hasChanges) {
                const message = '작성 중인 내용이 사라집니다. 정말 나가시겠습니까?';
                e.preventDefault();
                e.returnValue = message;
                return message;
            }
        });

        // 회원가입 완료 시 경고 해제
        this.ui.elements.form.addEventListener('submit', () => {
            hasChanges = false;
        });
    }

    /**
     * 디버깅용 메서드들
     */
    debug() {
        return {
            formData: this.ui.getFormData(),
            selectedTags: this.ui.getSelectedTags(),
            validationStates: this.ui.validationStates,
            isFormValid: this.ui.validateForm()
        };
    }
}