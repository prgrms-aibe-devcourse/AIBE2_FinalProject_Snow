// /js/mpg-host.js

// 상태 한글 변환 함수
function translateStatus(status) {
    switch (status) {
        case 'PLANNED': return '계획 중';
        case 'ONGOING': return '진행 중';
        case 'FINISHED': return '종료됨';
        case 'CANCELLED': return '취소됨';
        case 'PENDING': return '대기 중';
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

    // 팝업 카드
    renderPopups(popups) {
        const listEl = document.getElementById('my-popup-list');
        listEl.innerHTML = ''; // 초기화

        if (popups && popups.length > 0) {
            popups.forEach(p => {
                const status = p.status || 'PLANNED';
                const card = document.createElement('div');
                card.className = 'popup-card';
                card.innerHTML = `
          <img src="/img/placeholder.png" class="thumb" alt="썸네일">
          <div class="info">
            <div class="title">${p.title}</div>
            <div class="meta">
              <span class="status-badge ${status.toLowerCase()}">${translateStatus(status)}</span>
              <button class="btn-edit">수정</button>
              <button class="btn-delete">삭제</button>
            </div>
          </div>
          <div class="menu">
            <span>상세 정보</span>
            <span>예약 관리</span>
            <span>통계</span>
          </div>
        `;

                card.querySelector('.btn-edit').addEventListener('click', () => {
                    window.location.href = `/templates/pages/popup-edit.html?id=${p.id}`;
                });
                card.querySelector('.btn-delete').addEventListener('click', async () => {
                    if (!confirm('정말 삭제하시겠습니까?')) return;
                    await apiService.delete(`/hosts/popups/${p.id}`);
                    alert('팝업이 삭제되었습니다.');
                    this.renderPopups(await apiService.get('/hosts/popups')); // 재렌더링
                });

                listEl.appendChild(card);
            });
        } else {
            listEl.innerHTML = '<div class="empty">등록한 팝업이 없습니다.</div>';
        }
    },

    // 예약 카드
    renderReservations(reservations) {
        const listEl = document.getElementById('my-reservation-list');
        listEl.innerHTML = '';

        if (reservations && reservations.length > 0) {
            reservations.forEach(r => {
                const status = r.status || '';
                const card = document.createElement('div');
                card.className = 'rent-card';
                card.innerHTML = `
          <div class="left">
            <img src="${r.spaceImageUrl || '/img/placeholder.png'}" class="thumb" alt="공간 이미지" />
            <div>
              <div class="address"><strong>${r.spaceTitle || '공간명 없음'}</strong></div>
              <div class="desc">${r.spaceAddress || ''}</div>
              <div class="dates">${r.startDate || ''} ~ ${r.endDate || ''}</div>
              <span class="status-badge ${status.toLowerCase()}">${translateStatus(status)}</span>
            </div>
          </div>
          <div class="actions">
            <button class="call">📞</button>
            <button class="cancel">❌</button>
          </div>
        `;

                card.querySelector('.cancel').addEventListener('click', () => this.cancelReservation(r.id));

                const callBtn = card.querySelector('.call');
                if (r.hostPhone) {
                    callBtn.addEventListener('click', () => {
                        window.location.href = `tel:${r.hostPhone}`;
                    });
                } else {
                    callBtn.disabled = true;
                    callBtn.style.opacity = 0.5;
                }

                listEl.appendChild(card);
            });
        } else {
            listEl.innerHTML = '<div class="empty">예약 내역이 없습니다.</div>';
        }
    },

    async cancelReservation(reservationId) {
        if (!confirm('정말 예약을 취소하시겠습니까?')) return;
        try {
            await apiService.put(`/space-reservations/${reservationId}/cancel`, {});
            alert('예약이 취소되었습니다.');
            this.renderReservations(await apiService.get('/space-reservations/my-requests'));
        } catch (err) {
            console.error('예약 취소 실패:', err);
            alert('예약 취소에 실패했습니다.');
        }
    }
};

// 팝업 등록 버튼
document.addEventListener('DOMContentLoaded', () => {
    const btn = document.getElementById('btn-popup-register');
    if (btn) {
        btn.addEventListener('click', () => {
            window.location.href = '/templates/pages/popup-register.html';
        });
    }
});

window.HostPage = HostPage;
document.addEventListener('DOMContentLoaded', () => HostPage.init());
