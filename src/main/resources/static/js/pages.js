// 페이지별 로직
const Pages = {
    // 홈 (팝업 리스트 - index.html에서 표시)
    async popupList() {
        const manager = new PopupListManager();
        await manager.initialize();
    },

    // 팝업 검색 페이지로 이동
    popupSearch(searchParams = {}) {
        const params = new URLSearchParams();

        if (searchParams.query) params.set('query', searchParams.query);
        if (searchParams.region) params.set('region', searchParams.region);
        if (searchParams.category) params.set('category', searchParams.category);

        const queryString = params.toString();
        const url = queryString ? `/popup/search?${queryString}` : '/popup/search';

        window.location.href = url;
    },

    // 지도 페이지로 이동
    map(filterParams = {}) {
        const params = new URLSearchParams();

        if (filterParams.region) params.set('region', filterParams.region);
        if (filterParams.category) params.set('category', filterParams.category);

        const queryString = params.toString();
        const url = queryString ? `/map?${queryString}` : '/map';

        window.location.href = url;
    },

    // 북마크 페이지
    bookmark() {
        alert('북마크 기능은 준비 중입니다.');
        // TODO: 북마크 페이지 구현
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

    if (!popupId || !/^\d+$/.test(String(popupId))) {
        console.warn('잘못된 팝업 ID:', popupId);
        alert('잘못된 팝업 정보입니다.');
        return;
    }

    window.location.href = `/popup/${encodeURIComponent(popupId)}`;
}

window.Pages = Pages;
window.goToPopupDetail = goToPopupDetail;