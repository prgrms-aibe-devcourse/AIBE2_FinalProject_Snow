// /js/mpg-host.js
const HostPage = {
    async init() {
        try {
            // 내가 등록한 팝업 + 내가 예약한 공간 내역을 동시에 불러오기
            const [hostInfo,myPopups, myReservations] = await Promise.all([
                apiService.get('/hosts/popups'),
                apiService.get('/space-reservations/my-requests'),
                apiService.get('/hosts/me')
            ]);

            this.renderPopups(myPopups);
            this.renderReservations(myReservations);
            this.renderHostInfo(hostInfo);
        } catch (err) {
            console.error('HostPage init 실패:', err);
            alert('데이터 로딩 중 오류가 발생했습니다.');
        }
    },



    renderPopups(popups) {
        const listEl = document.getElementById('my-popup-list');
        const emptyEl = listEl.querySelector('[data-empty]');

        if (popups && popups.length > 0) {
            if (emptyEl) emptyEl.remove();

            popups.forEach(p => {
                const card = document.createElement('div');
                card.className = 'card popup-card';
                card.innerHTML = `
                <div class="card-body">
                    <div><strong>제목:</strong> ${p.title}</div>
                    <div><strong>브랜드명:</strong> ${p.brandName || '브랜드 없음'}</div>
                    <div><strong>요약:</strong> ${p.summary || '-'}</div>
                    <div><strong>기간:</strong> ${p.startDate || ''} ~ ${p.endDate || ''}</div>
                    <div><strong>상태:</strong> ${p.status || 'PLANNED'}</div>
                    <div class="btn-row">
                        <button class="btn-outline edit-btn">수정</button>
                        <button class="btn-danger delete-btn">삭제</button>
                    </div>
                </div>
            `;

                // 수정
                card.querySelector('.edit-btn').addEventListener('click', () => {
                    window.location.href = `/templates/pages/popup-edit.html?id=${p.id}`;
                });

                // 삭제
                card.querySelector('.delete-btn').addEventListener('click', async () => {
                    if (!confirm('정말 삭제하시겠습니까?')) return;
                    try {
                        await apiService.delete(`/hosts/popups/${p.id}`);
                        alert('팝업이 삭제되었습니다.');
                        location.reload();
                    } catch (err) {
                        console.error('팝업 삭제 실패:', err);
                        alert('삭제 실패');
                    }
                });

                listEl.appendChild(card);
            });
        }
    },



    renderReservations(reservations) {
        const listEl = document.getElementById('my-reservation-list');
        const emptyEl = listEl.querySelector('[data-empty]');

        if (reservations && reservations.length > 0) {
            if (emptyEl) emptyEl.remove();

            reservations.forEach(r => {
                const card = document.createElement('div');
                card.className = 'card reservation-card';
                card.innerHTML = `
                    <div class="card-body">
                        <div class="title">${r.popupTitle || '예약 팝업'}</div>
                        <div class="brand">브랜드: ${r.brand || '브랜드 없음'}</div>
                        <div class="dates">${r.startDate || ''} ~ ${r.endDate || ''}</div>
                        <div class="status">상태: ${r.status || ''}</div>
                        <button class="btn-outline cancel-btn">예약 취소</button>
                    </div>
                `;
                // 예약 취소 버튼 이벤트 연결
                const cancelBtn = card.querySelector('.cancel-btn');
                cancelBtn.addEventListener('click', () => this.cancelReservation(r.id));
                listEl.appendChild(card);
            });
        }
    },


    async cancelReservation(reservationId) {
        if (!confirm('정말 예약을 취소하시겠습니까?')) return;
        try {
            await apiService.put(`/space-reservations/${reservationId}/cancel`, {});
            alert('예약이 취소되었습니다.');
            location.reload();
        } catch (err) {
            console.error('예약 취소 실패:', err);
            alert('예약 취소에 실패했습니다.');
        }
    }
};

//팝업 등록하기 버튼 이벤
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
