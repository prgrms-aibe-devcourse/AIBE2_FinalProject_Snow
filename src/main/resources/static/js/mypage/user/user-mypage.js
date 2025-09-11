document.addEventListener('DOMContentLoaded', async function () {
    await loadComponents();   // header/footer 로드
    initializeLayout();

    try {
        // =============================
        // 사용자 정보 불러오기
        // =============================
        const user = await apiService.getCurrentUser();

        const setText = (id, val) => {
            const el = document.getElementById(id);
            if (el) el.textContent = val ?? '-';
        };
        setText('user-name', user.name);
        setText('user-nickname', user.nickname);
        setText('user-email', user.email);
        setText('user-phone', user.phone);

        // =============================
        // 사용자 정보 수정 버튼 이벤트
        // =============================
        document.querySelectorAll('.edit-btn').forEach((btn, idx) => {
            btn.addEventListener('click', () => {
                let field, label, spanEl;
                switch (idx) {
                    case 0: field = 'name'; label = '이름'; spanEl = document.getElementById('user-name'); break;
                    case 1: field = 'nickname'; label = '닉네임'; spanEl = document.getElementById('user-nickname'); break;
                    case 2: alert('아이디(이메일)는 수정할 수 없습니다.'); return;
                    case 3: field = 'phone'; label = '연락처'; spanEl = document.getElementById('user-phone'); break;
                }
                if (!spanEl) return;

                const currentValue = spanEl.textContent;
                const input = document.createElement('input');
                input.type = 'text';
                input.value = currentValue;
                input.className = 'inline-edit';

                const saveBtn = document.createElement('button');
                saveBtn.textContent = '저장';
                saveBtn.className = 'save-btn';

                const cancelBtn = document.createElement('button');
                cancelBtn.textContent = '취소';
                cancelBtn.className = 'cancel-btn';

                spanEl.replaceWith(input);
                btn.replaceWith(saveBtn);
                saveBtn.after(cancelBtn);

                cancelBtn.addEventListener('click', () => {
                    input.replaceWith(spanEl);
                    saveBtn.replaceWith(btn);
                    cancelBtn.remove();
                });

                saveBtn.addEventListener('click', async () => {
                    const newValue = input.value.trim();
                    if (!newValue || newValue === currentValue) {
                        cancelBtn.click();
                        return;
                    }

                    try {
                        const updatedUser = await apiService.put('/users/me', {
                            name: field === 'name' ? newValue : user.name,
                            nickname: field === 'nickname' ? newValue : user.nickname,
                            phone: field === 'phone' ? newValue : user.phone
                        });

                        user.name = updatedUser.name;
                        user.nickname = updatedUser.nickname;
                        user.phone = updatedUser.phone;

                        spanEl.textContent = updatedUser[field] || '-';
                        input.replaceWith(spanEl);
                        saveBtn.replaceWith(btn);
                        cancelBtn.remove();

                        alert(`${label}이(가) 수정되었습니다.`);
                    } catch (err) {
                        console.error(err);
                        alert(`${label} 수정 실패: ${err.message || err}`);
                    }
                });
            });
        });

        // =============================
        // 관심 카테고리 (API 연동)
        // =============================
        const container = document.getElementById('category-container');
        if (container) {
            try {
                // 전체 카테고리 가져오기
                const allCategories = await apiService.get('/categories');
                // 사용자 관심 카테고리 가져오기
                const myCategories = await apiService.get('/categories/me');
                const myIds = new Set(myCategories.map(c => c.id));

                allCategories.forEach(cat => {
                    const btn = document.createElement('button');
                    btn.textContent = cat.name;
                    btn.className = 'category-btn';
                    if (myIds.has(cat.id)) btn.classList.add('active');

                    btn.addEventListener('click', async () => {
                        btn.classList.toggle('active');
                        try {
                            // 서버 업데이트
                            const selected = Array.from(container.querySelectorAll('.category-btn.active'))
                                .map(b => b.textContent);
                            await apiService.put('/categories/me', selected);
                        } catch (err) {
                            console.error(err);
                            alert('카테고리 업데이트 실패');
                        }
                    });

                    container.appendChild(btn);
                });
            } catch (e) {
                console.error('카테고리 로드 실패:', e);
                container.innerHTML = `<p style="color:#777;">카테고리를 불러올 수 없습니다.</p>`;
            }
        }

    } catch (err) {
        console.error(err);
        const mc = document.getElementById('main-content') || document.querySelector('.main-content');
        if (mc) mc.innerHTML = `
            <p style="color:red; text-align:center;">로그인 후 이용 가능합니다.</p>
        `;
    }
});

