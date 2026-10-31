// Live chat widget (users) + waiting-questions badge (admins).
// Uses short polling so it works without WebSockets.
(function () {
    const widget = document.getElementById('chat-widget');
    if (!widget) return;

    const base = widget.dataset.base || '/';
    const mode = widget.dataset.mode;
    const csrfHeader = widget.dataset.csrfHeader;
    const csrfToken = widget.dataset.csrfToken;

    async function getJson(path) {
        const res = await fetch(base + path, {headers: {'Accept': 'application/json'}});
        const type = res.headers.get('content-type') || '';
        if (!res.ok || !type.includes('application/json')) throw new Error('HTTP ' + res.status);
        return res.json();
    }

    if (mode === 'admin') {
        const badges = document.querySelectorAll('[data-chat-badge]');
        async function refreshBadge() {
            try {
                const {count} = await getJson('admin/chat/api/waiting');
                badges.forEach(b => {
                    b.textContent = count;
                    b.hidden = count === 0;
                });
            } catch (e) { /* ignore transient errors */ }
        }
        refreshBadge();
        setInterval(refreshBadge, 5000);
        return;
    }

    const toggle = widget.querySelector('.chat-toggle');
    const panel = widget.querySelector('.chat-panel');
    const closeBtn = widget.querySelector('.chat-close');
    const list = widget.querySelector('.chat-messages');
    const form = widget.querySelector('.chat-form');
    const unreadDot = widget.querySelector('.chat-unread-dot');
    const STORAGE_KEY = 'hefest-chat-open';
    const SEEN_KEY = 'hefest-chat-seen';

    let lastId = 0;
    let timer = null;

    function store(key, value) {
        try { localStorage.setItem(key, value); } catch (e) { /* storage unavailable */ }
    }
    function load(key) {
        try { return localStorage.getItem(key); } catch (e) { return null; }
    }

    function isOpen() { return !panel.hidden; }

    function setOpen(open) {
        panel.hidden = !open;
        toggle.setAttribute('aria-expanded', String(open));
        store(STORAGE_KEY, open ? '1' : '0');
        if (open) {
            unreadDot.hidden = true;
            store(SEEN_KEY, String(lastId));
            list.scrollTop = list.scrollHeight;
            const textarea = form && form.querySelector('textarea');
            if (textarea) textarea.focus();
        }
        schedule();
    }

    function render(message) {
        const item = document.createElement('div');
        item.className = 'chat-msg ' + (message.fromAdmin ? 'from-admin' : 'from-me');
        const meta = document.createElement('span');
        meta.className = 'chat-msg-meta';
        meta.textContent = (message.fromAdmin ? message.senderName + ' · екип' : 'Вие') + ' · ' + message.time;
        const body = document.createElement('p');
        body.textContent = message.content;
        item.append(meta, body);
        list.appendChild(item);
    }

    function addMessages(messages) {
        let gotAdminReply = false;
        messages.forEach(m => {
            if (m.id <= lastId) return;
            render(m);
            lastId = m.id;
            if (m.fromAdmin) gotAdminReply = true;
        });
        if (messages.length) list.scrollTop = list.scrollHeight;
        if (isOpen()) {
            store(SEEN_KEY, String(lastId));
        } else if (gotAdminReply && lastId > Number(load(SEEN_KEY) || 0)) {
            unreadDot.hidden = false;
        }
    }

    async function poll() {
        try {
            addMessages(await getJson('chat/messages?after=' + lastId));
        } catch (e) { /* ignore transient errors */ }
        schedule();
    }

    function schedule() {
        if (mode !== 'user') return;
        clearTimeout(timer);
        timer = setTimeout(poll, isOpen() ? 2500 : 10000);
    }

    toggle.addEventListener('click', () => setOpen(!isOpen()));
    closeBtn.addEventListener('click', () => setOpen(false));

    if (form) {
        const textarea = form.querySelector('textarea');
        const button = form.querySelector('button');

        textarea.addEventListener('keydown', e => {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                form.requestSubmit();
            }
        });

        form.addEventListener('submit', async e => {
            e.preventDefault();
            const content = textarea.value.trim();
            if (!content) return;
            button.disabled = true;
            try {
                const res = await fetch(base + 'chat/messages', {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json', [csrfHeader]: csrfToken},
                    body: JSON.stringify({content})
                });
                const data = await res.json();
                if (!res.ok) throw new Error(data.error || 'Грешка');
                addMessages([data]);
                textarea.value = '';
            } catch (err) {
                alert(err.message || 'Съобщението не беше изпратено.');
            } finally {
                button.disabled = false;
                textarea.focus();
            }
        });
    }

    if (mode === 'user') {
        poll();
        if (load(STORAGE_KEY) === '1') setOpen(true);
    }
})();
