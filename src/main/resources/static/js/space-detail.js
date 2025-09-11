const IMG_PLACEHOLDER =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent(
        `<svg xmlns="http://www.w3.org/2000/svg" width="160" height="120">
     <rect width="100%" height="100%" fill="#f2f2f2"/>
     <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle"
           fill="#888" font-size="14">no image</text>
   </svg>`
    );

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
                this.onerror = null;
                this.src = IMG_PLACEHOLDER;
            };
        }

        if ($('spaceTitle')) $('spaceTitle').textContent = space.title || '(제목 없음)';
        if ($('ownerName')) $('ownerName').textContent = space.owner?.name || '-';
        if ($('areaSize')) $('areaSize').textContent = (space.areaSize ?? '-') + ' ㎡';
        if ($('rentalFee')) $('rentalFee').textContent = this.formatRentalFee(space.rentalFee);
        if ($('address')) $('address').textContent = this.formatAddress(space);
        if ($('period')) $('period').textContent = this.formatPeriod(space.startDate, space.endDate);
        if ($('contactPhone')) $('contactPhone').textContent = space.contactPhone || '-';
        if ($('description')) $('description').textContent = space.description || '';

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

    async reserveSpace(spaceId) {
        const modal = document.getElementById('reserveModal');
        modal.classList.remove('hidden');

        //  내 팝업 목록 불러오기 (수정됨)
        try {
            const popups = await apiService.get('/hosts/popups');
            const select = document.getElementById('popupSelect');
            if (popups.length === 0) {
                select.innerHTML = `<option value="">등록된 팝업이 없습니다</option>`;
            } else {
                select.innerHTML = popups.map(p => `<option value="${p.id}">${p.title}</option>`).join('');
            }
        } catch (e) {
            console.error('팝업 목록 불러오기 실패:', e);
            alert('내 팝업 목록을 불러오지 못했습니다.');
            return;
        }

        // 신청 버튼
        document.getElementById('reserveSubmit').onclick = async () => {
            const popupId = Number(document.getElementById('popupSelect').value);
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;

            if (!popupId) return alert("팝업을 선택하세요.");
            if (!startDate) return alert("시작일을 입력하세요.");
            if (!endDate) return alert("종료일을 입력하세요.");

            const body = {
                spaceId: Number(spaceId),
                popupId,
                startDate,
                endDate,
                message:null,
                contactPhone:null
            };
            console.log('=== 예약 요청 시작 ===');
            console.log('요청 데이터:', body);
            try {
                const result = await apiService.post('/space-reservations', body);
                console.log('성공 응답:', result);
                alert(`예약 신청 완료! (ID: ${result.id})`);
                modal.classList.add('hidden');
            } catch (error) {
                console.log('=== 예약 실패 상세 정보 ===');
                console.error('에러 객체:', error);

                // 응답 정보 상세 출력
                if (error.response) {
                    console.log('응답 상태:', error.response.status);
                    console.log('응답 헤더:', error.response.headers);
                    console.log('응답 데이터:', error.response.data);
                } else if (error.request) {
                    console.log('요청 정보:', error.request);
                } else {
                    console.log('에러 메시지:', error.message);
                }

                // 사용자에게 표시할 메시지
                let userMessage = '예약 신청 실패';
                if (error.response && error.response.data && error.response.data.error) {
                    userMessage = error.response.data.error;
                }

                alert(userMessage);
            }
        };
        document.getElementById('reserveCancel').onclick = () => {
            modal.classList.add('hidden');
        };
    },

    getThumbUrl(space) {
        if (space?.coverImageUrl) {
            return `${window.location.origin}${space.coverImageUrl}`;
        }
        if (space?.coverImage) {
            return `${window.location.origin}${space.coverImage}`;
        }
        const u = space?.thumbnailUrl || space?.imageUrl || space?.imagePath || space?.thumbnailPath || '';
        if (!u) return IMG_PLACEHOLDER;
        if (u.startsWith('http')) return u;
        if (u.startsWith('/')) return u;
        return `/uploads/${u}`;
    },

    formatAddress(space) {
        if (space?.address && space.address !== '주소 정보 없음') {
            return space.address;
        }
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

    showError(message) {
        const cont = document.getElementById('detailContent');
        if (cont) {
            cont.style.display = 'block';
            cont.innerHTML = `<div class="error-state">${message}</div>`;
        }
    }
};

window.SpaceDetailPage = SpaceDetailPage;
