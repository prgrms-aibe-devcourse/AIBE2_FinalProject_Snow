// 마이페이지 - 공간제공자
const ProviderPage = {
    // 페이지 초기화
    async init() {
        try {
            console.log('마이페이지 - provider 로딩중..');

            // 데이터 병렬로 가져오기
            const [spaces, reservations, stats] = await Promise.all([
                apiService.getMySpaces().catch(e => {
                    console.error('공간 목록 로드 실패:', e);
                    return [];
                }),
                apiService.getMyReservations().catch(e => {
                    console.error('예약 목록 로드 실패:', e);
                    return [];
                }),
                apiService.getReservationStats().catch(e => {
                    console.error('통계 로드 실패:', e);
                    return {};
                })
            ]);

            // 렌더링
            this.renderStats(stats);
            this.renderSpaces(spaces);
            this.renderReservations(reservations);

            console.log('Provider page loaded successfully');

        } catch (error) {
            console.error('Provider page initialization failed:', error);
            document.getElementById('main-content').innerHTML =
                '<div class="error">페이지 로딩 중 오류가 발생했습니다.</div>';
        }
    },

    // 통계 렌더링
    renderStats(stats) {
        const elements = {
            pending: document.getElementById('stat-pending'),
            accepted: document.getElementById('stat-accepted'),
            rejected: document.getElementById('stat-rejected'),
            total: document.getElementById('stat-total')
        };

        if (elements.pending) elements.pending.textContent = stats.pendingCount || 0;
        if (elements.accepted) elements.accepted.textContent = stats.acceptedCount || 0;
        if (elements.rejected) elements.rejected.textContent = stats.rejectedCount || 0;
        if (elements.total) elements.total.textContent = stats.totalReservations || 0;
    },

    // 공간 목록 렌더링
    renderSpaces(spaces) {
        const listEl = document.getElementById('provider-space-list');
        const emptyEl = listEl.querySelector('[data-empty]');

        if (spaces && spaces.length > 0) {
            if (emptyEl) emptyEl.remove();

            spaces.forEach(space => {
                const card = this.createSpaceCard(space);
                listEl.appendChild(card);
            });
        }
    },

    // 공간 카드 생성
    createSpaceCard(space) {
        const card = document.createElement('div');
        card.className = 'card space-card';

        const detailUrl = `/templates/pages/space-detail.html?id=${encodeURIComponent(space.id)}`;

        // 썸네일
        const thumbWrap = document.createElement('div');
        thumbWrap.className = 'thumb';
        if (space.coverImageUrl) {
            let src = space.coverImageUrl.trim();
            if (!/^https?:\/\//i.test(src) && !src.startsWith('/') && !src.startsWith('data:')) {
                src = `/uploads/${src}`;
            }
            const img = document.createElement('img');
            img.src = src;
            img.alt = space.title || 'space';
            img.onerror = () => { img.style.display = 'none'; };
            thumbWrap.appendChild(img);
        }

        //  썸네일 클릭 → 상세 페이지 이동
        const goDetail = () => { window.location.href = detailUrl; };
        thumbWrap.addEventListener('click', goDetail);

        // 정보 영역
        const info = document.createElement('div');
        info.className = 'info';

        const title = document.createElement('div');
        title.className = 'title linklike';
        title.textContent = space.title || '등록 공간';
        title.addEventListener('click', goDetail);

        const desc = document.createElement('div');
        desc.className = 'desc linklike';
        desc.textContent = space.description || '공간 설명';
        desc.addEventListener('click', goDetail);

        info.append(title, desc);

        // 버튼 영역
        const actions = document.createElement('div');
        actions.className = 'btn-row';

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
        btnDel.addEventListener('click', async () => {
            if (!confirm(`정말로 "${space.title}" 공간을 삭제하시겠습니까?`)) return;
            try {
                await apiService.deleteSpace(space.id);
                alert('공간이 삭제되었습니다.');
                card.remove();
            } catch (err) {
                console.error('공간 삭제 실패:', err);
                alert('공간 삭제에 실패했습니다.');
            }
        });

        actions.append(btnMap, btnDel);
        card.append(thumbWrap, info, actions);

        return card;
    },


    // 예약 목록 렌더링
    renderReservations(reservations) {
        const listEl = document.getElementById('reservation-list');
        const emptyEl = listEl.querySelector('[data-empty]');

        if (reservations && reservations.length > 0) {
            if (emptyEl) emptyEl.remove();

            reservations.forEach(reservation => {
                const card = this.createReservationCard(reservation);
                listEl.appendChild(card);
            });
        }
    },

    // 예약 카드 생성
    createReservationCard(reservation) {
        const card = document.createElement('div');
        card.className = 'card reservation-card';

        // 썸네일 (공간 이미지)
        const thumbWrap = document.createElement('div');
        thumbWrap.className = 'thumb';
        if (reservation.spaceImageUrl) {
            let src = reservation.spaceImageUrl.trim();
            if (!/^https?:\/\//i.test(src) && !src.startsWith('/') && !src.startsWith('data:')) {
                src = `/uploads/${src}`;
            }
            const img = document.createElement('img');
            img.src = src;
            img.alt = reservation.spaceTitle || 'space';
            img.onerror = () => { img.style.display = 'none'; };
            thumbWrap.appendChild(img);
        }

        // 예약 정보
        const info = document.createElement('div');
        info.className = 'info';

        // 상태 뱃지
        const statusBadge = document.createElement('div');
        statusBadge.className = `status-badge ${reservation.status.toLowerCase()}`;
        statusBadge.textContent = this.getStatusText(reservation.status);

        // 브랜드명
        const brand = document.createElement('div');
        brand.className = 'brand';
        brand.textContent = reservation.brand;

        // 팝업명
        const popupTitle = document.createElement('div');
        popupTitle.className = 'popup-title';
        popupTitle.textContent = reservation.popupTitle || '팝업 제목 없음';

        // 예약 기간
        const dates = document.createElement('div');
        dates.className = 'dates';
        dates.textContent = `${reservation.startDate} ~ ${reservation.endDate}`;

        // 공간 정보
        const spaceInfo = document.createElement('div');
        spaceInfo.className = 'space-info';
        spaceInfo.textContent = `${reservation.spaceTitle} • ${reservation.hostName}`;

        info.append(statusBadge, brand, popupTitle, dates, spaceInfo);

        // 버튼 영역
        const actions = document.createElement('div');
        actions.className = 'btn-row';

        // 상세보기 버튼
        const btnDetail = document.createElement('button');
        btnDetail.className = 'btn btn-outline';
        btnDetail.textContent = '상세보기';
        btnDetail.addEventListener('click', () => {
            this.showReservationDetail(reservation.id);
        });

        actions.appendChild(btnDetail);

        // 대기중 상태일 때만 승인/거절 버튼 표시
        if (reservation.status === 'PENDING') {
            const btnAccept = document.createElement('button');
            btnAccept.className = 'btn btn-success';
            btnAccept.textContent = '승인';
            btnAccept.addEventListener('click', () => {
                this.handleReservationAction('accept', reservation.id, reservation.brand);
            });

            const btnReject = document.createElement('button');
            btnReject.className = 'btn btn-danger';
            btnReject.textContent = '거절';
            btnReject.addEventListener('click', () => {
                this.handleReservationAction('reject', reservation.id, reservation.brand);
            });

            actions.append(btnAccept, btnReject);
        }

        card.append(thumbWrap, info, actions);
        return card;
    },

    // 상태 텍스트 변환
    getStatusText(status) {
        const statusMap = {
            'PENDING': '대기중',
            'ACCEPTED': '승인됨',
            'REJECTED': '거절됨',
            'CANCELLED': '취소됨'
        };
        return statusMap[status] || status;
    },

    // 예약 상세보기
    async showReservationDetail(reservationId) {
        try {
            const detail = await apiService.getReservationDetail(reservationId);

            // 간단한 모달이나 새 페이지로 상세 정보 표시
            const detailInfo = `
예약 ID: ${detail.id}
브랜드: ${detail.brand}
팝업명: ${detail.popupTitle}
기간: ${detail.startDate} ~ ${detail.endDate}
연락처: ${detail.contactPhone || '없음'}
메시지: ${detail.message || '없음'}
공간: ${detail.space.title}
예약자: ${detail.host.name} (${detail.host.email})
            `;

            alert(detailInfo); // 임시로 alert 사용, 추후 모달로 변경

        } catch (error) {
            console.error('예약 상세 조회 실패:', error);
            alert('예약 상세 정보를 불러올 수 없습니다.');
        }
    },

    // 예약 승인/거절 처리
    async handleReservationAction(action, reservationId, brandName) {
        const actionText = action === 'accept' ? '승인' : '거절';

        if (!confirm(`${brandName}의 예약을 ${actionText}하시겠습니까?`)) {
            return;
        }

        try {
            if (action === 'accept') {
                await apiService.acceptReservation(reservationId);
            } else {
                await apiService.rejectReservation(reservationId);
            }

            alert(`예약이 ${actionText}되었습니다.`);

            // 페이지 새로고침하여 업데이트된 상태 반영
            this.init();

        } catch (error) {
            console.error(`예약 ${actionText} 실패:`, error);
            alert(`예약 ${actionText}에 실패했습니다. ${error.message}`);
        }
    }
};

// 전역으로 노출
window.ProviderPage = ProviderPage;