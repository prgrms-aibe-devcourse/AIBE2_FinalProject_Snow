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
    // === 공간 ===
    // DELETE 요청
    async delete(endpoint) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                method: 'DELETE',
                headers: this.getHeaders()
            });

            if (response.status === 401) {
                this.removeToken();
                throw new Error('인증이 필요합니다.');
            }
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            // 보통 빈 응답이지만, 서버가 JSON을 주면 파싱
            const ct = response.headers.get('content-type') || '';
            return ct.includes('application/json') ? await response.json() : true;
        } catch (err) {
            console.error('API DELETE Error:', err);
            throw err;
        }
    }

}

// 전역 API 서비스 인스턴스
const apiService = new SimpleApiService();

//  === 마이페이지 - 공간제공자 API ===
//마이페이지 - 공간제공자의 내 등록 공간 로드
apiService.getMySpaces = async function () {
    const res = await fetch('/api/spaces/mine', {
        method: 'GET',
        headers: { 'Accept': 'application/json' },
        credentials: 'include'
    });
    if (!res.ok) throw new Error('failed to load my spaces');
    return await res.json();
};

// === 공간대여 API ===

// 목록 조회 (필요하면 params 객체로 필터링 지원)
apiService.listSpaces = async function (params = {}) {
    const sp = new URLSearchParams(params);
    const spList = sp.toString() ? `?${sp.toString()}` : '';
    return await this.get(`/spaces${spList}`);
};

// 상세 조회 (향후 상세 페이지에서 사용 예정)
apiService.getSpace = async function (spaceId) {
    return await this.get(`/spaces/${encodeURIComponent(spaceId)}`);
};

// 삭제
apiService.deleteSpace = async function (spaceId) {
    return await this.delete(`/spaces/${encodeURIComponent(spaceId)}`);
};

// 문의하기 (바디 필요 없으면 빈 객체)
apiService.inquireSpace = async function (spaceId) {
    return await this.post(`/spaces/${encodeURIComponent(spaceId)}/inquiries`, {});
};

// 신고하기 (바디 필요 없으면 빈 객체)
apiService.reportSpace = async function (spaceId) {
    return await this.post(`/spaces/${encodeURIComponent(spaceId)}/reports`, {});
};