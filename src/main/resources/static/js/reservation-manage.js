// ====== 날짜 포맷 유틸 ======
function formatDate(dateString) {
    if (!dateString) return "-";
    const d = new Date(dateString);
    if (isNaN(d)) return dateString;

    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    const hh = String(d.getHours()).padStart(2, "0");
    const mm = String(d.getMinutes()).padStart(2, "0");

    return `${y}-${m}-${day} ${hh}:${mm}`;
}

// ====== 사용자 이름에서 첫글자 추출 ======
function getInitial(name) {
    return name ? name.charAt(0) : "?";
}

// ====== 통계 업데이트 ======
function updateStats(reservations) {
    const totalCount = reservations.length;
    const reservedCount = reservations.filter(r => r.status === 'RESERVED').length;
    const visitedCount = reservations.filter(r => r.status === 'VISITED').length;
    const cancelledCount = reservations.filter(r => r.status === 'CANCELLED').length;

    document.getElementById('total-count').textContent = totalCount;
    document.getElementById('reserved-count').textContent = reservedCount;
    document.getElementById('visited-count').textContent = visitedCount;
}

const ReservationManagePage = {
    async init() {
        const params = new URLSearchParams(window.location.search);
        const popupId = params.get("popupId");
        if (!popupId) {
            alert("popupId가 없습니다.");
            return;
        }

        try {
            const popup = await apiService.get(`/popups/${popupId}`);
            document.getElementById("popup-title").innerText = popup.title;
        } catch (err) {
            console.error("팝업 정보 불러오기 실패:", err);
            document.getElementById("popup-title").innerText = "팝업명 불러오기 실패";
        }

        await this.loadReservations(popupId);
    },

    async loadReservations(popupId) {
        try {
            const res = await apiService.get(`/reservations/popups/${popupId}`);
            const list = document.getElementById("reservation-list");
            list.innerHTML = "";

            if (!res || res.length === 0) {
                list.innerHTML = `
                    <div class="empty-state">
                        <div class="icon">📅</div>
                        <div class="text">예약자가 없습니다.</div>
                    </div>
                `;
                updateStats([]);
                return;
            }

            const statusMap = {
                RESERVED: { text: "예약됨", class: "status-reserved" },
                VISITED: { text: "방문완료", class: "status-visited" },
                CANCELLED: { text: "예약취소", class: "status-cancelled" }
            };

            res.forEach(r => {
                const status = statusMap[r.status] || { text: r.status, class: "" };

                const card = document.createElement("div");
                card.className = "reservation-card";
                card.innerHTML = `
                    <div class="card-main">
                        <div class="user-avatar">${getInitial(r.name)}</div>
                        <div class="card-info">
                            <div class="user-name">${r.name}</div>
                            <div class="user-phone">${r.phone}</div>
                        </div>
                        <div class="status-badge ${status.class}">${status.text}</div>
                    </div>
                    <div class="card-details">
                        <div class="detail-row">
                            <span class="detail-label">예약일시</span>
                            <span class="detail-value">${formatDate(r.reservationDate)}</span>
                        </div>
                        ${r.partySize ? `
                        <div class="detail-row">
                            <span class="detail-label">인원</span>
                            <span class="detail-value">${r.partySize}명</span>
                        </div>` : ''}
                        ${r.note ? `
                        <div class="detail-row">
                            <span class="detail-label">메모</span>
                            <span class="detail-value">${r.note}</span>
                        </div>` : ''}
                    </div>
                    ${r.status === "RESERVED" ? `
                    <div class="card-actions">
                        <button class="action-btn btn-complete" onclick="ReservationManagePage.markVisited(${r.id})">방문완료</button>
                        <button class="action-btn btn-cancel" onclick="ReservationManagePage.cancel(${r.id})">취소</button>
                    </div>` : ""}
                `;
                list.appendChild(card);
            });

            // 통계 업데이트
            updateStats(res);

        } catch (err) {
            console.error(err);
            alert("예약 정보를 불러오지 못했습니다.");
            document.getElementById("reservation-list").innerHTML = `
                <div class="empty-state">
                    <div class="icon">❌</div>
                    <div class="text">예약 정보를 불러오는데 실패했습니다.</div>
                </div>
            `;
        }
    },

    async markVisited(reservationId) {
        if (!confirm('방문 완료 처리하시겠습니까?')) {
            return;
        }

        try {
            await apiService.put(`/reservations/${reservationId}/visit`);
            alert("방문 완료 처리했습니다.");
            location.reload();
        } catch (err) {
            console.error("방문 완료 처리 실패:", err);
            alert("처리에 실패했습니다. 다시 시도해주세요.");
        }
    },

    async cancel(reservationId) {
        if (!confirm('예약을 취소하시겠습니까?\n취소된 예약은 되돌릴 수 없습니다.')) {
            return;
        }

        try {
            await apiService.put(`/reservations/${reservationId}/cancel`);
            alert("예약을 취소했습니다.");
            location.reload();
        } catch (err) {
            console.error("예약 취소 실패:", err);
            alert("처리에 실패했습니다. 다시 시도해주세요.");
        }
    }
};

// 페이지 초기화
document.addEventListener("DOMContentLoaded", () => {
    // 기존 초기화 로직과 충돌하지 않도록 조건부 실행
    if (!window.componentLoaded) {
        ReservationManagePage.init();
    }
});

// 전역 함수로 노출 (기존 코드 호환성)
window.ReservationManagePage = ReservationManagePage;