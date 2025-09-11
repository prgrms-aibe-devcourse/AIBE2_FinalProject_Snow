const ReservationManagePage = {
    async init() {
        const params = new URLSearchParams(window.location.search);
        const popupId = params.get("popupId");
        if (!popupId) {
            alert("popupId가 없습니다.");
            return;
        }

        // 팝업명 불러오기
        try {
            const popup = await apiService.get(`/popups/${popupId}`);
            document.getElementById("popup-title").innerText = `팝업명 : ${popup.title}`;
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
                list.innerHTML = `<div class="empty">예약자가 없습니다.</div>`;
                return;
            }

            // 상태 매핑 (영문 → 한글)
            const statusMap = {
                RESERVED: "예약됨",
                VISITED: "방문완료",
                CANCELLED: "취소됨"
            };

            res.forEach(r => {
                const card = document.createElement("div");
                card.className = "card";
                card.innerHTML = `
          <div class="card-body">
            <p><b>예약자:</b> ${r.name}</p>
            <p><b>연락처:</b> ${r.phone}</p>
            <p><b>예약일:</b> ${r.reservationDate}</p>
            <p><b>상태:</b> <span class="status ${r.status.toLowerCase()}">${statusMap[r.status] || r.status}</span></p>
            <div class="actions">
              ${r.status === "RESERVED" ? `
                <button onclick="ReservationManagePage.markVisited(${r.id})">방문완료</button>
                <button onclick="ReservationManagePage.cancel(${r.id})">취소</button>
              ` : ""}
            </div>
          </div>
        `;
                list.appendChild(card);
            });
        } catch (err) {
            console.error(err);
            alert("예약 정보를 불러오지 못했습니다.");
        }
    },

    async markVisited(reservationId) {
        try {
            await apiService.put(`/reservations/${reservationId}/visit`);
            alert("방문 완료 처리했습니다.");
            location.reload();
        } catch (err) {
            alert("처리 실패");
        }
    },

    async cancel(reservationId) {
        try {
            await apiService.put(`/reservations/${reservationId}/cancel`);
            alert("예약을 취소했습니다.");
            location.reload();
        } catch (err) {
            alert("처리 실패");
        }
    }
};

document.addEventListener("DOMContentLoaded", () => ReservationManagePage.init());
