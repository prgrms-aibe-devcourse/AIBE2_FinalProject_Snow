// 페이지별 로직
const Pages = {
    async home() {
        try {
            // API에서 데이터 가져오기
            const userData = await apiService.getCurrentUser();

            // 템플릿에 넣을 데이터 준비
            const templateData = {
                userName: userData ? userData.name : '게스트',
                lastLogin: userData ? new Date(userData.lastLogin).toLocaleDateString() : '',
                activityCount: userData ? userData.activityCount : 0
            };

            // 템플릿 로드 및 렌더링
            const homeHtml = await TemplateLoader.load('pages/home', templateData);
            document.getElementById('main-content').innerHTML = homeHtml;

        } catch (error) {
            console.error('홈 페이지 로드 실패:', error);
            // 게스트용 기본 템플릿
            const guestData = {
                userName: '게스트',
                lastLogin: '',
                activityCount: 0
            };
            const homeHtml = await TemplateLoader.load('pages/home', guestData);
            document.getElementById('main-content').innerHTML = homeHtml;
        }
    },
};