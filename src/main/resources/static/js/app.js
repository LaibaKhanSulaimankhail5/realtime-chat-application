/**
 * Aura - Elite Messaging Engine
 * Professional Logic, iPhone Support, and Custom Themes
 */

const STATE = {
    token: localStorage.getItem('aura_token'),
    username: localStorage.getItem('aura_username'),
    stompClient: null,
    activeRoomId: null,
    subscriptions: {},
    isDarkMode: true,
    roomThemes: JSON.parse(localStorage.getItem('aura_room_themes') || '{}')
};

const UI = {
    get: (id) => document.getElementById(id),
    
    toggleView: (isChat) => {
        UI.get('auth-container').classList.toggle('hidden', isChat);
        UI.get('chat-container').classList.toggle('hidden', !isChat);
        if (isChat) initApp();
    },

    notify: (title, text, icon) => {
        Swal.fire({
            title, text, icon,
            background: 'rgba(20, 20, 20, 0.95)',
            color: '#fff',
            confirmButtonColor: '#fff',
            confirmButtonText: '<span style="color:#000">OK</span>',
            customClass: { popup: 'glass' }
        });
    }
};

// --- THEME LOGIC ---
const THEMES = {
    default: { bg: 'linear-gradient(135deg, #000 0%, #111 100%)', bubble: '#fff', text: '#000' },
    ocean: { bg: 'linear-gradient(135deg, #0077ff, #00d4ff)', bubble: '#fff', text: '#0077ff' },
    sunset: { bg: 'linear-gradient(135deg, #ff5f6d, #ffc371)', bubble: '#fff', text: '#ff5f6d' },
    lavender: { bg: 'linear-gradient(135deg, #834d9b, #d04ed6)', bubble: '#fff', text: '#834d9b' },
    forest: { bg: 'linear-gradient(135deg, #11998e, #38ef7d)', bubble: '#fff', text: '#11998e' }
};

function applyTheme(name) {
    if (!STATE.activeRoomId) return;
    
    const area = UI.get('message-area');
    area.className = 'chat-body theme-' + name;
    
    // Save per-room
    STATE.roomThemes[STATE.activeRoomId] = name;
    localStorage.setItem('aura_room_themes', JSON.stringify(STATE.roomThemes));

    document.querySelectorAll('.theme-dot').forEach(dot => {
        dot.classList.toggle('active', dot.dataset.theme === name);
    });
}

// --- AUTH ---
async function handleAuth(e) {
    e.preventDefault();
    const btn = UI.get('auth-submit');
    const originalText = btn.innerText;
    btn.innerHTML = '<i class="fas fa-circle-notch fa-spin"></i>';
    btn.disabled = true;

    const isRegister = !UI.get('username-group').classList.contains('hidden');
    const email = UI.get('email').value;
    const password = UI.get('password').value;
    const username = UI.get('username').value;

    const url = isRegister ? '/api/auth/register' : '/api/auth/login';
    const body = isRegister ? { username, email, password } : { email, password };

    try {
        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
        const data = await res.json();
        if (res.ok) {
            if (isRegister) {
                UI.notify('Success', 'Registration successful! Please login.', 'success');
                // Switch UI back to login mode
                UI.get('username-group').classList.add('hidden');
                UI.get('toggle-auth').innerText = "Don't have an account? Register";
                UI.get('auth-submit').innerText = 'Sign In';
                btn.innerText = 'Sign In';
                btn.disabled = false;
            } else {
                STATE.token = data.token;
                STATE.username = data.username;
                localStorage.setItem('aura_token', data.token);
                localStorage.setItem('aura_username', data.username);
                UI.toggleView(true);
            }
        } else {
            btn.innerText = originalText;
            btn.disabled = false;
            UI.notify('Access Denied', data.message || 'Error', 'error');
        }
    } catch (err) { 
        btn.innerText = originalText;
        btn.disabled = false;
        console.error(err); 
    }
}

// --- CHAT ---
function initApp() {
    UI.get('display-username').innerText = STATE.username;
    connectWS();
    fetchRooms();
}

function connectWS() {
    const socket = new SockJS('/ws');
    STATE.stompClient = Stomp.over(socket);
    STATE.stompClient.debug = null;
    STATE.stompClient.connect({ 'Authorization': 'Bearer ' + STATE.token }, () => {
        // Logged connection internally
    });
}

