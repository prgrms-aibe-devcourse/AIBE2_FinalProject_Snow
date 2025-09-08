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


window.Pages = Pages;
