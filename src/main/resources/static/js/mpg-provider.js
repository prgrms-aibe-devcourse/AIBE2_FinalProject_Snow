const ProviderPage = {
    async init() {
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
            this.bindFilters();

        } catch (error) {
            console.error('Provider page initialization failed:', error);
            document.getElementById('main-content').innerHTML =
                '<div class="error">페이지 로딩 중 오류가 발생했습니다.</div>';
        }
    },

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

    createSpaceCard(space) {
        const card = document.createElement('div');
        card.className = 'space-card';

        const detailUrl = `/templates/pages/space-detail.html?id=${encodeURIComponent(space.id)}`;

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
        thumbWrap.addEventListener('click', () => { window.location.href = detailUrl; });

        const info = document.createElement('div');
        info.className = 'info';

        const title = document.createElement('div');
        title.className = 'title linklike';
        title.textContent = space.title || '등록 공간';
        title.addEventListener('click', () => { window.location.href = detailUrl; });

        const desc = document.createElement('div');
        desc.className = 'desc linklike';
        desc.textContent = space.description || '공간 설명';
        desc.addEventListener('click', () => { window.location.href = detailUrl; });

        info.appendChild(title);
        info.appendChild(desc);

        const actions = document.createElement('div');
        actions.className = 'btn-row';

        const btnMap = document.createElement('button');
        btnMap.className = 'btn icon';
        btnMap.textContent = '🗺️';
        btnMap.addEventListener('click', () => this.openSpaceInMap(space));

        const btnDel = document.createElement('button');
        btnDel.className = 'btn icon';
        btnDel.textContent = '🗑️';
        btnDel.addEventListener('click', async () => {
            if (!confirm(`"${space.title}" 공간을 삭제하시겠습니까?`)) return;
            try {
                await apiService.deleteSpace(space.id);
                alert('공간이 삭제되었습니다.');
                card.remove();
            } catch (err) {
                alert('공간 삭제 실패');
            }
        });

        actions.appendChild(btnMap);
        actions.appendChild(btnDel);

        card.appendChild(thumbWrap);
        card.appendChild(info);
        card.appendChild(actions);

        return card;
    },

    openSpaceInMap(space) {
        let address = '';
        if (space.venue) {
            if (space.venue.roadAddress) address = space.venue.roadAddress;
            else if (space.venue.jibunAddress) address = space.venue.jibunAddress;
            if (space.venue.detailAddress && address) {
                address += ` ${space.venue.detailAddress}`;
            }
        } else if (space.address) address = space.address;

        if (address) {
            window.open(`https://map.naver.com/v5/search/${encodeURIComponent(address)}`, '_blank');
        } else {
            alert('주소 정보가 없습니다.');
        }
    },

    renderReservations(reservations) {
        const listEl = document.getElementById('reservation-list');
        listEl.innerHTML = "";

        if (reservations && reservations.length > 0) {
            reservations.forEach(reservation => {
                const card = this.createReservationCard(reservation);
                listEl.appendChild(card);
            });
        } else {
            listEl.innerHTML = '<div class="empty" data-empty>아직 받은 예약 요청이 없습니다.</div>';
        }
    },

    createReservationCard(reservation) {
        const card = document.createElement('div');
        card.className = 'reservation-card';

        // 썸네일
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
            img.alt = reservation.spaceTitle || 'space';
            img.onerror = () => { img.style.display = 'none'; };
            thumbWrap.appendChild(img);
        }

        // 정보
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

        // 버튼
        const actions = document.createElement('div');
        actions.className = 'btn-row';

        // 공통 버튼
        const btnDetail = document.createElement('button');
        btnDetail.className = 'btn btn-outline';
        btnDetail.textContent = '상세보기';
        btnDetail.addEventListener('click', () => this.showReservationDetail(reservation.id));

        const btnCall = document.createElement('button');
        btnCall.className = 'btn btn-call';
        btnCall.textContent = '통화';
        btnCall.addEventListener('click', () => {
            if (reservation.hostPhone) {
                window.open(`tel:${reservation.hostPhone}`, '_self');
            } else {
                alert('연락처 정보가 없습니다.');
            }
        });

        actions.appendChild(btnDetail);
        actions.appendChild(btnCall);

        if (reservation.status === 'PENDING') {
            const btnAccept = document.createElement('button');
            btnAccept.className = 'btn btn-success';
            btnAccept.textContent = '승인';
            btnAccept.addEventListener('click', () => {
                this.handleReservationAction('accept', reservation.id, reservation.popupTitle, card);
            });

            const btnReject = document.createElement('button');
            btnReject.className = 'btn btn-danger';
            btnReject.textContent = '거절';
            btnReject.addEventListener('click', () => {
                this.handleReservationAction('reject', reservation.id, reservation.popupTitle, card);
            });

            actions.appendChild(btnAccept);
            actions.appendChild(btnReject);
        } else if (reservation.status === 'REJECTED' || reservation.status === 'CANCELLED') {
            const btnDelete = document.createElement('button');
            btnDelete.className = 'btn btn-danger';
            btnDelete.textContent = '삭제';
            btnDelete.addEventListener('click', () => {
                if (!confirm('이 예약을 화면에서 지우시겠습니까?')) return;
                card.remove();
            });
            actions.appendChild(btnDelete);
        }

        card.appendChild(thumbWrap);
        card.appendChild(info);
        card.appendChild(actions);

        return card;
    },

    getStatusText(status) {
        const statusMap = {
            'PENDING': '대기중',
            'ACCEPTED': '승인됨',
            'REJECTED': '거절됨',
            'CANCELLED': '취소됨'
        };
        return statusMap[status] || status;
    },

    async showReservationDetail(reservationId) {
        try {
            const detail = await apiService.getReservationDetail(reservationId);
            alert(`예약 ID: ${detail.id}\n팝업명: ${detail.popupTitle || '제목 없음'}\n기간: ${detail.startDate} ~ ${detail.endDate}`);
        } catch (error) {
            alert('예약 상세 정보를 불러올 수 없습니다.');
        }
    },

    async handleReservationAction(action, reservationId, popupTitle, cardElement) {
        const actionText = action === 'accept' ? '승인' : '거절';
        if (!confirm(`${popupTitle}의 예약을 ${actionText}하시겠습니까?`)) return;

        try {
            if (action === 'accept') {
                await apiService.acceptReservation(reservationId);
            } else {
                await apiService.rejectReservation(reservationId);
            }
            alert(`예약이 ${actionText}되었습니다.`);
            this.init();
        } catch (error) {
            alert(`예약 ${actionText}에 실패했습니다.`);
        }
    },

    bindFilters() {
        document.querySelectorAll(".filter-btn").forEach((btn) => {
            btn.addEventListener("click", () => {
                const status = btn.dataset.status;
                if (status === "ALL") {
                    this.renderReservations(this.allReservations);
                } else {
                    const filtered = this.allReservations.filter((r) => r.status === status);
                    this.renderReservations(filtered);
                }
            });
        });
    }
};

window.ProviderPage = ProviderPage;
