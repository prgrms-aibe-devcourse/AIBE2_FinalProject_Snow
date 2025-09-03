(function () {
  function $(sel){ return document.querySelector(sel); }
  function qs(key){ return new URLSearchParams(location.search).get(key); }

  function openMissionModal({ mission, onSubmit }) {
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';
    backdrop.innerHTML = `
      <div class="modal-card">
        <div class="modal-title" id="modal-title"></div>
        <div class="modal-desc" id="modal-desc"></div>

        <div style="background:#f6f6f6;border:1px dashed #ccc;border-radius:10px;height:120px;display:grid;place-items:center;margin-bottom:12px;">
          <span style="color:#888">이미지 업로드 영역(추후 연결)</span>
        </div>

        <label style="display:block;font-weight:600;margin-bottom:6px;">정답 입력</label>
        <input id="modal-answer" type="text" placeholder="정답 또는 코드" style="width:100%;padding:10px;border:1px solid #ddd;border-radius:8px;">
        <div class="modal-actions">
          <button class="btn" id="modal-cancel">취소</button>
          <button class="btn primary" id="modal-submit">submit</button>
        </div>
      </div>
    `;
    document.body.appendChild(backdrop);
    backdrop.style.display = 'block';

    $('#modal-title').textContent = mission.title || ('mission ' + mission.id);
    $('#modal-desc').textContent  = mission.description || '';

    function close(){ document.body.removeChild(backdrop); }
    $('#modal-cancel').onclick = close;
    $('#modal-submit').onclick = async function(){
      const answer = $('#modal-answer').value;
      await onSubmit(answer).catch(err => alert(err?.message || '제출 실패'));
      close();
    };
  }

  function renderMissionBoard({ mount, setView, onOpenMission }) {
    const remaining = Math.max(0, (setView.requiredCount || 0) - (setView.successCount || 0));
    mount.innerHTML = `
      <section class="mission-board">
        <div class="mission-head">
          <div class="title">STAMP MISSION</div>
          <div class="subtitle"><strong>${remaining}</strong>개의 미션을 더 완료하시고 리워드를 받아가세요!</div>
        </div>

        <div class="mission-grid" id="mission-grid"></div>

        <button class="mission-complete-btn" id="complete-btn" ${remaining > 0 ? 'disabled' : ''}>
          미션 완료
        </button>
      </section>
    `;

    const grid = $('#mission-grid');
    (setView.missions || []).slice(0, 6).forEach(m => {
      const done = String(m.userStatus || '') === 'SUCCESS';
      const item = document.createElement('div');
      item.className = 'mission-item';
      item.innerHTML = `
        <div class="mission-stamp ${done ? 'done' : ''}"></div>
        <div class="mission-name ${done ? 'done' : ''}">${m.title || ('mission' + m.id)}</div>
      `;
      item.onclick = () => onOpenMission(m);
      grid.appendChild(item);
    });

    $('#complete-btn').onclick = function(){
      if (remaining > 0) return;
      alert('축하합니다! 리워드 지급 플로우로 이동합니다.');
    };
  }

  window.Pages = window.Pages || {};
  Pages.missionBoard = async function ({ popupId, setIndex = 0 } = {}) {
    popupId = (popupId || qs('popupId') || '').toString();
    if (popupId.indexOf('?') >= 0) popupId = popupId.split('?')[0];

    const mount = $('#main-content');

    if (!popupId || isNaN(Number(popupId))) {
      mount.innerHTML = `<div class="content-section"><h2 class="content-title">popupId가 올바르지 않습니다.</h2></div>`;
      return;
    }

    mount.innerHTML = `<div class="content-section"><h2 class="content-title">로딩 중...</h2><div class="loading"></div></div>`;


    let data;
    try {
      data = await apiService.getMissionSetsByPopup(popupId);
    } catch (e) {
      mount.innerHTML = `<div class="content-section"><h2 class="content-title">불러오기 실패</h2><p>${e.message || e}</p></div>`;
      return;
    }
    if (!Array.isArray(data) || data.length === 0) {
      mount.innerHTML = `<div class="content-section"><h2 class="content-title">미션 없음</h2><p>이 팝업에 등록된 미션셋이 없습니다.</p></div>`;
      return;
    }

    const setView = data[Math.min(setIndex, data.length - 1)];

    const handleOpen = (mission) => {
      openMissionModal({
        mission,
        onSubmit: async (answer) => {
          const res = await apiService.submitMissionAnswer(mission.id, answer);

          // 제출 후 새로고침
          const refreshed = await apiService.getMissionSetsByPopup(popupId);
          const sv = refreshed.find(s => s.missionSetId === setView.missionSetId) || setView;
          renderMissionBoard({ mount, setView: sv, onOpenMission: handleOpen });

          const pass = res.pass;
          alert(pass ? '미션 성공! 🎉' : '오답/미완료입니다 😢');
        }
      });
    };

    renderMissionBoard({ mount, setView, onOpenMission: handleOpen });
  };
})();
