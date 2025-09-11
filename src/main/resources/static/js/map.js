// 지도 페이지 전용 모듈 (컴팩트 버전)
class MapPageManager {
    constructor() {
        this.map = null;
        this.markers = [];
        this.overlays = [];
        this.popups = [];
        this.userLocation = null;
        this.currentLocationMarker = null;
    }

    // 페이지 초기화
    async initialize() {
        try {
            await this.renderHTML();
            await this.wait(100);
            await this.initializeMap();
            this.setupEventListeners();

            // 페이지 로드 시 자동으로 현재 위치 찾기
            await this.findMyLocation();

            await this.loadMapPopups();
        } catch (error) {
            console.error('지도 페이지 초기화 실패:', error);
        }
    }

    // HTML 렌더링
    async renderHTML() {
        try {
            const html = await TemplateLoader.load('pages/map');
            document.getElementById('main-content').innerHTML = html;
        } catch (error) {
            document.getElementById('main-content').innerHTML = `
                <div class="map-container">
                    <div id="kakao-map" class="kakao-map"></div>
                </div>
                <div class="nearby-section">
                    <h2 class="section-title">내 근처 팝업</h2>
                    <div class="category-filters" id="category-filters">
                        <button class="category-btn active" data-category="">전체</button>
                        <button class="category-btn" data-category="패션">패션</button>
                        <button class="category-btn" data-category="반려동물">반려동물</button>
                        <button class="category-btn" data-category="게임">게임</button>
                        <button class="category-btn" data-category="캐릭터/IP">캐릭터/IP</button>
                        <button class="category-btn" data-category="문화/콘텐츠">문화/콘텐츠</button>
                        <button class="category-btn" data-category="연예">연예</button>
                        <button class="category-btn" data-category="여행/레저/스포츠">여행/레저/스포츠</button>
                    </div>
                    <div class="popup-list" id="popup-list"></div>
                </div>
            `;
        }
    }

    // 카카오맵 초기화
    async initializeMap() {
        const container = document.getElementById('kakao-map');
        if (!container || !window.kakao || !window.kakao.maps) return;

        const options = {
            center: new window.kakao.maps.LatLng(37.5665, 126.9780),
            level: 5
        };

        this.map = new window.kakao.maps.Map(container, options);

        // 현재 위치 버튼만 추가
        this.addLocationButton();

        // 지도 이벤트
        window.kakao.maps.event.addListener(this.map, 'idle', () => {
            this.loadMapPopupsInBounds();
        });
    }

    // 현재 위치 버튼 (원형, 오른쪽 하단)
    addLocationButton() {
        const mapContainer = document.getElementById('kakao-map');

        const btn = document.createElement('button');
        btn.innerHTML = `
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="1.5"/>
                <circle cx="12" cy="12" r="3" fill="currentColor"/>
                <path d="M12 2v4M12 18v4M2 12h4M18 12h4" stroke="currentColor" stroke-width="1.5"/>
            </svg>
        `;

        btn.style.cssText = `
            position: absolute;
            bottom: 20px;
            right: 8px;
            width: 36px;
            height: 36px;
            border-radius: 50%;
            background: white;
            border: 1px solid #ddd;
            box-shadow: 0 2px 8px rgba(0,0,0,0.2);
            cursor: pointer;
            z-index: 1000;
            display: flex;
            align-items: center;
            justify-content: center;
            color: #333;
        `;

        btn.onclick = () => this.findMyLocation();
        mapContainer.appendChild(btn);
    }

    // 현재 위치 찾기
    async findMyLocation() {
        if (!navigator.geolocation) return;

        navigator.geolocation.getCurrentPosition(
            (position) => {
                this.userLocation = {
                    lat: position.coords.latitude,
                    lng: position.coords.longitude
                };

                const center = new window.kakao.maps.LatLng(
                    this.userLocation.lat,
                    this.userLocation.lng
                );

                this.map.setCenter(center);
                this.map.setLevel(4);
                this.showCurrentLocationMarker();
            },
            () => console.log('위치 정보를 가져올 수 없습니다.')
        );
    }

