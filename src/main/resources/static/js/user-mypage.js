document.addEventListener('DOMContentLoaded', async function () {
    await loadComponents();   // header/footer 로드
    initializeLayout();

    try {
        // 로그인된 사용자 정보 가져오기
        const response = await fetch('/api/users/me');
        if (!response.ok) throw new Error('사용자 정보를 불러오지 못했습니다.');

        const user = await response.json();

        document.getElementById('user-name').textContent = user.name;
        document.getElementById('user-nickname').textContent = user.nickname;
        document.getElementById('user-email').textContent = user.email;
        document.getElementById('user-phone').textContent = user.phone;

    } catch (error) {
        document.getElementById('main-content').textContent = error.message;
    }
});
