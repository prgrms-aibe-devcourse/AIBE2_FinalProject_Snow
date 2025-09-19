class ProviderManager {
    constructor() {
        this.allReservations = [];
        this.elements = {};
    }

    async initialize() {
        try {
            this.setupElements();
            await this.loadData();
            this.setupEventListeners();
        } catch (error) {
            console.error('Provider page initialization failed:', error);
            this.showError('페이지 로딩 중 오류가 발생했습니다.');
        }
    }

    setupElements() {
        this.elements = {
            spaceList: document.getElementById('provider-space-list'),
            reservationList: document.getElementById('reservation-list'),
            statPending: document.getElementById('stat-pending'),
            statAccepted: document.getElementById('stat-accepted'),
            statRejected: document.getElementById('stat-rejected'),
            statTotal: document.getElementById('stat-total'),
            mainContent: document.getElementById('main-content')
        };
    }

    async loadData() {
        try {
            const [spaces, reservations, stats] = await Promise.all([
                apiService.getMySpaces().catch(() => []),
                apiService.getMyReservations().catch(() => []),
                apiService.getReservationStats().catch(() => ({}))
            ]);

            this.allReservations = reservations;

            this.renderStats(stats);
            this.renderSpaces(spaces);
            this.renderReservations(reservations);

        } catch (error) {
            console.error('데이터 로딩 실패:', error);
            throw error;
        }
    }

    setupEventListeners() {
        document.querySelectorAll('.filter-btn').forEach(btn => {
            btn.addEventListener('click', () => this.handleFilter(btn.dataset.status));
        });
    }

    renderStats(stats) {
        if (this.elements.statPending) {
            this.elements.statPending.textContent = stats.pendingCount || 0;
        }
        if (this.elements.statAccepted) {
            this.elements.statAccepted.textContent = stats.acceptedCount || 0;
        }
        if (this.elements.statRejected) {
            this.elements.statRejected.textContent = stats.rejectedCount || 0;
        }
        if (this.elements.statTotal) {
            this.elements.statTotal.textContent = stats.totalReservations || 0;
        }
    }

    renderSpaces(spaces) {
        const listEl = this.elements.spaceList;
        if (!listEl) return;

        const emptyEl = listEl.querySelector('[data-empty]');

        if (spaces && spaces.length > 0) {
            if (emptyEl) emptyEl.remove();

            spaces.forEach(space => {
                const card = this.createSpaceCard(space);
                listEl.appendChild(card);
            });
        } else if (!emptyEl) {
            listEl.innerHTML = '<div class="empty" data-empty>아직 등록된 공간이 없습니다.</div>';
        }
    }

    createSpaceCard(space) {
        const card = document.createElement('div');
        card.className = 'space-card';

        const thumbWrap = document.createElement('div');
        thumbWrap.className = 'thumb';

        if (space.coverImageUrl) {
            let src = space.coverImageUrl.trim();
            if (!/^https?:\/\//i.test(src) && !src.startsWith('/') && !src.startsWith('data:')) {
                src = `/uploads/${src}`;
            }
            const img = document.createElement('img');
            img.src = src;
            img.alt = space.title || '공간';
            img.onerror = () => { img.style.display = 'none'; };
            thumbWrap.appendChild(img);
        }

        thumbWrap.addEventListener('click', () => this.goToSpaceDetail(space.id));

        const info = document.createElement('div');
        info.className = 'info';

        const title = document.createElement('div');
        title.className = 'title linklike';
        title.textContent = space.title || '등록 공간';
        title.addEventListener('click', () => this.goToSpaceDetail(space.id));

        const desc = document.createElement('div');
        desc.className = 'desc linklike';
        desc.textContent = space.description || '공간 설명';
        desc.addEventListener('click', () => this.goToSpaceDetail(space.id));

        info.appendChild(title);
        info.appendChild(desc);

        const actions = document.createElement('div');
        actions.className = 'btn-row';

        const btnMap = document.createElement('button');
        btnMap.className = 'btn icon';
        btnMap.innerHTML = '<div class="icon-map"></div>';
        btnMap.addEventListener('click', () => this.openSpaceInMap(space));

        const btnDel = document.createElement('button');
        btnDel.className = 'btn icon';
        btnDel.innerHTML = '<div class="icon-delete"></div>';
        btnDel.addEventListener('click', () => this.deleteSpace(space, card));

        actions.appendChild(btnMap);
        actions.appendChild(btnDel);

        card.appendChild(thumbWrap);
        card.appendChild(info);
        card.appendChild(actions);

        return card;
    }

    renderReservations(reservations) {
        const listEl = this.elements.reservationList;
        if (!listEl) return;

        listEl.innerHTML = '';

        if (reservations && reservations.length > 0) {
            reservations.forEach(reservation => {
                const card = this.createReservationCard(reservation);
                listEl.appendChild(card);
            });
        } else {
            listEl.innerHTML = '<div class="empty" data-empty>아직 받은 예약 요청이 없습니다.</div>';
        }
    }

    createReservationCard(reservation) {
        const card = document.createElement('div');
        card.className = `reservation-card ${reservation.status.toLowerCase()}`;

        // 헤더 생성
        const header = document.createElement('div');
        header.className = 'timeline-header';

        const status = document.createElement('div');
        status.className = `timeline-status ${reservation.status.toLowerCase()}`;
        status.textContent = this.getStatusText(reservation.status);

        header.appendChild(status);

        // 거절됨/취소됨 상태일 때만 삭제 버튼 추가
        if (reservation.status === 'REJECTED' || reservation.status === 'CANCELLED') {
            const btnDeleteTop = document.createElement('button');
            btnDeleteTop.className = 'btn-delete-top';
            btnDeleteTop.innerHTML = '×';
            btnDeleteTop.addEventListener('click', (e) => {
                e.stopPropagation();
                this.removeReservationCard(card);
            });
            header.appendChild(btnDeleteTop);
        }

        // 바디 생성
        const body = document.createElement('div');
        body.className = 'timeline-body';

        // 썸네일
        const thumbWrap = document.createElement('div');
        thumbWrap.className = 'timeline-thumb';

        const imageUrl = reservation.spaceImageUrl || reservation.popupMainImage;
        if (imageUrl) {
            let src = imageUrl.trim();
            if (!/^https?:\/\//i.test(src) && !src.startsWith('/') && !src.startsWith('data:')) {
                src = `/uploads/${src}`;
            }
            const img = document.createElement('img');
            img.src = src;
            img.alt = reservation.spaceTitle || '공간';
            img.onerror = () => { img.style.display = 'none'; };
            thumbWrap.appendChild(img);
        }

        // 콘텐츠
        const content = document.createElement('div');
        content.className = 'timeline-content';

        const title = document.createElement('div');
        title.className = 'timeline-title';
        title.textContent = reservation.popupTitle || '팝업 제목 없음';

        // 브랜드 정보 추가 (백엔드에서 오는 brandName 그대로 사용)
        const brand = document.createElement('div');
        brand.className = 'timeline-brand';
        brand.textContent = reservation.brandName || '브랜드명 없음';

        const dates = document.createElement('div');
        dates.className = 'timeline-dates';
        dates.textContent = `${reservation.startDate} ~ ${reservation.endDate}`;

        const meta = document.createElement('div');
        meta.className = 'timeline-meta';

        const applicant = document.createElement('span');
        applicant.textContent = `신청자: ${reservation.hostName || '이름 없음'}`;

        const contact = document.createElement('span');
        contact.textContent = `연락처: ${reservation.hostPhone || '연락처 없음'}`;

        meta.appendChild(applicant);
        meta.appendChild(contact);

        content.appendChild(title);
        content.appendChild(brand);
        content.appendChild(dates);
        content.appendChild(meta);

        // 채팅 플로팅 버튼 (거절됨/취소됨이 아닌 경우에만)
        if (reservation.status !== 'REJECTED' && reservation.status !== 'CANCELLED') {
            const btnChat = document.createElement('button');
            btnChat.className = 'chat-floating';
            btnChat.innerHTML = '💬';
            btnChat.addEventListener('click', () => this.openChat(reservation.id));
            body.appendChild(btnChat);
        }

        body.appendChild(thumbWrap);
        body.appendChild(content);

        // 액션바 (대기중 상태일 때만)
        if (reservation.status === 'PENDING') {
            const actionBar = document.createElement('div');
            actionBar.className = 'action-bar';

            const btnAccept = document.createElement('button');
            btnAccept.className = 'action-btn btn-accept';
            btnAccept.innerHTML = '<span>✓</span><span>수락</span>';
            btnAccept.addEventListener('click', () => {
                this.handleReservationAction('accept', reservation.id, reservation.popupTitle);
            });

            const btnReject = document.createElement('button');
            btnReject.className = 'action-btn btn-reject';
            btnReject.innerHTML = '<span>✗</span><span>거절</span>';
            btnReject.addEventListener('click', () => {
                this.handleReservationAction('reject', reservation.id, reservation.popupTitle);
            });

            actionBar.appendChild(btnAccept);
            actionBar.appendChild(btnReject);

            card.appendChild(header);
            card.appendChild(body);
            card.appendChild(actionBar);
        } else {
            card.appendChild(header);
            card.appendChild(body);
        }

        return card;
    }

    // 채팅 열기 (토큰을 URL 파라미터로 전달)
    openChat(reservationId) {
        if (!reservationId) {
            alert('예약 정보를 찾을 수 없습니다.');
            return;
        }

        // 현재 저장된 토큰 가져오기
        const token = localStorage.getItem('accessToken') ||
            localStorage.getItem('authToken') ||
            sessionStorage.getItem('accessToken') ||
            sessionStorage.getItem('authToken');

        if (!token) {
            alert('로그인이 필요합니다.');
            window.location.href = '/auth/login';
            return;
        }

        // 토큰을 URL 파라미터로 전달
        window.location.href = `/chat/${reservationId}?token=${encodeURIComponent(token)}`;
    }

    handleFilter(status) {
        if (status === 'ALL') {
            this.renderReservations(this.allReservations);
        } else {
            const filtered = this.allReservations.filter(r => r.status === status);
            this.renderReservations(filtered);
        }
    }

    goToSpaceDetail(spaceId) {
        if (window.Pages && window.Pages.spaceDetail) {
            window.Pages.spaceDetail(spaceId);
        } else {
            window.location.href = `/space/detail/${encodeURIComponent(spaceId)}`;
        }
    }

    openSpaceInMap(space) {
        let address = '';
        if (space.venue) {
            if (space.venue.roadAddress) address = space.venue.roadAddress;
            else if (space.venue.jibunAddress) address = space.venue.jibunAddress;
            if (space.venue.detailAddress && address) {
                address += ` ${space.venue.detailAddress}`;
            }
        } else if (space.address) {
            address = space.address;
        }

        if (address) {
            window.open(`https://map.naver.com/v5/search/${encodeURIComponent(address)}`, '_blank');
        } else {
            alert('주소 정보가 없습니다.');
        }
    }

    async deleteSpace(space, cardElement) {
        if (!confirm(`"${space.title}" 공간을 삭제하시겠습니까?`)) return;

        try {
            await apiService.deleteSpace(space.id);
            alert('공간이 삭제되었습니다.');
            cardElement.remove();

            const remainingCards = this.elements.spaceList.querySelectorAll('.space-card');
            if (remainingCards.length === 0) {
                this.elements.spaceList.innerHTML = '<div class="empty" data-empty>아직 등록된 공간이 없습니다.</div>';
            }
        } catch (err) {
            console.error('공간 삭제 실패:', err);
            alert('공간 삭제에 실패했습니다.');
        }
    }

    async showReservationDetail(reservationId) {
        try {
            const detail = await apiService.getReservationDetail(reservationId);
            const info = `
예약 ID: ${detail.id}
팝업명: ${detail.popupTitle || '제목 없음'}
기간: ${detail.startDate} ~ ${detail.endDate}
신청자: ${detail.hostName || '이름 없음'}
연락처: ${detail.hostPhone || '연락처 없음'}
상태: ${this.getStatusText(detail.status)}
            `.trim();
            alert(info);
        } catch (error) {
            console.error('예약 상세 조회 실패:', error);
            alert('예약 상세 정보를 불러올 수 없습니다.');
        }
    }

    async handleReservationAction(action, reservationId, popupTitle) {
        const actionText = action === 'accept' ? '승인' : '거절';
        if (!confirm(`${popupTitle}의 예약을 ${actionText}하시겠습니까?`)) return;

        try {
            if (action === 'accept') {
                await apiService.acceptReservation(reservationId);
            } else {
                await apiService.rejectReservation(reservationId);
            }
            alert(`예약이 ${actionText}되었습니다.`);

            await this.loadData();
        } catch (error) {
            console.error(`예약 ${actionText} 실패:`, error);
            alert(`예약 ${actionText}에 실패했습니다.`);
        }
    }

    removeReservationCard(cardElement) {
        if (!confirm('이 예약을 화면에서 지우시겠습니까?')) return;
        cardElement.remove();

        const remainingCards = this.elements.reservationList.querySelectorAll('.reservation-card');
        if (remainingCards.length === 0) {
            this.elements.reservationList.innerHTML = '<div class="empty" data-empty>아직 받은 예약 요청이 없습니다.</div>';
        }
    }

    getStatusText(status) {
        const statusMap = {
            'PENDING': '대기중',
            'ACCEPTED': '승인됨',
            'REJECTED': '거절됨',
            'CANCELLED': '취소됨'
        };
        return statusMap[status] || status;
    }

    showError(message) {
        if (this.elements.mainContent) {
            this.elements.mainContent.innerHTML = `<div class="error">${message}</div>`;
        }
    }
}

window.ProviderManager = ProviderManager;