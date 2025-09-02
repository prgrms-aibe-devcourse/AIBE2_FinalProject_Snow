(function () {
  function $(sel) { return document.querySelector(sel); }
  function q(key) { return new URLSearchParams(location.search).get(key); }

  function renderMission(m) {
    $('#title').textContent = m.title || ('Mission #' + m.id);
    $('#desc').textContent  = m.description || '';
    $('#meta').textContent  = m.missionSetId ? ('MissionSet: ' + m.missionSetId) : '';
  }

  document.addEventListener('DOMContentLoaded', function () {
    var missionId = q('id'); // /mission.html?id=7
    if (!missionId) {
      $('#msg').innerHTML = '<span class="err">URL에 ?id=미션ID 를 넣어주세요.</span>';
      return;
    }

    // 1) 미션 정보 로드
    API.getMission(missionId).then(function (r) {
      if (!r.ok) throw new Error('mission load failed: ' + r.status);
      renderMission(r.data);
    }).catch(function (e) {
      $('#msg').innerHTML = '<span class="err">' + e + '</span>';
    });

    // 2) 정답 제출
    $('#submitBtn').addEventListener('click', function () {
      var userId = Number($('#userId').value.trim());
      var answer = $('#answer').value;
      if (!userId || !answer) {
        $('#msg').innerHTML = '<span class="err">모든 값을 입력하세요.</span>';
        return;
      }

      API.submitAnswer(missionId, userId, answer).then(function (r) {
        $('#resp').textContent = JSON.stringify(r.data, null, 2);
        if (r.ok) {
          $('#msg').innerHTML = '<span class="ok">성공! (pass=' + r.data.isPass + ', cleared=' + r.data.isCleared + ')</span>';
        } else {
          $('#msg').innerHTML = '<span class="err">실패(' + r.status + ')</span>';
        }
      }).catch(function (e) {
        $('#msg').innerHTML = '<span class="err">네트워크 오류: ' + e + '</span>';
      });
    });
  });
})();
