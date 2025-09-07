// 공간 수정 페이지 (MPA)
const SpaceEditPage = {
    async init() {
        this.id = new URLSearchParams(location.search).get('id');
        if (!this.id) {
            alert('잘못된 접근입니다.');
            location.href = '/templates/pages/space-list/index.html';
            return;
        }

        // 폼 이벤트
        const form = document.getElementById('spaceForm') || document.querySelector('form');
        if (form) form.addEventListener('submit', (e) => this.handleSubmit(e));

        // 초기 데이터 로드
        try {
            const data = await apiService.getSpace(this.id);
            this.fillForm(data);
        } catch (err) {
            console.error('상세 조회 실패:', err);
            alert('데이터를 불러오지 못했습니다.');
        }

        // 취소/목록/상세 이동 버튼 연결
        document.querySelectorAll('[data-act="cancel"]').forEach(b => b.addEventListener('click', () => this.goDetail()));
        document.querySelectorAll('[data-act="list"]').forEach(b => b.addEventListener('click', () => this.goList()));
    },

    fillForm(space) {
        const set = (id, v) => { const el = document.getElementById(id); if (el) el.value = v ?? ''; };
        set('title', space.title);
        set('address', space.address);
        set('areaSize', space.areaSize);
        set('startDate', space.startDate?.slice(0, 10));
        set('endDate', space.endDate?.slice(0, 10));
        set('rentalFee', space.rentalFee);
        set('contactPhone', space.contactPhone);
        const desc = document.getElementById('description');
        if (desc) desc.value = space.description ?? '';
        const preview = document.getElementById('imagePreview');
        if (preview) preview.src = this.getImageUrl(space.coverImageUrl);
    },

    getImageUrl(u) {
        if (!u) return '/images/noimage.png';
        if (u.startsWith('http')) return u;
        if (u.startsWith('/')) return u;
        return `/uploads/${u}`;
    },

    async handleSubmit(e) {
        e.preventDefault();
        const submitBtn = (e.submitter) || document.querySelector('button[type="submit"]');
        if (submitBtn) submitBtn.disabled = true;

        try {
            const fd = new FormData();
            const get = id => document.getElementById(id)?.value ?? '';

            fd.append('title', get('title'));
            fd.append('address', get('address'));
            fd.append('areaSize', get('areaSize'));
            fd.append('startDate', get('startDate'));
            fd.append('endDate', get('endDate'));
            fd.append('rentalFee', get('rentalFee'));
            fd.append('contactPhone', get('contactPhone'));
            fd.append('description', get('description'));

            const imageFile = document.getElementById('imageFile')?.files?.[0];
            if (imageFile) fd.append('image', imageFile);

            await apiService.updateSpace(this.id, fd);

            //  성공 시 alert 없이 바로 리스트로 이동
            this.goList();

        } catch (err) {
            console.error('수정 실패:', err);
            const msg = String(err?.message || '');
            if (msg.includes('401')) alert('로그인이 필요합니다.');
            else if (msg.includes('400') || msg.includes('422')) alert('입력 정보를 확인해주세요.');
            else alert('수정 중 오류가 발생했습니다. 다시 시도해주세요.');
        } finally {
            if (submitBtn) submitBtn.disabled = false;
        }
    },

    goList() {
        location.href = '/templates/pages/space-list.html';
    },
    goDetail() {
        location.href = `/templates/pages/space-detail.html?id=${encodeURIComponent(this.id)}`;
    }
};

window.SpaceEditPage = SpaceEditPage;
