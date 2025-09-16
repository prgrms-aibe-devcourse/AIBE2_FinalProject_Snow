/**
 * 회원가입 폼 UI 관리 클래스
 * 폼 요소들의 상태, 검증, 이벤트 처리를 담당
 */
class SignupUI {
    constructor() {
        this.elements = this.initializeElements();
        this.selectedTags = new Set(); // 선택된 관심사 태그
        this.categories = []; // 카테고리 데이터
        this.categoryTags = {}; // 카테고리별 태그 데이터

        // 검증 상태 관리
        this.validationStates = {
            email: false,
            nickname: false
        };

        this.init();
    }

    /**
     * DOM 요소 초기화
     */
    initializeElements() {
        return {
            form: document.getElementById('signupForm'),

            // 입력 필드들
            nameInput: document.getElementById('name'),
            nicknameInput: document.getElementById('nickname'),
            emailInput: document.getElementById('email'),
            passwordInput: document.getElementById('password'),
            passwordConfirmInput: document.getElementById('passwordConfirm'),
            phoneInput: document.getElementById('phone'),

            // 버튼들
            nicknameCheckBtn: document.getElementById('nicknameCheckBtn'),
            emailCheckBtn: document.getElementById('emailCheckBtn'),
            submitBtn: document.getElementById('signupBtn'),

            // 비밀번호 요구사항 요소들
            requirements: {
                length: document.getElementById('req-length'),
                upperLower: document.getElementById('req-upper-lower'),
                number: document.getElementById('req-number'),
                special: document.getElementById('req-special')
            },

            // 관심사 관련 요소들
            categoryBtns: document.querySelectorAll('.category-btn'),
            tagGroups: document.querySelectorAll('.tag-group'),
            tagBtns: document.querySelectorAll('.tag-btn'),
            selectedTagsContainer: document.getElementById('selectedTags'),

            // 카테고리 컨테이너
            categoryContainer: document.querySelector('.interest-categories'),
            tagsContainer: document.querySelector('.interest-tags')
        };
    }

    /**
     * UI 초기화
     */
    async init() {
        this.setupEventListeners();
        await this.loadCategories(); // 카테고리 데이터 로드
        this.updateSubmitButton();
    }

    /**
     * 카테고리 데이터 로드 및 UI 생성
     */
    async loadCategories() {
        try {
            console.log('카테고리 데이터 로딩 시작...');

            const signupApi = new SignupApi();
            this.categories = await signupApi.getAllCategories();

            console.log('로드된 카테고리 데이터:', this.categories);

            if (this.categories && this.categories.length > 0) {
                this.buildCategoryUI();
                this.setupCategoryEventListeners();

                // 첫 번째 카테고리 활성화
                if (this.categories[0]) {
                    this.switchCategory(this.categories[0].slug);
                }
            } else {
                console.warn('카테고리 데이터가 없습니다.');
                this.showAlert('카테고리 정보를 불러오는데 실패했습니다.', 'error');
            }
        } catch (error) {
            console.error('카테고리 로드 실패:', error);
            this.showAlert('카테고리 정보를 불러오는데 실패했습니다.', 'error');
            // 기본 카테고리로 폴백
            this.setupFallbackCategories();
        }
    }

    /**
     * 카테고리 UI 동적 생성
     */
    buildCategoryUI() {
        // 카테고리 버튼 생성
        this.elements.categoryContainer.innerHTML = '';

        this.categories.forEach((category, index) => {
            const button = document.createElement('button');
            button.type = 'button';
            button.className = `category-btn ${index === 0 ? 'active' : ''}`;
            button.setAttribute('data-category', category.slug);
            button.textContent = category.name;

            this.elements.categoryContainer.appendChild(button);
        });

        // 카테고리별 태그 그룹 생성
        this.elements.tagsContainer.innerHTML = '';

        this.categories.forEach((category, index) => {
            const tagGroup = document.createElement('div');
            tagGroup.className = 'tag-group';
            tagGroup.setAttribute('data-category', category.slug);
            tagGroup.style.display = index === 0 ? 'flex' : 'none';

            // 각 카테고리별 기본 태그들 생성
            const defaultTags = this.getDefaultTagsForCategory(category.slug);
            defaultTags.forEach(tag => {
                const tagButton = document.createElement('button');
                tagButton.type = 'button';
                tagButton.className = 'tag-btn';
                tagButton.setAttribute('data-tag', tag);
                tagButton.textContent = tag;

                tagGroup.appendChild(tagButton);
            });

            this.elements.tagsContainer.appendChild(tagGroup);
        });

        // 요소 참조 업데이트
        this.elements.categoryBtns = document.querySelectorAll('.category-btn');
        this.elements.tagGroups = document.querySelectorAll('.tag-group');
        this.elements.tagBtns = document.querySelectorAll('.tag-btn');
    }

