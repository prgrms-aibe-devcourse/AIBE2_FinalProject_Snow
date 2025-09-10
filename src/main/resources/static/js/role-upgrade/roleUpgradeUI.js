/**
 * 역할 승격 요청 UI 관리 클래스
 */
class RoleUpgradeUI {
    constructor() {
        this.activeTab = 'guest'; // 기본값: 기업
        this.elements = this.getElements();

        this.init();
    }

    /**
     * DOM 요소 가져오기
     */
    getElements() {
        return {
            form: document.getElementById('upgradeForm'),
            tabBtns: document.querySelectorAll('.tab-btn'),

            // 폼 필드들
            companyInput: document.getElementById('company'),
            companyLabel: document.getElementById('company-label'),
            businessNumberInput: document.getElementById('businessNumber'),
            permissionSelect: document.getElementById('permission'),
            additionalTextarea: document.getElementById('additional'),
            businessFileInput: document.getElementById('businessFile'),

            // 그룹 요소들
            roleGroup: document.getElementById('role-group'),

            // 에러 표시 요소들
            companyError: document.getElementById('company-error'),
            businessNumberError: document.getElementById('businessNumber-error'),
            permissionError: document.getElementById('permission-error'),
            additionalError: document.getElementById('additional-error'),
            fileError: document.getElementById('file-error'),

            // 기타
            alertContainer: document.getElementById('alert-container'),
            fileNameDisplay: document.getElementById('file-name'),
            submitBtn: document.getElementById('upgradeBtn')
        };
    }

    /**
     * UI 초기화
     */
    init() {
        this.setupTabSwitching();
        this.setupFormValidation();
        this.setupFileUpload();

        // 초기 상태 설정
        this.switchTab('guest');
    }

