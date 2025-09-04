/**
 * 로그인 API 서비스
 * 로그인 관련 HTTP 통신과 토큰 관리를 담당
 */
class LoginApiService {
    constructor() {
        this.baseURL = '/api';
        this.maxRetries = 3;
        this.retryDelay = 1000; // 1초
    }

    /**
     * 로그인 API 요청
     * @param {string} email
     * @param {string} password
     * @returns {Promise<Object>} 로그인 응답 데이터
     */
    async login(email, password) {
        for (let attempt = 1; attempt <= this.maxRetries; attempt++) {
            try {
                const response = await fetch(`${this.baseURL}/auth/login`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ email, password })
                });

                const data = await response.json();

                if (!response.ok) {
                    const errorMessage = data.message || this.getHttpErrorMessage(response.status);
                    throw new Error(errorMessage);
                }

                return data;
            } catch (error) {
                // 네트워크 에러인 경우 재시도
                if (error instanceof TypeError) {
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
     * HTTP 상태 코드에 따른 에러 메시지 반환
     * @param {number} status HTTP 상태 코드
     * @returns {string} 에러 메시지
     */
    getHttpErrorMessage(status) {
        const messages = {
            400: '입력 정보를 확인해주세요.',
            401: '이메일 또는 비밀번호가 올바르지 않습니다.',
            403: '접근이 거부되었습니다.',
            404: '요청한 리소스를 찾을 수 없습니다.',
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
     * 토큰과 사용자 정보 저장
     * @param {string} token 액세스 토큰
     * @param {Object} userInfo 사용자 정보
     */
    storeToken(token, userInfo) {
        const storage = this.getStorage();
        const data = {
            accessToken: token,
            userName: userInfo.name,
            userEmail: userInfo.email,
            userId: String(userInfo.userId),
            userRole: userInfo.role,
            loginTime: new Date().toISOString()
        };

        Object.entries(data).forEach(([key, value]) => {
            storage.setItem(key, value);
        });
    }

    /**
     * 저장된 토큰 반환
     * @returns {string|null} 액세스 토큰
     */
    getStoredToken() {
        return this.getStorage().getItem('accessToken');
    }

    /**
     * 토큰 및 사용자 정보 제거
     */
    removeToken() {
        const keys = ['accessToken', 'userName', 'userEmail', 'userId', 'userRole', 'loginTime'];
        keys.forEach(key => {
            try {
                localStorage.removeItem(key);
                sessionStorage.removeItem(key);
            } catch (e) {
                // 스토리지 접근 불가능한 경우 무시
            }
        });
    }

    /**
     * 사용 가능한 스토리지 반환 (localStorage 우선, 실패시 sessionStorage)
     * @returns {Storage} localStorage 또는 sessionStorage
     */
    getStorage() {
        try {
            const testKey = '__storage_test__';
            localStorage.setItem(testKey, 'test');
            localStorage.removeItem(testKey);
            return localStorage;
        } catch (e) {
            return sessionStorage;
        }
    }

    /**
     * 토큰 만료 여부 확인 (24시간 기준)
     * @returns {boolean} 만료 여부
     */
    isTokenExpired() {
        const loginTime = this.getStorage().getItem('loginTime');
        if (!loginTime) return true;

        try {
            const hoursElapsed = (Date.now() - new Date(loginTime).getTime()) / (1000 * 60 * 60);
            return hoursElapsed > 24;
        } catch (e) {
            return true;
        }
    }
}