async function fetchRooms() {
    const res = await fetch('/api/chat/rooms', {
        headers: { 'Authorization': 'Bearer ' + STATE.token }
    });
    const rooms = await res.json();
    const list = UI.get('room-list');
    list.innerHTML = '';
    rooms.forEach(room => {
        const div = document.createElement('div');
        div.style.padding = '15px';
        div.style.borderRadius = '15px';
        div.style.cursor = 'pointer';
        div.style.marginBottom = '10px';
        div.style.background = STATE.activeRoomId === room.id ? 'rgba(255,255,255,0.1)' : 'transparent';
        div.innerHTML = `<h4 style="font-weight:600"># ${room.name}</h4>`;
        div.onclick = () => selectRoom(room);
        list.appendChild(div);
    });
}

function selectRoom(room) {
    if (STATE.activeRoomId === room.id) return;
    if (STATE.activeRoomId && STATE.subscriptions[STATE.activeRoomId]) {
        STATE.subscriptions[STATE.activeRoomId].unsubscribe();
    }
    STATE.activeRoomId = room.id;
    UI.get('active-room-name').innerText = room.name;
    UI.get('message-area').innerHTML = '';
    UI.get('input-area').classList.remove('hidden');
    fetchRooms();
    loadHistory(room.id);
    
    // Load per-room theme
    const savedTheme = STATE.roomThemes[room.id] || 'default';
    applyTheme(savedTheme);

    STATE.subscriptions[room.id] = STATE.stompClient.subscribe('/topic/room/' + room.id, (p) => {
        const msg = JSON.parse(p.body);
        const existing = document.querySelector(`[data-msg-id="${msg.id}"]`);
        if (existing) {
            updateMessageUI(existing, msg);
        } else {
            appendMessage(msg);
        }
    });
}

async function loadHistory(id) {
    const res = await fetch(`/api/chat/rooms/${id}/history`, {
        headers: { 'Authorization': 'Bearer ' + STATE.token }
    });
    const data = await res.json();
    data.content.reverse().forEach(appendMessage);
}

function appendMessage(msg) {
    const area = UI.get('message-area');
    const isSent = msg.senderUsername === STATE.username;
    const div = document.createElement('div');
    div.className = `bubble ${isSent ? 'sent' : 'received'}`;
    div.setAttribute('data-msg-id', msg.id);
    div.style.opacity = '0';
    div.style.transform = 'translateY(5px)';
    div.style.transition = '0.3s cubic-bezier(0.4, 0, 0.2, 1)';
    
    updateMessageUI(div, msg);
    
    area.appendChild(div);
    setTimeout(() => {
        div.style.opacity = '1';
        div.style.transform = 'translateY(0)';
    }, 10);
    area.scrollTop = area.scrollHeight;
}

function updateMessageUI(div, msg) {
    const isSent = msg.senderUsername === STATE.username;
    const time = new Date(msg.timestamp).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'});
    
    let contentHtml = `<div>${msg.content}</div>`;
    if (msg.deleted) {
        div.classList.add('deleted');
        contentHtml = `<div style="font-style:italic; opacity:0.6;"><i class="fas fa-ban"></i> Message deleted</div>`;
    } else if (msg.edited) {
        contentHtml += `<div style="font-size:0.7rem; opacity:0.5; margin-top:2px;">edited</div>`;
    }

    let optionsHtml = '';
    if (isSent && !msg.deleted) {
        optionsHtml = `
            <div class="msg-options">
                <i class="fas fa-ellipsis-v" onclick="showMsgMenu(${msg.id})"></i>
                <div id="menu-${msg.id}" class="msg-menu hidden glass">
                    <div onclick="initEdit(${msg.id}, '${msg.content}')"><i class="fas fa-edit"></i> Edit</div>
                    <div onclick="confirmDelete(${msg.id})" style="color:#ff4d4d"><i class="fas fa-trash"></i> Delete</div>
                </div>
            </div>
        `;
    }

    div.innerHTML = `
        <div style="display:flex; justify-content:space-between; align-items:flex-start; gap:10px;">
            <div style="flex:1">${contentHtml}</div>
            ${optionsHtml}
        </div>
        <div class="m-time">${time}</div>
    `;
}

function showMsgMenu(id) {
    const menu = UI.get(`menu-${id}`);
    const allMenus = document.querySelectorAll('.msg-menu');
    allMenus.forEach(m => { if(m !== menu) m.classList.add('hidden') });
    menu.classList.toggle('hidden');
}

