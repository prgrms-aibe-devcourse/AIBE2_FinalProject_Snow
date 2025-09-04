(function () {
  function $(sel){ return document.querySelector(sel); }
  function qs(key){ return new URLSearchParams(location.search).get(key); }

  // 미션 정답 입력 모달
  function openMissionModal({ mission, onSubmit }) {
    const backdrop = document.createElement('div');
    backdrop.className = 'modal-backdrop';

    const card = document.createElement('div');
    card.className = 'modal-card';
    card.innerHTML = `
      <div class="modal-title">${mission.title || ('mission ' + mission.id)}</div>
      <div class="modal-desc">${mission.description || ''}</div>
      <label style="display:block;font-weight:600;margin-bottom:6px;">정답 입력</label>
      <input id="modal-answer" type="text" placeholder="정답 또는 코드"
             style="width:100%;padding:10px;border:1px solid #ddd;border-radius:8px;">
      <div class="modal-actions">
        <button class="btn" id="modal-cancel">취소</button>
        <button class="btn primary" id="modal-submit">제출</button>
      </div>
    `;
    backdrop.appendChild(card);
    document.body.appendChild(backdrop);

    function close(){ document.body.removeChild(backdrop); }
    card.querySelector('#modal-cancel').onclick = close;
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
      <div class="modal-title">리워드 수령 확인</div>
      <label>스태프 PIN 입력</label>
      <input id="staff-pin" type="password" placeholder="PIN 코드"
             style="width:100%;padding:10px;margin-top:6px;border:1px solid #ddd;border-radius:8px;">
      <div class="modal-actions">
        <button class="btn" id="cancel">취소</button>
        <button class="btn primary" id="confirm">확인</button>
      </div>
    `;
    backdrop.appendChild(card);
    document.body.appendChild(backdrop);

    function close(){ document.body.removeChild(backdrop); }
    card.querySelector('#cancel').onclick = close;
    card.querySelector('#confirm').onclick = async () => {
      const pin = card.querySelector('#staff-pin').value;
      try {
        const resp = await fetch('/api/rewards/redeem', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ missionSetId, staffPin: pin })
        });
        const res = await resp.json();
        if (resp.ok && res.ok) {
          alert('수령 완료 🎉');
          close();
          location.reload();
        } else {
          alert(res.error || 'PIN 인증 실패');
        }
      } catch (e) {
        alert('네트워크 오류: ' + e.message);
      }
    };
  }

  // 미션 보드
  async function renderMissionBoard({ mount, setView, onOpenMission }) {
    const remaining = Math.max(0, (setView.requiredCount || 0) - (setView.successCount || 0));

    // 서버의 실제 응답 형태에 영향 안 받도록: 버튼 렌더링과 바인딩을 분리
    let myReward = null;
    try {
      myReward = await apiService.get(`/rewards/my/${setView.missionSetId}`);
    } catch (_) { /* ignore */ }

    // 버튼 렌더
    let btnHtml = '';
    if (myReward && myReward.status === 'ISSUED') {
      btnHtml = `<button class="mission-complete-btn enabled" id="reward-redeem-btn">리워드 수령하기</button>`;
    } else if (myReward && myReward.status === 'REDEEMED') {
      btnHtml = `<button class="mission-complete-btn disabled">수령 완료</button>`;
    } else {
      // 아직 발급 전이거나, 응답이 실패/예상과 다를 때도 '미션 완료' 버튼 노출
      btnHtml = `<button class="mission-complete-btn ${remaining > 0 ? 'disabled' : 'enabled'}" id="complete-btn">미션 완료</button>`;
    }

    mount.innerHTML = `
      <section class="mission-board">
        <div class="mission-head">
          <div class="title">STAMP MISSION</div>
          <div class="subtitle"><strong>${remaining}</strong>개의 미션을 더 완료하시고 리워드를 받아가세요!</div>
        </div>
        <div class="mission-grid" id="mission-grid"></div>
        ${btnHtml}
      </section>
    `;

    // 미션 타일
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

    const claimBtn = document.getElementById('complete-btn');
    if (claimBtn) {
      claimBtn.onclick = () => {
        if (remaining > 0) return;
        if (typeof rewardClaim !== 'function') {
          alert('룰렛 모듈이 로드되지 않았습니다.');
          return;
        }
        rewardClaim(setView.missionSetId); // reward.js
      };
    }

    const redeemBtn = document.getElementById('reward-redeem-btn');
    if (redeemBtn) {
      redeemBtn.onclick = () => openStaffPinModal(setView.missionSetId);
    }
  }

  // 엔트리
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

          // 보드 새로고침
          const refreshed = await apiService.getMissionSetsByPopup(popupId);
          const sv = refreshed.find(s => s.missionSetId === setView.missionSetId) || setView;
          await renderMissionBoard({ mount, setView: sv, onOpenMission: handleOpen });

          alert(res?.pass ? '미션 성공! 🎉' : '오답/미완료입니다 😢');
        }
      });
    };

    await renderMissionBoard({ mount, setView, onOpenMission: handleOpen });
  };
})();
