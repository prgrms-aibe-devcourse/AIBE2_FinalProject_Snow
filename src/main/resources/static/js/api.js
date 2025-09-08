// 간단한 API 통신 모듈
class SimpleApiService {
    constructor() {
        this.baseURL = '/api';
        this.token = this.getStoredToken();
    }

    // 로컬 스토리지에서 토큰 가져오기
    getStoredToken() {
        try {
            const raw =
                localStorage.getItem('accessToken') ||
                localStorage.getItem('authToken')   ||
                sessionStorage.getItem('accessToken') ||
                sessionStorage.getItem('authToken');
            return (raw || '').trim();
        } catch {
            return null;
        }
    }

    // 토큰 저장
    storeToken(token) {
        const clean = String(token || '').trim();
        try {
            localStorage.setItem('accessToken', clean);
            localStorage.setItem('authToken',   clean);
        } catch {
            sessionStorage.setItem('accessToken', clean);
            sessionStorage.setItem('authToken',   clean);
        }
        this.token = clean;
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
                headers: this.getHeaders(),
                credentials: 'include'
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
                body: JSON.stringify(data),
                credentials: 'include'
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
    // PUT 요청
    async put(endpoint, data) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                method: 'PUT',
                headers: this.getHeaders(),
                body: JSON.stringify(data),
                credentials: 'include'
            });


            if (response.status === 401) {
                this.removeToken();
                throw new Error('인증이 필요합니다.');
            }

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            // 응답이 비어있을 수 있음
            const text = await response.text();
            return text ? JSON.parse(text) : true;
        } catch (error) {
            console.error('API PUT Error:', error);
            throw error;
        }
    }

    // DELETE 요청
    async delete(endpoint) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                method: 'DELETE',
                headers: this.getHeaders(),
                credentials: 'include'
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



}

// 전역 API 서비스 인스턴스
const apiService = new SimpleApiService();

// === 공간대여 API ===

// 공간 목록 조회
apiService.listSpaces = async function(params = {}) {
    const sp = new URLSearchParams(params);
    const query = sp.toString() ? `?${sp.toString()}` : '';
    return await this.get(`/spaces${query}`);
};

// 내 공간 목록 조회
apiService.getMySpaces = async function() {
    return await this.get('/spaces/mine');
};

// 공간 상세 조회
apiService.getSpace = async function(spaceId) {
    return await this.get(`/spaces/${encodeURIComponent(spaceId)}`);
};

// 공간 등록
apiService.createSpace = async function(formData) {
    try {
        const response = await fetch(`${this.baseURL}/spaces`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${this.getStoredToken()}`
            },
            body: formData,
            credentials: 'include'
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
        console.error('공간 등록 API Error:', error);
        throw error;
    }
};




// 공간 수정
apiService.updateSpace = async function(spaceId, formData) {
    try {
        const response = await fetch(`${this.baseURL}/spaces/${encodeURIComponent(spaceId)}`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${this.getStoredToken()}`
            },
            body: formData,
            credentials: 'include'
        });

        if (response.status === 401) {
            this.removeToken();
            throw new Error('401');
        }
        if (!response.ok) {
            throw new Error(`${response.status}`);
        }

        const text = await response.text();
        if (!text || text.trim().length === 0) {
            return true; // 빈 응답도 성공으로 처리
        }
        try {
            return JSON.parse(text);
        } catch {
            return text; // JSON 아니면 그냥 텍스트 반환
        }
    } catch (error) {
        console.error('공간 수정 API Error:', error);
        throw error;
    }
};

// 공간 삭제
apiService.deleteSpace = async function(spaceId) {
    return await this.delete(`/spaces/${encodeURIComponent(spaceId)}`);
};

// 문의하기
apiService.inquireSpace = async function(spaceId) {
    return await this.post(`/spaces/${encodeURIComponent(spaceId)}/inquiries`, {});
};

// 신고하기
apiService.reportSpace = async function(spaceId) {
    return await this.post(`/spaces/${encodeURIComponent(spaceId)}/reports`, {});
};

// === 공간 예약 관련 (Provider 전용) ===

// 내 공간에 신청된 예약 목록 조회
apiService.getMyReservations = async function() {
    return await this.get('/space-reservations/my-spaces');
};

// 예약 상세 조회
apiService.getReservationDetail = async function(reservationId) {
    return await this.get(`/space-reservations/${encodeURIComponent(reservationId)}`);
};

// 예약 승인
apiService.acceptReservation = async function(reservationId) {
    return await this.put(`/space-reservations/${encodeURIComponent(reservationId)}/accept`, {});
};

// 예약 거절
apiService.rejectReservation = async function(reservationId) {
    return await this.put(`/space-reservations/${encodeURIComponent(reservationId)}/reject`, {});
};
// 예약 현황 통계
apiService.getReservationStats = async function() {
    return await this.get('/space-reservations/stats');
};