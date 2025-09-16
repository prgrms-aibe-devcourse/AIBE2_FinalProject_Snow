function translateStatus(status) {
    switch (status) {
        case 'PLANNED': return '준비 중';
        case 'ONGOING': return '진행 중';
        case 'FINISHED': return '종료됨';
        case 'CANCELLED': return '취소됨';
        case 'PENDING': return '예약 대기 중';
        case 'ACCEPTED': return '승인됨';
        case 'REJECTED': return '거절됨';
        default: return status || '';
    }
}

const HostPage = {
    async init() {
        try {
            const [hostInfo, myPopups, myReservations] = await Promise.all([
                apiService.get('/hosts/me'),
                apiService.get('/hosts/popups'),
                apiService.get('/space-reservations/my-requests')
            ]);

            this.renderHostInfo(hostInfo);
            this.renderPopups(myPopups);
            this.renderReservations(myReservations);
        } catch (err) {
            console.error('HostPage init 실패:', err);
            alert('데이터 로딩 중 오류가 발생했습니다.');
        }
    },

    renderHostInfo(info) {
        document.getElementById('user-email').textContent = info.email || '-';
        document.getElementById('user-name').textContent = info.name || '-';
        document.getElementById('user-nickname').textContent = info.nickname || '-';
        document.getElementById('user-phone').textContent = info.phone || '-';
        document.getElementById('user-brand').textContent = info.brandName || '-';
    },

    renderPopups(popups) {
        const listEl = document.getElementById('my-popup-list');
        listEl.innerHTML = '';

        if (popups && popups.length > 0) {
            popups.forEach(p => {
                const status = p.status || 'PLANNED';
                const card = document.createElement('div');
                card.className = 'popup-card';
                card.innerHTML = `
                    <img src="${p.mainImageUrl || '/img/placeholder.png'}" class="thumb" alt="썸네일">
                    <div class="info">
                        <div class="title">${p.title || '제목 없음'}</div>
                        <div class="meta">
                            <span class="status-badge ${status.toLowerCase()}">${translateStatus(status)}</span>
                        </div>
                    </div>
                    <div class="right-actions">
                        <button class="btn-detail" data-popup-id="${p.id}">상세보기</button>
                        <button class="btn-manage" data-popup-id="${p.id}">예약관리</button>
                        <button class="btn-stats" data-popup-id="${p.id}">통계</button>
                    </div>
                `;
                this.addPopupCardEventListeners(card, p);
                listEl.appendChild(card);
            });
        } else {
            listEl.innerHTML = '<div class="empty">등록한 팝업이 없습니다.</div>';
        }
    },

    addPopupCardEventListeners(card, popup) {
        card.querySelector('.btn-detail').addEventListener('click', () => {
            window.location.href = `/templates/pages/host-popup-detail.html?popupId=${popup.id}`;
        });
        card.querySelector('.btn-manage').addEventListener('click', () => {
            window.location.href = `/templates/pages/reservation-manage.html?popupId=${popup.id}`;
        });
        card.querySelector('.btn-stats').addEventListener('click', () => {
            alert('통계는 준비중입니다.');
        });
    },

    renderReservations(reservations) {
        const listEl = document.getElementById('my-reservation-list');
        listEl.innerHTML = '';

        if (reservations && reservations.length > 0) {
            // 취소된 예약은 제외하고 필터링
            const activeReservations = reservations.filter(r => r.status !== 'CANCELLED');

            if (activeReservations.length > 0) {
                activeReservations.forEach(r => {
                    const status = r.status || '';
                    const card = document.createElement('div');
                    card.className = 'rent-card';
                    card.innerHTML = `
                        <div class="left">
                            <img src="${r.spaceImageUrl || '/img/placeholder.png'}" class="thumb" alt="공간 이미지" />
                            <div>
                                <div class="address"><strong>${r.spaceTitle || '공간명 없음'}</strong></div>
                                <div class="desc">주소 : ${r.spaceAddress || '주소 정보 없음'}</div>
                                <div class="dates">${r.startDate || ''} ~ ${r.endDate || ''}</div>
                                <span class="status-badge ${status.toLowerCase()}">${translateStatus(status)}</span>
                            </div>
                        </div>
                        <div class="right-actions">
                            <button class="btn-detail" data-reservation-id="${r.id}" data-space-id="${r.spaceId}">상세보기</button>
                            <button class="btn-map" data-address="${r.spaceAddress || ''}">지도로 보기</button>
                            <button class="btn-cancel" data-reservation-id="${r.id}">예약취소</button>
                        </div>
                    `;
                    this.addReservationCardEventListeners(card, r);
                    listEl.appendChild(card);
                });
            } else {
                listEl.innerHTML = '<div class="empty">진행 중인 예약 내역이 없습니다.</div>';
            }
        } else {
            listEl.innerHTML = '<div class="empty">예약 내역이 없습니다.</div>';
        }
    },

    addReservationCardEventListeners(card, reservation) {
        // 상세보기 → 공간 상세 페이지로 이동 (수정됨)
        card.querySelector('.btn-detail').addEventListener('click', () => {
            if (reservation.spaceId) {
                window.location.href = `/templates/pages/space-detail.html?id=${reservation.spaceId}`;
            } else {
                // 백업: spaceId가 없으면 예약 정보 표시
                const info = `
예약 ID: ${reservation.id}
공간명: ${reservation.spaceTitle || '공간명 없음'}
주소: ${reservation.spaceAddress || '주소 없음'}
예약 기간: ${reservation.startDate || ''} ~ ${reservation.endDate || ''}
상태: ${translateStatus(reservation.status)}
                `.trim();
                alert(info);
            }
        });

        // 지도로 보기 (개선됨)
        card.querySelector('.btn-map').addEventListener('click', () => {
            const address = reservation.spaceAddress;
            if (address && address !== '주소 정보 없음') {
                // 네이버 지도로 주소 검색
                const searchUrl = `https://map.naver.com/v5/search/${encodeURIComponent(address)}`;
                window.open(searchUrl, '_blank');
            } else {
                alert("주소 정보가 없습니다.");
            }
        });

        // 예약 취소 (HOST용 취소 API 사용)
        card.querySelector('.btn-cancel').addEventListener('click', async () => {
            if (!confirm('정말 예약을 취소하시겠습니까?')) return;
            try {
                console.log('예약 취소 시작, ID:', reservation.id);
                console.log('예약 상태:', reservation.status);

                // HOST용 예약 취소 API 사용
                const result = await apiService.delete(`/space-reservations/${reservation.id}`);
                console.log('취소 성공:', result);
                alert('예약이 취소되었습니다.');

                // 카드를 즉시 제거
                card.remove();
            } catch (err) {
                console.error('예약 취소 실패 상세:', err);

                let errorMessage = '예약 취소에 실패했습니다.';

                if (err.message && err.message.includes('500')) {
                    errorMessage = '서버에서 처리 중 오류가 발생했습니다. 예약 상태를 확인해주세요.';
                } else if (err.message && err.message.includes('401')) {
                    errorMessage = '인증이 필요합니다. 다시 로그인해주세요.';
                } else if (err.message && err.message.includes('403')) {
                    errorMessage = '권한이 없습니다.';
                } else if (err.message && err.message.includes('404')) {
                    errorMessage = '예약을 찾을 수 없습니다.';
                } else if (err.message && err.message.includes('400')) {
                    errorMessage = '취소할 수 없는 예약입니다. (이미 승인되었거나 완료된 예약일 수 있습니다)';
                }

                alert(errorMessage);

                // 500 에러 발생 시 상세 정보 표시
                if (err.message && err.message.includes('500')) {
                    console.log('=== 디버깅 정보 ===');
                    console.log('예약 ID:', reservation.id);
                    console.log('예약 상태:', reservation.status);
                    console.log('공간 정보:', reservation.spaceTitle);
                    console.log('전체 예약 객체:', reservation);
                }
            }
        });
    }
};

document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('btn-popup-register');
    if (btn) {
        btn.addEventListener('click', () => {
            window.location.href = '/templates/pages/popup-register.html';
        });
    }
});

window.HostPage = HostPage;