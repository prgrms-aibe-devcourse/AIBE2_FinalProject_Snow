const HostPopupDetailPage = {
    async init() {
        const params = new URLSearchParams(window.location.search);
        const popupId = params.get("popupId") || params.get("id");

        if (!popupId) {
            alert("popupId가 없습니다.");
            // 뒤로 가기 또는 메인 페이지로 이동
            window.history.back();
            return;
        }

        try {
            const popup = await apiService.get(`/hosts/popups/${popupId}`);
            console.log("팝업 상세 응답:", popup);

            // 데이터 렌더링
            this.renderPopupDetail(popup);

        } catch (err) {
            console.error("팝업 상세 불러오기 실패:", err);

            // 인증 문제일 경우 안내
            if (err.message?.includes("401") || err.status === 401) {
                alert("로그인이 필요합니다.");
                window.location.href = "/auth/login"; // 프로젝트 로그인 경로로 수정
            } else {
                alert("팝업 정보를 불러올 수 없습니다.");
                // 이전 페이지로 돌아가기
                window.history.back();
            }
        }
    },

    renderPopupDetail(popup) {
        // 안전한 렌더링을 위해 각 요소 존재 확인
        const elements = {
            'popup-title': popup.title || '-',
            'popup-summary': popup.summary || '-',
            'popup-description': popup.description || '-',
            'popup-schedule': `${popup.startDate || ''} ~ ${popup.endDate || ''}`,
            'popup-venue': popup.venueName || '-',
            'popup-address': popup.venueAddress || '-',
            'popup-status': this.translateStatus(popup.status) || '-'
        };

        // 각 요소에 데이터 설정
        Object.entries(elements).forEach(([id, value]) => {
            const element = document.getElementById(id);
            if (element) {
                element.textContent = value;
            }
        });

        // 이미지 설정 - 와이어프레임에 맞게 이미지 영역 표시
        const imageElement = document.getElementById("popup-image");
        if (imageElement) {
            if (popup.mainImageUrl) {
                imageElement.src = popup.mainImageUrl;
                imageElement.style.display = 'block';
            } else if (popup.imageUrl) {
                imageElement.src = popup.imageUrl;
                imageElement.style.display = 'block';
            } else {
                // 이미지가 없어도 영역은 유지 (회색 박스로 표시)
                imageElement.style.display = 'block';
                imageElement.style.backgroundColor = '#f5f5f5';
                imageElement.style.border = '2px dashed #ddd';
                imageElement.removeAttribute('src');
            }

            // 이미지 로드 실패 시 회색 박스로 대체
            imageElement.onerror = () => {
                imageElement.style.backgroundColor = '#f5f5f5';
                imageElement.style.border = '2px dashed #ddd';
                imageElement.removeAttribute('src');
            };
        }
    },

    translateStatus(status) {
        switch (status) {
            case 'PLANNED': return '준비 중';
            case 'ONGOING': return '진행 중';
            case 'FINISHED': return '종료됨';
            case 'CANCELLED': return '취소됨';
            case 'PENDING': return '승인 대기';
            case 'ACCEPTED': return '승인됨';
            case 'REJECTED': return '거절됨';
            default: return status || '-';
        }
    }
};

// 전역에서 사용할 수 있도록 노출
window.HostPopupDetailPage = HostPopupDetailPage;