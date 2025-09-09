document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("popup-register-form");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const payload = {
            title: form.title.value,
            summary: form.summary.value,
            description: form.description.value,
            startDate: form.startDate.value,
            endDate: form.endDate.value,
            entryFee: parseInt(form.entryFee.value) || 0,
            reservationAvailable: form.reservationAvailable.checked,
            reservationLink: form.reservationLink.value,
            waitlistAvailable: form.waitlistAvailable.checked,
            notice: form.notice.value,
            mainImageUrl: form.mainImageUrl.value,
            isFeatured: form.isFeatured.checked
        };

        try {
            console.log("보낼 payload:", payload);
            const result = await apiService.post("/hosts/popups", payload);
            console.log("등록 성공:", result);
            alert("팝업이 등록되었습니다!");
            window.location.href = "/pages/mpg-host.html";
        } catch (error) {
            console.error("팝업 등록 실패:", error);
            alert("팝업 등록 실패: " + (error.message || "알 수 없는 오류"));
        }
    });
});
