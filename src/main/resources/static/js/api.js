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

    // PUT 요청 (추가됨)
    async put(endpoint, data) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                method: 'PUT',
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
            console.error('API PUT Error:', error);
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

    // === 미션 관련 API ===

    // 미션 단건 조회
    async getMission(missionId) {
        return await this.get(`/missions/${encodeURIComponent(missionId)}`);
    }

    // (옵션) 미션 목록 조회 ?missionSetId=
    async listMissions(params = {}) {
        let url = '/missions';
        if (params.missionSetId != null) {
            url += `?missionSetId=${encodeURIComponent(params.missionSetId)}`;
        }
        return await this.get(url);
    }

    async getMissionSet(missionSetId) {
        const url = `/mission-sets/${encodeURIComponent(missionSetId)}`;
        return this.get(url);
    }

    async submitMissionAnswer(missionId, answer) {
        return this.post(`/user-missions/${encodeURIComponent(missionId)}/submit-answer`, { answer });
    }

    // === 마이페이지 - 공간제공자 API  ===

    // 내 등록 공간 목록
    async getMySpaces() {
        return await this.get('/provider/spaces');
    }

    // 내 공간에 신청된 예약 목록
    async getMyReservations() {
        return await this.get('/provider/reservations');
    }

    // 예약 상세 조회
    async getReservationDetail(reservationId) {
        return await this.get(`/provider/reservations/${reservationId}`);
    }

    // 예약 승인
    async acceptReservation(reservationId) {
        return await this.put(`/provider/reservations/${reservationId}/accept`);
    }

    // 예약 거절
    async rejectReservation(reservationId) {
        return await this.put(`/provider/reservations/${reservationId}/reject`);
    }

    // 예약 현황 통계
    async getReservationStats() {
        return await this.get('/provider/reservations/stats');
    }

}

// 전역 API 서비스 인스턴스
const apiService = new SimpleApiService();