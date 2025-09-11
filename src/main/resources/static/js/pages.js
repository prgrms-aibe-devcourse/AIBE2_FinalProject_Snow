// 페이지별 로직
const Pages = {
    // 홈
    async home() {
        try {
            const userData = await apiService.getCurrentUser();

            const templateData = {
                userName: userData ? userData.name : '게스트',
                lastLogin: userData ? new Date(userData.lastLogin).toLocaleDateString() : '',
                activityCount: userData ? userData.activityCount : 0
            };

            const homeHtml = await TemplateLoader.load('pages/home', templateData);
            document.getElementById('main-content').innerHTML = homeHtml;
        } catch (error) {
            console.error('홈 페이지 로드 실패:', error);
            const guestData = { userName: '게스트', lastLogin: '', activityCount: 0 };
            const homeHtml = await TemplateLoader.load('pages/home', guestData);
            document.getElementById('main-content').innerHTML = homeHtml;
        }
    },

    // 팝업 리스트
    async popupList() {
        const manager = new PopupListManager();
        await manager.initialize();
    },

    async popupSearch() {
        const manager = new PopupSearchManager();
        await manager.initialize();
    },

    // 지도 페이지
    async map() {
        const manager = new MapPageManager();
        await manager.initialize();
    },

    // == 마이페이지 - 공간제공자 (현재 비어있음) ===


    // === 공간 목록 페이지 ===
    spaceList() {
        location.href = '/templates/pages/space-list.html';
    },

    //공간 등록 페이지
    spaceRegister() {
        location.href = '/templates/pages/space-register.html';
    },

    // 공간 상세 페이지
    spaceDetail(spaceId) {
        location.href = `/templates/pages/space-detail.html?id=${spaceId}`;
    },


    // 공간 수정 페이지
    spaceEdit(spaceId) {
        location.href = `/templates/pages/space-edit.html?id=${spaceId}`;
    },
};

// 팝업 상세 페이지로 이동
function goToPopupDetail(popupId) {
    console.log('팝업 상세 이동:', popupId);
    if (popupId == null || (typeof popupId !== 'number' && !/^\d+$/.test(String(popupId)))) {
        console.warn('잘못된 팝업 ID:', popupId);
        return;
    }
    const id = encodeURIComponent(String(popupId));
    // TODO: 상세 페이지 구현 시 활성화
    // location.href = `/templates/pages/popup-detail.html?id=${id}`;
    alert(`팝업 ID ${id} 상세 페이지는 준비 중입니다.`);
}

window.Pages = Pages;
window.goToPopupDetail = goToPopupDetail;
