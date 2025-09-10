document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);
    const popupId = params.get('id');

    if (!popupId) {
        alert('잘못된 접근입니다.');
        history.back();
        return;
    }

    const form = document.getElementById('popup-edit-form');

    // 기존 데이터 불러오기
    try {
        const popup = await apiService.get(`/hosts/popups/${popupId}`);
        form.title.value = popup.title;
        form.summary.value = popup.summary;
        form.description.value = popup.description;
        form.startDate.value = popup.startDate;
        form.endDate.value = popup.endDate;
        form.entryFee.value = popup.entryFee;
        form.reservationAvailable.checked = popup.reservationAvailable;
        form.reservationLink.value = popup.reservationLink;
        form.waitlistAvailable.checked = popup.waitlistAvailable;
        form.notice.value = popup.notice;
        form.mainImageUrl.value = popup.mainImageUrl;
        form.isFeatured.checked = popup.isFeatured;
    } catch (err) {
        console.error('팝업 상세 불러오기 실패:', err);
        alert('데이터 로딩 실패');
    }

    // 수정 완료
    form.addEventListener('submit', async (e) => {
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
            await apiService.put(`/hosts/popups/${popupId}`, payload);
            alert('팝업이 수정되었습니다.');
            window.location.href = '/templates/pages/mpg-host.html';
        } catch (err) {
            console.error('팝업 수정 실패:', err);
            alert('수정 실패');
        }
    });
});
