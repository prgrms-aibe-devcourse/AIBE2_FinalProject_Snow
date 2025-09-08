/**
 * 회원가입 UI 관리 클래스
 * 폼 상태 관리, 에러 표시, 로딩 상태, 관심사 선택 등 UI 관련 기능을 담당
 */
class SignupUI {
    constructor() {
        this.elements = this.initElements();
        this.selectedTags = new Set();
        this.validationStates = {
            email: false,
            nickname: false
        };
        this.setupEventListeners();
    }

    /**
     * DOM 요소들 초기화
     * @returns {Object} DOM 요소들
     */
    initElements() {
        return {
            form: document.getElementById('signupForm'),
            button: document.getElementById('signupBtn'),
            alertContainer: document.getElementById('alert-container'),

            // 입력 필드들
            nameInput: document.getElementById('name'),
            nicknameInput: document.getElementById('nickname'),
            emailInput: document.getElementById('email'),
            passwordInput: document.getElementById('password'),
            passwordConfirmInput: document.getElementById('passwordConfirm'),
            phoneInput: document.getElementById('phone'),

            // 에러 메시지들
            nameError: document.getElementById('name-error'),
            nicknameError: document.getElementById('nickname-error'),
            emailError: document.getElementById('email-error'),
            passwordError: document.getElementById('password-error'),
            passwordConfirmError: document.getElementById('passwordConfirm-error'),
            phoneError: document.getElementById('phone-error'),

            // 성공 메시지들
            nicknameSuccess: document.getElementById('nickname-success'),
            emailSuccess: document.getElementById('email-success'),
            passwordConfirmSuccess: document.getElementById('passwordConfirm-success'),

            // 중복 확인 버튼들
            emailCheckBtn: document.getElementById('emailCheckBtn'),
            nicknameCheckBtn: document.getElementById('nicknameCheckBtn'),

            // 관심사 관련
            categoryBtns: document.querySelectorAll('.category-btn'),
            tagGroups: document.querySelectorAll('.tag-group'),
            selectedTagsContainer: document.getElementById('selectedTags'),

            // 비밀번호 요구사항
            requirements: {
                length: document.getElementById('req-length'),
                letter: document.getElementById('req-letter'),
                number: document.getElementById('req-number'),
                special: document.getElementById('req-special')
            }
        };
    }

    /**
     * 이벤트 리스너 설정
     */
    setupEventListeners() {
        // 폼 필드 검증 이벤트
        this.setupFieldValidation();

        // 중복 확인 버튼 이벤트
        this.setupDuplicateCheck();

        // 관심사 선택 이벤트
        this.setupInterestSelection();

        // 핸드폰 번호 자동 포매팅
        this.setupPhoneFormatting();

        // Enter 키 처리
        this.setupKeyboardEvents();
    }

    /**
     * 필드 검증 이벤트 설정
     */
    setupFieldValidation() {
        // 이름 검증
        this.elements.nameInput.addEventListener('blur', () => {
            this.validateNameField();
        });
        this.elements.nameInput.addEventListener('input', () => {
            this.clearFieldError('name');
        });

        // 닉네임 검증
        this.elements.nicknameInput.addEventListener('blur', () => {
            this.validateNicknameField(false); // blur시에는 중복확인 안함
        });
        this.elements.nicknameInput.addEventListener('input', () => {
            this.clearFieldError('nickname');
            this.clearFieldSuccess('nickname');
            this.validationStates.nickname = false;
            this.updateSubmitButton();
        });

        // 이메일 검증
        this.elements.emailInput.addEventListener('blur', () => {
            this.validateEmailField(false); // blur시에는 중복확인 안함
        });
        this.elements.emailInput.addEventListener('input', () => {
            this.clearFieldError('email');
            this.clearFieldSuccess('email');
            this.validationStates.email = false;
            this.updateSubmitButton();
        });

        // 비밀번호 검증
        this.elements.passwordInput.addEventListener('input', () => {
            this.validatePasswordField();
            // 비밀번호 확인도 다시 검증
            if (this.elements.passwordConfirmInput.value) {
                this.validatePasswordConfirmField();
            }
        });

        // 비밀번호 확인 검증
        this.elements.passwordConfirmInput.addEventListener('input', () => {
            this.validatePasswordConfirmField();
        });

        // 핸드폰 번호 검증
        this.elements.phoneInput.addEventListener('blur', () => {
            this.validatePhoneField();
        });
        this.elements.phoneInput.addEventListener('input', () => {
            this.clearFieldError('phone');
        });
    }

