// 이미지 없을 때 인라인 플레이스홀더
const IMG_PLACEHOLDER =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent(
        `<svg xmlns="http://www.w3.org/2000/svg" width="160" height="120">
       <rect width="100%" height="100%" fill="#f2f2f2"/>
       <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle"
             fill="#888" font-size="14">no image</text>
     </svg>`
    );

// 공간 상세 페이지 (MPA)
const SpaceDetailPage = {
    async init() {
        const id = new URLSearchParams(location.search).get('id');
        if (!id) {
            alert('잘못된 접근입니다.');
            this.goList();
            return;
        }

        try {
            this.showLoading();
            const space = await apiService.getSpace(id);
            this.render(space);
            this.bindActions(space);
        } catch (err) {
            console.error('상세 불러오기 실패:', err);
            this.showError('상세 정보를 불러오지 못했습니다.');
        } finally {
            this.hideLoading();
        }
    },

    showLoading() {
        const el = document.getElementById('loading');
        const cont = document.getElementById('detailContent');
        if (el) el.style.display = 'block';
        if (cont) cont.style.display = 'none';
    },
    hideLoading() {
        const el = document.getElementById('loading');
        if (el) el.style.display = 'none';
    },

    render(space) {
        const $ = (id) => document.getElementById(id);
        const img = $('heroImage');

        if (img) {
            img.src = this.getThumbUrl(space);
            img.onerror = function () {
                this.onerror = null;       // 무한 루프 방지
                this.src = IMG_PLACEHOLDER;
            };
        }

        if ($('spaceTitle'))   $('spaceTitle').textContent   = space.title || '(제목 없음)';
        if ($('ownerName'))    $('ownerName').textContent    = space.owner?.name || '-';
        if ($('areaSize'))     $('areaSize').textContent     = (space.areaSize ?? '-') + ' ㎡';
        if ($('rentalFee'))    $('rentalFee').textContent    = this.formatRentalFee(space.rentalFee);
        if ($('address'))      $('address').textContent      = this.formatAddress(space);
        if ($('period'))       $('period').textContent       = this.formatPeriod(space.startDate, space.endDate);
        if ($('contactPhone')) $('contactPhone').textContent = space.contactPhone || '-';
        if ($('description'))  $('description').textContent  = space.description || '';

        const ownerActions = document.getElementById('ownerActions');
        if (ownerActions) ownerActions.style.display = space.mine ? 'flex' : 'none';

        const cont = document.getElementById('detailContent');
        if (cont) cont.style.display = 'block';
    },

    bindActions(space) {
        const id = space?.id ?? space?.spaceId ?? space?.space_id;
        document.querySelectorAll('[data-act]').forEach(el => {
            el.addEventListener('click', () => {
                const act = el.getAttribute('data-act');
                if (act === 'list') this.goList();
                else if (act === 'edit') this.editSpace(id);
                else if (act === 'delete') this.deleteSpace(id);
                else if (act === 'reserve') this.reserveSpace(id);
            });
        });
    },

    // 예약하기 형식만 (아직 hostprofile) 존재하지 않음
    async reserveSpace(spaceId) {
        try {
            // 👉 지금은 형식만, 나중에 로그인 Host 정보 붙이면 됨
            const result = await apiService.post('/space-reservations', {
                spaceId: Number(spaceId),
                brand: "임시브랜드",          // TODO: Host 프로필 연동
                popupTitle: "임시팝업",       // TODO: Host 프로필 연동
                startDate: "2025-09-10",     // TODO: UI에서 선택
                endDate: "2025-09-12",       // TODO: UI에서 선택
                contactPhone: "010-0000-0000", // TODO: Host 프로필 연동
                message: "임시 신청 메세지"    // TODO: UI에서 입력
            });

            alert(`예약 신청 완료! (ID: ${result.id})`);
        } catch (e) {
            console.error('예약 실패:', e);
            alert('예약 신청 실패');
        }
    },

    getThumbUrl(space) {
        if (space?.coverImageUrl) {
            return `${window.location.origin}${space.coverImageUrl}`;
        }
        if (space?.coverImage) {
            return `${window.location.origin}${space.coverImage}`;
        }

        // 기타 이미지 필드들
        const u = space?.thumbnailUrl || space?.imageUrl || space?.imagePath || space?.thumbnailPath || '';
        if (!u) return IMG_PLACEHOLDER;
        if (u.startsWith('http')) return u;
        if (u.startsWith('/')) return u;
        return `/uploads/${u}`;
    },

    // 주소 포맷팅 - 서버에서 이미 조합된 주소 사용
    formatAddress(space) {
        if (space?.address && space.address !== '주소 정보 없음') {
            return space.address;
        }

        // fallback: venue 정보로부터 직접 조합 (혹시 모를 경우)
        if (space?.venue) {
            const venue = space.venue;
            let address = '';

            if (venue.roadAddress) {
                address = venue.roadAddress;
            } else if (venue.jibunAddress) {
                address = venue.jibunAddress;
            }

            if (venue.detailAddress && address) {
                address += ` ${venue.detailAddress}`;
            } else if (venue.detailAddress && !address) {
                address = venue.detailAddress;
            }

            return address || '주소 정보 없음';
        }

        return '주소 정보 없음';
    },

    formatRentalFee(amount) {
        if (!amount && amount !== 0) return '-';
        return `${amount} 만원`;
    },

    formatPeriod(s, e) {
        const f = (d) => {
            if (!d) return '-';
            try {
                return new Date(d).toLocaleDateString('ko-KR');
            } catch {
                return d;
            }
        };
        return `${f(s)} ~ ${f(e)}`;
    },

    goList() {
        location.assign('/templates/pages/space-list.html');
    },

    editSpace(id) {
        location.assign(`/templates/pages/space-edit.html?id=${encodeURIComponent(id)}`);
    },

    async deleteSpace(id) {
        if (!confirm('정말 삭제하시겠습니까?')) return;
        try {
            await apiService.deleteSpace(id);
            alert('삭제되었습니다.');
            this.goList();
        } catch (e) {
            console.error('삭제 실패:', e);
            alert('삭제 실패');
        }
    },

    async inquireSpace(id) {
        try {
            await apiService.inquireSpace(id);
            alert('문의 요청이 접수되었습니다.');
        } catch (e) {
            console.error('문의 실패:', e);
            alert('문의 실패');
        }
    },

    async reportSpace(id) {
        try {
            await apiService.reportSpace(id);
            alert('신고가 접수되었습니다.');
        } catch (e) {
            console.error('신고 실패:', e);
            alert('신고 실패');
        }
    },

    showError(message) {
        const cont = document.getElementById('detailContent');
        if (cont) {
            cont.style.display = 'block';
            cont.innerHTML = `<div class="error-state">${message}</div>`;
        }
    }
};

window.SpaceDetailPage = SpaceDetailPage;