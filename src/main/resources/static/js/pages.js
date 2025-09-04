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

    // == 마이페이지 - 공간제공자 ===
    async mypageProvider() {
        try {
            // 1) 템플릿 먼저 로드
            const html = await TemplateLoader.load('pages/mpg-provider/provider', {});
            document.getElementById('main-content').innerHTML = html;

            // 2) 내 공간 목록 가져오기
            const spaces = await apiService.getMySpaces(); // /api/provider/spaces

            // 3) 카드 렌더링
            const listEl = document.getElementById('provider-space-list');
            const emptyEl = listEl.querySelector('[data-empty]');

            if (spaces && spaces.length > 0) {
                if (emptyEl) emptyEl.remove();

                spaces.forEach(sp => {
                    const card = document.createElement('div');
                    card.className = 'card space-card';

                    const detailUrl = `/spaces/detail.html?id=${encodeURIComponent(sp.id)}`;

                    // === 썸네일 ===
                    const thumbWrap = document.createElement('div');
                    thumbWrap.className = 'thumb';
                    if (sp.coverImageUrl) {
                        let src = sp.coverImageUrl.trim();
                        if (!/^https?:\/\//i.test(src) && !src.startsWith('/') && !src.startsWith('data:')) {
                            src = `/uploads/${src}`;
                        }
                        const img = document.createElement('img');
                        img.src = src;
                        img.alt = sp.title || 'space';
                        img.onerror = () => { img.style.display = 'none'; };
                        thumbWrap.appendChild(img);
                    }

                    // === 텍스트 (제목/설명 클릭 시 상세 이동) ===
                    const text = document.createElement('div');
                    text.className = 'info';
                    const title = document.createElement('div');
                    title.className = 'title linklike';
                    title.textContent = sp.title || '등록 공간';

                    const desc = document.createElement('div');
                    desc.className = 'desc linklike';
                    desc.textContent = sp.description || '임대 요청 상세 정보';

                    const goDetail = () => { window.location.href = detailUrl; };
                    title.addEventListener('click', goDetail);
                    desc.addEventListener('click', goDetail);

                    text.append(title, desc);

                    // === 버튼 영역 (지도 + 삭제) ===
                    const right = document.createElement('div');
                    right.className = 'btn-row';

                    const btnMap = document.createElement('button');
                    btnMap.className = 'btn icon';
                    btnMap.title = '지도 보기';
                    btnMap.textContent = '🗺️';
                    btnMap.addEventListener('click', () => {
                        alert('지도 기능은 준비중입니다.');
                    });

                    const btnDel = document.createElement('button');
                    btnDel.className = 'btn icon';
                    btnDel.title = '삭제';
                    btnDel.textContent = '🗑️';
                    btnDel.addEventListener('click', () => {
                        alert(`공간(ID=${sp.id}) 삭제 API 호출 예정`);
                    });

                    right.append(btnMap, btnDel);

                    card.append(thumbWrap, text, right);
                    listEl.appendChild(card);
                });

            }
        } catch (e) {
            console.error('[mypageProvider] render error:', e);
            // 실패하더라도 템플릿은 보이도록
            if (!document.getElementById('main-content').innerHTML.trim()) {
                const html = await TemplateLoader.load('pages/mpg-provider/provider', {});
                document.getElementById('main-content').innerHTML = html;
            }
        }
    },// 페이지 추가
};

window.Pages = Pages;