    // 현재 위치 마커 표시
    showCurrentLocationMarker() {
        if (!this.userLocation) return;

        if (this.currentLocationMarker) {
            this.currentLocationMarker.setMap(null);
        }

        const position = new window.kakao.maps.LatLng(
            this.userLocation.lat,
            this.userLocation.lng
        );

        const svgString = '<svg width="24" height="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><circle cx="12" cy="12" r="11" fill="#FF4444" stroke="white" stroke-width="2"/><circle cx="12" cy="12" r="4" fill="white"/></svg>';

        const markerImage = new window.kakao.maps.MarkerImage(
            'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svgString),
            new window.kakao.maps.Size(24, 24),
            { offset: new window.kakao.maps.Point(12, 12) }
        );

        this.currentLocationMarker = new window.kakao.maps.Marker({
            position: position,
            image: markerImage
        });

        this.currentLocationMarker.setMap(this.map);
    }

    // 이벤트 리스너
    setupEventListeners() {
        const categoryFilters = document.getElementById('category-filters');
        if (categoryFilters) {
            categoryFilters.addEventListener('click', (e) => {
                if (e.target.classList.contains('category-btn')) {
                    this.handleCategoryFilter(e.target);
                }
            });
        }
    }

    // 지도 팝업 로드
    async loadMapPopups() {
        try {
            const popups = await apiService.getMapPopups();
            this.popups = popups || [];
            this.renderMapMarkers(this.popups);
            this.renderBottomSheetList(this.popups);
        } catch (error) {
            console.error('팝업 로드 실패:', error);
        }
    }

    async loadMapPopupsInBounds() {
        if (!this.map) return;

        try {
            const bounds = this.map.getBounds();
            const sw = bounds.getSouthWest();
            const ne = bounds.getNorthEast();

            const popups = await apiService.getPopupsInBounds(
                sw.getLat(), sw.getLng(), ne.getLat(), ne.getLng()
            );

            this.popups = popups || [];
            this.renderMapMarkers(this.popups);
            this.renderBottomSheetList(this.popups);
        } catch (error) {
            console.error('범위 내 팝업 로드 실패:', error);
        }
    }

    // 지도 마커 렌더링
    renderMapMarkers(popups) {
        if (!this.map) return;

        this.markers.forEach(marker => marker.setMap(null));
        this.markers = [];

        popups.forEach(popup => {
            if (!popup.latitude || !popup.longitude) return;

            const position = new window.kakao.maps.LatLng(popup.latitude, popup.longitude);
            const marker = new window.kakao.maps.Marker({ position, title: popup.title });

            marker.setMap(this.map);
            this.markers.push(marker);
        });
    }

    // 하단 팝업 리스트
    renderBottomSheetList(popups) {
        const popupList = document.getElementById('popup-list');
        if (!popupList) return;

        if (!popups || popups.length === 0) {
            popupList.innerHTML = '<div class="empty-state">주변에 팝업이 없습니다</div>';
            return;
        }

        const cardsHTML = popups.map(popup => `
            <div class="popup-card-horizontal" onclick="goToPopupDetail('${popup.id}')">
                <div class="popup-image-wrapper">
                    <img src="${popup.mainImageUrl || 'https://via.placeholder.com/80x80/667eea/ffffff?text=🎪'}" 
                         alt="${popup.title}" class="popup-image">
                </div>
                <div class="popup-info">
                    <div class="popup-title">${popup.title}</div>
                    <div class="popup-period">${popup.period || '기간 미정'}</div>
                    <div class="popup-location">${popup.venueName || '장소 미정'}</div>
                </div>
            </div>
        `).join('');

        popupList.innerHTML = cardsHTML;
    }

    // 카테고리 필터
    handleCategoryFilter(button) {
        document.querySelectorAll('.category-btn').forEach(btn => btn.classList.remove('active'));
        button.classList.add('active');

        const selectedCategory = button.dataset.category || '';
        let filteredPopups = [...this.popups];

        if (selectedCategory) {
            filteredPopups = filteredPopups.filter(popup => popup.categoryName === selectedCategory);
        }

        this.renderBottomSheetList(filteredPopups);
    }

    wait(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    cleanup() {
        this.markers.forEach(marker => marker.setMap(null));
        this.markers = [];

        if (this.currentLocationMarker) {
            this.currentLocationMarker.setMap(null);
            this.currentLocationMarker = null;
        }
    }
}

window.MapPageManager = MapPageManager;