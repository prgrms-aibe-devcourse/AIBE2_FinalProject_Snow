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
            localStorage.removeItem('accessToken');
            sessionStorage.removeItem('authToken');
            sessionStorage.removeItem('accessToken');
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
    async getMission(missionId) {
        return await this.get(`/missions/${encodeURIComponent(missionId)}`);
    }

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

    // === 리워드 관련 API ===
    async getMyReward(missionSetId) {
        return this.get(`/rewards/my/${encodeURIComponent(missionSetId)}`);
    }

    async redeemReward(missionSetId, staffPin) {
        return this.post(`/rewards/redeem`, { missionSetId, staffPin });
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

// 예약 삭제 (거절된 예약만)
apiService.deleteReservation = async function(reservationId) {
    return await this.delete(`/space-reservations/${encodeURIComponent(reservationId)}/delete`);
};
// 예약 현황 통계
apiService.getReservationStats = async function() {
    try {
        const reservations = await this.getMyReservations();

        const stats = {
            pendingCount: 0,
            acceptedCount: 0,
            rejectedCount: 0,
            cancelledCount: 0,
            totalReservations: reservations.length
        };

        reservations.forEach(reservation => {
            switch(reservation.status) {
                case 'PENDING':
                    stats.pendingCount++;
                    break;
                case 'ACCEPTED':
                    stats.acceptedCount++;
                    break;
                case 'REJECTED':
                    stats.rejectedCount++;
                    break;
                case 'CANCELLED':
                    stats.cancelledCount++;
                    break;
            }
        });

        return stats;
    } catch (error) {
        console.error('통계 계산 오류:', error);
        return {
            pendingCount: 0,
            acceptedCount: 0,
            rejectedCount: 0,
            cancelledCount: 0,
            totalReservations: 0
        };
    }
};

// === 팝업 관련 API ===

// TODO: 실제 배포 시 삭제 - 개발 모드 체크 함수
const isDevelopment = () => {
    return window.location.hostname === 'localhost' ||
        window.location.hostname === '127.0.0.1' ||
        window.location.port !== '';
};

// 팝업 목록 조회
apiService.getPopups = async function(params = {}) {
    // TODO: 실제 배포 시 삭제 - 개발 환경 분기 처리
    if (isDevelopment()) {
        return await this.getDummyPopups(params);
    }

    // 실제 API 호출
    const sp = new URLSearchParams(params);
    const query = sp.toString() ? `?${sp.toString()}` : '';
    return await this.get(`/popups${query}`);
};

// TODO: 실제 배포 시 전체 함수 삭제 - Dummy Data 로드 함수
apiService.getDummyPopups = async function(params = {}) {
    try {
        const response = await fetch('/popup-dummy-data.json');
        if (!response.ok) {
            throw new Error('Dummy data 로드 실패');
        }

        const data = await response.json();

        // 클라이언트에서 간단한 필터링/정렬 시뮬레이션
        let filteredPopups = [...data.popups];

        // 정렬 처리
        const sortBy = params.sortBy || 'latest';
        switch(sortBy) {
            case 'featured':
                filteredPopups = filteredPopups.filter(p => p.featured);
                break;
            case 'deadline':
                filteredPopups.sort((a, b) => new Date(a.endDate) - new Date(b.endDate));
                break;
            case 'latest':
            default:
                filteredPopups.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
                break;
        }

        // 페이지네이션 시뮬레이션
        const page = parseInt(params.page || 0);
        const size = parseInt(params.size || 10);
        const startIndex = page * size;
        const endIndex = startIndex + size;
        const pagedPopups = filteredPopups.slice(startIndex, endIndex);

        // API 응답 형식에 맞게 반환
        return {
            popups: pagedPopups,
            totalElements: filteredPopups.length,
            totalPages: Math.ceil(filteredPopups.length / size),
            size: size,
            number: page,
            first: page === 0,
            last: endIndex >= filteredPopups.length
        };

    } catch (error) {
        console.error('Dummy data 로드 오류:', error);
        // 오류 시 빈 응답 반환
        return {
            popups: [],
            totalElements: 0,
            totalPages: 0,
            size: parseInt(params.size || 10),
            number: parseInt(params.page || 0),
            first: true,
            last: true
        };
    }
};

// 팝업 상세 조회
apiService.getPopup = async function(popupId) {
    // TODO: 실제 배포 시 삭제 - 개발 환경 분기 처리
    if (isDevelopment()) {
        return await this.getDummyPopup(popupId);
    }
    return await this.get(`/popups/${encodeURIComponent(popupId)}`);
};

// TODO: 실제 배포 시 전체 함수 삭제 - Dummy 팝업 상세 조회
apiService.getDummyPopup = async function(popupId) {
    try {
        const response = await fetch('/popup-dummy-data.json');
        if (!response.ok) {
            throw new Error('Dummy data 파일을 찾을 수 없습니다.');
        }

        const data = await response.json();
        const popup = data.popups.find(p => p.id == popupId);

        if (!popup) {
            throw new Error('팝업을 찾을 수 없습니다.');
        }

        // 팝업 상세 페이지에 필요한 추가 정보 보강
        return {
            ...popup,
            hours: popup.hours || [
                { dayOfWeek: 0, openTime: '10:30', closeTime: '22:30' },
                { dayOfWeek: 1, openTime: '10:30', closeTime: '22:30' },
                { dayOfWeek: 2, openTime: '10:30', closeTime: '22:30' },
                { dayOfWeek: 3, openTime: '10:30', closeTime: '22:30' },
                { dayOfWeek: 4, openTime: '10:30', closeTime: '22:30' }
            ],
            reservationAvailable: popup.reservationAvailable !== false,
            waitlistAvailable: popup.waitlistAvailable || false,
            summary: popup.summary || '흥미로운 팝업 스토어입니다.',
            categoryId: popup.categoryId || 1,
            categoryName: popup.categoryName || '라이프스타일'
        };
    } catch (error) {
        console.error('Dummy 팝업 상세 로드 오류:', error);
        throw error;
    }
};

// 추천 팝업 조회
apiService.getFeaturedPopups = async function(page = 0, size = 20) {
    // TODO: 실제 배포 시 삭제 - 개발 환경 분기 처리
    if (isDevelopment()) {
        return await this.getDummyPopups({ page, size, sortBy: 'featured' });
    }
    return await this.get(`/popups/featured?page=${page}&size=${size}`);
};

// 팝업 검색
apiService.searchPopups = async function(params = {}) {
    // TODO: 실제 배포 시 삭제 - 개발 환경 분기 처리
    if (isDevelopment()) {
        // TODO: Dummy 검색 구현
        return await this.getDummyPopups(params);
    }

    const sp = new URLSearchParams(params);
    const query = sp.toString() ? `?${sp.toString()}` : '';
    return await this.get(`/search/popups${query}`);
};

// 지역 목록 조회
apiService.getMapRegions = async function() {
    return await this.get('/map/regions');
};

// 좌표 기반 지역 목록 조회
apiService.getMapRegionsWithCoordinates = async function() {
    return await this.get('/map/regions/coordinates');
};

// 지도용 팝업 목록 조회
apiService.getMapPopups = async function(params = {}) {
    const sp = new URLSearchParams(params);
    const query = sp.toString() ? `?${sp.toString()}` : '';
    return await this.get(`/map/popups${query}`);
};

// 특정 범위 내 팝업 조회
apiService.getPopupsInBounds = async function(southWestLat, southWestLng, northEastLat, northEastLng) {
    const params = new URLSearchParams({
        southWestLat,
        southWestLng,
        northEastLat,
        northEastLng
    });
    return await this.get(`/map/popups/bounds?${params}`);
};

// 주변 팝업 조회
apiService.getNearbyPopups = async function(lat, lng, radiusKm = 10) {
    const params = new URLSearchParams({
        lat,
        lng,
        radiusKm
    });
    return await this.get(`/map/popups/nearby?${params}`);
};

// 카테고리별 지도 팝업 통계
apiService.getMapPopupStatsByCategory = async function(region = null) {
    const params = region ? `?region=${encodeURIComponent(region)}` : '';
    return await this.get(`/map/popups/stats/category${params}`);
};

// 지역별 지도 팝업 통계
apiService.getMapPopupStatsByRegion = async function() {
    return await this.get('/map/popups/stats/region');
};

// 지도 팝업 검색
apiService.searchMapPopups = async function(params = {}) {
    const sp = new URLSearchParams(params);
    const query = sp.toString() ? `?${sp.toString()}` : '';
    return await this.get(`/map/popups/search${query}`);
};

// === 팝업 제보 API ===

// 제보 생성
apiService.createPopupReport = async function(formData) {
    try {
        const response = await fetch(`${this.baseURL}/popups/reports`, {
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
        console.error('팝업 제보 API Error:', error);
        throw error;
    }
};


// 제보 단건 조회
apiService.getPopupReport = async function(reportId) {
    return await this.get(`/popups/reports/${encodeURIComponent(reportId)}`);
};

// 상태별 제보 목록 (관리자)
apiService.listPopupReportsByStatus = async function(status, page = 0, size = 20) {
    const params = new URLSearchParams({ status, page, size });
    return await this.get(`/popups/reports?${params}`);
};

// 내 제보 목록
apiService.getMyPopupReports = async function(page = 0, size = 20) {
    const params = new URLSearchParams({ page, size });
    return await this.get(`/popups/reports/me?${params}`);
};

// 제보 승인 (관리자)
apiService.approvePopupReport = async function(reportId) {
    return await this.put(`/popups/reports/${encodeURIComponent(reportId)}/approve`, {});
};

// 제보 반려 (관리자)
apiService.rejectPopupReport = async function(reportId) {
    return await this.put(`/popups/reports/${encodeURIComponent(reportId)}/reject`, {});
};
