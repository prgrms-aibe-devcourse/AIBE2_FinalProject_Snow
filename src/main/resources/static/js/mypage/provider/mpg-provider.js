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
        card.className = 'reservation-card';

        const thumbWrap = document.createElement('div');
        thumbWrap.className = 'thumb';

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

        const info = document.createElement('div');
        info.className = 'info';

        const statusBox = document.createElement('div');
        statusBox.className = `status-box ${reservation.status.toLowerCase()}`;
        statusBox.textContent = this.getStatusText(reservation.status);

        const popupTitle = document.createElement('div');
        popupTitle.className = 'popup-title';
        popupTitle.textContent = reservation.popupTitle || '팝업 제목 없음';

        const period = document.createElement('div');
        period.className = 'dates';
        period.textContent = `${reservation.startDate} ~ ${reservation.endDate}`;

        const applicant = document.createElement('div');
        applicant.className = 'applicant';
        applicant.textContent = `신청자: ${reservation.hostName || '이름 없음'}`;

        const contact = document.createElement('div');
        contact.className = 'contact';
        contact.textContent = `연락처: ${reservation.hostPhone || '연락처 없음'}`;

        const spaceInfo = document.createElement('div');
        spaceInfo.className = 'space-info';
        spaceInfo.textContent = `공간: ${reservation.spaceTitle || '공간명 없음'}`;

        info.appendChild(statusBox);
        info.appendChild(popupTitle);
        info.appendChild(period);
        info.appendChild(applicant);
        info.appendChild(contact);
        info.appendChild(spaceInfo);

        const actions = document.createElement('div');
        actions.className = 'btn-row';

        const btnDetail = document.createElement('button');
        btnDetail.className = 'btn btn-outline';
        btnDetail.innerHTML = '<div class="icon-detail"></div>';
        btnDetail.addEventListener('click', () => this.showReservationDetail(reservation.id));

        const btnCall = document.createElement('button');
        btnCall.className = 'btn btn-call';
        btnCall.innerHTML = '<div class="icon-phone"></div>';
        btnCall.addEventListener('click', () => this.callHost(reservation.hostPhone));

        actions.appendChild(btnDetail);
        actions.appendChild(btnCall);

        // 채팅 버튼 추가 (토큰 전달 포함)
        if (reservation.status !== 'REJECTED' && reservation.status !== 'CANCELLED') {
            const btnChat = document.createElement('button');
            btnChat.className = 'btn btn-chat';
            btnChat.textContent = '채팅하기';
            btnChat.addEventListener('click', () => this.openChat(reservation.id));
            actions.appendChild(btnChat);
        }

        if (reservation.status === 'PENDING') {
            const btnAccept = document.createElement('button');
            btnAccept.className = 'btn btn-success';
            btnAccept.innerHTML = '<div class="icon-check"></div>';
            btnAccept.addEventListener('click', () => {
                this.handleReservationAction('accept', reservation.id, reservation.popupTitle);
            });

            const btnReject = document.createElement('button');
            btnReject.className = 'btn btn-danger';
            btnReject.innerHTML = '<div class="icon-x"></div>';
            btnReject.addEventListener('click', () => {
                this.handleReservationAction('reject', reservation.id, reservation.popupTitle);
            });

            actions.appendChild(btnAccept);
            actions.appendChild(btnReject);
        } else if (reservation.status === 'REJECTED' || reservation.status === 'CANCELLED') {
            const btnDelete = document.createElement('button');
            btnDelete.className = 'btn btn-danger';
            btnDelete.innerHTML = '<div class="icon-delete"></div>';
            btnDelete.addEventListener('click', () => this.removeReservationCard(card));
            actions.appendChild(btnDelete);
        }

        card.appendChild(thumbWrap);
        card.appendChild(info);
        card.appendChild(actions);

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

    callHost(phoneNumber) {
        if (phoneNumber) {
            window.open(`tel:${phoneNumber}`, '_self');
        } else {
            alert('연락처 정보가 없습니다.');
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