    /**
     * 탭 전환 이벤트 설정
     */
    setupTabSwitching() {
        this.elements.tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const tab = btn.getAttribute('data-tab');
                this.switchTab(tab);
            });
        });
    }

    /**
     * 폼 유효성 검사 이벤트 설정
     */
    setupFormValidation() {
        // 회사명/공간 표시명 입력
        if (this.elements.companyInput) {
            this.elements.companyInput.addEventListener('blur', () => {
                this.validateField('company');
            });
        }

        // 사업자등록번호 입력
        if (this.elements.businessNumberInput) {
            this.elements.businessNumberInput.addEventListener('input', (e) => {
                // 숫자와 하이픈만 허용
                e.target.value = e.target.value.replace(/[^0-9-]/g, '');
            });

            this.elements.businessNumberInput.addEventListener('blur', () => {
                this.validateField('businessNumber');
            });
        }

        // 권한 선택
        if (this.elements.permissionSelect) {
            this.elements.permissionSelect.addEventListener('change', () => {
                this.validateField('permission');
            });
        }

        // 추가 작성 사항
        if (this.elements.additionalTextarea) {
            this.elements.additionalTextarea.addEventListener('blur', () => {
                this.validateField('additional');
            });
        }

        // 파일 업로드
        if (this.elements.businessFileInput) {
            this.elements.businessFileInput.addEventListener('change', (e) => {
                this.handleFileSelect(e.target.files[0]);
            });
        }
    }

    /**
     * 탭 전환 (수정된 로직)
     * @param {string} tab 탭 이름 ('guest' 또는 'host')
     */
    switchTab(tab) {
        this.activeTab = tab;

        if (tab === 'guest') {
            this.elements.companyLabel.textContent = '회사명';
            this.elements.companyInput.placeholder = '회사명을 입력해 주세요';
            this.showRoleField();
        } else {
            this.elements.companyLabel.textContent = '공간 표시명';
            this.elements.companyInput.placeholder = '공간 표시명을 입력해 주세요';
            this.hideRoleField();
        }

        // 탭 활성화 토글 (tabButtons → tabBtns)
        this.elements.tabBtns.forEach(btn => btn.classList.remove('active'));
        const activeBtn = Array.from(this.elements.tabBtns).find(btn => btn.dataset.tab === tab);
        if (activeBtn) activeBtn.classList.add('active');
    }

    /**
     * 권한 필드 표시
     */
    showRoleField() {
        this.elements.roleGroup.classList.remove('hidden');
    }

    /**
     * 권한 필드 숨기기
     */
    hideRoleField() {
        this.elements.roleGroup.classList.add('hidden');
        // 권한 선택 초기화
        if (this.elements.permissionSelect) {
            this.elements.permissionSelect.value = '';
        }
    }

    /**
     * 파일 업로드 설정
     */
    setupFileUpload() {
        // 파일 선택 영역 클릭 이벤트
        const fileLabel = document.querySelector('.file-label');
        if (fileLabel) {
            fileLabel.addEventListener('click', () => {
                this.elements.businessFileInput.click();
            });
        }
    }

    /**
     * 파일 선택 처리
     */
    handleFileSelect(file) {
        if (!file) {
            this.elements.fileNameDisplay.textContent = '';
            this.clearFieldError('file');
            return;
        }

        // 파일 검증 (이제 선택사항이므로 에러가 나면 표시만)
        const validation = RoleUpgradeValidator.validateFile(file);
        if (!validation.isValid) {
            this.showFieldError('file', validation.message);
            this.elements.businessFileInput.value = '';
            this.elements.fileNameDisplay.textContent = '';
            return;
        }

        // 파일명 표시
        this.elements.fileNameDisplay.textContent = file.name;
        this.clearFieldError('file');
    }

    /**
     * 필드별 유효성 검사
     */
    validateField(fieldName) {
        const value = this.getFieldValue(fieldName);
        const result = RoleUpgradeValidator.validateField(fieldName, value, this.activeTab);

        if (result.isValid) {
            this.clearFieldError(fieldName);
        } else {
            this.showFieldError(fieldName, result.message);
        }

        return result.isValid;
    }

    /**
     * 필드 값 가져오기
     */
    getFieldValue(fieldName) {
        switch (fieldName) {
            case 'company':
                return this.elements.companyInput?.value?.trim() || '';
            case 'businessNumber':
                return this.elements.businessNumberInput?.value?.trim() || '';
            case 'permission':
                return this.elements.permissionSelect?.value || '';
            case 'additional':
                return this.elements.additionalTextarea?.value?.trim() || '';
            default:
                return '';
        }
    }

    /**
     * 전체 폼 유효성 검사 (파일 검증 제거)
     */
    validateForm() {
        const errors = {};

        // 기본 필수 필드
        const requiredFields = ['company', 'businessNumber'];

        // 기업(guest) 탭에서만 권한 필수
        if (this.activeTab === 'guest') {
            requiredFields.push('permission');
        }

        requiredFields.forEach(field => {
            const value = this.getFieldValue(field);
            const validation = RoleUpgradeValidator.validateField(field, value, this.activeTab);
            if (!validation.isValid) {
                errors[field] = validation.message;
            }
        });

        return errors;
    }


    /**
     * 승격 요청 데이터 가져오기
     */
    getUpgradeRequestData() {
        const isBusiness = this.activeTab === 'guest'; // 기업 탭 여부

        // 폼 값을 payload로 모으기
        const payload = {
            company: this.getFieldValue('company'),
            businessNumber: this.getFieldValue('businessNumber'),
            additional: this.getFieldValue('additional'),
            ...(isBusiness ? { permission: this.getFieldValue('permission') } : {})
        };

        // 기업(guest) → HOST, 공간제공자(host) → PROVIDER
        return {
            requestedRole: isBusiness ? 'HOST' : 'PROVIDER',
            // 대부분 스프링 컨트롤러에서 payload를 String으로 받으므로 문자열로 전송
            payload: JSON.stringify(payload)
        };
    }

    /**
     * 선택된 파일 가져오기
     */
    getSelectedFile() {
        return this.elements.businessFileInput?.files[0] || null;
    }

    /**
     * 필드 에러 표시
     */
    showFieldError(fieldName, message) {
        const errorElement = this.elements[fieldName + 'Error'];
        const inputElement = this.getInputElement(fieldName);

        if (errorElement) {
            errorElement.textContent = message;
        }

        if (inputElement) {
            inputElement.classList.add('error');
        }
    }

    /**
     * 필드 에러 제거
     */
    clearFieldError(fieldName) {
        const errorElement = this.elements[fieldName + 'Error'];
        const inputElement = this.getInputElement(fieldName);

        if (errorElement) {
            errorElement.textContent = '';
        }

        if (inputElement) {
            inputElement.classList.remove('error');
        }
    }

    /**
     * 입력 요소 가져오기
     */
    getInputElement(fieldName) {
        switch (fieldName) {
            case 'company':
                return this.elements.companyInput;
            case 'businessNumber':
                return this.elements.businessNumberInput;
            case 'permission':
                return this.elements.permissionSelect;
            case 'additional':
                return this.elements.additionalTextarea;
            case 'file':
                return this.elements.businessFileInput;
            default:
                return null;
        }
    }

    /**
     * 모든 에러 제거
     */
    clearErrors() {
        const errorFields = ['company', 'businessNumber', 'permission', 'additional', 'file'];
        errorFields.forEach(field => {
            this.clearFieldError(field);
        });
    }

    /**
     * 알림 메시지 표시
     */
    showAlert(message, type = 'info') {
        const container = this.elements.alertContainer;
        if (!container) return;

        container.textContent = message;
        container.className = `alert ${type}`;
        container.style.display = 'block';

        // 3초 후 자동 숨김
        setTimeout(() => {
            container.style.display = 'none';
        }, 3000);
    }

    /**
     * 로딩 상태 토글
     */
    toggleLoading(isLoading) {
        if (!this.elements.submitBtn) return;

        if (isLoading) {
            this.elements.submitBtn.classList.add('loading');
            this.elements.submitBtn.disabled = true;
        } else {
            this.elements.submitBtn.classList.remove('loading');
            this.elements.submitBtn.disabled = false;
        }
    }

    /**
     * 폼 초기화
     */
    resetForm() {
        if (this.elements.form) {
            this.elements.form.reset();
        }

        this.elements.fileNameDisplay.textContent = '';
        this.clearErrors();
        this.switchTab('guest'); // 기본 탭으로 초기화
    }
}