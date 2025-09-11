// 공간 등록 페이지
const SpaceRegisterPage = {
    init() {
        const form = document.getElementById('space-register-form');
        if (form) form.addEventListener('submit', (e) => this.handleSubmit(e));

        // 뒤로/목록 이동 버튼
        document.querySelectorAll('[data-act="back"], [data-act="list"]')
            .forEach(btn => btn.addEventListener('click', () => this.goList()));

        // 날짜 유효성 검증 이벤트
        this.setupDateValidation();

        // 주소 검색 기능 초기화
        this.setupAddressSearch();
    },

    setupDateValidation() {
        const startDateInput = document.getElementById('startDate');
        const endDateInput = document.getElementById('endDate');

        // 시작일 변경 시 종료일 최소값 설정
        startDateInput?.addEventListener('change', (e) => {
            if (e.target.value) {
                endDateInput.min = e.target.value;
                if (endDateInput.value && endDateInput.value < e.target.value) {
                    endDateInput.value = e.target.value;
                }
            }
        });

        // 오늘 날짜를 최소값으로 설정
        const today = new Date().toISOString().split('T')[0];
        if (startDateInput) startDateInput.min = today;
    },

    setupAddressSearch() {
        const btn = document.getElementById("btn-search-address");
        btn?.addEventListener("click", function () {
            new daum.Postcode({
                oncomplete: function (data) {
                    document.getElementById("roadAddress").value = data.roadAddress;
                    document.getElementById("jibunAddress").value = data.jibunAddress;

                    // 주소 입력 후 상세 주소 필드로 포커스 이동
                    document.getElementById("detailAddress")?.focus();
                }
            }).open();
        });
    },

    async handleSubmit(e) {
        e.preventDefault();

        const submitBtn = e.submitter || document.querySelector('button[type="submit"]');
        if (submitBtn) submitBtn.disabled = true;

        try {
            // 유효성 검증
            if (!this.validateForm()) {
                return;
            }

            const fd = new FormData();
            const v = id => document.getElementById(id)?.value?.trim() ?? '';

            // venue 관련 - 필수 필드 검증
            const roadAddress = v('roadAddress');
            if (!roadAddress) {
                alert('주소 검색을 통해 주소를 입력해주세요.');
                return;
            }

            fd.append('roadAddress', roadAddress);
            fd.append('jibunAddress', v('jibunAddress'));
            fd.append('detailAddress', v('detailAddress'));

            // 공간 정보
            fd.append('title', v('title'));
            fd.append('description', v('description'));
            fd.append('areaSize', v('areaSize'));
            fd.append('startDate', v('startDate'));
            fd.append('endDate', v('endDate'));
            fd.append('rentalFee', v('rentalFee'));
            fd.append('contactPhone', v('contactPhone')); // 필드명 수정

            // 이미지
            const img = document.getElementById('image')?.files?.[0];
            if (img) fd.append('image', img);

            // API 호출
            await apiService.createSpace(fd);
            alert('공간이 성공적으로 등록되었습니다.');
            this.goList();

        } catch (err) {
            console.error('공간 등록 실패:', err);
            this.handleError(err);
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    },

    validateForm() {
        const requiredFields = [
            { id: 'roadAddress', name: '주소' },
            { id: 'title', name: '제목' },
            { id: 'areaSize', name: '면적' },
            { id: 'startDate', name: '임대 시작일' },
            { id: 'endDate', name: '임대 종료일' },
            { id: 'rentalFee', name: '임대료' },
            { id: 'contactPhone', name: '연락처' }
        ];

        for (const field of requiredFields) {
            const element = document.getElementById(field.id);
            const value = element?.value?.trim();

            if (!value) {
                alert(`${field.name}을(를) 입력해주세요.`);
                element?.focus();
                return false;
            }
        }

        // 날짜 유효성 검증
        const startDate = document.getElementById('startDate').value;
        const endDate = document.getElementById('endDate').value;

        if (startDate && endDate && endDate < startDate) {
            alert('종료일은 시작일 이후여야 합니다.');
            document.getElementById('endDate').focus();
            return false;
        }

        // 연락처 형식 검증
        const phone = document.getElementById('contactPhone').value.trim();
        const phoneRegex = /^[0-9-+()\s]+$/;
        if (phone && !phoneRegex.test(phone)) {
            alert('올바른 전화번호 형식이 아닙니다.');
            document.getElementById('contactPhone').focus();
            return false;
        }

        return true;
    },

    handleError(err) {
        const msg = String(err?.message || '');

        if (msg.includes('401')) {
            alert('로그인이 필요합니다.');
        } else if (msg.includes('400') || msg.includes('422')) {
            // 서버 유효성 검증 오류 처리
            if (err.response?.data?.errors) {
                const errors = err.response.data.errors;
                const errorMsg = Object.values(errors).join('\n');
                alert('입력 정보를 확인해주세요:\n' + errorMsg);
            } else {
                alert('입력 정보를 확인해주세요.');
            }
        } else {
            alert('등록 중 오류가 발생했습니다. 다시 시도해주세요.');
        }
    },

    goList() {
        location.assign('/templates/pages/space-list.html');
    }
};

// 페이지 로드 시 초기화
document.addEventListener("DOMContentLoaded", function () {
    // SpaceRegisterPage 초기화
    SpaceRegisterPage.init();
});

// 전역 객체로 등록
window.SpaceRegisterPage = SpaceRegisterPage;