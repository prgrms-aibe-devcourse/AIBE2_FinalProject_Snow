// id 추출 (id / spaceId / space_id 호환)
function pickSpaceId(space) {
    return space?.id ?? space?.spaceId ?? space?.space_id ?? null;
}

// 이미지 없을 때 쓸 인라인 플레이스홀더 (파일 의존 없음)
const IMG_PLACEHOLDER =
    'data:image/svg+xml;utf8,' +
    encodeURIComponent(
        `<svg xmlns="http://www.w3.org/2000/svg" width="160" height="120">
       <rect width="100%" height="100%" fill="#f2f2f2"/>
       <text x="50%" y="50%" dominant-baseline="middle" text-anchor="middle"
             fill="#888" font-size="14">no image</text>
     </svg>`
    );

// 공간 목록 페이지 전용
const SpaceListPage = {
    async init() {
        try {
            this.showLoading();
            const spaces = await apiService.listSpaces();
            this.renderSpaces(spaces);
        } catch (error) {
            console.error('Space List page initialization failed:', error);
            this.showError('공간 목록을 불러오는데 실패했습니다.');
        }
    },

    showLoading() {
        const loadingEl = document.getElementById('loading');
        const spaceListEl = document.getElementById('spaceList');
        const emptyStateEl = document.getElementById('emptyState');
        if (loadingEl) loadingEl.style.display = 'block';
        if (spaceListEl) spaceListEl.style.display = 'none';
        if (emptyStateEl) emptyStateEl.style.display = 'none';
    },
    hideLoading() {
        const loadingEl = document.getElementById('loading');
        if (loadingEl) loadingEl.style.display = 'none';
    },

    renderSpaces(spaces) {
        this.hideLoading();

        const spaceListEl = document.getElementById('spaceList');
        const emptyStateEl = document.getElementById('emptyState');

        if (!spaces || spaces.length === 0) {
            if (spaceListEl) spaceListEl.style.display = 'none';
            if (emptyStateEl) emptyStateEl.style.display = 'block';
            return;
        }

        if (spaceListEl) {
            spaceListEl.style.display = 'block';
            spaceListEl.innerHTML = '';
            spaces.forEach(space => {
                const spaceCard = this.createSpaceCard(space);
                spaceListEl.appendChild(spaceCard);
            });
        }
        if (emptyStateEl) emptyStateEl.style.display = 'none';
    },

    createSpaceCard(space) {
        const id = pickSpaceId(space);
        const card = document.createElement('div');
        card.className = 'space-card';

        const imageUrl = this.getThumbUrl(space);
        const imageHtml = `<img class="thumb" src="${imageUrl}" alt="썸네일">`;

        card.innerHTML = `
      <div class="space-header">
        <div>
          <h4 class="space-title">${space.title || '(제목 없음)'}</h4>
          <div class="space-details">
            <div>등록자: ${space.ownerName || '-'}</div>
            <div>임대료: 하루 ${this.formatCurrency(space.rentalFee)} 원</div>
            <div>주소: ${space.address || '-'}</div>
            <div>면적: ${space.areaSize || '-'} m²</div>
            <div class="actions-inline">
              <button class="link" data-act="detail" data-id="${id}">상세정보</button>
              <button class="link" data-act="inquire" data-id="${id}">문의하기</button>
              <button class="link" data-act="report"  data-id="${id}">신고</button>
            </div>
          </div>
        </div>
        ${imageHtml}
      </div>
      <div class="space-meta">
        <span>등록일: ${this.formatDate(space.createdAt)}</span>
        <div class="space-actions">
          ${space.mine ? `
            <button class="action-btn edit"   data-act="edit"   data-id="${id}">수정</button>
            <button class="action-btn delete" data-act="delete" data-id="${id}">삭제</button>
          ` : ''}
        </div>
      </div>
    `;

        // 이미지 실패 시 1회만 플레이스홀더로 대체 (무한 루프 방지)
        const imgEl = card.querySelector('.thumb');
        if (imgEl) {
            imgEl.onerror = function () {
                this.onerror = null;
                this.src = IMG_PLACEHOLDER;
            };
        }

        // 버튼 동작
        card.addEventListener('click', (e) => {
            const btn = e.target.closest('[data-act]');
            if (!btn) return;

            const act = btn.getAttribute('data-act');
            const targetId = btn.getAttribute('data-id');

            switch (act) {
                case 'detail':
                    location.assign(`/templates/pages/space-detail.html?id=${encodeURIComponent(targetId)}`);
                    break;
                case 'edit':
                    location.assign(`/templates/pages/space-edit.html?id=${encodeURIComponent(targetId)}`);
                    break;
                case 'delete':
                    this.deleteSpace(targetId);
                    break;
                case 'inquire':
                    this.inquireSpace(targetId);
                    break;
                case 'report':
                    this.reportSpace(targetId);
                    break;
            }
        });

        return card;
    },

    // 썸네일 URL 처리 (coverImageUrl → 절대 경로 변환 포함)
    getThumbUrl(space) {
        if (space.coverImageUrl) {
            return `${window.location.origin}${space.coverImageUrl}`;
        }
        if (space.coverImage) {
            return `${window.location.origin}${space.coverImage}`;
        }
        return IMG_PLACEHOLDER;
    },

    // API
    async deleteSpace(spaceId) {
        if (!confirm('정말로 이 공간을 삭제하시겠습니까?')) return;
        try {
            await apiService.deleteSpace(spaceId);
            alert('공간이 삭제되었습니다.');
            location.reload();
        } catch (error) {
            console.error('공간 삭제 실패:', error);
            alert('삭제에 실패했습니다.');
        }
    },
    async inquireSpace(spaceId) {
        try {
            await apiService.inquireSpace(spaceId);
        } catch (error) {
            console.error('문의 실패:', error);
            alert('문의 중 오류가 발생했습니다.');
        }
    },
    async reportSpace(spaceId) {
        try {
            await apiService.reportSpace(spaceId);
        } catch (error) {
            console.error('신고 실패:', error);
            alert('신고 중 오류가 발생했습니다.');
        }
    },

    // 유틸
    formatDate(dateString) {
        if (!dateString) return '날짜 정보 없음';
        const date = new Date(dateString);
        return date.toLocaleDateString('ko-KR');
    },
    formatCurrency(amount) {
        if (!amount && amount !== 0) return '0';
        return Number(amount).toLocaleString('ko-KR');
    },
    showError(message) {
        this.hideLoading();
        const spaceListEl = document.getElementById('spaceList');
        const emptyStateEl = document.getElementById('emptyState');
        if (spaceListEl) {
            spaceListEl.style.display = 'block';
            spaceListEl.innerHTML = `<div class="error-state"><p>${message}</p></div>`;
        }
        if (emptyStateEl) emptyStateEl.style.display = 'none';
    }
};

window.SpaceListPage = SpaceListPage;