    /**
     * 카테고리별 기본 태그 반환
     * @param {string} categorySlug 카테고리 슬러그
     * @returns {Array} 태그 배열
     */
    getDefaultTagsForCategory(categorySlug) {
        const categoryTagMap = {
            'fashion': ['의류', '신발', '가방', '액세서리', '빈티지'],
            'pet': ['강아지', '고양이', '용품', '건강', '훈련'],
            'game': ['PC게임', '모바일게임', '콘솔게임', 'E스포츠', '리뷰'],
            'character': ['애니메이션', '만화', '피규어', '굿즈', '코스프레'],
            'culture': ['음악', '영화', '도서', '전시', '카페'],
            'entertainment': ['드라마', '예능', 'K-POP', '연예인', '팬덤'],
            'travel': ['국내여행', '해외여행', '맛집', '호텔', '액티비티']
        };

        return categoryTagMap[categorySlug] || ['기타'];
    }

    /**
     * 폴백 카테고리 설정 (API 실패 시)
     */
    setupFallbackCategories() {
        this.categories = [
            { id: 1, name: '패션', slug: 'fashion' },
            { id: 2, name: '반려동물', slug: 'pet' },
            { id: 3, name: '게임', slug: 'game' },
            { id: 4, name: '캐릭터/IP', slug: 'character' },
            { id: 5, name: '문화/컨텐츠', slug: 'culture' },
            { id: 6, name: '연예', slug: 'entertainment' },
            { id: 7, name: '여행/레저/스포츠', slug: 'travel' }
        ];

        this.buildCategoryUI();
        this.setupCategoryEventListeners();
        this.switchCategory('fashion');
    }

