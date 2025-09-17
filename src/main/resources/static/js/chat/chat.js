const ChatPage = {
    stompClient: null,
    reservationId: null,
    userId: null,

    async init() {
        const urlParams = new URLSearchParams(window.location.search);
        const tokenParam = urlParams.get('token');

        if (tokenParam) {
            localStorage.setItem('accessToken', tokenParam);
            localStorage.setItem('authToken', tokenParam);

            const newUrl = window.location.pathname;
            window.history.replaceState({}, '', newUrl);
        }

        this.reservationId = this.getReservationIdFromUrl();
        if (!this.reservationId) {
            alert("잘못된 접근입니다.");
            window.history.back();
            return;
        }

        this.cacheElements();
        this.bindEvents();

        await this.loadCurrentUser();
        await this.loadMessages();
        this.connectWebSocket();
    },

    cacheElements() {
        this.el = {
            messages: document.getElementById("chat-messages"),
            input: document.getElementById("chat-input"),
            sendBtn: document.getElementById("btn-send"),
            backBtn: document.getElementById("btn-back")
        };
    },

    bindEvents() {
        this.el.sendBtn.addEventListener("click", () => this.sendMessage());
        this.el.input.addEventListener("keypress", e => {
            if (e.key === "Enter") this.sendMessage();
        });
        this.el.backBtn.addEventListener("click", () => window.history.back());
    },

    getReservationIdFromUrl() {
        const path = window.location.pathname;
        const parts = path.split("/");
        return parts.length > 2 ? parts[2] : null; // /chat/{id}
    },

    async loadCurrentUser() {
        try {
            const me = await apiService.get("/users/me");
            this.userId = me.id;
        } catch (err) {
            console.error("사용자 정보 로드 실패:", err);
            alert("로그인 정보가 필요합니다.");
            window.location.href = "/auth/login";
        }
    },

    async loadMessages() {
        try {
            const messages = await apiService.getChatMessages(this.reservationId);
            console.log(" 이전 메시지:", messages);
            messages.forEach(m =>
                this.addMessage(m.senderId, m.content, m.sentAt, m.senderName)
            );
            this.scrollToBottom();
        } catch (err) {
            console.error("메시지 불러오기 실패:", err);
        }
    },

    connectWebSocket() {
        const socket = new SockJS("/ws");
        this.stompClient = Stomp.over(socket);

        const token = this.getJwtToken();
        const headers = {};
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        this.stompClient.connect(
            headers,
            () => {
                console.log('WebSocket 연결 성공');
                this.stompClient.subscribe(
                    `/topic/reservation/${this.reservationId}`,
                    (msg) => {
                        const payload = JSON.parse(msg.body);
                        console.log("받은 메시지:", payload);

                        if (payload.error) {
                            alert(payload.error);
                            this.el.input.disabled = true;
                            this.el.sendBtn.disabled = true;
                        } else {
                            this.addMessage(
                                payload.senderId,
                                payload.content,
                                payload.sentAt,
                                payload.senderName
                            );
                            this.scrollToBottom();
                        }
                    }
                );
            },
            (error) => {
                console.error('WebSocket 연결 실패:', error);
                setTimeout(() => {
                    console.log('WebSocket 재연결 시도...');
                    this.connectWebSocket();
                }, 2000);
            }
        );

        this.stompClient.onclose = () => {
            console.log('⚠WebSocket 연결이 끊어졌습니다.');
        };
    },

    getJwtToken() {
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            const [name, value] = cookie.trim().split('=');
            if (name === 'jwtToken') {
                return value;
            }
        }
        return (
            localStorage.getItem('accessToken') ||
            localStorage.getItem('authToken') ||
            sessionStorage.getItem('accessToken') ||
            sessionStorage.getItem('authToken')
        );
    },

    sendMessage() {
        const content = this.el.input.value.trim();
        if (!content) return;

        const dto = {
            reservationId: this.reservationId,
            senderId: this.userId,  // 현재 로그인한 사용자 ID 추가
            content: content,
            sentAt: new Date().toISOString()
        };

        console.log("전송:", dto);
        this.stompClient.send("/app/chat.send", {}, JSON.stringify(dto));
        this.el.input.value = "";
    },

    addMessage(senderId, content, sentAt, senderName) {
        const div = document.createElement("div");
        div.className = senderId === this.userId ? "chat-message me" : "chat-message";

        const timeText = sentAt ? new Date(sentAt).toLocaleTimeString() : "";

        div.innerHTML = `
            <div class="sender">${senderName || '익명'}</div>
            <div class="content">${content}</div>
            <div class="time">${timeText}</div>
        `;
        this.el.messages.appendChild(div);
    },

    scrollToBottom() {
        this.el.messages.scrollTop = this.el.messages.scrollHeight;
    }
};

window.ChatPage = ChatPage;