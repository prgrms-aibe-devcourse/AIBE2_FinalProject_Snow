class MissionManagement {
  constructor() {
    this.currentPage = 1;
    this.pageSize = 10;
    this.totalPages = 0;
    this.currentFilters = {};
    this.selectedSetId = null; // UUID

    this.init();
  }

  init() {
    this.checkAdminAuth();
    this.bindEvents();
    this.loadMissionSets();
  }

  // 관리자 권한 확인
  checkAdminAuth() {
    const token = localStorage.getItem('authToken');
    const userRole = localStorage.getItem('userRole');
    if (!token || userRole !== 'ADMIN') {
      alert('관리자만 접근할 수 있습니다.');
      window.location.href = '/templates/auth/login.html';
    }
  }

  // 이벤트 바인딩
  bindEvents() {
    document.getElementById('searchBtn').addEventListener('click', () => this.search());
    document.getElementById('resetBtn').addEventListener('click', () => this.resetFilters());
    document.getElementById('searchKeyword').addEventListener('keypress', e => { if (e.key === 'Enter') this.search(); });

    document.getElementById('modalCloseBtn').addEventListener('click', () => this.closeDetailModal());
    document.getElementById('closeModalBtn').addEventListener('click', () => this.closeDetailModal());

    document.getElementById('createMissionSetBtn').addEventListener('click', () => this.openCreateSetModal());
    document.getElementById('createSetCloseBtn').addEventListener('click', () => this.closeCreateSetModal());
    document.getElementById('createSetCancelBtn').addEventListener('click', () => this.closeCreateSetModal());
    document.getElementById('createSetConfirmBtn').addEventListener('click', () => this.createMissionSet());

    document.getElementById('addMissionBtn').addEventListener('click', () => this.openAddMissionModal());
    document.getElementById('addMissionCloseBtn').addEventListener('click', () => this.closeAddMissionModal());
    document.getElementById('addMissionCancelBtn').addEventListener('click', () => this.closeAddMissionModal());
    document.getElementById('addMissionConfirmBtn').addEventListener('click', () => this.addMission());

    document.getElementById('completeSetBtn').addEventListener('click', () => this.completeSet());
    document.getElementById('deleteSetBtn').addEventListener('click', () => this.deleteSet());
  }

  // 목록
  async loadMissionSets() {
    try {
      this.showLoading();
      const params = {
        page: this.currentPage - 1,
        size: this.pageSize,
        ...this.currentFilters
      };
      const data = await apiService.getMissionSets(params);
      this.renderTable(data.content);
      this.renderPagination(data);
    } catch (e) {
      console.error(e);
      this.showError('미션셋 목록을 불러오는데 실패했습니다.');
    }
  }

  showLoading() {
    document.getElementById('tableContainer').innerHTML = '<div class="loading">데이터를 불러오는 중...</div>';
  }
  showError(msg) {
    document.getElementById('tableContainer').innerHTML = `<div class="no-data">${msg}</div>`;
  }

  // 테이블
  renderTable(items) {
    if (!items || items.length === 0) {
      document.getElementById('tableContainer').innerHTML = '<div class="no-data">등록된 미션셋이 없습니다.</div>';
      return;
    }
    const html = `
      <table class="popup-table">
        <thead>
          <tr>
            <th>미션셋ID</th>
            <th>팝업ID</th>
            <th>필요완료수</th>
            <th>미션수</th>
            <th>상태</th>
            <th>등록일</th>
            <th>액션</th>
          </tr>
        </thead>
        <tbody>
          ${items.map(s => this.renderRow(s)).join('')}
        </tbody>
      </table>`;
    document.getElementById('tableContainer').innerHTML = html;
  }

  renderRow(set) {
    const statusClass = (set.status || '').toLowerCase();
    const missionsCount = Array.isArray(set.missions) ? set.missions.length : (set.totalMissions || 0);
    const idShort = (set.id || set.missionSetId || '').toString().replace(/-/g,'').slice(0,8);
    return `
      <tr>
        <td class="mono">${idShort}</td>
        <td>${set.popupId ?? '-'}</td>
        <td>${set.requiredCount ?? 0}</td>
        <td>${missionsCount}</td>
        <td><span class="status-badge ${statusClass}">${set.status || '-'}</span></td>
        <td>${this.formatDate(set.createdAt)}</td>
        <td>
          <div class="action-buttons">
            <button class="btn btn-sm btn-primary" onclick="missionManagement.viewDetail('${set.id || set.missionSetId}')">상세</button>
          </div>
        </td>
      </tr>`;
  }

  // 페이지네이션
  renderPagination(p) {
    const { totalPages, number, first, last } = p;
    this.totalPages = totalPages;
    if (totalPages <= 1) {
      document.getElementById('pagination').innerHTML = '';
      return;
    }
    let h = '';
    h += `<button ${first ? 'disabled' : ''} onclick="missionManagement.goToPage(${number})">이전</button>`;
    const start = Math.max(0, number - 2);
    const end = Math.min(totalPages - 1, number + 2);
    for (let i = start; i <= end; i++) {
      h += `<button class="${i===number?'active':''}" onclick="missionManagement.goToPage(${i+1})">${i+1}</button>`;
    }
    h += `<button ${last ? 'disabled' : ''} onclick="missionManagement.goToPage(${number+2})">다음</button>`;
    document.getElementById('pagination').innerHTML = h;
  }
  goToPage(page){ this.currentPage = page; this.loadMissionSets(); }

  // 필터
  search() {
    const f = {
      status: document.getElementById('statusFilter').value,
      popupId: document.getElementById('popupIdFilter').value,
      keyword: document.getElementById('searchKeyword').value.trim()
    };
    this.currentFilters = Object.fromEntries(Object.entries(f).filter(([,v]) => v !== '' && v != null));
    this.currentPage = 1;
    this.loadMissionSets();
  }
  resetFilters() {
    document.getElementById('statusFilter').value = '';
    document.getElementById('popupIdFilter').value = '';
    document.getElementById('searchKeyword').value = '';
    this.currentFilters = {};
    this.currentPage = 1;
    this.loadMissionSets();
  }

  // 상세 보기
  async viewDetail(setId) {
    try {
      const set = await apiService.getMissionSetDetail(setId);
      this.selectedSetId = set.id || set.missionSetId;
      this.showDetailModal(set);
    } catch (e) {
      console.error(e);
      alert('미션셋 상세 정보를 불러오는데 실패했습니다.');
    }
  }

  showDetailModal(set) {
    const missions = Array.isArray(set.missions) ? set.missions : [];
    const idDisp = (set.id || set.missionSetId || '').toString();
    const modalBody = document.getElementById('modalBody');
    modalBody.innerHTML = `
      <div class="detail-section">
        <h3>기본 정보</h3>
        <div class="detail-grid">
          <div class="detail-item"><span class="detail-label">미션셋 ID</span><span class="detail-value mono small">${idDisp}</span></div>
          <div class="detail-item"><span class="detail-label">팝업 ID</span><span class="detail-value">${set.popupId ?? '-'}</span></div>
          <div class="detail-item"><span class="detail-label">필요 완료 수</span><span class="detail-value">${set.requiredCount ?? 0}</span></div>
          <div class="detail-item"><span class="detail-label">상태</span><span class="detail-value"><span class="status-badge ${(set.status||'').toLowerCase()}">${set.status || '-'}</span></span></div>
          <div class="detail-item"><span class="detail-label">리워드 PIN</span><span class="detail-value">${set.rewardPin || '-'}</span></div>
          <div class="detail-item"><span class="detail-label">생성일</span><span class="detail-value">${this.formatDate(set.createdAt)}</span></div>
        </div>
      </div>

      <div class="detail-section">
        <h3>미션 목록</h3>
        <div class="mission-list">
          ${missions.length === 0 ? '<div class="no-data">등록된 미션이 없습니다.</div>' : missions.map(m => `
            <div class="mission-row">
              <div>
                <div><strong>${m.title || '(제목없음)'}</strong></div>
                <div class="small mono">${(m.id||'').toString().replace(/-/g,'')}</div>
                ${m.description ? `<div class="small" style="margin-top:4px;">${m.description}</div>` : ''}
              </div>
              <div class="action-buttons">
                <button class="btn btn-sm btn-danger-outline" onclick="missionManagement.deleteMission('${m.id}')">삭제</button>
              </div>
            </div>
          `).join('')}
        </div>
      </div>
    `;
    document.getElementById('detailModal').style.display = 'block';
  }

  closeDetailModal() {
    document.getElementById('detailModal').style.display = 'none';
    this.selectedSetId = null;
  }

  // 미션셋 등록
  openCreateSetModal(){ document.getElementById('createSetModal').style.display = 'block'; }
  closeCreateSetModal(){ document.getElementById('createSetModal').style.display = 'none'; }

  async createMissionSet() {
    const popupId = Number(document.getElementById('createPopupId').value);
    const requiredCount = Number(document.getElementById('createRequiredCount').value || 0);
    const rewardPin = (document.getElementById('createRewardPin').value || '').trim();
    const status = document.getElementById('createStatus').value || 'ACTIVE';

    if (!popupId) { alert('팝업 ID는 필수입니다.'); return; }

    try {
      await apiService.createMissionSet({ popupId, requiredCount, status, rewardPin });
      alert('미션셋이 등록되었습니다.');
      this.closeCreateSetModal();
      this.loadMissionSets();
    } catch (e) {
      console.error(e);
      alert('미션셋 등록에 실패했습니다.');
    }
  }

  // 미션 추가
  openAddMissionModal() {
    if (!this.selectedSetId) return alert('미션셋을 먼저 선택하세요.');
    document.getElementById('missionTitle').value = '';
    document.getElementById('missionDesc').value = '';
    document.getElementById('missionAnswer').value = '';
    document.getElementById('addMissionModal').style.display = 'block';
  }
  closeAddMissionModal(){ document.getElementById('addMissionModal').style.display = 'none'; }

  async addMission() {
    if (!this.selectedSetId) return;
    const title = document.getElementById('missionTitle').value.trim();
    const description = document.getElementById('missionDesc').value.trim();
    const answer = document.getElementById('missionAnswer').value.trim();

    if (!title) { alert('제목은 필수입니다.'); return; }

    try {
      await apiService.addMission(this.selectedSetId, { title, description, answer });
      alert('미션이 추가되었습니다.');
      this.closeAddMissionModal();
      this.viewDetail(this.selectedSetId);
    } catch (e) {
      console.error(e);
      alert('미션 추가에 실패했습니다.');
    }
  }

  // 미션 삭제
  async deleteMission(missionId) {
    if (!confirm('이 미션을 삭제하시겠습니까?')) return;
    try {
      await apiService.deleteMission(missionId);
      alert('삭제되었습니다.');
      this.viewDetail(this.selectedSetId);
    } catch (e) {
      console.error(e);
      alert('미션 삭제에 실패했습니다.');
    }
  }

  // 미션셋 완료 처리
  async completeSet() {
    if (!this.selectedSetId) return;
    if (!confirm('이 미션셋을 완료 처리하시겠습니까?')) return;
    try {
      await apiService.completeMissionSet(this.selectedSetId);
      alert('완료 처리되었습니다.');
      this.closeDetailModal();
      this.loadMissionSets();
    } catch (e) {
      console.error(e);
      alert('완료 처리에 실패했습니다.');
    }
  }

  // 미션셋 삭제
  async deleteSet() {
    if (!this.selectedSetId) return;
    if (!confirm('이 미션셋을 삭제하시겠습니까?')) return;
    try {
      await apiService.deleteMissionSet(this.selectedSetId);
      alert('미션셋이 삭제되었습니다.');
      this.closeDetailModal();
      this.loadMissionSets();
    } catch (e) {
      console.error(e);
      alert('미션셋 삭제에 실패했습니다.');
    }
  }

  // 유틸
  formatDate(s) {
    if (!s) return '-';
    const d = new Date(s);
    if (Number.isNaN(d.getTime())) return '-';
    return d.toLocaleDateString('ko-KR', { year:'numeric', month:'2-digit', day:'2-digit' });
  }
}

// 전역 인스턴스
let missionManagement;
document.addEventListener('DOMContentLoaded', () => {
  missionManagement = new MissionManagement();
});
