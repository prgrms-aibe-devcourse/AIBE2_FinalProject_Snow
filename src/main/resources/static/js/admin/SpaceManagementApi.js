/**
 * 장소 관리 API 클래스
 */
class SpaceManagementApi {
    constructor() {
        this.baseURL = '/api/admin/spaces';
    }

    /**
     * JWT 토큰 가져오기
     */
    getToken() {
        return localStorage.getItem('authToken');
    }

    /**
     * 장소 통계 조회
     */
    async getStats() {
        try {
            const response = await fetch(`${this.baseURL}/stats`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.getToken()}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('장소 통계 조회 실패:', error);
            throw error;
        }
    }

    /**
     * 장소 목록 조회 (필터링 및 페이징 지원)
     */
    /**
     * 장소 목록 조회 (필터링 및 페이징 지원)
     */
    async getSpaces(params = {}) {
        try {
            const queryParams = new URLSearchParams();

            // 필터 파라미터 추가
            if (params.owner) queryParams.append('owner', params.owner);
            if (params.title) queryParams.append('title', params.title);
            if (params.isPublic !== undefined && params.isPublic !== '') {
                queryParams.append('isPublic', params.isPublic);
            }

            // 페이징 파라미터 추가
            if (params.page !== undefined) queryParams.append('page', params.page);
            if (params.size) queryParams.append('size', params.size);
            if (params.sort) queryParams.append('sort', params.sort);

            console.log('🔍 API 요청 URL:', `${this.baseURL}?${queryParams.toString()}`);
            console.log('🔍 API 요청 파라미터:', params);

            const response = await fetch(`${this.baseURL}?${queryParams.toString()}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.getToken()}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('장소 목록 조회 실패:', error);
            throw error;
        }
    }


    /**
     * 장소 상세 조회
     */
    async getSpaceDetail(spaceId) {
        try {
            const response = await fetch(`${this.baseURL}/${spaceId}`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${this.getToken()}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('장소 상세 조회 실패:', error);
            throw error;
        }
    }


    /**
     * 장소 비활성화 (관리자용)
     * TODO: 백엔드에서 구현 예정
     */
    async hideSpace(spaceId) {
        try {
            const response = await fetch(`${this.baseURL}/${spaceId}/hide`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${this.getToken()}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('장소 비활성화 실패:', error);
            throw error;
        }
    }

    /**
     * 장소 활성화 (관리자용)
     * TODO: 백엔드에서 구현 예정
     */
    async showSpace(spaceId) {
        try {
            const response = await fetch(`${this.baseURL}/${spaceId}/show`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${this.getToken()}`,
                    'Content-Type': 'application/json'
                }
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error('장소 활성화 실패:', error);
            throw error;
        }
    }

    /**
     * 에러 처리
     */
    handleError(error) {
        console.error('API 에러:', error);

        if (error.message.includes('401')) {
            alert('인증이 만료되었습니다. 다시 로그인해주세요.');
            window.location.href = '/templates/pages/auth/login.html';
        } else if (error.message.includes('403')) {
            alert('권한이 없습니다.');
        } else if (error.message.includes('404')) {
            alert('요청한 리소스를 찾을 수 없습니다.');
        } else {
            alert('오류가 발생했습니다: ' + error.message);
        }
    }
}