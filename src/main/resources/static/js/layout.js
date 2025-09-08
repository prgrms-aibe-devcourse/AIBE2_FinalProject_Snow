// 공통 레이아웃 스크립트 (템플릿 로더 방식)

// HTML 컴포넌트 로드 함수
async function loadComponents() {
    try {
        // 헤더 로드 - 경로 변경
        const headerHTML = await TemplateLoader.load('components/header');
        document.getElementById('header-container').innerHTML = headerHTML;

        // 푸터 로드 - 경로 변경
        document.getElementById('footer-container').innerHTML = createFooterByRole();

        // 컴포넌트 로드 완료 후 이벤트 설정
        setupComponentEvents();

    } catch (error) {
        console.error('컴포넌트 로드 실패:', error);
        createFallbackLayout();
    }
}

// 컴포넌트 이벤트 설정
function setupComponentEvents() {
    // 푸터 네비게이션 이벤트
    setupFooterNavigation();
}

// 푸터 네비게이션 이벤트 설정
function setupFooterNavigation() {
    const footerItems = document.querySelectorAll('.footer-item');
    footerItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();

            const page = this.getAttribute('data-page');

            // 모든 탭에서 active 클래스 제거
            footerItems.forEach(tab => tab.classList.remove('active'));

            // 현재 탭에 active 클래스 추가
            this.classList.add('active');

            // 페이지 로드 (pages.js 사용)
            if (typeof Pages !== 'undefined' && Pages[page]) {
                Pages[page]();
            } else {
                console.error(`Page handler not found: ${page}`);
            }
        });
    });
}

// 컴포넌트 로드 실패 시 기본 레이아웃 생성
function createFallbackLayout() {
    // 기본 헤더 생성
    document.getElementById('header-container').innerHTML = `
        <header class="header">
            <div class="header-left"></div>
            <div class="logo">
                <div class="logo-bubble">
                    <span class="logo-text">POPIN</span>
                </div>
            </div>
            <div class="header-right">
                <div class="icon-btn" onclick="showNotifications()">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
                        <path d="m13.73 21a2 2 0 0 1-3.46 0"></path>
                    </svg>
                </div>
                <div class="icon-btn" onclick="goToProfile()">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                        <circle cx="12" cy="7" r="4"></circle>
                    </svg>
                </div>
            </div>
        </header>
    `;

    // 동적 푸터 생성
    document.getElementById('footer-container').innerHTML = createFooterByRole();

    // 폴백 이벤트 설정
    setupComponentEvents();
}

// 레이아웃 초기화
function initializeLayout() {
    // 스크롤 위치 초기화
    const mainContent = document.querySelector('.main-content');
    if (mainContent) {
        mainContent.scrollTop = 0;
    }
}

// 헤더 아이콘 클릭 이벤트
function showNotifications() {
    console.log('알림 클릭');
    // 알림 템플릿 로드 예정
}

function goToProfile() {
    console.log('프로필 클릭');
    // 프로필 템플릿 로드 예정
}

// 알림 메시지 표시
function showAlert(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;

    const mainContent = document.querySelector('.main-content');
    if (mainContent) {
        mainContent.insertBefore(alertDiv, mainContent.firstChild);

        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.parentNode.removeChild(alertDiv);
            }
        }, 3000);
    }
}
// 역할에 따른 푸터 생성 함수
function createFooterByRole() {
    const userRole = getUserRole();

    let navItems = `
        <a href="#" class="footer-item active" data-page="home">
            <svg class="footer-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                <polyline points="9,22 9,12 15,12 15,22"></polyline>
            </svg>
            <span class="footer-text">홈</span>
        </a>
        <a href="#" class="footer-item" data-page="search">
            <svg class="footer-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
            </svg>
            <span class="footer-text">검색</span>
        </a>
        <a href="#" class="footer-item" data-page="bookmark">
            <svg class="footer-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M17 3H7a2 2 0 0 0-2 2v16l7-3 7 3V5a2 2 0 0 0-2-2z"></path>
            </svg>
            <span class="footer-text">북마크</span>
        </a>`;

    // PROVIDER나 HOST일 경우 공간대여 메뉴 추가
    if (userRole === 'PROVIDER' || userRole === 'HOST') {
        navItems += `
        <a href="#" class="footer-item" data-page="spaceList">
            <svg class="footer-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
            </svg>
            <span class="footer-text">공간대여</span>
        </a>`;
    }

    navItems += `
        <a href="#" class="footer-item" data-page="map">
            <svg class="footer-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
                <circle cx="12" cy="10" r="3"></circle>
            </svg>
            <span class="footer-text">지도</span>
        </a>`;

    return `
        <footer class="footer">
            ${navItems}
        </footer>
    `;
}

// 사용자 역할 가져오기 함수 (테스트용으로 쓰고 나중엔 실제로 가져오게)
function getUserRole() {

    return 'USER'; // ROLE을 입력
}
