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
    },
    // === 공간 목록 페이지 ===
    async spaceList() {
        try {
            // 템플릿 로드
            const spaceListHtml = await TemplateLoader.load('pages/space-list');
            document.getElementById('main-content').innerHTML = spaceListHtml;

            // 공간 목록 데이터 로드
            await this.loadSpaces();
        } catch (error) {
            console.error('공간 목록 페이지 로드 실패:', error);
            document.getElementById('main-content').innerHTML = '<div class="alert alert-error">페이지를 불러올 수 없습니다.</div>';
        }
    },

    // 공간 목록 데이터 로드
    async loadSpaces() {
        try {
            const spaces = await apiService.listSpaces();

            document.getElementById('loading').style.display = 'none';

            if (spaces.length === 0) {
                document.getElementById('emptyState').style.display = 'block';
            } else {
                this.displaySpaces(spaces);
            }
        } catch (error) {
            console.error('공간 목록 로드 실패:', error);
            document.getElementById('loading').innerHTML = '목록을 불러오는데 실패했습니다.';
        }
    },

    // 공간 목록 표시
    displaySpaces(spaces) {
        const spaceListContainer = document.getElementById('spaceList');
        spaceListContainer.innerHTML = '';

        spaces.forEach(space => {
            const spaceCard = this.createSpaceCard(space);
            spaceListContainer.appendChild(spaceCard);
        });
    },

    // 공간 카드 생성
    createSpaceCard(space) {
        const card = document.createElement('div');
        card.className = 'space-card';

        card.innerHTML = `
            <div class="space-header">
                <div>
                    <h4 class="card-title">${space.title ?? '(제목 없음)'}</h4><br>
                    <div class="space-details">
                        <div>등록자 : ${space.ownerName ?? '-'}</div>
                        <div>임대료 : 하루 ${this.formatCurrency(space.rentalFee)} 만 원</div>
                        <div>주소 : ${space.address ?? '-'}</div>
                        <div>면적 : ${space.areaSize ?? '-'} m&sup2;</div><br>
                        <div class="actions-inline">
                            <button class="link" onclick="Pages.spaceDetail(${space.id})">상세정보</button>
                            <button class="link" onclick="Pages.inquireSpace(${space.id})">문의하기</button>
                            <button class="link" onclick="Pages.reportSpace(${space.id})">신고</button>
                        </div>
                    </div>
                </div>
                ${space.coverImageUrl ? `<img class="thumb" src="${space.coverImageUrl}" alt="thumbnail">` : ''}
            </div>
            <div class="space-meta">
                <span>등록일: ${this.formatDate(space.createdAt)}</span>
                <div class="space-actions">
                    ${space.mine ? `
                        <button class="action-btn edit" onclick="Pages.editSpace(${space.id})">수정</button>
                        <button class="action-btn delete" onclick="Pages.deleteSpace(${space.id})">삭제</button>
                    ` : ``}
                </div>
            </div>
        `;

        return card;
    },

    // 공간 상세 페이지
    async spaceDetail(spaceId) {
        console.log('공간 상세 페이지:', spaceId);
        // TODO: 상세 페이지 템플릿 구현
    },

    // 공간 등록 페이지
    async spaceRegister() {
        console.log('공간 등록 페이지');
        // TODO: 등록 페이지 템플릿 구현
    },

    // 공간 수정
    async editSpace(spaceId) {
        console.log('공간 수정:', spaceId);
        // TODO: 수정 페이지 구현
    },

    // 공간 삭제
    async deleteSpace(spaceId) {
        if (confirm('정말로 이 공간을 삭제하시겠습니까?')) {
            try {
                await apiService.deleteSpace(spaceId);
                alert('공간이 삭제되었습니다.');
                await this.loadSpaces(); // 목록 새로고침
            } catch (error) {
                console.error('공간 삭제 실패:', error);
                alert('삭제에 실패했습니다.');
            }
        }
    },

    // 문의하기
    async inquireSpace(spaceId) {
        try {
            await apiService.inquireSpace(spaceId);
            alert('문의 요청이 접수되었습니다.');
        } catch (error) {
            console.error('문의 실패:', error);
            alert('문의 중 오류가 발생했습니다.');
        }
    },

    // 신고하기
    async reportSpace(spaceId) {
        try {
            await apiService.reportSpace(spaceId);
            alert('신고가 접수되었습니다.');
        } catch (error) {
            console.error('신고 실패:', error);
            alert('신고 중 오류가 발생했습니다.');
        }
    },

    // 날짜 포맷팅
    formatDate(dateString) {
        if (!dateString) return '날짜 정보 없음';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR');
    },

    // 통화 포맷팅
    formatCurrency(amount) {
        if (!amount) return '0';
        return amount.toLocaleString('ko-KR');
    },
};

// 북마크 탭 클릭 시 → 공간 목록 페이지로 연결 (임시)
Pages.bookmark = async function() {
    console.log('Pages.spaceList 시작');
    try {
        const tpl = await TemplateLoader.load('pages/space-list');
        document.getElementById('main-content').innerHTML = tpl;

        console.log('템플릿 로드 완료, API 호출 전');
        const spaces = await apiService.listSpaces();  // 여기 로그
        console.log('API 응답:', spaces);

    } catch (e) {
        console.error('spaceList 오류', e);
    }
};

window.Pages = Pages;
