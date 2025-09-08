/**
 * 회원가입 폼 검증 유틸리티
 * 이름, 닉네임, 이메일, 비밀번호, 핸드폰번호 유효성 검사를 담당
 */
class SignupValidator {

    /**
     * 이름 검증
     * @param {string} name 이름
     * @returns {Object} { isValid: boolean, message: string }
     */
    static validateName(name) {
        const trimmedName = name.trim();

        if (!trimmedName) {
            return { isValid: false, message: '이름을 입력해주세요.' };
        }

        if (trimmedName.length < 2) {
            return { isValid: false, message: '이름은 2자 이상 입력해주세요.' };
        }

        if (trimmedName.length > 10) {
            return { isValid: false, message: '이름은 10자 이하로 입력해주세요.' };
        }

        // 한글, 영문만 허용 (공백 제외)
        const nameRegex = /^[가-힣a-zA-Z]+$/;
        if (!nameRegex.test(trimmedName)) {
            return { isValid: false, message: '이름은 한글 또는 영문만 입력 가능합니다.' };
        }

        return { isValid: true, message: '' };
    }

    /**
     * 닉네임 검증
     * @param {string} nickname 닉네임
     * @returns {Object} { isValid: boolean, message: string }
     */
    static validateNickname(nickname) {
        const trimmedNickname = nickname.trim();

        if (!trimmedNickname) {
            return { isValid: false, message: '닉네임을 입력해주세요.' };
        }

        if (trimmedNickname.length < 2) {
            return { isValid: false, message: '닉네임은 2자 이상 입력해주세요.' };
        }

        if (trimmedNickname.length > 15) {
            return { isValid: false, message: '닉네임은 15자 이하로 입력해주세요.' };
        }

        // 한글, 영문, 숫자만 허용
        const nicknameRegex = /^[가-힣a-zA-Z0-9]+$/;
        if (!nicknameRegex.test(trimmedNickname)) {
            return { isValid: false, message: '닉네임은 한글, 영문, 숫자만 사용 가능합니다.' };
        }

        // 금지어 체크 (예시)
        const forbiddenWords = ['admin', '관리자', 'test', 'null', 'undefined'];
        if (forbiddenWords.some(word => trimmedNickname.toLowerCase().includes(word))) {
            return { isValid: false, message: '사용할 수 없는 닉네임입니다.' };
        }

        return { isValid: true, message: '' };
    }

    /**
     * 이메일 검증
     * @param {string} email 이메일 주소
     * @returns {Object} { isValid: boolean, message: string }
     */
    static validateEmail(email) {
        const trimmedEmail = email.trim();

        if (!trimmedEmail) {
            return { isValid: false, message: '이메일을 입력해주세요.' };
        }

        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(trimmedEmail)) {
            return { isValid: false, message: '올바른 이메일 형식을 입력해주세요.' };
        }

        if (trimmedEmail.length > 100) {
            return { isValid: false, message: '이메일은 100자를 초과할 수 없습니다.' };
        }

