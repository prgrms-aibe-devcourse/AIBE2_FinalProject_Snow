class SpaceRegisterManager {
    async initialize() {
        const form = document.getElementById("space-register-form");
        if (form) form.addEventListener("submit", (e) => this.handleSubmit(e));

        document
            .querySelectorAll('[data-act="back"], [data-act="list"]')
            .forEach((btn) => {
                btn.addEventListener("click", () => this.goList());
            });

        this.setupDateValidation();
        this.setupAddressSearch();
    }

    setupDateValidation() {
        const startDateInput = document.getElementById("startDate");
        const endDateInput = document.getElementById("endDate");

        startDateInput?.addEventListener("change", (e) => {
            if (e.target.value) {
                endDateInput.min = e.target.value;
                if (endDateInput.value && endDateInput.value < e.target.value) {
                    endDateInput.value = e.target.value;
                }
            }
        });

        const today = new Date().toISOString().split("T")[0];
        if (startDateInput) startDateInput.min = today;
    }

    setupAddressSearch() {
        const btn = document.getElementById("btn-search-address");
        btn?.addEventListener("click", function () {
            new daum.Postcode({
                oncomplete: function (data) {
                    document.getElementById("roadAddress").value = data.roadAddress;
                    document.getElementById("jibunAddress").value = data.jibunAddress;
                    document.getElementById("detailAddress")?.focus();
                },
            }).open();
        });
    }

    async handleSubmit(e) {
        e.preventDefault();

        const submitBtn =
            e.submitter || document.querySelector('button[type="submit"]');
        if (submitBtn) submitBtn.disabled = true;

        try {
            if (!this.validateForm()) return;

            const fd = new FormData();
            const v = (id) => document.getElementById(id)?.value?.trim() ?? "";

            const roadAddress = v("roadAddress");
            if (!roadAddress) {
                alert("주소 검색을 통해 주소를 입력해주세요.");
                return;
            }

            fd.append("roadAddress", roadAddress);
            fd.append("jibunAddress", v("jibunAddress"));
            fd.append("detailAddress", v("detailAddress"));

            fd.append("title", v("title"));
            fd.append("description", v("description"));
            fd.append("areaSize", v("areaSize"));
            fd.append("startDate", v("startDate"));
            fd.append("endDate", v("endDate"));
            fd.append("rentalFee", v("rentalFee"));
            fd.append("contactPhone", v("contactPhone"));

            const img = document.getElementById("image")?.files?.[0];
            if (img) fd.append("image", img);

            await apiService.createSpace(fd);
            alert("공간이 성공적으로 등록되었습니다.");
            this.goList();
        } catch (err) {
            console.error("공간 등록 실패:", err);
            this.handleError(err);
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    }

    validateForm() {
        const requiredFields = [
            { id: "roadAddress", name: "주소" },
            { id: "title", name: "제목" },
            { id: "areaSize", name: "면적" },
            { id: "startDate", name: "임대 시작일" },
            { id: "endDate", name: "임대 종료일" },
            { id: "rentalFee", name: "임대료" },
            { id: "contactPhone", name: "연락처" },
        ];

        for (const field of requiredFields) {
            const el = document.getElementById(field.id);
            if (!el?.value?.trim()) {
                alert(`${field.name}을(를) 입력해주세요.`);
                el?.focus();
                return false;
            }
        }

        const startDate = document.getElementById("startDate").value;
        const endDate = document.getElementById("endDate").value;
        if (startDate && endDate && endDate < startDate) {
            alert("종료일은 시작일 이후여야 합니다.");
            document.getElementById("endDate").focus();
            return false;
        }

        const phone = document.getElementById("contactPhone").value.trim();
        const phoneRegex = /^[0-9-+()\s]+$/;
        if (phone && !phoneRegex.test(phone)) {
            alert("올바른 전화번호 형식이 아닙니다.");
            document.getElementById("contactPhone").focus();
            return false;
        }

        return true;
    }

    handleError(err) {
        const msg = String(err?.message || "");

        if (msg.includes("401")) {
            alert("로그인이 필요합니다.");
        } else if (msg.includes("400") || msg.includes("422")) {
            if (err.response?.data?.errors) {
                const errors = err.response.data.errors;
                const errorMsg = Object.values(errors).join("\n");
                alert("입력 정보를 확인해주세요:\n" + errorMsg);
            } else {
                alert("입력 정보를 확인해주세요.");
            }
        } else {
            alert("등록 중 오류가 발생했습니다. 다시 시도해주세요.");
        }
    }

    goList() {
        Pages.spaceList();
    }
}

window.SpaceRegisterManager = SpaceRegisterManager;
