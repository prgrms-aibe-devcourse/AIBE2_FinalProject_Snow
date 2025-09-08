document.addEventListener('DOMContentLoaded', async function () {
    await loadComponents();   // header/footer 로드
    initializeLayout();

    try {
        // 로그인된 사용자 정보 가져오기
        const user = await apiService.getCurrentUser();

        // DOM에 값 바인딩
        document.getElementById('user-name').textContent = user.name || '-';
        document.getElementById('user-nickname').textContent = user.nickname || '-';
        document.getElementById('user-email').textContent = user.email || '-';
        document.getElementById('user-phone').textContent = user.phone || '-';

        // 수정 버튼 이벤트 (지금은 단순 alert, 이후 모달 연결 가능)
        document.querySelectorAll('.edit-btn').forEach((btn, idx) => {
            btn.addEventListener('click', () => {
                switch (idx) {
                    case 0: alert('이름 수정 기능 연결 예정'); break;
                    case 1: alert('닉네임 수정 기능 연결 예정'); break;
                    case 2: alert('아이디(이메일) 수정은 불가'); break;
                    case 3: alert('연락처 수정 기능 연결 예정'); break;
                }
            });
        });

    } catch (err) {
        console.error(err);
        document.getElementById('main-content').innerHTML = `
            <p style="color:red; text-align:center;">로그인 후 이용 가능합니다.</p>
        `;
    }
});
