document.addEventListener("DOMContentLoaded", async () => {
    await loadComponents();   // header/footer 먼저 로드
    initializeLayout();

    const section = document.querySelector(".content-section");

    const reviews = [
        { id: 1, content: "분위기도 좋고 제품도 괜찮았습니다.", rating: 4, created_at: "2025-09-11" },
        { id: 2, content: "기대한 것보다 규모가 작았어요.", rating: 2, created_at: "2025-09-10" }
    ];

    function getStars(rating) {
        return Array.from({ length: 5 }, (_, i) => i < rating ? "★" : "☆").join("");
    }

    // 리뷰 카드 렌더링
    reviews.forEach(r => {
        const card = document.createElement("div");
        card.className = "card";
        card.innerHTML = `
            <div class="review-rating">${getStars(r.rating)}</div>
            <div class="review-content">${r.content}</div>
            <div class="review-date">${r.created_at}</div>
            <div class="review-actions">
                <button class="review-btn" onclick="openEditModal(${r.id})">수정</button>
                <button class="review-btn" onclick="deleteReview(${r.id})">삭제</button>
            </div>
        `;
        section.appendChild(card);
    });

    // 모달 생성용 컨테이너
    const modalContainer = document.createElement("div");
    document.body.appendChild(modalContainer);

    // 수정 모달 열기
    window.openEditModal = function(id) {
        const review = reviews.find(r => r.id === id);
        if (!review) return;

        modalContainer.innerHTML = `
            <div class="modal-backdrop">
              <div class="modal">
                <h2 class="modal-title">리뷰 수정</h2>
                <div class="modal-stars" id="edit-stars">${getStars(review.rating)}</div>
                <textarea id="edit-content" class="modal-input">${review.content}</textarea>
                <div class="modal-actions">
                  <button class="review-btn" onclick="closeModal()">취소</button>
                  <button class="review-btn" onclick="saveReview(${id})">저장</button>
                </div>
              </div>
            </div>
        `;

        // 별 클릭 이벤트 (별점 수정 가능)
        const starsEl = document.getElementById("edit-stars");
        starsEl.addEventListener("click", e => {
            const x = e.offsetX;
            const starWidth = starsEl.offsetWidth / 5;
            const newRating = Math.ceil(x / starWidth);
            review.rating = newRating;
            starsEl.textContent = getStars(newRating);
        });
    };

    // 모달 닫기
    window.closeModal = function() {
        modalContainer.innerHTML = "";
    };

    // 리뷰 저장
    window.saveReview = function(id) {
        const review = reviews.find(r => r.id === id);
        if (!review) return;

        review.content = document.getElementById("edit-content").value;

        alert("리뷰가 수정되었습니다. (백엔드 연동 필요)");

        closeModal();
        location.reload(); // 새로고침으로 반영
    };

    // 리뷰 삭제
    window.deleteReview = function(id) {
        if (confirm(`리뷰 ${id}을(를) 삭제하시겠습니까?`)) {
            alert("삭제 완료 (실제로는 API 호출 필요)");
        }
    };
});
