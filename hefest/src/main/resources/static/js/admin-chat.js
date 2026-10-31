// Admin inbox: lists conversations and lets the admin answer in real time (short polling).
(function () {
    const root = document.getElementById('admin-chat');
    if (!root) return;

    const base = root.dataset.base || '/';
    const csrfHeader = root.dataset.csrfHeader;
    const csrfToken = root.dataset.csrfToken;

    const listEl = root.querySelector('.admin-chat-list');
    const emptyEl = root.querySelector('.admin-chat-empty');
    const userEl = root.querySelector('.admin-chat-user');
    const emailEl = root.querySelector('.admin-chat-email');
    const messagesEl = root.querySelector('.admin-chat-messages');
    const form = root.querySelector('.admin-chat-form');
    const textarea = form.querySelector('textarea');
    const button = form.querySelector('button');

    let selectedId = null;
    let lastId = 0;
    const originalTitle = document.title;

    async function getJson(path) {
        const res = await fetch(base + path, {headers: {'Accept': 'application/json'}});
        const type = res.headers.get('content-type') || '';
        if (!res.ok || !type.includes('application/json')) throw new Error('HTTP ' + res.status);
        return res.json();
    }

    function renderConversations(conversations) {
        listEl.querySelectorAll('.admin-chat-item').forEach(el => el.remove());
        emptyEl.hidden = conversations.length > 0;

        const waiting = conversations.filter(c => c.waiting).length;
        document.title = waiting ? '(' + waiting + ') ' + originalTitle : originalTitle;

        conversations.forEach(c => {
            const item = document.createElement('button');
            item.type = 'button';
            item.className = 'admin-chat-item' + (c.id === selectedId ? ' is-active' : '') + (c.waiting ? ' is-waiting' : '');

            const top = document.createElement('span');
            top.className = 'admin-chat-item-top';
            const name = document.createElement('strong');
            name.textContent = c.userName;
            const time = document.createElement('span');
            time.textContent = c.time;
            top.append(name, time);

            const preview = document.createElement('span');
            preview.className = 'admin-chat-item-preview';
            preview.textContent = c.preview || '';

            item.append(top, preview);
            if (c.waiting) {
                const tag = document.createElement('span');
                tag.className = 'admin-chat-item-tag';
                tag.textContent = 'ЧАКА ОТГОВОР';
                item.append(tag);
            }
            item.addEventListener('click', () => select(c));
            listEl.appendChild(item);
        });
    }

    function renderMessage(m) {
        const item = document.createElement('div');
        // From the admin's point of view, admin messages are "mine".
        item.className = 'chat-msg ' + (m.fromAdmin ? 'from-me' : 'from-admin');
        const meta = document.createElement('span');
        meta.className = 'chat-msg-meta';
        meta.textContent = m.senderName + (m.fromAdmin ? ' · екип' : '') + ' · ' + m.time;
        const body = document.createElement('p');
        body.textContent = m.content;
        item.append(meta, body);
        messagesEl.appendChild(item);
    }

    function addMessages(messages) {
        messages.forEach(m => {
            if (m.id <= lastId) return;
            renderMessage(m);
            lastId = m.id;
        });
        if (messages.length) messagesEl.scrollTop = messagesEl.scrollHeight;
    }

    async function select(conversation) {
        selectedId = conversation.id;
        lastId = 0;
        messagesEl.replaceChildren();
        userEl.textContent = conversation.userName;
        emailEl.textContent = conversation.userEmail;
        form.hidden = false;
        listEl.querySelectorAll('.admin-chat-item').forEach(el => el.classList.remove('is-active'));
        await refreshMessages();
        textarea.focus();
        refreshConversations();
    }

    async function refreshConversations() {
        try {
            renderConversations(await getJson('admin/chat/api/conversations'));
        } catch (e) { /* ignore transient errors */ }
    }

    async function refreshMessages() {
        if (selectedId == null) return;
        const id = selectedId;
        try {
            const messages = await getJson('admin/chat/api/conversations/' + id + '/messages?after=' + lastId);
            if (id === selectedId) addMessages(messages);
        } catch (e) { /* ignore transient errors */ }
    }

    textarea.addEventListener('keydown', e => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            form.requestSubmit();
        }
    });

    form.addEventListener('submit', async e => {
        e.preventDefault();
        const content = textarea.value.trim();
        if (!content || selectedId == null) return;
        button.disabled = true;
        try {
            const res = await fetch(base + 'admin/chat/api/conversations/' + selectedId + '/messages', {
                method: 'POST',
                headers: {'Content-Type': 'application/json', [csrfHeader]: csrfToken},
                body: JSON.stringify({content})
            });
            const data = await res.json();
            if (!res.ok) throw new Error(data.error || 'Грешка');
            addMessages([data]);
            textarea.value = '';
            refreshConversations();
        } catch (err) {
            alert(err.message || 'Отговорът не беше изпратен.');
        } finally {
            button.disabled = false;
            textarea.focus();
        }
    });

    refreshConversations();
    setInterval(refreshConversations, 3000);
    setInterval(refreshMessages, 2000);
})();