    /**
     * 중복 확인 버튼 이벤트 설정
     */
    setupDuplicateCheck() {
        this.elements.emailCheckBtn.addEventListener('click', () => {
            this.validateEmailField(true);
        });

        this.elements.nicknameCheckBtn.addEventListener('click', () => {
            this.validateNicknameField(true);
        });
    }

    /**
     * 관심사 선택 이벤트 설정
     */
    setupInterestSelection() {
        // 카테고리 버튼 이벤트
        this.elements.categoryBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                this.switchCategory(btn.dataset.category);
            });
        });

        // 태그 버튼 이벤트 (이벤트 위임 사용)
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('tag-btn')) {
                e.preventDefault();
                this.toggleTag(e.target.dataset.tag);
            }
        });
    }

    /**
     * 핸드폰 번호 자동 포매팅 설정
     */
    setupPhoneFormatting() {
        this.elements.phoneInput.addEventListener('input', (e) => {
            const formatted = SignupValidator.formatPhone(e.target.value);
            if (formatted !== e.target.value) {
                e.target.value = formatted;
            }
        });
    }

    /**
     * 키보드 이벤트 설정
     */
    setupKeyboardEvents() {
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && this.elements.button.disabled) {
                e.preventDefault(); // 버튼이 비활성화일 때만 막음
            }
        });
    }

    // ================ 필드별 검증 메서드 ================

    /**
     * 이름 필드 검증
     */
    validateNameField() {
        const result = SignupValidator.validateName(this.elements.nameInput.value);
        if (!result.isValid) {
            this.showFieldError('name', result.message);
            return false;
        }
        this.clearFieldError('name');
        return true;
    }

    /**
     * 닉네임 필드 검증
     * @param {boolean} checkDuplicate 중복 확인 여부
     */
    async validateNicknameField(checkDuplicate = false) {
        const nickname = this.elements.nicknameInput.value.trim();

        // 기본 유효성 검사
        const basicValidation = SignupValidator.validateNickname(nickname);
        if (!basicValidation.isValid) {
            this.showFieldError('nickname', basicValidation.message);
            this.validationStates.nickname = false;
            this.updateSubmitButton();
            return false;
        }

        // 중복 확인
        if (checkDuplicate) {
            this.setButtonLoading('nicknameCheckBtn', true);
            try {
                const signupApi = new SignupApi();
                const result = await signupApi.validateNickname(nickname);

                if (result.isValid) {
                    this.showFieldSuccess('nickname', result.message);
                    this.validationStates.nickname = true;
                } else {
                    this.showFieldError('nickname', result.message);
                    this.validationStates.nickname = false;
                }
            } catch (error) {
                this.showFieldError('nickname', error.message);
                this.validationStates.nickname = false;
            } finally {
                this.setButtonLoading('nicknameCheckBtn', false);
                this.updateSubmitButton();
            }
            return this.validationStates.nickname; // 중복확인 결과 반환
        }

        // 중복확인이 아닌 경우에만 에러 클리어
        this.clearFieldError('nickname');
        return true;
    }
    /**
     * 이메일 필드 검증
     * @param {boolean} checkDuplicate 중복 확인 여부
     */
    async validateEmailField(checkDuplicate = false) {
        const email = this.elements.emailInput.value.trim();

        // 기본 유효성 검사
        const basicValidation = SignupValidator.validateEmail(email);
        if (!basicValidation.isValid) {
            this.showFieldError('email', basicValidation.message);
            this.validationStates.email = false;
            this.updateSubmitButton();
            return false;
        }

        // 중복 확인
        if (checkDuplicate) {
            this.setButtonLoading('emailCheckBtn', true);
            try {
                const signupApi = new SignupApi();
                const result = await signupApi.validateEmail(email);

                if (result.isValid) {
                    this.showFieldSuccess('email', result.message);
                    this.validationStates.email = true;
                } else {
                    this.showFieldError('email', result.message);
                    this.validationStates.email = false;
                }
            } catch (error) {
                this.showFieldError('email', error.message);
                this.validationStates.email = false;
            } finally {
                this.setButtonLoading('emailCheckBtn', false);
                this.updateSubmitButton();
            }
            return this.validationStates.email; // 중복확인 결과 반환
        }

        // 중복확인이 아닌 경우에만 에러 클리어
        this.clearFieldError('email');
        return true;
    }

    /**
     * 비밀번호 필드 검증
     */
    validatePasswordField() {
        const password = this.elements.passwordInput.value;
        const result = SignupValidator.validatePassword(password);

        // 비밀번호 요구사항 업데이트
        if (result.requirements) {
            Object.keys(result.requirements).forEach(key => {
                const element = this.elements.requirements[key];
                if (element) {
                    if (result.requirements[key].valid) {
                        element.classList.add('valid');
                    } else {
                        element.classList.remove('valid');
                    }
                }
            });
        }

        if (!result.isValid && password.length > 0) {
            this.showFieldError('password', result.message);
            return false;
        } else {
            this.clearFieldError('password');
            return result.isValid;
        }
    }

    /**
     * 비밀번호 확인 필드 검증
     */
    validatePasswordConfirmField() {
        const password = this.elements.passwordInput.value;
        const passwordConfirm = this.elements.passwordConfirmInput.value;

        const result = SignupValidator.validatePasswordConfirm(password, passwordConfirm);

        if (!result.isValid) {
            this.showFieldError('passwordConfirm', result.message);
            return false;
        } else {
            this.showFieldSuccess('passwordConfirm', result.message);
            return true;
        }
    }

    /**
     * 핸드폰 번호 필드 검증
     */
    validatePhoneField() {
        const phone = this.elements.phoneInput.value;
        const result = SignupValidator.validatePhone(phone);

        if (!result.isValid) {
            this.showFieldError('phone', result.message);
            return false;
        } else {
            this.clearFieldError('phone');
            return true;
        }
    }

    // ================ UI 상태 관리 메서드 ================

    /**
     * 필드 에러 표시
     * @param {string} field 필드명
     * @param {string} message 에러 메시지
     */
    showFieldError(field, message) {
        const input = this.elements[`${field}Input`];
        const error = this.elements[`${field}Error`];

        if (input && error) {
            input.classList.add('error');
            input.classList.remove('success');
            error.textContent = message;
            error.classList.add('show'); // 이 한 줄만 추가하면 됨!
        }
    }


    /**
     * 필드 성공 표시 (수정된 버전)
     * @param {string} field 필드명
     * @param {string} message 성공 메시지
     */
    showFieldSuccess(field, message) {
        const input = this.elements[`${field}Input`];
        const success = this.elements[`${field}Success`];

        if (input && success) {
            input.classList.add('success');
            input.classList.remove('error');
            success.textContent = message;
            success.classList.add('show'); // 이 줄 추가!
        }

        // 중복확인 버튼 성공 상태 표시
        if (field === 'email' && this.elements.emailCheckBtn) {
            this.elements.emailCheckBtn.classList.add('success');
            this.elements.emailCheckBtn.textContent = '확인완료';
        }
        if (field === 'nickname' && this.elements.nicknameCheckBtn) {
            this.elements.nicknameCheckBtn.classList.add('success');
            this.elements.nicknameCheckBtn.textContent = '확인완료';
        }
    }


    /**
     * 필드 에러 제거
     * @param {string} field 필드명
     */
    clearFieldError(field) {
        const input = this.elements[`${field}Input`];
        const error = this.elements[`${field}Error`];

        if (input && error) {
            input.classList.remove('error');
            error.textContent = '';
            error.classList.remove('show'); // 이 한 줄도 추가
        }
    }

    /**
     * 필드 성공 제거 (수정된 버전)
     * @param {string} field 필드명
     */
    clearFieldSuccess(field) {
        const input = this.elements[`${field}Input`];
        const success = this.elements[`${field}Success`];

        if (input && success) {
            input.classList.remove('success');
            success.textContent = '';
            success.classList.remove('show'); // 이 줄 추가!
        }

        // 중복확인 버튼 상태 초기화
        if (field === 'email' && this.elements.emailCheckBtn) {
            this.elements.emailCheckBtn.classList.remove('success');
            this.elements.emailCheckBtn.textContent = '중복확인';
        }
        if (field === 'nickname' && this.elements.nicknameCheckBtn) {
            this.elements.nicknameCheckBtn.classList.remove('success');
            this.elements.nicknameCheckBtn.textContent = '중복확인';
        }
    }

    /**
     * 전체 필드 에러 제거
     */
    clearAllErrors() {
        ['name', 'nickname', 'email', 'password', 'passwordConfirm', 'phone'].forEach(field => {
            this.clearFieldError(field);
            this.clearFieldSuccess(field);
        });
    }

    /**
     * 알림 메시지 표시
     * @param {string} message 메시지 내용
     * @param {string} type 메시지 타입 (success, error, info)
     */
    showAlert(message, type = 'error') {
        this.elements.alertContainer.className = `alert alert-${type}`;
        this.elements.alertContainer.textContent = message;
        this.elements.alertContainer.style.display = 'block';

        const hideDelay = type === 'success' ? 3000 : 5000;
        setTimeout(() => {
            this.elements.alertContainer.style.display = 'none';
        }, hideDelay);
    }

    /**
     * 로딩 상태 토글
     * @param {boolean} loading 로딩 여부
     */
    toggleLoading(loading) {
        if (loading) {
            this.elements.button.disabled = true;
            this.elements.button.classList.add('btn-loading');
            this.elements.button.querySelector('.btn-text').textContent = '가입 중...';
        } else {
            this.elements.button.disabled = false;
            this.elements.button.classList.remove('btn-loading');
            this.elements.button.querySelector('.btn-text').textContent = '가입하기';
        }
    }

    /**
     * 버튼 로딩 상태 설정
     * @param {string} buttonId 버튼 ID
     * @param {boolean} loading 로딩 여부
     */
    setButtonLoading(buttonId, loading) {
        const button = this.elements[buttonId];
        if (!button) return;

        if (loading) {
            button.disabled = true;
            button.textContent = '확인 중...';
        } else {
            button.disabled = false;
            if (!button.classList.contains('success')) {
                button.textContent = '중복확인';
            }
        }
    }

    /**
     * 제출 버튼 활성화 상태 업데이트
     */
    updateSubmitButton() {
        // 이메일과 닉네임 중복확인이 완료되었는지 확인
        const emailValid = this.validationStates.email;
        const nicknameValid = this.validationStates.nickname;

        // 기본 필드 검증
        const basicFieldsValid = this.validateBasicFields();

        // 모든 조건이 만족되면 버튼 활성화
        this.elements.button.disabled = !(emailValid && nicknameValid && basicFieldsValid);
    }

    /**
     * 기본 필드들 검증 (중복확인 제외)
     */
    validateBasicFields() {
        const formData = this.getFormData();

        const name = formData.get('name').trim();
        const password = formData.get('password');
        const passwordConfirm = formData.get('passwordConfirm');
        const phone = formData.get('phone').trim();

        const nameValid = SignupValidator.validateName(name).isValid;
        const passwordValid = SignupValidator.validatePassword(password).isValid;
        const passwordConfirmValid = SignupValidator.validatePasswordConfirm(password, passwordConfirm).isValid;
        const phoneValid = SignupValidator.validatePhone(phone).isValid;

        return nameValid && passwordValid && passwordConfirmValid && phoneValid;
    }

    // ================ 관심사 관리 메서드 ================

    /**
     * 카테고리 전환
     * @param {string} category 카테고리명
     */
    switchCategory(category) {
        // 모든 카테고리 버튼에서 active 제거
        this.elements.categoryBtns.forEach(btn => {
            btn.classList.remove('active');
        });

        // 선택된 카테고리 버튼에 active 추가
        const selectedBtn = document.querySelector(`[data-category="${category}"]`);
        if (selectedBtn) {
            selectedBtn.classList.add('active');
        }

        // 모든 태그 그룹 숨기기
        this.elements.tagGroups.forEach(group => {
            group.style.display = 'none';
        });

        // 선택된 카테고리의 태그 그룹 보이기
        const selectedGroup = document.querySelector(`.tag-group[data-category="${category}"]`);
        if (selectedGroup) {
            selectedGroup.style.display = 'flex';
        }
    }

    /**
     * 태그 선택/해제 토글
     * @param {string} tag 태그명
     */
    toggleTag(tag) {
        if (this.selectedTags.has(tag)) {
            this.selectedTags.delete(tag);
        } else {
            // 최대 10개까지만 선택 가능
            if (this.selectedTags.size >= 10) {
                this.showAlert('관심사는 최대 10개까지 선택 가능합니다.', 'error');
                return;
            }
            this.selectedTags.add(tag);
        }

        this.updateTagButtons();
        this.updateSelectedTags();
    }

    /**
     * 태그 버튼 상태 업데이트
     */
    updateTagButtons() {
        document.querySelectorAll('.tag-btn').forEach(btn => {
            const tag = btn.dataset.tag;
            if (this.selectedTags.has(tag)) {
                btn.classList.add('selected');
            } else {
                btn.classList.remove('selected');
            }
        });
    }

    /**
     * 선택된 태그 표시 업데이트
     */
    updateSelectedTags() {
        const container = this.elements.selectedTagsContainer;
        container.innerHTML = '';

        if (this.selectedTags.size === 0) {
            container.innerHTML = '<span style="color: #9ca3af; font-size: 12px;">선택된 관심사가 없습니다.</span>';
            return;
        }

        this.selectedTags.forEach(tag => {
            const tagElement = document.createElement('span');
            tagElement.className = 'selected-tag';
            tagElement.innerHTML = `
                ${tag}
                <span class="remove" onclick="signupUI.removeTag('${tag}')">&times;</span>
            `;
            container.appendChild(tagElement);
        });
    }

    /**
     * 태그 제거
     * @param {string} tag 제거할 태그
     */
    removeTag(tag) {
        this.selectedTags.delete(tag);
        this.updateTagButtons();
        this.updateSelectedTags();
    }

    /**
     * 선택된 태그 배열 반환
     * @returns {Array} 선택된 태그 배열
     */
    getSelectedTags() {
        return Array.from(this.selectedTags);
    }

    // ================ 기타 유틸리티 메서드 ================

    /**
     * 폼 데이터 가져오기
     * @returns {FormData} 폼 데이터
     */
    getFormData() {
        return new FormData(this.elements.form);
    }

    /**
     * 폼 리셋
     */
    resetForm() {
        this.elements.form.reset();
        this.clearAllErrors();
        this.selectedTags.clear();
        this.updateSelectedTags();
        this.updateTagButtons();
        this.validationStates.email = false;
        this.validationStates.nickname = false;
        this.updateSubmitButton();

        // 비밀번호 요구사항 초기화
        Object.values(this.elements.requirements).forEach(req => {
            req.classList.remove('valid');
        });
    }

    /**
     * 폼 유효성 검사 및 UI 업데이트
     * @returns {boolean} 전체 검증 결과
     */
    validateForm() {
        const formData = this.getFormData();
        const selectedTags = this.getSelectedTags();

        const validation = SignupValidator.validateForm(formData, selectedTags);

        // 에러 표시
        Object.keys(validation.errors).forEach(field => {
            this.showFieldError(field, validation.errors[field]);
        });

        // 중복확인 상태 체크
        if (!this.validationStates.email && formData.get('email').trim()) {
            this.showFieldError('email', '이메일 중복확인이 필요합니다.');
            validation.isValid = false;
        }

        if (!this.validationStates.nickname && formData.get('nickname').trim()) {
            this.showFieldError('nickname', '닉네임 중복확인이 필요합니다.');
            validation.isValid = false;
        }

        return validation.isValid;
    }
}