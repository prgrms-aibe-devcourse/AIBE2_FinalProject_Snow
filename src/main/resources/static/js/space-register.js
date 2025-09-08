// 공간 등록 페이지
const SpaceRegisterPage = {
    init() {
        const form = document.getElementById('spaceForm') || document.querySelector('form');
        if (form) form.addEventListener('submit', (e) => this.handleSubmit(e));

        // 뒤로/목록 이동 버튼
        document.querySelectorAll('[data-act="back"], [data-act="list"]')
            .forEach(btn => btn.addEventListener('click', () => this.goList()));
    },

    async handleSubmit(e) {
        e.preventDefault();

        const submitBtn = e.submitter || document.querySelector('button[type="submit"]');
        if (submitBtn) submitBtn.disabled = true;

        try {
            // FormData 구성
            const fd = new FormData();
            const v = id => document.getElementById(id)?.value ?? '';
            fd.append('title',        v('title'));
            fd.append('address',      v('address'));
            fd.append('areaSize',     v('areaSize'));
            fd.append('startDate',    v('startDate'));
            fd.append('endDate',      v('endDate'));
            fd.append('rentalFee',    v('rentalFee'));
            fd.append('contactPhone', v('contactPhone'));
            fd.append('description',  v('description'));
            const img = document.getElementById('imageFile')?.files?.[0];
            if (img) fd.append('image', img);

            await apiService.createSpace(fd);
            alert('공간이 성공적으로 등록되었습니다.');
            this.goList(); // 성공 시 목록으로 이동

            // 만약 상세로 보내고 싶으면:
            // const res = await apiService.createSpace(fd);
            // if (res?.id) location.assign(`/templates/pages/space-detail.html?id=${encodeURIComponent(res.id)}`);
            // else this.goList();

        } catch (err) {
            console.error('공간 등록 실패:', err);
            const msg = String(err?.message || '');
            if (msg.includes('401')) alert('로그인이 필요합니다.');
            else if (msg.includes('400') || msg.includes('422')) alert('입력 정보를 확인해주세요.');
            else alert('등록 중 오류가 발생했습니다. 다시 시도해주세요.');
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    },

    goList() {
        location.assign('/templates/pages/space-list.html');
    }
};

window.SpaceRegisterPage = SpaceRegisterPage;