async function initEdit(id, oldContent) {
    const { value: newText } = await Swal.fire({
        title: 'Edit Message',
        input: 'text',
        inputValue: oldContent,
        showCancelButton: true,
        background: 'rgba(20, 20, 20, 0.95)',
        color: '#fff'
    });

    if (newText && newText !== oldContent) {
        await fetch(`/api/chat/messages/${id}`, {
            method: 'PUT',
            headers: { 'Authorization': 'Bearer ' + STATE.token, 'Content-Type': 'application/json' },
            body: newText
        });
    }
}

async function confirmDelete(id) {
    const result = await Swal.fire({
        title: 'Delete Message?',
        text: "You won't be able to revert this!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#ff4d4d',
        cancelButtonColor: '#3085d6',
        confirmButtonText: 'Yes, delete it!',
        background: 'rgba(20, 20, 20, 0.95)',
        color: '#fff'
    });

    if (result.isConfirmed) {
        await fetch(`/api/chat/messages/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': 'Bearer ' + STATE.token }
        });
    }
}

function sendMessage(e) {
    e.preventDefault();
    const input = UI.get('message-input');
    if (!input.value.trim()) return;
    STATE.stompClient.send("/app/chat.send", {}, JSON.stringify({
        content: input.value,
        roomId: STATE.activeRoomId
    }));
    input.value = '';
}

// --- EVENTS ---
document.addEventListener('DOMContentLoaded', () => {
    if (STATE.token) UI.toggleView(true);

    UI.get('auth-submit').onclick = handleAuth;
    UI.get('message-form').onsubmit = sendMessage;
    
    UI.get('toggle-auth').onclick = () => {
        const group = UI.get('username-group');
        const text = UI.get('toggle-auth');
        const btn = UI.get('auth-submit');
        if (group.classList.contains('hidden')) {
            group.classList.remove('hidden');
            text.innerText = 'Already have an account? Login';
            btn.innerText = 'Register';
        } else {
            group.classList.add('hidden');
            text.innerText = "Don't have an account? Register";
            btn.innerText = 'Sign In';
        }
    };

    const modeToggle = UI.get('mode-toggle');
    if (modeToggle) {
        modeToggle.onclick = () => {
            STATE.isDarkMode = !STATE.isDarkMode;
            document.body.classList.toggle('light-mode-active', !STATE.isDarkMode);
            const icon = UI.get('mode-icon');
            if (icon) icon.className = STATE.isDarkMode ? 'fas fa-moon' : 'fas fa-sun';
        };
    }

    // Theme Picker
    const btnTheme = UI.get('btn-theme');
    if (btnTheme) {
        btnTheme.onclick = () => UI.get('theme-modal').classList.remove('hidden');
    }

    const themeOverlay = UI.get('theme-overlay');
    if (themeOverlay) {
        themeOverlay.onclick = () => UI.get('theme-modal').classList.add('hidden');
    }

    const closeTheme = UI.get('close-theme');
    if (closeTheme) {
        closeTheme.onclick = () => UI.get('theme-modal').classList.add('hidden');
    }

    document.querySelectorAll('.theme-dot').forEach(dot => {
        dot.onclick = () => applyTheme(dot.dataset.theme);
    });

    const btnCreateRoom = UI.get('btn-create-room');
    if (btnCreateRoom) {
        btnCreateRoom.onclick = () => UI.get('room-modal').classList.remove('hidden');
    }

    const roomOverlay = UI.get('room-overlay');
    if (roomOverlay) {
        roomOverlay.onclick = () => UI.get('room-modal').classList.add('hidden');
    }

    const btnConfirmRoom = UI.get('btn-confirm-room');
    if (btnConfirmRoom) {
        btnConfirmRoom.onclick = async () => {
            const nameInput = UI.get('new-room-name');
            const name = nameInput.value;
            if (!name) return;
            
            const res = await fetch('/api/chat/rooms', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + STATE.token },
                body: JSON.stringify({ name })
            });
            if (res.ok) { 
                UI.get('room-modal').classList.add('hidden'); 
                nameInput.value = '';
                fetchRooms(); 
            }
        };
    }

    const logoutBtn = UI.get('logout-btn');
    if (logoutBtn) {
        logoutBtn.onclick = () => { localStorage.clear(); location.reload(); };
    }
});
