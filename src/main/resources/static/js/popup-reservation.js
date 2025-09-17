// 팝업 예약하기 페이지 매니저
class PopupReservationManager {
    constructor(popupId) {
        this.popupId = popupId;
        this.popupData = null;
        this.availableDates = [];
        this.selectedDate = null;
        this.selectedTimeSlot = null;
        this.timeSlots = [];
    }

    // 페이지 초기화
    async initialize() {
        try {
            this.showLoading();
            this.setupEventListeners();
            await this.loadPopupData();
            await this.loadAvailableDates();
            this.showContent();
        } catch (error) {
            console.error('예약 페이지 초기화 실패:', error);
            this.showError();
        }
    }

    // 이벤트 리스너 설정
    setupEventListeners() {
        // 예약일 선택
        const reservationDateInput = document.getElementById('reservation-date');
        if (reservationDateInput) {
            reservationDateInput.addEventListener('change', (e) => {
                this.handleDateChange(e.target.value);
            });
        }

        // 전화번호 자동 포맷팅
        const phoneInput = document.getElementById('phone');
        if (phoneInput) {
            phoneInput.addEventListener('input', this.formatPhoneNumber);
            phoneInput.addEventListener('blur', () => this.validatePhone());
        }

        // 실시간 validation
        const nameInput = document.getElementById('name');
        if (nameInput) {
            nameInput.addEventListener('blur', () => this.validateName());
        }

        const partySizeSelect = document.getElementById('party-size');
        if (partySizeSelect) {
            partySizeSelect.addEventListener('change', () => {
                this.validatePartySize();
                this.updateSubmitButton();
            });
        }

        // 예약하기 버튼
        const submitBtn = document.getElementById('submit-btn');
        if (submitBtn) {
            submitBtn.addEventListener('click', () => this.handleSubmit());
        }

        // 실시간 validation 체크
        document.addEventListener('input', () => {
            this.updateSubmitButton();
        });
    }

    // 팝업 데이터 로드
    async loadPopupData() {
        try {
            // API 호출 (기존 popup-detail과 동일한 방식)
            this.popupData = await apiService.getPopup(this.popupId);
            this.renderPopupInfo();
        } catch (error) {
            console.error('팝업 데이터 로드 실패:', error);
            throw error;
        }
    }

    // 예약 가능한 날짜 로드
    async loadAvailableDates() {
        try {
            const response = await fetch(`/api/reservations/popups/${this.popupId}/available-dates`);

            if (!response.ok) {
                throw new Error('예약 가능한 날짜 조회 실패');
            }

            this.availableDates = await response.json();
            this.setupDatePicker();
        } catch (error) {
            console.error('예약 가능 날짜 로드 실패:', error);
            throw error;
        }
    }

    // 팝업 정보 렌더링
    renderPopupInfo() {
        const popupImage = document.getElementById('popup-image');
        const popupName = document.getElementById('popup-name');
        const popupLocation = document.getElementById('popup-location');

        if (popupImage && this.popupData) {
            const fallbackImage = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iODAiIGhlaWdodD0iODAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PHJlY3Qgd2lkdGg9IjEwMCUiIGhlaWdodD0iMTAwJSIgZmlsbD0iIzRCNUFFNCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LXNpemU9IjE0IiBmaWxsPSJ3aGl0ZSIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZHk9Ii4zZW0iPk5vIEltYWdlPC90ZXh0Pjwvc3ZnPg==';

            popupImage.src = this.popupData.mainImageUrl || fallbackImage;
            popupImage.alt = this.popupData.title;

            popupImage.onerror = function() {
                this.onerror = null; // 무한루프 방지
                this.src = fallbackImage;
            };
        }

        if (popupName && this.popupData) {
            popupName.textContent = this.popupData.title;
        }

        if (popupLocation && this.popupData) {
            const location = `${this.popupData.venueName || ''} ${this.popupData.venueAddress || ''}`.trim();
            popupLocation.textContent = location || '위치 정보 없음';
        }

        // 페이지 제목 업데이트
        const pageTitle = document.getElementById('page-title');
        if (pageTitle && this.popupData) {
            pageTitle.textContent = `${this.popupData.title} 예약하기 - POPIN`;
        }
    }

    // 날짜 선택기 설정
    setupDatePicker() {
        const dateInput = document.getElementById('reservation-date');
        if (!dateInput) return;

        // 오늘 날짜로 최소값 설정
        const today = new Date().toISOString().split('T')[0];
        dateInput.min = today;

        // 예약 가능한 날짜가 있으면 최대값 설정
        if (this.availableDates.length > 0) {
            const maxDate = this.availableDates[this.availableDates.length - 1];
            dateInput.max = maxDate;
        }
    }

