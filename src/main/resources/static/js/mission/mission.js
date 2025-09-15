(function () {
  function $(sel){ return document.querySelector(sel); }

  // URL 경로에서 missionSetId 추출 (/missions/{id})
  function getMissionSetIdFromPath() {
    const parts = window.location.pathname.split('/').filter(Boolean);
    const idx = parts.lastIndexOf('missions');
    if (idx === -1 || idx === parts.length - 1) return null;
    return parts[idx + 1];
  }

  // 미션 정답 입력 모달
  function openMissionModal({ mission, onSubmit }) {
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';

    const card = document.createElement('div');
    card.className = 'modal-card';
    card.innerHTML = `
    <button class="modal-close" id="modal-close">&times;</button>
    <div class="modal-title">${mission.title || ('mission ' + mission.id)}</div>
    <div class="modal-desc">${mission.description || ''}</div>
    <label class="modal-label">정답 입력</label>
    <input id="modal-answer" type="text" placeholder="정답 또는 코드" class="modal-input">
    <div class="modal-actions">
      <button class="submit-btn" id="modal-submit">제출</button>
    </div>
  `;
    backdrop.appendChild(card);
    document.body.appendChild(backdrop);

    function close(){ document.body.removeChild(backdrop); }
    card.querySelector('#modal-close').onclick = close;
    card.querySelector('#modal-submit').onclick = async function(){
      const answer = card.querySelector('#modal-answer').value;
      await onSubmit(answer).catch(err => alert(err?.message || '제출 실패'));
      close();
    };
  }


  // 스태프 PIN 모달
  function openStaffPinModal(missionSetId) {
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';

    const card = document.createElement('div');
    card.className = 'modal-card';
    card.innerHTML = `
    <button class="modal-close" id="modal-close">&times;</button>
    <div class="modal-title">리워드 수령 확인</div>
    <label class="modal-label">스태프 PIN 입력</label>
    <input id="staff-pin" type="password" placeholder="PIN 코드" class="modal-input">
    <div class="modal-actions">
      <button class="submit-btn" id="confirm">확인</button>
    </div>
  `;
    backdrop.appendChild(card);
    document.body.appendChild(backdrop);

    function close(){ document.body.removeChild(backdrop); }
    card.querySelector('#modal-close').onclick = close;

    card.querySelector('#confirm').onclick = async () => {
      const pin = card.querySelector('#staff-pin').value;
      try {
        const res = await apiService.redeemReward(missionSetId, pin);
        if (res && res.ok) {
          alert('수령 완료 🎉');
          close();
          location.reload();
        } else {
          alert(res?.error || 'PIN 인증 실패');
        }
      } catch (e) {
        alert('네트워크 오류: ' + e.message);
      }
    };
  }


  async function renderMissionBoard({ mount, setView, onOpenMission }) {
    const remaining = Math.max(0, (setView.requiredCount || 0) - (setView.successCount || 0));

    let myReward = null;
    try {
      myReward = await apiService.getMyReward(setView.missionSetId);
    } catch (_) { /* ignore */ }

    let btnHtml = '';
    if (myReward && myReward.status === 'ISSUED') {
      btnHtml = `<button class="mission-complete-btn enabled" id="reward-redeem-btn">리워드 수령하기</button>`;
    } else if (myReward && myReward.status === 'REDEEMED') {
      btnHtml = `<button class="mission-complete-btn disabled">수령 완료</button>`;
    } else {
      btnHtml = `<button class="mission-complete-btn ${remaining > 0 ? 'disabled' : 'enabled'}" id="complete-btn">미션 완료</button>`;
    }

    // 항상 content-section 안에 card를 추가
    mount.innerHTML = `
    <div class="content-section">
      <div class="card">
        <div class="mission-head">
          <div class="title">STAMP MISSION</div>
          <div class="subtitle"><strong>${remaining}</strong>개의 미션을 더 완료하시고 리워드를 받아가세요!</div>
        </div>
        <div class="mission-grid" id="mission-grid"></div>
        ${btnHtml}
      </div>
    </div>
  `;

    const grid = document.getElementById('mission-grid');
    (setView.missions || []).slice(0, 6).forEach(m => {
      const done = String(m.userStatus || '') === 'COMPLETED';
      const item = document.createElement('div');
      item.className = 'mission-item';
      item.innerHTML = `
      <div class="mission-stamp ${done ? 'done' : ''}"></div>
      <div class="mission-name ${done ? 'done' : ''}">${m.title || ('mission' + m.id)}</div>
    `;
      item.onclick = () => onOpenMission(m);
      grid.appendChild(item);
    });

    const claimBtn = document.getElementById('complete-btn');
    if (claimBtn) {
      claimBtn.onclick = () => {
        if (remaining > 0) return;
        if (typeof rewardClaim !== 'function') {
          alert('룰렛 모듈이 로드되지 않았습니다.');
          return;
        }
        rewardClaim(setView.missionSetId);
      };
    }

    const redeemBtn = document.getElementById('reward-redeem-btn');
    if (redeemBtn) {
      redeemBtn.onclick = () => openStaffPinModal(setView.missionSetId);
    }
  }


  // 엔트리
  window.Pages = window.Pages || {};
  Pages.missionBoard = async function () {
    const missionSetId = getMissionSetIdFromPath();

    const mount = $('#main-content');
    if (!missionSetId) {
      mount.innerHTML = `<div class="content-section"><h2 class="content-title">missionSetId가 올바르지 않습니다.</h2></div>`;
      return;
    }

    mount.innerHTML = `<div class="content-section"><h2 class="content-title">로딩 중...</h2><div class="loading"></div></div>`;

    let data;
    try {
      data = await apiService.getMissionSet(missionSetId);
    } catch (e) {
      mount.innerHTML = `<div class="content-section"><h2 class="content-title">불러오기 실패</h2><p>${e.message || e}</p></div>`;
      return;
    }
    if (!data) {
      mount.innerHTML = `<div class="content-section"><h2 class="content-title">미션셋 없음</h2><p>해당 missionSetId에 해당하는 미션셋이 없습니다.</p></div>`;
      return;
    }

    const setView = data;

    const handleOpen = (mission) => {
      openMissionModal({
        mission,
        onSubmit: async (answer) => {
          const res = await apiService.submitMissionAnswer(mission.id, answer);

          const refreshed = await apiService.getMissionSet(missionSetId);
          await renderMissionBoard({ mount, setView: refreshed, onOpenMission: handleOpen });

          alert(res?.pass ? '미션 성공! 🎉' : '오답/미완료입니다 😢');
        }
      });
    };

    await renderMissionBoard({ mount, setView, onOpenMission: handleOpen });
  };
})();
