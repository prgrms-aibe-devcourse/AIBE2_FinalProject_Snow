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

// === 팝업 관련 API - 실제 백엔드 연결 ===

// 팝업 목록 조회 - 실제 API 호출
apiService.getPopups = async function(params = {}) {
    const sp = new URLSearchParams();

    // 파라미터 정리
    if (params.page !== undefined) sp.set('page', params.page);
    if (params.size !== undefined) sp.set('size', params.size);
    if (params.sortBy !== undefined) sp.set('sortBy', params.sortBy);
    if (params.categoryIds && params.categoryIds.length > 0) {
        params.categoryIds.forEach(id => sp.append('categoryIds', id));
    }
    if (params.region) sp.set('region', params.region);
    if (params.status) sp.set('status', params.status);

    const query = sp.toString() ? `?${sp.toString()}` : '';
    return await this.get(`/popups${query}`);
};

// 팝업 상세 조회 - 실제 API 호출
apiService.getPopup = async function(popupId) {
    return await this.get(`/popups/${encodeURIComponent(popupId)}`);
};

// 추천 팝업 조회 - 실제 API 호출
apiService.getFeaturedPopups = async function(page = 0, size = 20) {
    return await this.get(`/popups/featured?page=${page}&size=${size}`);
};

// 팝업 검색 - 실제 API 호출
apiService.searchPopups = async function(params = {}) {
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

// === 알림 관련 API ===
apiService.getNotifications = async function() {
    return await this.get('/notifications/me');
};

apiService.markNotificationAsRead = async function(notificationId) {
    return await this.post(`/notifications/${encodeURIComponent(notificationId)}/read`);
};

apiService.createReservationNotification = async function(userId) {
    return await this.post('/notifications/reservation', null, {
        params: { userId }
    });
};

apiService.markNotificationRead = async function(id) {
    return await this.post(`/notifications/${id}/read`);
};

// === 마이페이지 USER api ===
apiService.getNotificationSettings = async function() {
    return await this.get("/notifications/settings/me");
};

apiService.updateNotificationSetting = async function(type, enabled) {
    return await this.post("/notifications/settings/update", {
        type, enabled
    });
};

// === 마이페이지 HOST api ===
// 팝업 등록
apiService.createPopup = async function(data) {
    return await this.post('/hosts/popups', data);
};

// 내 팝업 목록 조회
apiService.getMyPopups = async function() {
    return await this.get('/hosts/popups');
};

// 내 팝업 상세 조회
apiService.getMyPopupDetail = async function(popupId) {
    return await this.get(`/hosts/popups/${encodeURIComponent(popupId)}`);
};

// 팝업 수정
apiService.updatePopup = async function(popupId, data) {
    return await this.put(`/hosts/popups/${encodeURIComponent(popupId)}`, data);
};

// 팝업 삭제
apiService.deletePopup = async function(popupId) {
    return await this.delete(`/hosts/popups/${encodeURIComponent(popupId)}`);
};

// 호스트 프로필 조회
apiService.getMyHostProfile = async function() {
    return await this.get('/hosts/me');
};
// === 채팅 api ===
apiService.getChatMessages = async function(reservationId) {
    return await this.get(`/chat/${encodeURIComponent(reservationId)}/messages`);
};

// 채팅자 정보 조회 함수 추가
apiService.getChatContext = async function(reservationId) {
    return await this.get(`/chat/${encodeURIComponent(reservationId)}/context`);
};

// === 리뷰 관련 API ===

// 리뷰 작성
apiService.createReview = async function(reviewData) {
    return await this.post('/reviews', reviewData);
};

// 리뷰 수정
apiService.updateReview = async function(reviewId, reviewData) {
    return await this.put(`/reviews/${encodeURIComponent(reviewId)}`, reviewData);
};

// 리뷰 삭제
apiService.deleteReview = async function(reviewId) {
    return await this.delete(`/reviews/${encodeURIComponent(reviewId)}`);
};

// 특정 팝업의 최근 리뷰 조회
apiService.getRecentReviews = async function(popupId, limit = 2) {
    return await this.get(`/reviews/popup/${encodeURIComponent(popupId)}/recent?limit=${limit}`);
};

// 특정 팝업의 전체 리뷰 조회 (페이징)
apiService.getReviewsByPopup = async function(popupId, page = 0, size = 10, sort = 'createdAt,desc') {
    const params = new URLSearchParams({ page, size, sort });
    return await this.get(`/reviews/popup/${encodeURIComponent(popupId)}?${params}`);
};

// 팝업 리뷰 통계 조회
apiService.getReviewStats = async function(popupId) {
    return await this.get(`/reviews/popup/${encodeURIComponent(popupId)}/stats`);
};

// 내 리뷰 목록 조회
apiService.getMyReviews = async function(page = 0, size = 10, sort = 'createdAt,desc') {
    const params = new URLSearchParams({ page, size, sort });
    return await this.get(`/reviews/me?${params}`);
};

// 리뷰 단건 조회
apiService.getReview = async function(reviewId) {
    return await this.get(`/reviews/${encodeURIComponent(reviewId)}`);
};

// 사용자가 특정 팝업에 리뷰 작성 여부 확인
apiService.checkUserReview = async function(popupId) {
    return await this.get(`/reviews/popup/${encodeURIComponent(popupId)}/check`);
};

// 사용자의 특정 팝업 리뷰 조회
apiService.getUserReviewForPopup = async function(popupId) {
    return await this.get(`/reviews/popup/${encodeURIComponent(popupId)}/me`);
};

// 팝업별 리뷰 통계와 최근 리뷰 통합 조회
apiService.getPopupReviewSummary = async function(popupId) {
    return await this.get(`/reviews/popup/${encodeURIComponent(popupId)}/summary`);
};

// === 북마크 관련 API ===

// 북마크 추가
apiService.addBookmark = async function(popupId) {
    return await this.post('/bookmarks', { popupId });
};

// 북마크 제거
apiService.removeBookmark = async function(popupId) {
    return await this.delete(`/bookmarks/${encodeURIComponent(popupId)}`);
};

// 북마크 상태 확인
apiService.checkBookmark = async function(popupId) {
    try {
        return await this.get(`/bookmarks/${encodeURIComponent(popupId)}/check`);
    } catch (error) {
        return { isBookmarked: false };
    }
};

// 내 북마크 목록 조회
apiService.getMyBookmarks = async function(page = 0, size = 10) {
    const params = new URLSearchParams({ page, size });
    return await this.get(`/bookmarks/me?${params}`);
};

// === 유사한 팝업 조회 API (필요한 경우) ===

// 유사한 팝업 조회
apiService.getSimilarPopups = async function(popupId, limit = 6) {
    try {
        return await this.get(`/popups/${encodeURIComponent(popupId)}/similar?limit=${limit}`);
    } catch (error) {
        // 유사한 팝업이 없어도 에러가 아님
        return [];
    }
};

// === 유틸리티 메서드 ===

// 현재 사용자 ID 가져오기
apiService.getCurrentUserId = function() {
    try {
        const userId = localStorage.getItem('userId') ||
            sessionStorage.getItem('userId');
        return userId ? parseInt(userId) : null;
    } catch (error) {
        return null;
    }
};

// 로그인 상태 확인
apiService.isLoggedIn = function() {
    const token = this.getStoredToken();
    const userId = this.getCurrentUserId();
    return !!(token && userId);
};

// 에러 핸들링 (기존 메서드가 없다면 추가)
apiService.handleApiError = function(error) {
    console.error('API Error:', error);

    if (error.message.includes('401') || error.message.includes('인증이 필요합니다')) {
        this.removeToken();
        alert('로그인이 필요합니다.');
        window.location.href = '/login';
        return;
    }

    if (error.message.includes('403')) {
        alert('접근 권한이 없습니다.');
        return;
    }

    if (error.message.includes('404')) {
        alert('요청하신 정보를 찾을 수 없습니다.');
        return;
    }

    if (error.message.includes('500')) {
        alert('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        return;
    }

    // 기타 오류
    alert(error.message || '알 수 없는 오류가 발생했습니다.');
};

// === 관리자용 미션셋 / 미션 API ===

// 미션셋 목록 (페이지네이션 지원)
apiService.getMissionSets = async function(params = {}) {
    const sp = new URLSearchParams(params);
    const query = sp.toString() ? `?${sp.toString()}` : '';
    return await this.get(`/admin/mission-sets${query}`);
};

// 미션셋 상세
apiService.getMissionSetDetail = async function(setId) {
    return await this.get(`/admin/mission-sets/${encodeURIComponent(setId)}`);
};

// 미션셋 생성
apiService.createMissionSet = async function(data) {
    return await this.post(`/admin/mission-sets`, data);
};

// 미션셋 삭제
apiService.deleteMissionSet = async function(setId) {
    return await this.delete(`/admin/mission-sets/${encodeURIComponent(setId)}`);
};

// 특정 미션셋에 미션 추가
apiService.addMission = async function(setId, data) {
    return await this.post(`/admin/mission-sets/${encodeURIComponent(setId)}/missions`, data);
};

// 미션 삭제
apiService.deleteMission = async function(missionId) {
    return await this.delete(`/admin/mission-sets/missions/${encodeURIComponent(missionId)}`);
};

apiService.listAdminPopups = async function ({ page = 0, size = 500 } = {}) {
    const sp = new URLSearchParams({ page, size });
    return await this.get(`/admin/popups?${sp.toString()}`);
};

apiService.updateMissionSet = async function(setId, data) {
    return await this.put(`/admin/mission-sets/${encodeURIComponent(setId)}`, data);
};