    // 날짜 변경 처리
    async handleDateChange(dateString) {
        if (!dateString) {
            this.clearTimeSlots();
            return;
        }

        this.selectedDate = dateString;
        this.selectedTimeSlot = null;

        // 선택된 날짜가 예약 가능한 날짜인지 확인
        if (!this.availableDates.includes(dateString)) {
            this.showDateError('선택하신 날짜는 예약이 불가능합니다.');
            this.clearTimeSlots();
            return;
        }

        try {
            await this.loadTimeSlots(dateString);
        } catch (error) {
            console.error('시간 슬롯 로드 실패:', error);
            this.showTimeError('시간 정보를 불러올 수 없습니다.');
        }
    }

    // 시간 슬롯 로드
    async loadTimeSlots(date) {
        try {
            const response = await fetch(
                `/api/reservations/popups/${this.popupId}/available-slots?date=${date}`
            );

            if (!response.ok) {
                throw new Error('시간 슬롯 조회 실패');
            }

            this.timeSlots = await response.json();
            this.renderTimeSlots();
            this.updateSubmitButton();
        } catch (error) {
            console.error('시간 슬롯 로드 실패:', error);
            throw error;
        }
    }

    // 시간 슬롯 렌더링
    renderTimeSlots() {
        const container = document.getElementById('time-slots-container');
        if (!container) return;

        if (!this.timeSlots || this.timeSlots.length === 0) {
            container.innerHTML = '<p class="time-slots-placeholder">선택하신 날짜에는 예약 가능한 시간이 없습니다.</p>';
            return;
        }

        const slotsHTML = this.timeSlots.map(slot => {
            const isAvailable = slot.available && slot.remainingSlots > 0;
            const slotClass = isAvailable ? 'time-slot' : 'time-slot unavailable';

            return `
                <button type="button" 
                        class="${slotClass}" 
                        data-start-time="${slot.startTime}" 
                        data-end-time="${slot.endTime}"
                        ${!isAvailable ? 'disabled' : ''}>
                    <span class="time-range">${slot.timeRangeText}</span>
                    <span class="remaining-count">
                        ${isAvailable ? `잔여 ${slot.remainingSlots}석` : '예약 마감'}
                    </span>
                </button>
            `;
        }).join('');

        container.innerHTML = `<div class="time-slots-grid">${slotsHTML}</div>`;

        // 시간 슬롯 클릭 이벤트 추가
        container.querySelectorAll('.time-slot:not(.unavailable)').forEach(slot => {
            slot.addEventListener('click', () => this.selectTimeSlot(slot));
        });
    }

    // 시간 슬롯 선택
    selectTimeSlot(slotElement) {
        // 이전 선택 해제
        document.querySelectorAll('.time-slot.selected').forEach(el => {
            el.classList.remove('selected');
        });

        // 새 선택 적용
        slotElement.classList.add('selected');

        this.selectedTimeSlot = {
            startTime: slotElement.dataset.startTime,
            endTime: slotElement.dataset.endTime
        };

        this.clearTimeError();
        this.updateSubmitButton();
    }

    // 폼 제출 처리
    async handleSubmit() {
        if (!this.validateForm()) {
            return;
        }

        const submitBtn = document.getElementById('submit-btn');
        const originalText = submitBtn.textContent;

        try {
            // 버튼 로딩 상태
            submitBtn.disabled = true;
            submitBtn.textContent = '예약 중...';

            // 날짜 형식 수정
            let timeString = this.selectedTimeSlot.startTime;

            // "15:00" 형식이면 ":00" 추가, "15:00:00" 형식이면 그대로 사용
            if (timeString.length === 5) {
                timeString += ':00';
            }

            const reservationDateTime = `${this.selectedDate}T${timeString}`;

            // 예약 데이터 구성
            const reservationData = {
                name: document.getElementById('name').value.trim(),
                phone: document.getElementById('phone').value.trim(),
                partySize: parseInt(document.getElementById('party-size').value),
                reservationDate: reservationDateTime
            };

            // 예약 API 호출
            const response = await fetch(`/api/reservations/popups/${this.popupId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${apiService.getStoredToken()}`
                },
                body: JSON.stringify(reservationData),
                credentials: 'include'
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '예약 실패');
            }

