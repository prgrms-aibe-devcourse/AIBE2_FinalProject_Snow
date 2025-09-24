document.addEventListener('DOMContentLoaded', async () => {

    await loadComponents();
    initializeLayout();

    const pathParts = window.location.pathname.split("/");
    const popupId = pathParts[pathParts.indexOf("popup") + 1];

    if (!popupId) {
        alert("잘못된 접근입니다.");
        history.back();
        return;
    }

    const form = document.getElementById('popup-edit-form');
    const selectedTags = new Set();

    //  태그 버튼 토글
    document.querySelectorAll(".tag-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            const tagId = parseInt(btn.dataset.id, 10);
            if (selectedTags.has(tagId)) {
                selectedTags.delete(tagId);
                btn.classList.remove("selected");
            } else {
                selectedTags.add(tagId);
                btn.classList.add("selected");
            }
        });
    });

    try {
        const popup = await apiService.get(`/hosts/popups/${popupId}`);

        form.title.value = popup.title || "";
        form.summary.value = popup.summary || "";
        form.description.value = popup.description || "";
        form.startDate.value = popup.startDate || "";
        form.endDate.value = popup.endDate || "";
        form.entryFee.value = popup.entryFee ?? 0;
        form.reservationAvailable.checked = !!popup.reservationAvailable;
        form.reservationLink.value = popup.reservationLink || "";
        form.waitlistAvailable.checked = !!popup.waitlistAvailable;
        form.notice.value = popup.notice || "";
        form.mainImageUrl.value = popup.mainImageUrl || "";
        form.isFeatured.checked = !!popup.isFeatured;

        //  카테고리 세팅
        if (popup.categoryId) {
            form.categoryId.value = popup.categoryId;
        }

        //  태그 세팅
        if (popup.tagIds && popup.tagIds.length > 0) {
            popup.tagIds.forEach(id => {
                selectedTags.add(id);
                const btn = document.querySelector(`.tag-btn[data-id='${id}']`);
                if (btn) btn.classList.add("selected");
            });
        }

    } catch (err) {
        console.error('팝업 상세 불러오기 실패:', err);
        alert('데이터 로딩 실패');
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const payload = {
            title: form.title.value?.trim() || "",
            summary: form.summary.value?.trim() || "",
            description: form.description.value?.trim() || "",
            startDate: form.startDate.value || "",
            endDate: form.endDate.value || "",
            entryFee: parseInt(form.entryFee.value) || 0,
            reservationAvailable: !!form.reservationAvailable.checked,
            reservationLink: form.reservationLink.value?.trim() || "",
            waitlistAvailable: !!form.waitlistAvailable.checked,
            notice: form.notice.value?.trim() || "",
            mainImageUrl: form.mainImageUrl.value?.trim() || "",
            isFeatured: !!form.isFeatured.checked,
            categoryId: parseInt(form.categoryId.value) || null,
            tagIds: Array.from(selectedTags)
        };

        //  undefined 제거
        Object.keys(payload).forEach(key => {
            if (payload[key] === undefined) {
                delete payload[key];
            }
        });

        try {
            await apiService.put(`/hosts/popups/${popupId}`, payload);
            alert('팝업이 수정되었습니다.');
            window.location.href = `/mypage/host/popup/${popupId}`;
        } catch (err) {
            console.error('팝업 수정 실패:', err);
            alert('수정 실패');
        }
    });
});