    /**
     * 카테고리 관련 이벤트 리스너 설정
     */
    setupCategoryEventListeners() {
        // 카테고리 버튼 클릭 이벤트
        this.elements.categoryBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const category = e.target.dataset.category;
                this.switchCategory(category);
            });
        });

        // 태그 버튼 클릭 이벤트
        this.elements.tagBtns.forEach(btn => {
            btn.addEventListener('click', (e) => {
                const tag = e.target.dataset.tag;
                this.toggleTag(tag);
                this.updateTagButtonState(e.target);
            });
        });
    }

    /**
     * 기존 이벤트 리스너 설정
     */
    setupEventListeners() {
        // 입력 필드 이벤트 리스너
        if (this.elements.nameInput) {
            this.elements.nameInput.addEventListener('input', () => this.validateNameField());
        }

        if (this.elements.nicknameInput) {
            this.elements.nicknameInput.addEventListener('input', () => this.validateNicknameField());
        }

        if (this.elements.emailInput) {
            this.elements.emailInput.addEventListener('input', () => this.validateEmailField());
        }

        if (this.elements.passwordInput) {
            this.elements.passwordInput.addEventListener('input', () => this.validatePasswordField());
        }

        if (this.elements.passwordConfirmInput) {
            this.elements.passwordConfirmInput.addEventListener('input', () => this.validatePasswordConfirmField());
        }

        if (this.elements.phoneInput) {
            this.elements.phoneInput.addEventListener('input', () => this.validatePhoneField());
        }

        // 중복확인 버튼 이벤트
        if (this.elements.nicknameCheckBtn) {
            this.elements.nicknameCheckBtn.addEventListener('click', () => this.validateNicknameField(true));
        }

        if (this.elements.emailCheckBtn) {
            this.elements.emailCheckBtn.addEventListener('click', () => this.validateEmailField(true));
        }

        // 폼 제출 방지 (컨트롤러에서 처리)
        if (this.elements.form) {
            this.elements.form.addEventListener('submit', (e) => {
                e.preventDefault();
            });
        }
    }

    // ================ 카테고리 관리 메서드 ================

    /**
     * 카테고리 전환
     * @param {string} categorySlug 카테고리 슬러그
     */
    switchCategory(categorySlug) {
        // 모든 카테고리 버튼에서 active 제거
        this.elements.categoryBtns.forEach(btn => {
            btn.classList.remove('active');
        });

        // 선택된 카테고리 버튼에 active 추가
        const selectedBtn = document.querySelector(`[data-category="${categorySlug}"]`);
        if (selectedBtn) {
            selectedBtn.classList.add('active');
        }

        // 모든 태그 그룹 숨기기
        this.elements.tagGroups.forEach(group => {
            group.style.display = 'none';
        });

        // 선택된 카테고리의 태그 그룹 보이기
        const selectedGroup = document.querySelector(`.tag-group[data-category="${categorySlug}"]`);
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
                this.showAlert('관심사는 최대 10개까지 선택 가능합니다.', 'warning');
                return;
            }
            this.selectedTags.add(tag);
        }

        this.updateSelectedTagsDisplay();
        this.updateSubmitButton();
    }

    /**
     * 태그 버튼 상태 업데이트
     * @param {HTMLElement} tagButton 태그 버튼 요소
     */
    updateTagButtonState(tagButton) {
        const tag = tagButton.dataset.tag;

        if (this.selectedTags.has(tag)) {
            tagButton.classList.add('selected');
        } else {
            tagButton.classList.remove('selected');
        }
    }

    /**
     * 선택된 태그 표시 업데이트
     */
    updateSelectedTagsDisplay() {
        if (!this.elements.selectedTagsContainer) return;

        this.elements.selectedTagsContainer.innerHTML = '';

        if (this.selectedTags.size === 0) {
            this.elements.selectedTagsContainer.innerHTML = '<span class="no-selection">선택된 관심사가 없습니다.</span>';
            return;
        }

        this.selectedTags.forEach(tag => {
            const tagElement = document.createElement('span');
            tagElement.className = 'selected-tag';
            tagElement.innerHTML = `
                ${tag}
                <button type="button" class="remove-tag" data-tag="${tag}">&times;</button>
            `;

            // 삭제 버튼 이벤트
            const removeBtn = tagElement.querySelector('.remove-tag');
            removeBtn.addEventListener('click', () => {
                this.toggleTag(tag);
                this.updateAllTagButtonStates();
            });

            this.elements.selectedTagsContainer.appendChild(tagElement);
        });
    }

    /**
     * 모든 태그 버튼 상태 업데이트
     */
    updateAllTagButtonStates() {
        this.elements.tagBtns.forEach(btn => {
            this.updateTagButtonState(btn);
        });
    }

    /**
     * 선택된 태그 배열 반환
     * @returns {Array} 선택된 태그 배열
     */
    getSelectedTags() {
        return Array.from(this.selectedTags);
    }

    // ================ 기존 검증 메서드들 ================

    /**
     * 이름 필드 검증
     */
    validateNameField() {
        const name = this.elements.nameInput.value.trim();
        const result = SignupValidator.validateName(name);

        if (!result.isValid) {
            this.showFieldError('name', result.message);
        } else {
            this.clearFieldError('name');
        }

        this.updateSubmitButton();
        return result.isValid;
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
            return this.validationStates.nickname;
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
            return this.validationStates.email;
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
                        element.classList.remove('invalid');
                    } else {
                        element.classList.add('invalid');
                        element.classList.remove('valid');
                    }
                }
            });
        }

        if (!result.isValid) {
            this.showFieldError('password', result.message);
        } else {
            this.clearFieldError('password');
        }

        // 비밀번호 확인 재검증
        this.validatePasswordConfirmField();
        this.updateSubmitButton();

        return result.isValid;
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
        } else {
            this.clearFieldError('passwordConfirm');
        }

        this.updateSubmitButton();
        return result.isValid;
    }

    /**
     * 핸드폰 번호 필드 검증
     */
    validatePhoneField() {
        const phone = this.elements.phoneInput.value.trim();
        const result = SignupValidator.validatePhone(phone);

        if (!result.isValid) {
            this.showFieldError('phone', result.message);
        } else {
            this.clearFieldError('phone');
        }

        this.updateSubmitButton();
        return result.isValid;
    }

    // ================ UI 헬퍼 메서드들 ================

    /**
     * 필드 에러 표시
     * @param {string} fieldName 필드명
     * @param {string} message 에러 메시지
     */
    showFieldError(fieldName, message) {
        const errorElement = document.getElementById(`${fieldName}-error`);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.className = 'field-error show';
        }

        const inputElement = document.getElementById(fieldName);
        if (inputElement) {
            inputElement.classList.add('error');
            inputElement.classList.remove('success');
        }
    }

    /**
     * 필드 성공 표시
     * @param {string} fieldName 필드명
     * @param {string} message 성공 메시지
     */
    showFieldSuccess(fieldName, message) {
        const errorElement = document.getElementById(`${fieldName}-error`);
        if (errorElement) {
            errorElement.textContent = message;
            errorElement.className = 'field-error success show';
        }

        const inputElement = document.getElementById(fieldName);
        if (inputElement) {
            inputElement.classList.add('success');
            inputElement.classList.remove('error');
        }
    }

    /**
     * 필드 에러 클리어
     * @param {string} fieldName 필드명
     */
    clearFieldError(fieldName) {
        const errorElement = document.getElementById(`${fieldName}-error`);
        if (errorElement) {
            errorElement.textContent = '';
            errorElement.className = 'field-error';
        }

        const inputElement = document.getElementById(fieldName);
        if (inputElement) {
            inputElement.classList.remove('error', 'success');
        }
    }

    /**
     * 버튼 로딩 상태 설정
     * @param {string} buttonId 버튼 ID
     * @param {boolean} loading 로딩 여부
     */
    setButtonLoading(buttonId, loading) {
        const button = document.getElementById(buttonId);
        if (!button) return;

        if (loading) {
            button.disabled = true;
            button.innerHTML = '<span class="loading"></span>확인 중...';
        } else {
            button.disabled = false;
            if (buttonId === 'nicknameCheckBtn') {
                button.textContent = '중복확인';
            } else if (buttonId === 'emailCheckBtn') {
                button.textContent = '중복확인';
            }
        }
    }

    /**
     * 알림 메시지 표시
     * @param {string} message 메시지
     * @param {string} type 메시지 타입 (success, error, warning, info)
     */
    showAlert(message, type = 'info') {
        // 기존 알림 제거
        const existingAlert = document.querySelector('.alert-message');
        if (existingAlert) {
            existingAlert.remove();
        }

        // 새 알림 생성
        const alert = document.createElement('div');
        alert.className = `alert-message alert-${type}`;
        alert.textContent = message;

        // 폼 상단에 추가
        if (this.elements.form) {
            this.elements.form.insertBefore(alert, this.elements.form.firstChild);
        }

        // 3초 후 자동 제거
        setTimeout(() => {
            if (alert.parentNode) {
                alert.remove();
            }
        }, 3000);
    }

    /**
     * 로딩 상태 토글
     * @param {boolean} loading 로딩 여부
     */
    toggleLoading(loading) {
        if (!this.elements.submitBtn) return;

        const loadingElement = this.elements.submitBtn.querySelector('.loading');
        const textElement = this.elements.submitBtn.querySelector('.btn-text');

        if (loading) {
            this.elements.submitBtn.disabled = true;
            if (loadingElement) loadingElement.style.display = 'inline-block';
            if (textElement) textElement.textContent = '가입 중...';
        } else {
            this.elements.submitBtn.disabled = false;
            if (loadingElement) loadingElement.style.display = 'none';
            if (textElement) textElement.textContent = '가입하기';
        }
    }

    /**
     * 제출 버튼 상태 업데이트
     */
    updateSubmitButton() {
        if (!this.elements.submitBtn) return;

        const isFormValid = this.validateForm();
        this.elements.submitBtn.disabled = !isFormValid;
    }

    /**
     * 폼 전체 검증
     * @returns {boolean} 검증 결과
     */
    validateForm() {
        const formData = this.getFormData();
        const selectedTags = this.getSelectedTags();

        // FormData 검증
        const formValidation = SignupValidator.validateForm(formData, selectedTags);
        if (!formValidation.isValid) {
            return false;
        }

        // 중복확인 상태 검증
        const email = formData.get('email').trim();
        const nickname = formData.get('nickname').trim();

        const emailValid = !email || this.validationStates.email;
        const nicknameValid = !nickname || this.validationStates.nickname;
        const basicFieldsValid = this.validateBasicFields();

        return (emailValid && nicknameValid && basicFieldsValid);
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
        if (this.elements.form) {
            this.elements.form.reset();
        }

        // 선택된 태그 초기화
        this.selectedTags.clear();
        this.updateSelectedTagsDisplay();
        this.updateAllTagButtonStates();

        // 검증 상태 초기화
        this.validationStates = {
            email: false,
            nickname: false
        };

        // 에러 메시지 클리어
        const errorElements = document.querySelectorAll('.field-error');
        errorElements.forEach(element => {
            element.textContent = '';
            element.className = 'field-error';
        });

        // 입력 필드 상태 클리어
        const inputElements = document.querySelectorAll('.form-input');
        inputElements.forEach(element => {
            element.classList.remove('error', 'success');
        });

        // 비밀번호 요구사항 초기화
        Object.values(this.elements.requirements).forEach(element => {
            if (element) {
                element.classList.remove('valid', 'invalid');
            }
        });

        // 첫 번째 카테고리로 초기화
        if (this.categories.length > 0) {
            this.switchCategory(this.categories[0].slug);
        }

        this.updateSubmitButton();
    }
}