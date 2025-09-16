const HostPopupDetailPage = {
    async init() {
        const params = new URLSearchParams(window.location.search);
        const popupId = params.get("popupId") || params.get("id");

        if (!popupId) {
            alert("popupId가 없습니다.");
            window.history.back();
            return;
        }

        try {
            const popup = await apiService.get(`/hosts/popups/${popupId}`);
            this.renderPopupDetail(popup);
        } catch (err) {
            console.error("팝업 상세 불러오기 실패:", err);
            if (err.message?.includes("401") || err.status === 401) {
                alert("로그인이 필요합니다.");
                window.location.href = "/auth/login";
            } else {
                alert("팝업 정보를 불러올 수 없습니다.");
                window.history.back();
            }
        }
    },

    renderPopupDetail(popup) {
        const elements = {
            'popup-title': popup.title || '-',
            'popup-summary': popup.summary || '-',
            'popup-description': popup.description || '-',
            'popup-schedule': `${popup.startDate || ''} ~ ${popup.endDate || ''}`,
            'popup-venue': popup.venueName || '-',
            'popup-address': popup.venueAddress || '-',
            'popup-status': this.translateStatus(popup.status) || '-'
        };

        Object.entries(elements).forEach(([id, value]) => {
            const element = document.getElementById(id);
            if (element) element.textContent = value;
        });

        // 운영시간 렌더링
        this.renderOperatingHours(popup.hours);

        const imageElement = document.getElementById("popup-image");
        if (imageElement) {
            if (popup.mainImageUrl) {
                imageElement.src = popup.mainImageUrl;
            } else if (popup.imageUrl) {
                imageElement.src = popup.imageUrl;
            } else {
                imageElement.style.display = 'block';
                imageElement.style.backgroundColor = '#f5f5f5';
                imageElement.style.border = '2px dashed #ddd';
                imageElement.removeAttribute('src');
            }
            imageElement.onerror = () => {
                imageElement.style.backgroundColor = '#f5f5f5';
                imageElement.style.border = '2px dashed #ddd';
                imageElement.removeAttribute('src');
            };
        }

        // 수정 버튼
        const editBtn = document.querySelector('.btn-edit');
        if (editBtn) {
            editBtn.addEventListener('click', () => {
                window.location.href = `/templates/pages/popup-edit.html?id=${popup.id}`;
            });
        }

        // 삭제 버튼
        const deleteBtn = document.querySelector('.btn-delete');
        if (deleteBtn) {
            deleteBtn.addEventListener('click', async () => {
                if (!confirm("정말 삭제하시겠습니까?")) return;
                try {
                    await apiService.delete(`/hosts/popups/${popup.id}`);
                    alert("팝업이 삭제되었습니다.");
                    window.location.href = "/templates/pages/mpg-host.html";
                } catch (err) {
                    console.error("삭제 실패:", err);
                    alert("팝업 삭제에 실패했습니다.");
                }
            });
        }
    },

    renderOperatingHours(hours) {
        // 운영시간을 표시할 요소 찾기 (기존 요소를 활용하거나 새로 생성)
        let hoursElement = document.getElementById('popup-hours');

        // 만약 해당 요소가 없다면, 상태 정보 다음에 추가
        if (!hoursElement) {
            const statusElement = document.getElementById('popup-status');
            if (statusElement && statusElement.parentElement) {
                const newHoursRow = document.createElement('p');
                newHoursRow.innerHTML = '<b>운영시간:</b><span id="popup-hours-content"></span>';
                statusElement.parentElement.insertAdjacentElement('afterend', newHoursRow);
                hoursElement = document.getElementById('popup-hours-content');
            }
        }

        if (!hoursElement) return;

        if (!hours || hours.length === 0) {
            hoursElement.textContent = '운영시간 정보가 없습니다.';
            return;
        }

        // 요일별로 그룹화
        const groupedHours = this.groupHoursByDay(hours);

        // HTML 생성
        const hoursHTML = Object.entries(groupedHours).map(([timeSlot, days]) => {
            const dayNames = days.sort().map(day => this.getDayName(day)).join(', ');
            return `<div class="hour-item">
                <span class="hour-days">${dayNames}</span>
                <span class="hour-time">${timeSlot}</span>
            </div>`;
        }).join('');

        hoursElement.innerHTML = `<div class="operating-hours">${hoursHTML}</div>`;
    },

    groupHoursByDay(hours) {
        const grouped = {};

        hours.forEach(hour => {
            const timeSlot = `${hour.openTime} - ${hour.closeTime}`;
            if (!grouped[timeSlot]) {
                grouped[timeSlot] = [];
            }
            grouped[timeSlot].push(hour.dayOfWeek);
        });

        return grouped;
    },

    getDayName(dayOfWeek) {
        const days = ['일', '월', '화', '수', '목', '금', '토'];
        return days[dayOfWeek] || dayOfWeek;
    },

    translateStatus(status) {
        switch (status) {
            case 'PLANNED': return '준비 중';
            case 'ONGOING': return '진행 중';
            case 'FINISHED': return '종료됨';
            case 'CANCELLED': return '취소됨';
            case 'PENDING': return '승인 대기';
            case 'ACCEPTED': return '승인됨';
            case 'REJECTED': return '거절됨';
            default: return status || '-';
        }
    }
};

window.HostPopupDetailPage = HostPopupDetailPage;