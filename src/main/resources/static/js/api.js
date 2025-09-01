// 간단한 API 통신 모듈
class SimpleApiService {
    constructor() {
        this.baseURL = '/api';
        this.token = this.getStoredToken();
    }

    // 로컬 스토리지에서 토큰 가져오기
    getStoredToken() {
        try {
            return localStorage.getItem('authToken');
        } catch (error) {
            console.warn('localStorage 접근 불가, 세션 스토리지 사용');
            return sessionStorage.getItem('authToken');
        }
    }

    // 토큰 저장
    storeToken(token) {
        try {
            localStorage.setItem('authToken', token);
        } catch (error) {
            sessionStorage.setItem('authToken', token);
        }
        this.token = token;
    }

    // 토큰 제거
    removeToken() {
        try {
            localStorage.removeItem('authToken');
            sessionStorage.removeItem('authToken');
        } catch (error) {
            console.warn('토큰 제거 실패');
        }
        this.token = null;
    }

    // 공통 헤더 설정
    getHeaders() {
        const headers = {
            'Content-Type': 'application/json',
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        return headers;
    }

    // GET 요청
    async get(endpoint) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                method: 'GET',
                headers: this.getHeaders()
            });

            if (response.status === 401) {
                this.removeToken();
                throw new Error('인증이 필요합니다.');
            }

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('API GET Error:', error);
            throw error;
        }
    }

    // POST 요청
    async post(endpoint, data) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                method: 'POST',
                headers: this.getHeaders(),
                body: JSON.stringify(data)
            });

            if (response.status === 401) {
                this.removeToken();
                throw new Error('인증이 필요합니다.');
            }

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('API POST Error:', error);
            throw error;
        }
    }

    // 로그인
    async login(username, password) {
        const result = await this.post('/auth/login', { username, password });
        if (result.token) {
            this.storeToken(result.token);
        }
        return result;
    }

    // 로그아웃
    async logout() {
        try {
            await this.post('/auth/logout');
        } finally {
            this.removeToken();
        }
    }

    // 현재 사용자 정보
    async getCurrentUser() {
        return await this.get('/users/me');
    }

    // 메인 페이지 데이터
    async getMainData() {
        return await this.get('/main');
    }
}

// 전역 API 서비스 인스턴스
const apiService = new SimpleApiService();