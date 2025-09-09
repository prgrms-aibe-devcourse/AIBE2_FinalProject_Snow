/**
 * 회원가입 API 서비스
 * 회원가입 관련 HTTP 통신과 중복 확인을 담당
 */
class SignupApi {
    constructor() {
        this.baseURL = '/api';
        this.maxRetries = 3;
        this.retryDelay = 1000; // 1초
    }

    /**
     * 회원가입 API 요청
     * @param {Object} signupData 회원가입 데이터
     * @returns {Promise<Object>} 회원가입 응답 데이터
     */
    async signup(signupData) {
        for (let attempt = 1; attempt <= this.maxRetries; attempt++) {
            try {
                const response = await fetch(`${this.baseURL}/auth/signup`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(signupData)
                });

                const data = await response.json();

                if (!response.ok) {
                    const errorMessage = data.message || this.getHttpErrorMessage(response.status);
                    throw new Error(errorMessage);
                }

                return data;
            } catch (error) {
                // 네트워크 에러인 경우 재시도
                if (error instanceof TypeError || error?.name === 'AbortError') {
                    if (attempt === this.maxRetries) {
                        throw new Error('네트워크 연결을 확인해주세요. 잠시 후 다시 시도해주세요.');
                    }
                    await this.delay(this.retryDelay * attempt);
                    continue;
                }
                // API 에러는 바로 throw
                throw error;
            }
        }
    }

    /**
     * 이메일 중복 확인 API
     * @param {string} email 확인할 이메일
     * @returns {Promise<Object>} { available: boolean, exists: boolean }
     */
    async checkEmailDuplicate(email) {
        try {
            const response = await fetch(`${this.baseURL}/auth/check-email?email=${encodeURIComponent(email)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('이메일 중복 확인 중 오류가 발생했습니다.');
            }

            return await response.json();
        } catch (error) {
            console.error('이메일 중복 확인 실패:', error);
            throw new Error('이메일 중복 확인 중 오류가 발생했습니다.');
        }
    }

    /**
     * 닉네임 중복 확인 API
     * @param {string} nickname 확인할 닉네임
     * @returns {Promise<Object>} { available: boolean, exists: boolean }
     */
    async checkNicknameDuplicate(nickname) {
        try {
            const response = await fetch(`${this.baseURL}/auth/check-nickname?nickname=${encodeURIComponent(nickname)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error('닉네임 중복 확인 중 오류가 발생했습니다.');
            }

            return await response.json();
        } catch (error) {
            console.error('닉네임 중복 확인 실패:', error);
            throw new Error('닉네임 중복 확인 중 오류가 발생했습니다.');
        }
    }

    /**
     * HTTP 상태 코드에 따른 에러 메시지 반환
     * @param {number} status HTTP 상태 코드
     * @returns {string} 에러 메시지
     */
    getHttpErrorMessage(status) {
        const messages = {
            400: '입력 정보를 확인해주세요.',
            401: '인증에 실패했습니다.',
            403: '접근이 거부되었습니다.',
            404: '요청한 리소스를 찾을 수 없습니다.',
            409: '이미 존재하는 정보입니다.',
            422: '입력 데이터가 올바르지 않습니다.',
            429: '너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요.',
            500: '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
            502: '서버가 일시적으로 사용할 수 없습니다.',
            503: '서버가 일시적으로 사용할 수 없습니다.'
        };
        return messages[status] || `오류가 발생했습니다. (${status})`;
    }

    /**
     * 지연 함수
     * @param {number} ms 밀리초
     * @returns {Promise}
     */
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    /**
     * 회원가입 데이터 준비
     * @param {FormData} formData 폼 데이터
     * @param {Array} selectedTags 선택된 관심사 태그
     * @returns {Object} API 요청용 데이터
     */
    prepareSignupData(formData, selectedTags) {
        return {
            name: formData.get('name').trim(),
            nickname: formData.get('nickname').trim(),
            email: formData.get('email').trim(),
            password: formData.get('password'),
            passwordConfirm: formData.get('passwordConfirm'),
            phone: formData.get('phone').trim(),
            interestTags: selectedTags || []
        };
    }

    /**
     * 이메일 유효성 + 중복 확인 (통합)
     * @param {string} email 이메일
     * @returns {Promise<Object>} { isValid: boolean, message: string, available: boolean }
     */
    async validateEmail(email) {
        // 먼저 클라이언트 사이드 검증
        const clientValidation = SignupValidator.validateEmail(email);
        if (!clientValidation.isValid) {
            return {
                isValid: false,
                message: clientValidation.message,
                available: false
            };
        }

        // 서버 중복 확인
        try {
            const duplicateCheck = await this.checkEmailDuplicate(email);
            return {
                isValid: duplicateCheck.available,
                message: duplicateCheck.available ?
                    '사용 가능한 이메일입니다.' :
                    '이미 사용 중인 이메일입니다.',
                available: duplicateCheck.available
            };
        } catch (error) {
            return {
                isValid: false,
                message: error.message,
                available: false
            };
        }
    }

    /**
     * 닉네임 유효성 + 중복 확인 (통합)
     * @param {string} nickname 닉네임
     * @returns {Promise<Object>} { isValid: boolean, message: string, available: boolean }
     */
    async validateNickname(nickname) {
        // 먼저 클라이언트 사이드 검증
        const clientValidation = SignupValidator.validateNickname(nickname);
        if (!clientValidation.isValid) {
            return {
                isValid: false,
                message: clientValidation.message,
                available: false
            };
        }

        // 서버 중복 확인
        try {
            const duplicateCheck = await this.checkNicknameDuplicate(nickname);
            return {
                isValid: duplicateCheck.available,
                message: duplicateCheck.available ?
                    '사용 가능한 닉네임입니다.' :
                    '이미 사용 중인 닉네임입니다.',
                available: duplicateCheck.available
            };
        } catch (error) {
            return {
                isValid: false,
                message: error.message,
                available: false
            };
        }
    }
}