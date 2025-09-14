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
        card.className = 'card space-card';

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

        info.append(title, desc);

        const actions = document.createElement('div');
        actions.className = 'btn-row';

        const btnMap = document.createElement('button');
        btnMap.className = 'btn icon';
        btnMap.title = '지도 보기';
        btnMap.textContent = '🗺️';
        // 지도 기능 개선
        btnMap.addEventListener('click', () => {
            this.openSpaceInMap(space);
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

    // 공간 지도에서 보기 기능 추가
    openSpaceInMap(space) {
        let address = '';

        // venue 정보가 있으면 활용
        if (space.venue) {
            if (space.venue.roadAddress) {
                address = space.venue.roadAddress;
            } else if (space.venue.jibunAddress) {
                address = space.venue.jibunAddress;
            }
            if (space.venue.detailAddress && address) {
                address += ` ${space.venue.detailAddress}`;
            }
        } else if (space.address) {
            address = space.address;
        }

        if (address) {
            const searchUrl = `https://map.naver.com/v5/search/${encodeURIComponent(address)}`;
            window.open(searchUrl, '_blank');
        } else {
            alert('주소 정보가 없습니다.');
        }
    },

    renderReservations(reservations) {
        const listEl = document.getElementById('reservation-list');
        listEl.innerHTML = "";

        if (reservations && reservations.length > 0) {
            // 취소/거절된 예약 제외하고 필터링
            const activeReservations = reservations.filter(r =>
                r.status !== 'CANCELLED' && r.status !== 'REJECTED'
            );

            if (activeReservations.length > 0) {
                activeReservations.forEach(reservation => {
                    const card = this.createReservationCard(reservation);
                    listEl.appendChild(card);
                });
            } else {
                listEl.innerHTML = '<div class="empty" data-empty>진행 중인 예약 요청이 없습니다.</div>';
            }
        } else {
            listEl.innerHTML = '<div class="empty" data-empty>아직 받은 예약 요청이 없습니다.</div>';
        }
    },

    createReservationCard(reservation) {
        const card = document.createElement('div');
        card.className = 'card reservation-card';

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

        const info = document.createElement('div');
        info.className = 'info';

        const statusBadge = document.createElement('div');
        statusBadge.className = `status-badge ${reservation.status.toLowerCase()}`;
        statusBadge.textContent = this.getStatusText(reservation.status);

        const brand = document.createElement('div');
        brand.className = 'brand';
        brand.textContent = reservation.brandName || reservation.brand;

        const popupTitle = document.createElement('div');
        popupTitle.className = 'popup-title';
        popupTitle.textContent = reservation.popupTitle || '팝업 제목 없음';

        const dates = document.createElement('div');
        dates.className = 'dates';
        dates.textContent = `${reservation.startDate} ~ ${reservation.endDate}`;

        const spaceInfo = document.createElement('div');
        spaceInfo.className = 'space-info';
        spaceInfo.textContent = `${reservation.spaceTitle} • ${reservation.hostName}`;

        info.append(statusBadge, brand, popupTitle, dates, spaceInfo);

        const actions = document.createElement('div');
        actions.className = 'btn-row';

        const btnDetail = document.createElement('button');
        btnDetail.className = 'btn btn-outline';
        btnDetail.textContent = '상세보기';
        btnDetail.addEventListener('click', () => { this.showReservationDetail(reservation.id); });
        actions.appendChild(btnDetail);

        if (reservation.status === 'PENDING') {
            const btnAccept = document.createElement('button');
            btnAccept.className = 'btn btn-success';
            btnAccept.textContent = '승인';
            btnAccept.addEventListener('click', () => {
                this.handleReservationAction('accept', reservation.id, reservation.brandName || reservation.brand, card);
            });

            const btnReject = document.createElement('button');
            btnReject.className = 'btn btn-danger';
            btnReject.textContent = '거절';
            btnReject.addEventListener('click', () => {
                this.handleReservationAction('reject', reservation.id, reservation.brandName || reservation.brand, card);
            });

            actions.append(btnAccept, btnReject);
        }

        card.append(thumbWrap, info, actions);
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
            const detailInfo = `
예약 ID: ${detail.id}
브랜드: ${detail.brandName || detail.brand}
팝업명: ${detail.popupTitle}
기간: ${detail.startDate} ~ ${detail.endDate}
연락처: ${detail.contactPhone || '없음'}
메시지: ${detail.message || '없음'}
공간: ${detail.space.title}
예약자: ${detail.hostName} (${detail.hostEmail || 'email 없음'})
            `;
            alert(detailInfo);
        } catch (error) {
            console.error('예약 상세 조회 실패:', error);
            alert('예약 상세 정보를 불러올 수 없습니다.');
        }
    },

    async handleReservationAction(action, reservationId, brandName, cardElement) {
        const actionText = action === 'accept' ? '승인' : '거절';
        if (!confirm(`${brandName}의 예약을 ${actionText}하시겠습니까?`)) return;

        try {
            if (action === 'accept') {
                await apiService.acceptReservation(reservationId);
            } else {
                await apiService.rejectReservation(reservationId);
            }
            alert(`예약이 ${actionText}되었습니다.`);

            // 거절한 경우 카드 즉시 제거, 승인한 경우는 새로고침
            if (action === 'reject') {
                cardElement.remove();
            } else {
                this.init(); // 승인의 경우 상태 업데이트를 위해 새로고침
            }
        } catch (error) {
            console.error(`예약 ${actionText} 실패:`, error);
            alert(`예약 ${actionText}에 실패했습니다. ${error.message}`);
        }
    },

    bindFilters() {
        document.querySelectorAll(".filter-btn").forEach((btn) => {
            btn.addEventListener("click", () => {
                const status = btn.dataset.status;

                if (status === "ALL") {
                    // 전체 보기에서도 취소/거절된 것은 제외
                    const activeReservations = this.allReservations.filter(r =>
                        r.status !== 'CANCELLED' && r.status !== 'REJECTED'
                    );
                    this.renderReservations(activeReservations);
                } else {
                    const filtered = this.allReservations.filter((r) => r.status === status);
                    this.renderReservations(filtered);
                }
            });
        });
    }
};

window.ProviderPage = ProviderPage;