        return { isValid: true, message: '' };
    }

    /**
     * 비밀번호 검증
     * @param {string} password 비밀번호
     * @returns {Object} { isValid: boolean, message: string, requirements: Object }
     */
    static validatePassword(password) {
        if (!password) {
            return {
                isValid: false,
                message: '비밀번호를 입력해주세요.',
                requirements: this.getPasswordRequirements(password)
            };
        }

        const requirements = this.getPasswordRequirements(password);
        const allValid = Object.values(requirements).every(req => req.valid);

        if (!allValid) {
            return {
                isValid: false,
                message: '비밀번호 조건을 만족해주세요.',
                requirements: requirements
            };
        }

        if (password.length > 20) {
            return {
                isValid: false,
                message: '비밀번호는 20자 이하로 입력해주세요.',
                requirements: requirements
            };
        }

        return {
            isValid: true,
            message: '사용 가능한 비밀번호입니다.',
            requirements: requirements
        };
    }

    /**
     * 비밀번호 요구사항 체크
     * @param {string} password 비밀번호
     * @returns {Object} 각 요구사항별 만족 여부
     */
    static getPasswordRequirements(password) {
        const requirements = {
            length: {
                valid: password && password.length >= 8,
                message: '8자 이상'
            },
            letter: {
                valid: password && /[a-zA-Z]/.test(password),
                message: '영문 포함'
            },
            number: {
                valid: password && /\d/.test(password),
                message: '숫자 포함'
            },
            special: {
                valid: password && /[@$!%*?&]/.test(password),
                message: '특수문자 포함'
            }
        };

        return requirements;
    }

    /**
     * 비밀번호 확인 검증
     * @param {string} password 원본 비밀번호
     * @param {string} passwordConfirm 확인 비밀번호
     * @returns {Object} { isValid: boolean, message: string }
     */
    static validatePasswordConfirm(password, passwordConfirm) {
        if (!passwordConfirm) {
            return { isValid: false, message: '비밀번호 확인을 입력해주세요.' };
        }

        if (password !== passwordConfirm) {
            return { isValid: false, message: '비밀번호가 일치하지 않습니다.' };
        }

        return { isValid: true, message: '비밀번호가 일치합니다.' };
    }

    /**
     * 핸드폰번호 검증
     * @param {string} phone 핸드폰번호
     * @returns {Object} { isValid: boolean, message: string }
     */
    static validatePhone(phone) {
        const trimmedPhone = phone.trim();

        if (!trimmedPhone) {
            return { isValid: false, message: '핸드폰 번호를 입력해주세요.' };
        }

        // 010-0000-0000 형식 체크
        const phoneRegex = /^010-\d{4}-\d{4}$/;
        if (!phoneRegex.test(trimmedPhone)) {
            return { isValid: false, message: '010-0000-0000 형식으로 입력해주세요.' };
        }

        return { isValid: true, message: '올바른 핸드폰 번호입니다.' };
    }

    /**
     * 관심사 검증
     * @param {Array} selectedTags 선택된 태그 배열
     * @returns {Object} { isValid: boolean, message: string }
     */
    static validateInterests(selectedTags) {
        if (!selectedTags || selectedTags.length === 0) {
            return { isValid: true, message: '관심사는 선택사항입니다.' };
        }

        if (selectedTags.length > 10) {
            return { isValid: false, message: '관심사는 최대 10개까지 선택 가능합니다.' };
        }

        return { isValid: true, message: `${selectedTags.length}개의 관심사가 선택되었습니다.` };
    }

    /**
     * 폼 전체 검증
     * @param {FormData} formData 폼 데이터
     * @param {Array} selectedTags 선택된 관심사 태그
     * @returns {Object} { isValid: boolean, errors: Object }
     */
    static validateForm(formData, selectedTags = []) {
        const name = String(formData.get('name') ?? '').trim();
        const nickname = String(formData.get('nickname') ?? '').trim();
        const email = String(formData.get('email') ?? '').trim();
        const password = String(formData.get('password') ?? '');
        const passwordConfirm = String(formData.get('passwordConfirm') ?? '');
        const phone = String(formData.get('phone') ?? '').trim();

        const nameResult = this.validateName(name);
        const nicknameResult = this.validateNickname(nickname);
        const emailResult = this.validateEmail(email);
        const passwordResult = this.validatePassword(password);
        const passwordConfirmResult = this.validatePasswordConfirm(password, passwordConfirm);
        const phoneResult = this.validatePhone(phone);
        const interestsResult = this.validateInterests(selectedTags);

        const errors = {};
        if (!nameResult.isValid) {
            errors.name = nameResult.message;
        }
        if (!nicknameResult.isValid) {
            errors.nickname = nicknameResult.message;
        }
        if (!emailResult.isValid) {
            errors.email = emailResult.message;
        }
        if (!passwordResult.isValid) {
            errors.password = passwordResult.message;
        }
        if (!passwordConfirmResult.isValid) {
            errors.passwordConfirm = passwordConfirmResult.message;
        }
        if (!phoneResult.isValid) {
            errors.phone = phoneResult.message;
        }
        if (!interestsResult.isValid) {
            errors.interests = interestsResult.message;
        }

        return {
            isValid: nameResult.isValid && nicknameResult.isValid && emailResult.isValid &&
                passwordResult.isValid && passwordConfirmResult.isValid && phoneResult.isValid &&
                interestsResult.isValid,
            errors,
            passwordRequirements: passwordResult.requirements
        };
    }

    /**
     * 핸드폰 번호 자동 포매팅
     * @param {string} phone 입력된 핸드폰 번호
     * @returns {string} 포매팅된 핸드폰 번호
     */
    static formatPhone(phone) {
        // 숫자만 추출
        const numbers = phone.replace(/\D/g, '');

        // 010으로 시작하지 않으면 그대로 반환
        if (!numbers.startsWith('010')) {
            return phone;
        }

        // 자동 하이픈 추가
        if (numbers.length <= 3) {
            return numbers;
        } else if (numbers.length <= 7) {
            return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
        } else if (numbers.length <= 11) {
            return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
        } else {
            // 11자리 초과시 자름
            return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
        }
    }

    /**
     * 실시간 입력 검증용 - 간단한 체크만
     * @param {string} fieldName 필드명
     * @param {string} value 입력값
     * @returns {Object} { isValid: boolean, message: string }
     */
    static validateField(fieldName, value) {
        switch (fieldName) {
            case 'name':
                return this.validateName(value);
            case 'nickname':
                return this.validateNickname(value);
            case 'email':
                return this.validateEmail(value);
            case 'password':
                return this.validatePassword(value);
            case 'phone':
                return this.validatePhone(value);
            default:
                return { isValid: true, message: '' };
        }
    }
}