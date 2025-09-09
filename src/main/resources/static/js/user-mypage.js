document.addEventListener('DOMContentLoaded', async function () {
    await loadComponents();   // header/footer 로드
    initializeLayout();

    try {
        // 로그인된 사용자 정보 가져오기
        const user = await apiService.getCurrentUser();

        // DOM에 값 바인딩 (안전)
        const setText = (id, val) => {
          const el = document.getElementById(id);
          if (el) el.textContent = val ?? '-';
        };
        setText('user-name', user.name);
        setText('user-nickname', user.nickname);
        setText('user-email', user.email);
        setText('user-phone', user.phone);
        // 수정 버튼 이벤트 연결
        document.querySelectorAll('.edit-btn').forEach((btn, idx) => {
            btn.addEventListener('click', () => {
                let field, label, spanEl;

                switch (idx) {
                    case 0:
                        field = 'name';
                        label = '이름';
                        spanEl = document.getElementById('user-name');
                        break;
                    case 1:
                        field = 'nickname';
                        label = '닉네임';
                        spanEl = document.getElementById('user-nickname');
                        break;
                    case 2:
                        alert('아이디(이메일)는 수정할 수 없습니다.');
                        return;
                    case 3:
                        field = 'phone';
                        label = '연락처';
                        spanEl = document.getElementById('user-phone');
                        break;
                }

                if (!spanEl) return;

                const currentValue = spanEl.textContent;
                const input = document.createElement('input');
                input.type = 'text';
                input.value = currentValue;
                input.className = 'inline-edit';

                // 버튼 교체
                const saveBtn = document.createElement('button');
                saveBtn.textContent = '저장';
                saveBtn.className = 'save-btn';

                const cancelBtn = document.createElement('button');
                cancelBtn.textContent = '취소';
                cancelBtn.className = 'cancel-btn';

                // 기존 span 교체
                spanEl.replaceWith(input);
                btn.replaceWith(saveBtn);
                saveBtn.after(cancelBtn);

                // 취소 이벤트
                cancelBtn.addEventListener('click', () => {
                    input.replaceWith(spanEl);
                    saveBtn.replaceWith(btn);
                    cancelBtn.remove();
                });

                // 저장 이벤트
                saveBtn.addEventListener('click', async () => {
                    const newValue = input.value.trim();
                    if (!newValue || newValue === currentValue) {
                        cancelBtn.click(); // 변경 없으면 취소 처리
                        return;
                    }

                    try {
                        const updatedUser = await apiService.put('/users/me', {
                            name: field === 'name' ? newValue : user.name,
                            nickname: field === 'nickname' ? newValue : user.nickname,
                            phone: field === 'phone' ? newValue : user.phone
                        });

                        // user 객체 갱신
                        user.name = updatedUser.name;
                        user.nickname = updatedUser.nickname;
                        user.phone = updatedUser.phone;

                        // 화면 갱신
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
        // 관심 카테고리 예시 데이터 UI
        // =============================
        const categories = [
            { id: 1, name: "패션", active: true },
            { id: 2, name: "반려동물", active: true },
            { id: 3, name: "게임", active: false },
            { id: 4, name: "캐릭터/IP", active: true },
            { id: 5, name: "문화/콘텐츠", active: true },
            { id: 6, name: "연예", active: false },
            { id: 7, name: "여행/레저/스포츠", active: true },
        ];

        const container = document.getElementById('category-container');
        if (container) {
            categories.forEach(cat => {
                const btn = document.createElement('button');
                btn.textContent = cat.name;
                btn.className = 'category-btn';
                if (cat.active) btn.classList.add('active');

                // 클릭 시 토글 (백엔드 연동되면 API 호출 자리)
                btn.addEventListener('click', () => {
                    btn.classList.toggle('active');
                    cat.active = !cat.active;
                });

                container.appendChild(btn);
            });
        }

    } catch (err) {
        console.error(err);
        const mc = document.getElementById('main-content') || document.querySelector('.main-content');
        if (mc) mc.innerHTML = `
            <p style="color:red; text-align:center;">로그인 후 이용 가능합니다.</p>
        `;
    }
});