            const result = await response.json();
            this.showSuccessModal();

        } catch (error) {
            console.error('예약 실패:', error);
            alert(error.message || '예약 처리 중 오류가 발생했습니다.');
        } finally {
            // 버튼 원상복구
            submitBtn.disabled = false;
            submitBtn.textContent = originalText;
        }
    }

    // 폼 전체 validation
    validateForm() {
        const isNameValid = this.validateName();
        const isPhoneValid = this.validatePhone();
        const isPartySizeValid = this.validatePartySize();
        const isDateValid = this.validateDate();
        const isTimeValid = this.validateTime();

        return isNameValid && isPhoneValid && isPartySizeValid && isDateValid && isTimeValid;
    }

    // 각 필드 validation 메서드들
    validateName() {
        const nameInput = document.getElementById('name');
        const errorEl = document.getElementById('name-error');
        const name = nameInput.value.trim();

        if (!name) {
            this.showFieldError(nameInput, errorEl, '이름을 입력해주세요.');
            return false;
        }

        if (name.length < 2) {
            this.showFieldError(nameInput, errorEl, '이름은 2글자 이상 입력해주세요.');
            return false;
        }

        this.clearFieldError(nameInput, errorEl);
        return true;
    }

    validatePhone() {
        const phoneInput = document.getElementById('phone');
        const errorEl = document.getElementById('phone-error');
        const phone = phoneInput.value.trim();

        if (!phone) {
            this.showFieldError(phoneInput, errorEl, '전화번호를 입력해주세요.');
            return false;
        }

        const phoneRegex = /^010-\d{4}-\d{4}$/;
        if (!phoneRegex.test(phone)) {
            this.showFieldError(phoneInput, errorEl, '올바른 전화번호 형식이 아닙니다. (010-0000-0000)');
            return false;
        }

        this.clearFieldError(phoneInput, errorEl);
        return true;
    }

    validatePartySize() {
        const partySizeSelect = document.getElementById('party-size');
        const errorEl = document.getElementById('party-size-error');
        const partySize = partySizeSelect.value;

        if (!partySize) {
            this.showFieldError(partySizeSelect, errorEl, '예약 인원을 선택해주세요.');
            return false;
        }

        this.clearFieldError(partySizeSelect, errorEl);
        return true;
    }

    validateDate() {
        const dateInput = document.getElementById('reservation-date');
        const errorEl = document.getElementById('date-error');

        if (!this.selectedDate) {
            this.showFieldError(dateInput, errorEl, '예약 날짜를 선택해주세요.');
            return false;
        }

        this.clearFieldError(dateInput, errorEl);
        return true;
    }

    validateTime() {
        const errorEl = document.getElementById('time-error');

        if (!this.selectedTimeSlot) {
            this.showTimeError('예약 시간을 선택해주세요.');
            return false;
        }

        this.clearTimeError();
        return true;
    }

    // 유틸리티 메서드들
    formatPhoneNumber(e) {
        let value = e.target.value.replace(/[^0-9]/g, '');

        if (value.length >= 3) {
            value = value.slice(0, 3) + '-' + value.slice(3);
        }
        if (value.length >= 8) {
            value = value.slice(0, 8) + '-' + value.slice(8, 12);
        }

        e.target.value = value;
    }

    showFieldError(inputEl, errorEl, message) {
        if (inputEl) inputEl.classList.add('error');
        if (errorEl) errorEl.textContent = message;
    }

    clearFieldError(inputEl, errorEl) {
        if (inputEl) inputEl.classList.remove('error');
        if (errorEl) errorEl.textContent = '';
    }

    showDateError(message) {
        const errorEl = document.getElementById('date-error');
        if (errorEl) errorEl.textContent = message;
    }

    showTimeError(message) {
        const errorEl = document.getElementById('time-error');
        if (errorEl) errorEl.textContent = message;
    }

    clearTimeError() {
        const errorEl = document.getElementById('time-error');
        if (errorEl) errorEl.textContent = '';
    }

    clearTimeSlots() {
        const container = document.getElementById('time-slots-container');
        if (container) {
            container.innerHTML = '<p class="time-slots-placeholder">날짜를 먼저 선택해주세요.</p>';
        }
        this.selectedTimeSlot = null;
        this.updateSubmitButton();
    }

    updateSubmitButton() {
        const submitBtn = document.getElementById('submit-btn');
        if (!submitBtn) return;

        const hasRequiredFields =
            document.getElementById('name').value.trim() &&
            document.getElementById('phone').value.trim() &&
            document.getElementById('party-size').value &&
            this.selectedDate &&
            this.selectedTimeSlot;

        submitBtn.disabled = !hasRequiredFields;
    }

    showSuccessModal() {
        const modal = document.getElementById('success-modal');
        if (modal) {
            modal.style.display = 'flex';
        }
    }

    // 상태 관리 메서드들
    showLoading() {
        document.getElementById('reservation-loading').style.display = 'flex';
        document.getElementById('reservation-content').style.display = 'none';
        document.getElementById('reservation-error').style.display = 'none';
    }

    showContent() {
        document.getElementById('reservation-loading').style.display = 'none';
        document.getElementById('reservation-content').style.display = 'block';
        document.getElementById('reservation-error').style.display = 'none';
    }

    showError() {
        document.getElementById('reservation-loading').style.display = 'none';
        document.getElementById('reservation-content').style.display = 'none';
        document.getElementById('reservation-error').style.display = 'flex';
    }

    // 컴포넌트 정리
    cleanup() {
    }
}

// 모달 버튼 이벤트
function goToPopupDetail() {
    const pathParts = window.location.pathname.split('/');
    const popupId = pathParts[2];
    window.location.href = `/popup/${popupId}`;
}

function goToMyPage() {
    window.location.href = '/mypage/reservations';
}

// 전역 등록
window.PopupReservationManager = PopupReservationManager;
window.goToPopupDetail = goToPopupDetail;
window.goToMyPage = goToMyPage;