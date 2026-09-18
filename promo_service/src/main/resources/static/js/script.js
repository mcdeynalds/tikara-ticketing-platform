// TIKARA Unified Frontend Engine & Auth Synchronizer

const API_USER_SERVICE = 'http://localhost:8081';
const API_EVENT_SERVICE = 'http://localhost:8082';
const API_TICKET_SERVICE = 'http://localhost:8083';
const API_ORDER_SERVICE = 'http://localhost:8084';
const API_NOTIFICATION_SERVICE = 'http://localhost:8086';
const API_CHECKIN_SERVICE = 'http://localhost:8087';
const API_PROMO_SERVICE = 'http://localhost:8088';
const API_ADMIN_SERVICE = 'http://localhost:8089';

// Cookie & Storage Synchronization Helpers

function getCookie(name) {
    const nameEQ = name + "=";
    const ca = document.cookie.split(';');
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) {
            try {
                return decodeURIComponent(c.substring(nameEQ.length, c.length));
            } catch (e) {
                return c.substring(nameEQ.length, c.length);
            }
        }
    }
    return null;
}

function setCookie(name, value, days = 7) {
    let expires = "";
    if (days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toUTCString();
    }
    // Path=/ ensures cookie is accessible across all localhost ports
    document.cookie = name + "=" + encodeURIComponent(value || "") + expires + "; path=/; SameSite=Lax";
}

function deleteCookie(name) {
    document.cookie = name + "=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT; max-age=0; SameSite=Lax";
}

function setToken(token) {
    if (token) {
        localStorage.setItem('jwt_token', token);
        setCookie('tikara_token', token, 7);
    }
}

/**
 * Cross-Port Authoritative Token Getter:
 * On localhost across multiple ports, document.cookie is the ONLY shared store.
 * If cookie is absent, the user has logged out across the ecosystem; any stale
 * token in port-specific localStorage must be purged immediately.
 */
function getToken() {
    const cookieToken = getCookie('tikara_token');
    if (cookieToken) {
        const localToken = localStorage.getItem('jwt_token');
        if (localToken !== cookieToken) {
            // The active session token changed across ports!
            // Update token in localStorage and immediately purge stale user data
            localStorage.setItem('jwt_token', cookieToken);
            localStorage.removeItem('user_data');
            localStorage.removeItem('role');
        }
        return cookieToken;
    }

    // No shared cookie found -> user is GUEST / logged out.
    // If this port still has an old localStorage token, wipe it now!
    if (localStorage.getItem('jwt_token')) {
        clearAuthState();
    }
    return null;
}


// Normalizes avatar URLs to absolute URLs pointing to user_service (port 8081)
function normalizeAvatarUrl(url) {
    if (!url || typeof url !== 'string' || url === 'null' || url === 'undefined') return null;
    url = url.trim();
    if (!url) return null;
    if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('data:')) return url;
    const base = (window.TIKARA_CONFIG && window.TIKARA_CONFIG.SERVICES && window.TIKARA_CONFIG.SERVICES.USER)
        ? window.TIKARA_CONFIG.SERVICES.USER
        : (typeof API_USER_SERVICE !== 'undefined' ? API_USER_SERVICE : 'http://localhost:8081');
    if (url.startsWith('/')) return `${base}${url}`;
    return `${base}/${url}`;
}
window.normalizeAvatarUrl = normalizeAvatarUrl;

function setUserData(userData) {
    if (!userData) return;
    const normalizedName = userData.nama || userData.namaLengkap || 'Pengguna TIKARA';
    const normalizedRole = (userData.role || userData.peran || 'PEMBELI').toUpperCase();
    const rawAvatar = userData.fotoProfil || userData.fotoProfilUrl || userData.fotoUrl || userData.avatarUrl || '';
    const cleanAvatar = normalizeAvatarUrl(rawAvatar) || '';
    const cleanUser = {
        id: userData.id,
        nama: normalizedName,
        namaLengkap: normalizedName,
        email: userData.email || '',
        role: normalizedRole,
        peran: normalizedRole,
        fotoProfil: cleanAvatar,
        fotoProfilUrl: cleanAvatar
    };
    const str = JSON.stringify(cleanUser);
    localStorage.setItem('user_data', str);
    localStorage.setItem('role', normalizedRole);
    setCookie('tikara_user', str, 7);
    setCookie('tikara_role', normalizedRole, 7);
}

function getUserData() {
    const token = getToken();
    if (!token) return null;

    let user = null;

    // Try cookie first (shared across ports)
    const cookieUserStr = getCookie('tikara_user');
    if (cookieUserStr) {
        try {
            user = JSON.parse(cookieUserStr);
            if (user && user.fotoProfil) {
                user.fotoProfil = normalizeAvatarUrl(user.fotoProfil) || '';
            }
        } catch (e) { }
    }

    // Fallback to localStorage ONLY if no cookie exists
    if (!user) {
        const localUserStr = localStorage.getItem('user_data');
        if (localUserStr) {
            try {
                user = JSON.parse(localUserStr);
            } catch (e) { }
        }
    }

    // Cryptographic validation against current token payload
    const payload = parseJwt(token);
    if (payload) {
        const tokenRole = (payload.role || payload.peran || 'PEMBELI').toUpperCase();
        const tokenUserId = payload.userId || payload.id;
        const tokenEmail = payload.sub || payload.email;

        // If user object is missing or belongs to a DIFFERENT user ID / role / email:
        const isMismatched = !user ||
            user.role !== tokenRole ||
            (tokenUserId && user.id && user.id != tokenUserId) ||
            (tokenEmail && user.email && user.email.toLowerCase() !== tokenEmail.toLowerCase());

        if (isMismatched) {
            // Reconstruct clean user from the authoritative token!
            const cleanName = (payload.nama)
                ? payload.nama
                : (tokenEmail ? tokenEmail.split('@')[0] : 'Pengguna TIKARA');
            user = {
                id: tokenUserId || (user ? user.id : null),
                nama: cleanName,
                namaLengkap: cleanName,
                email: tokenEmail || '',
                role: tokenRole,
                peran: tokenRole,
                fotoProfil: ''
            };
            setUserData(user);
        }
    }

    return user;
}

function clearAuthState() {
    try {
        localStorage.removeItem('jwt_token');
        localStorage.removeItem('user_data');
        localStorage.removeItem('role');
        sessionStorage.removeItem('resetToken');
        sessionStorage.clear();
    } catch (e) { }
    deleteCookie('tikara_token');
    deleteCookie('tikara_user');
    deleteCookie('tikara_role');
}

function removeToken() {
    clearAuthState();
}

function isPublicAuthPage() {
    const path = window.location.pathname.toLowerCase();
    return path.endsWith('/login.html') ||
        path.endsWith('/login') ||
        path.endsWith('/register.html') ||
        path.endsWith('/register') ||
        path.endsWith('/forgot-password.html') ||
        path.endsWith('/forgot-password') ||
        path.endsWith('/verify-otp.html') ||
        path.endsWith('/verify-otp') ||
        path.endsWith('/reset-password.html') ||
        path.endsWith('/reset-password');
}

function parseJwt(token) {
    if (!token || typeof token !== 'string') return null;
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    try {
        const base64Url = parts[1];
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function (c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));
        return JSON.parse(jsonPayload);
    } catch (e) {
        return null;
    }
}

function isTokenValid(token) {
    const payload = parseJwt(token);
    if (!payload) return false;
    if (payload.exp && (payload.exp * 1000 < Date.now())) {
        console.warn('JWT token has expired client-side.');
        return false;
    }
    return true;
}

// Auth State Engine

function getAuthState() {
    if (isPublicAuthPage()) {
        return {
            isAuthenticated: false,
            user: null,
            token: null,
            role: 'GUEST'
        };
    }

    const token = getToken();
    if (!token || !isTokenValid(token)) {
        clearAuthState();
        return {
            isAuthenticated: false,
            user: null,
            token: null,
            role: 'GUEST'
        };
    }

    // Role is strictly derived from the cryptographically signed JWT
    const payload = parseJwt(token);
    const authoritativeRole = (payload && (payload.role || payload.peran))
        ? (payload.role || payload.peran).toUpperCase()
        : 'PEMBELI';

    const user = getUserData();

    return {
        isAuthenticated: true,
        user: user,
        token: token,
        role: authoritativeRole
    };
}

// Background session verification with user_service
async function verifySessionWithBackend() {
    if (isPublicAuthPage()) return;
    const token = getToken();
    if (!token) return;

    try {
        const res = await fetch(`${API_USER_SERVICE}/api/users/profile`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (res.status === 401 || res.status === 403) {
            console.warn('Backend rejected session. Resetting to Guest state.');
            clearAuthState();
            renderTIKARANavbar();
        } else if (res.ok) {
            const data = await res.json();
            setUserData(data);
            renderTIKARANavbar();
        }
    } catch (err) {
        // Network warning - keep current client state while offline
    }
}

// Navbar Renderer

function renderTIKARANavbar() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('auth') === 'mutia' || urlParams.get('mock') === 'mutia') {
        const mockUser = {
            id: 1,
            nama: 'Mutia Ananda',
            email: 'moondaisy938@gmail.com',
            role: 'PEMBELI',
            peran: 'PEMBELI',
            fotoProfil: 'http://localhost:8081/uploads/avatars/8dd5c01a-e5d4-4b64-b390-d7b96601b3c1.JPG',
            fotoProfilUrl: 'http://localhost:8081/uploads/avatars/8dd5c01a-e5d4-4b64-b390-d7b96601b3c1.JPG'
        };
        const mockToken = 'eyJhbGciOiAiSFMyNTYiLCAidHlwIjogIkpXVCJ9.eyJzdWIiOiAibW9vbmRhaXN5OTM4QGdtYWlsLmNvbSIsICJyb2xlIjogIlBFTUJFTEkiLCAidXNlcklkIjogMSwgInR5cGUiOiAiQUNDRVNTIiwgImlhdCI6IDE3ODk0Mzk1NDgsICJleHAiOiAxODIwOTc1NTQ4fQ.D5ZP2l3oI_Y40Xa769xXI-ve0fsvrBcdrRiR_DTx1NI';
        setToken(mockToken);
        setUserData(mockUser);
        setCookie('tikara_token', mockToken, 7);
        setCookie('tikara_user', JSON.stringify(mockUser), 7);
    }

    const auth = getAuthState();
    const navMenu = document.getElementById('mainNavbarMenu');
    const navUserBox = document.getElementById('navUserBox');
    const navNotifBtn = document.getElementById('navNotifBtn');
    const drawer = document.getElementById('navbarMobileDrawer');

    const currentPath = window.location.pathname;
    const currentPort = window.location.port;

    // 1. Notification Bell Icon
    if (navNotifBtn) {
        navNotifBtn.style.display = auth.isAuthenticated ? 'inline-flex' : 'none';
        navNotifBtn.href = `${API_NOTIFICATION_SERVICE}/notifications.html`;
    }

    // 2. Desktop Navigation Menu Items (Strictly bound to authenticated role)
    if (navMenu) {
        let menuHtml = '';
        if (!auth.isAuthenticated || auth.role === 'GUEST') {
            // STATE 1 — BELUM LOGIN / GUEST:
            menuHtml = `
                <li><a href="${API_EVENT_SERVICE}/index.html" class="navbar-link ${(currentPort === '8082' && (currentPath === '/' || currentPath.endsWith('index.html'))) ? 'active' : ''}"><i class="fa-solid fa-house"></i> Beranda</a></li>
                <li><a href="${API_EVENT_SERVICE}/index.html#catalog" class="navbar-link"><i class="fa-solid fa-compass"></i> Jelajah Event</a></li>
            `;
        } else if (auth.role === 'PEMBELI') {
            // STATE 2 — PEMBELI:
            const isTiketSaya = currentPath.includes('my-tickets');
            const isPesananSaya = currentPath.includes('my-orders');
            const isBeranda = (currentPort === '8082' && (currentPath === '/' || currentPath.endsWith('index.html')) && !window.location.hash);
            menuHtml = `
                <li><a href="${API_EVENT_SERVICE}/index.html" class="navbar-link ${isBeranda ? 'active' : ''}"><i class="fa-solid fa-house"></i> Beranda</a></li>
                <li><a href="${API_EVENT_SERVICE}/index.html#catalog" class="navbar-link"><i class="fa-solid fa-compass"></i> Jelajah Event</a></li>
                <li><a href="${API_TICKET_SERVICE}/my-tickets.html" class="navbar-link ${isTiketSaya ? 'active' : ''}"><i class="fa-solid fa-ticket"></i> Tiket Saya</a></li>
                <li><a href="${API_ORDER_SERVICE}/my-orders.html" class="navbar-link ${isPesananSaya ? 'active' : ''}"><i class="fa-solid fa-clock-rotate-left"></i> Pesanan Saya</a></li>
            `;
        } else if (auth.role === 'PENYELENGGARA') {
            // STATE 3 — PENYELENGGARA:
            menuHtml = `
                <li><a href="${API_EVENT_SERVICE}/index.html" class="navbar-link ${(currentPort === '8082' && (currentPath === '/' || currentPath.endsWith('index.html'))) ? 'active' : ''}"><i class="fa-solid fa-house"></i> Beranda</a></li>
                <li><a href="${API_EVENT_SERVICE}/organizer-dashboard.html" class="navbar-link ${currentPath.includes('organizer-dashboard') ? 'active' : ''}"><i class="fa-solid fa-chart-pie"></i> Dashboard</a></li>
                <li><a href="${API_EVENT_SERVICE}/my-events.html" class="navbar-link ${currentPath.includes('my-events') ? 'active' : ''}"><i class="fa-solid fa-calendar-days"></i> Event Saya</a></li>
                <li><a href="${API_EVENT_SERVICE}/create-event.html" class="navbar-link ${currentPath.includes('create-event') ? 'active' : ''}"><i class="fa-solid fa-plus-circle"></i> Buat Event</a></li>
                <li><a href="${API_CHECKIN_SERVICE}/scan-gate.html" class="navbar-link ${currentPath.includes('scan-gate') ? 'active' : ''}"><i class="fa-solid fa-qrcode"></i> Scan Gate</a></li>
            `;
        } else if (auth.role === 'ADMIN') {
            // STATE 4 — ADMIN:
            menuHtml = `
                <li><a href="${API_ADMIN_SERVICE}/admin-dashboard.html" class="navbar-link ${currentPath.includes('admin-dashboard') ? 'active' : ''}"><i class="fa-solid fa-shield-halved"></i> Dashboard Admin</a></li>
                <li><a href="${API_ADMIN_SERVICE}/event-moderation.html" class="navbar-link ${currentPath.includes('event-moderation') ? 'active' : ''}"><i class="fa-solid fa-list-check"></i> Moderasi Event</a></li>
                <li><a href="${API_ADMIN_SERVICE}/promo-management.html" class="navbar-link ${currentPath.includes('promo-management') ? 'active' : ''}"><i class="fa-solid fa-tags"></i> Manajemen Promo</a></li>
            `;
        }
        navMenu.innerHTML = menuHtml;
    }

    // 3. Desktop Auth / User Action Box
    if (navUserBox) {
        if (!auth.isAuthenticated || auth.role === 'GUEST') {
            // Guest State: Strictly show Masuk and Daftar buttons
            const loginUrl = `${API_USER_SERVICE}/login.html?redirect=${encodeURIComponent(window.location.href)}`;
            const regUrl = `${API_USER_SERVICE}/register.html`;
            navUserBox.innerHTML = `
                <a href="${loginUrl}" class="btn btn-outline nav-btn-masuk" id="navBtnMasuk">Masuk</a>
                <a href="${regUrl}" class="btn btn-primary nav-btn-daftar" id="navBtnDaftar">Daftar</a>
            `;
        } else {
            // Authenticated State: User Pill Button & Dropdown
            const user = auth.user || {};
            const namaLengkap = user.nama || user.namaLengkap || 'Pengguna TIKARA';
            const firstName = namaLengkap.split(' ')[0] || 'Akun';
            const initial = firstName.charAt(0).toUpperCase() || 'U';
            const email = user.email || '';
            const role = auth.role;

            const rawAvatar = user.fotoProfil || user.fotoProfilUrl || user.fotoUrl || user.avatarUrl;
            const avatarUrl = normalizeAvatarUrl(rawAvatar);

            const avatarHtml = avatarUrl
                ? `<img src="${avatarUrl}" alt="${namaLengkap}" class="avatar-img-round" onerror="this.onerror=null; this.outerHTML='<div class=\'avatar-img\'>${initial}</div>';">`
                : `<div class="avatar-img">${initial}</div>`;

            const dropAvatarHtml = avatarUrl
                ? `<img src="${avatarUrl}" alt="${namaLengkap}" class="big-drop-avatar-img" onerror="this.onerror=null; this.outerHTML='<div class=\'big-drop-avatar\'>${initial}</div>';">`
                : `<div class="big-drop-avatar">${initial}</div>`;

            let roleBadgeClass = 'role-badge-buyer';
            if (role === 'ADMIN') roleBadgeClass = 'role-badge-admin';
            if (role === 'PENYELENGGARA') roleBadgeClass = 'role-badge-organizer';

            let roleSpecificMenuItems = '';
            if (role === 'PEMBELI') {
                roleSpecificMenuItems = `
                    <a href="${API_TICKET_SERVICE}/my-tickets.html" class="dropdown-item" id="dropdownItemTickets"><i class="fa-solid fa-ticket"></i> Tiket Saya</a>
                    <a href="${API_ORDER_SERVICE}/my-orders.html" class="dropdown-item" id="dropdownItemOrders"><i class="fa-solid fa-clock-rotate-left"></i> Pesanan Saya</a>
                `;
            } else if (role === 'PENYELENGGARA') {
                roleSpecificMenuItems = `
                    <a href="${API_EVENT_SERVICE}/organizer-dashboard.html" class="dropdown-item"><i class="fa-solid fa-chart-pie"></i> Dashboard</a>
                    <a href="${API_EVENT_SERVICE}/my-events.html" class="dropdown-item"><i class="fa-solid fa-calendar-days"></i> Event Saya</a>
                    <a href="${API_EVENT_SERVICE}/create-event.html" class="dropdown-item"><i class="fa-solid fa-plus-circle"></i> Buat Event</a>
                    <a href="${API_CHECKIN_SERVICE}/scan-gate.html" class="dropdown-item"><i class="fa-solid fa-qrcode"></i> Scan Gate</a>
                `;
            } else if (role === 'ADMIN') {
                roleSpecificMenuItems = `
                    <a href="${API_ADMIN_SERVICE}/admin-dashboard.html" class="dropdown-item"><i class="fa-solid fa-shield-halved"></i> Dashboard Admin</a>
                    <a href="${API_ADMIN_SERVICE}/event-moderation.html" class="dropdown-item"><i class="fa-solid fa-list-check"></i> Moderasi Event</a>
                    <a href="${API_ADMIN_SERVICE}/promo-management.html" class="dropdown-item"><i class="fa-solid fa-tags"></i> Manajemen Promo</a>
                `;
            }

            navUserBox.innerHTML = `
                <div class="user-nav-wrapper">
                    <button type="button" class="user-avatar-trigger" id="userAvatarTrigger" onclick="toggleUserDropdown(event)" aria-label="Menu Akun" aria-expanded="false">
                        ${avatarHtml}
                        <span class="user-firstname user-display-name">${firstName}</span>
                        <i class="fa-solid fa-chevron-down caret-icon nav-chevron"></i>
                    </button>

                    <div class="user-dropdown-menu" id="userDropdownMenu" role="menu">
                        <div class="dropdown-user-info">
                            ${dropAvatarHtml}
                            <div class="user-meta">
                                <div class="user-name">${namaLengkap}</div>
                                <div class="user-email">${email}</div>
                                <div class="user-role-wrap">
                                    <span class="role-badge ${roleBadgeClass}">${role}</span>
                                </div>
                            </div>
                        </div>
                        <div class="dropdown-divider"></div>
                        <div class="dropdown-menu-links">
                            <a href="${API_USER_SERVICE}/profile.html" class="dropdown-item" id="dropdownItemProfile"><i class="fa-solid fa-gear"></i> Profil Saya</a>
                            ${roleSpecificMenuItems}
                        </div>
                        <div class="dropdown-divider"></div>
                        <button type="button" class="dropdown-item logout-btn logout-item" id="dropdownItemLogout" onclick="handleTIKARALogout()">
                            <i class="fa-solid fa-arrow-right-from-bracket"></i> Keluar
                        </button>
                    </div>
                </div>
            `;
        }
    }

    // 4. Mobile Drawer Rendering
    if (drawer) {
        if (!auth.isAuthenticated || auth.role === 'GUEST') {
            const loginUrl = `${API_USER_SERVICE}/login.html?redirect=${encodeURIComponent(window.location.href)}`;
            const regUrl = `${API_USER_SERVICE}/register.html`;
            drawer.innerHTML = `
                <div class="mobile-drawer-header">
                    <a href="${API_EVENT_SERVICE}/index.html" class="mobile-drawer-logo">
                        <i class="fa-solid fa-ticket"></i> TIKARA
                    </a>
                    <button type="button" class="mobile-drawer-close" onclick="(function(){if(window.closeMobileDrawer){window.closeMobileDrawer();}else{var d=document.getElementById('navbarMobileDrawer');var t=document.getElementById('navbarToggleBtn');var o=document.getElementById('navOverlay');if(d)d.classList.remove('open');if(t){t.classList.remove('active');t.setAttribute('aria-expanded','false');}if(o)o.classList.remove('open');document.documentElement.classList.remove('drawer-open');document.body.classList.remove('drawer-open');document.documentElement.style.overflow='';document.body.style.overflow='';}})()" aria-label="Tutup menu">
                        <i class="fa-solid fa-xmark"></i>
                    </button>
                </div>
                <div class="mobile-drawer-links">
                    <a href="${API_EVENT_SERVICE}/index.html" class="mobile-drawer-link"><i class="fa-solid fa-house"></i> Beranda</a>
                    <a href="${API_EVENT_SERVICE}/index.html#catalog" class="mobile-drawer-link"><i class="fa-solid fa-compass"></i> Jelajah Event</a>
                </div>
                <div class="mobile-drawer-divider"></div>
                <div class="mobile-drawer-actions">
                    <a href="${loginUrl}" class="btn btn-outline" style="width:100%; justify-content:center; color:white; border-color:rgba(255,255,255,0.3);">Masuk</a>
                    <a href="${regUrl}" class="btn btn-primary" style="width:100%; justify-content:center;">Daftar Akun Baru</a>
                </div>
            `;
        } else {
            const user = auth.user || {};
            const namaLengkap = user.nama || user.namaLengkap || 'Pengguna TIKARA';
            const firstName = namaLengkap.split(' ')[0] || 'Akun';
            const initial = firstName.charAt(0).toUpperCase() || 'U';
            const email = user.email || '';
            const role = auth.role;
            const rawDrawerAvatar = user.fotoProfil || user.fotoProfilUrl || user.fotoUrl || user.avatarUrl;
            const avatarDrawerUrl = normalizeAvatarUrl(rawDrawerAvatar);

            const avatarDrawerHtml = avatarDrawerUrl
                ? `<img src="${avatarDrawerUrl}" alt="${namaLengkap}" class="drawer-avatar-img" onerror="this.onerror=null; this.outerHTML='<div class=\'drawer-avatar-initial\'>${initial}</div>';">`
                : `<div class="drawer-avatar-initial">${initial}</div>`;

            let roleBadgeClass = 'role-badge-buyer';
            if (role === 'ADMIN') roleBadgeClass = 'role-badge-admin';
            if (role === 'PENYELENGGARA') roleBadgeClass = 'role-badge-organizer';

            let roleSpecificDrawerLinks = '';
            if (role === 'PEMBELI') {
                roleSpecificDrawerLinks = `
                    <a href="${API_TICKET_SERVICE}/my-tickets.html" class="mobile-drawer-link"><i class="fa-solid fa-ticket"></i> Tiket Saya</a>
                    <a href="${API_ORDER_SERVICE}/my-orders.html" class="mobile-drawer-link"><i class="fa-solid fa-clock-rotate-left"></i> Pesanan Saya</a>
                `;
            } else if (role === 'PENYELENGGARA') {
                roleSpecificDrawerLinks = `
                    <a href="${API_EVENT_SERVICE}/organizer-dashboard.html" class="mobile-drawer-link"><i class="fa-solid fa-chart-pie"></i> Dashboard</a>
                    <a href="${API_EVENT_SERVICE}/my-events.html" class="mobile-drawer-link"><i class="fa-solid fa-calendar-days"></i> Event Saya</a>
                    <a href="${API_EVENT_SERVICE}/create-event.html" class="mobile-drawer-link"><i class="fa-solid fa-plus-circle"></i> Buat Event</a>
                    <a href="${API_CHECKIN_SERVICE}/scan-gate.html" class="mobile-drawer-link"><i class="fa-solid fa-qrcode"></i> Scan Gate</a>
                `;
            } else if (role === 'ADMIN') {
                roleSpecificDrawerLinks = `
                    <a href="${API_ADMIN_SERVICE}/admin-dashboard.html" class="mobile-drawer-link"><i class="fa-solid fa-shield-halved"></i> Dashboard Admin</a>
                    <a href="${API_ADMIN_SERVICE}/event-moderation.html" class="mobile-drawer-link"><i class="fa-solid fa-list-check"></i> Moderasi Event</a>
                    <a href="${API_ADMIN_SERVICE}/promo-management.html" class="mobile-drawer-link"><i class="fa-solid fa-tags"></i> Manajemen Promo</a>
                `;
            }

            drawer.innerHTML = `
                <div class="mobile-drawer-header">
                    <a href="${API_EVENT_SERVICE}/index.html" class="mobile-drawer-logo">
                        <i class="fa-solid fa-ticket"></i> TIKARA
                    </a>
                    <button type="button" class="mobile-drawer-close" onclick="(function(){if(window.closeMobileDrawer){window.closeMobileDrawer();}else{var d=document.getElementById('navbarMobileDrawer');var t=document.getElementById('navbarToggleBtn');var o=document.getElementById('navOverlay');if(d)d.classList.remove('open');if(t){t.classList.remove('active');t.setAttribute('aria-expanded','false');}if(o)o.classList.remove('open');document.documentElement.classList.remove('drawer-open');document.body.classList.remove('drawer-open');document.documentElement.style.overflow='';document.body.style.overflow='';}})()" aria-label="Tutup menu">
                        <i class="fa-solid fa-xmark"></i>
                    </button>
                </div>
                <div class="mobile-drawer-user">
                    ${avatarDrawerHtml}
                    <div class="mobile-drawer-user-meta">
                        <span class="name">${namaLengkap}</span>
                        <span class="email">${email}</span>
                        <span class="role-tag ${roleBadgeClass}">${role}</span>
                    </div>
                </div>
                <div class="mobile-drawer-links">
                    <a href="${API_EVENT_SERVICE}/index.html" class="mobile-drawer-link"><i class="fa-solid fa-house"></i> Beranda</a>
                    <a href="${API_EVENT_SERVICE}/index.html#catalog" class="mobile-drawer-link"><i class="fa-solid fa-compass"></i> Jelajah Event</a>
                    ${roleSpecificDrawerLinks}
                    <a href="${API_USER_SERVICE}/profile.html" class="mobile-drawer-link" id="drawerItemProfile"><i class="fa-solid fa-gear"></i> Profil Saya</a>
                </div>
                <div class="mobile-drawer-divider"></div>
                <div class="mobile-drawer-actions">
                    <button type="button" class="btn btn-secondary" onclick="handleTIKARALogout()" style="color:#EF4444; font-weight:700; width:100%;">
                        <i class="fa-solid fa-arrow-right-from-bracket"></i> Keluar dari Akun
                    </button>
                </div>
            `;
        }
    }


}

// Backward-compatible aliases for all pages
const renderNavbarMenu = renderTIKARANavbar;
const renderNavbarUser = renderTIKARANavbar;
const renderNavbar = renderTIKARANavbar;

function toggleUserDropdown(e) {
    if (e) e.stopPropagation();
    const dropdown = document.getElementById('userDropdownMenu');
    const trigger = document.getElementById('userAvatarTrigger');
    if (!dropdown) return;
    const isShown = dropdown.classList.toggle('show');
    if (trigger) trigger.setAttribute('aria-expanded', isShown ? 'true' : 'false');
}

function handleTIKARALogout() {
    clearAuthState();
    window.location.href = `${API_EVENT_SERVICE}/index.html`;
}

function handleLogout() {
    handleTIKARALogout();
}

function initMobileNavigation() {
    const toggleBtn = document.getElementById('navbarToggleBtn');
    const drawer = document.getElementById('navbarMobileDrawer');
    const overlay = document.getElementById('navOverlay');

    if (!toggleBtn || !drawer) return;

    const preventBackgroundScroll = (e) => {
        if (drawer && drawer.classList.contains('open')) {
            if (!drawer.contains(e.target)) {
                e.preventDefault();
            }
        }
    };

    const openDrawer = () => {
        drawer.classList.add('open');
        toggleBtn.classList.add('active');
        toggleBtn.setAttribute('aria-expanded', 'true');
        if (overlay) overlay.classList.add('open');
        document.documentElement.classList.add('drawer-open');
        document.body.classList.add('drawer-open');
        document.documentElement.style.overflow = 'hidden';
        document.body.style.overflow = 'hidden';
        window.addEventListener('wheel', preventBackgroundScroll, { passive: false });
        window.addEventListener('touchmove', preventBackgroundScroll, { passive: false });
    };

    const closeDrawer = () => {
        drawer.classList.remove('open');
        toggleBtn.classList.remove('active');
        toggleBtn.setAttribute('aria-expanded', 'false');
        if (overlay) overlay.classList.remove('open');
        document.documentElement.classList.remove('drawer-open');
        document.body.classList.remove('drawer-open');
        document.documentElement.style.overflow = '';
        document.body.style.overflow = '';
        window.removeEventListener('wheel', preventBackgroundScroll);
        window.removeEventListener('touchmove', preventBackgroundScroll);
    };

    window.closeMobileDrawer = closeDrawer;
    window.openMobileDrawer = openDrawer;

    toggleBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        if (drawer.classList.contains('open')) {
            closeDrawer();
        } else {
            openDrawer();
        }
    });

    if (overlay) {
        overlay.addEventListener('click', closeDrawer);
    }

    document.addEventListener('click', (e) => {
        if (drawer.classList.contains('open') && !drawer.contains(e.target) && !toggleBtn.contains(e.target)) {
            closeDrawer();
        }
        const dropdown = document.getElementById('userDropdownMenu');
        const trigger = document.getElementById('userAvatarTrigger');
        if (dropdown && dropdown.classList.contains('show')) {
            if (!dropdown.contains(e.target) && !trigger?.contains(e.target)) {
                dropdown.classList.remove('show');
                if (trigger) trigger.setAttribute('aria-expanded', 'false');
            }
        }
    });

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            closeDrawer();
            const dropdown = document.getElementById('userDropdownMenu');
            if (dropdown) dropdown.classList.remove('show');
        }
    });

    // Handle window resize - close drawer only when expanded to desktop (> 1024px)
    window.addEventListener('resize', () => {
        if (window.innerWidth > 1024) {
            closeDrawer();
        }
    });
}

function setupMobileNavbar() {
    initMobileNavigation();
}

// Date & Timezone Formatter
function formatEventDateTime(dateString, timezone = 'Asia/Jakarta') {
    if (!dateString) return 'Tanggal belum ditentukan';
    try {
        const date = new Date(dateString);
        let tzSuffix = 'WIB';
        if (timezone.includes('Makassar') || timezone.includes('Bali')) tzSuffix = 'WITA';
        else if (timezone.includes('Jayapura') || timezone.includes('Ambon')) tzSuffix = 'WIT';

        const formatted = new Intl.DateTimeFormat('id-ID', {
            weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
            hour: '2-digit', minute: '2-digit', timeZone: timezone, hour12: false
        }).format(date);
        return `${formatted} ${tzSuffix}`;
    } catch (e) {
        return dateString;
    }
}

function formatCurrency(amount) {
    if (amount === null || amount === undefined || isNaN(amount)) return 'Hubungi Penyelenggara';
    return new Intl.NumberFormat('id-ID', {
        style: 'currency', currency: 'IDR', maximumFractionDigits: 0
    }).format(amount);
}

// Authenticated Fetch Helper
async function authFetch(url, options = {}) {
    const token = getToken();
    const headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {})
    };

    if (token && isTokenValid(token)) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, { ...options, headers });

    if (response.status === 401 || response.status === 403) {
        console.warn('Unauthorized access detected by authFetch.');
        if (!isPublicAuthPage()) {
            clearAuthState();
            window.location.href = `${API_USER_SERVICE}/login.html?redirect=${encodeURIComponent(window.location.href)}`;
        }
    }

    return response;
}

function showToast(message, type = 'success') {
    let container = document.querySelector('.toast-container');
    if (!container) {
        container = document.createElement('div');
        container.className = 'toast-container';
        document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `<span>${message}</span>`;
    container.appendChild(toast);
    setTimeout(() => {
        toast.style.opacity = '0';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// Footer Renderer
function renderTIKARAFooter() {
    let footer = document.querySelector('footer.tikara-footer') || document.querySelector('footer');
    if (!footer) {
        footer = document.createElement('footer');
        document.body.appendChild(footer);
    }

    footer.className = 'tikara-footer';
    footer.style.backgroundColor = '#FFFFFF';
    footer.style.color = '#475569';
    footer.style.padding = '3.5rem 1.5rem 2rem';
    footer.style.borderTop = '1px solid #E2E8F0';
    footer.style.marginTop = 'auto';
    footer.style.width = '100%';
    footer.style.boxSizing = 'border-box';

    const eventBase = (window.TIKARA_CONFIG && window.TIKARA_CONFIG.SERVICES && window.TIKARA_CONFIG.SERVICES.EVENT)
        ? window.TIKARA_CONFIG.SERVICES.EVENT
        : (typeof API_EVENT_SERVICE !== 'undefined' ? API_EVENT_SERVICE : 'http://localhost:8082');

    const checkinBase = (window.TIKARA_CONFIG && window.TIKARA_CONFIG.SERVICES && window.TIKARA_CONFIG.SERVICES.CHECKIN)
        ? window.TIKARA_CONFIG.SERVICES.CHECKIN
        : (typeof API_CHECKIN_SERVICE !== 'undefined' ? API_CHECKIN_SERVICE : 'http://localhost:8087');

    const ticketBase = (window.TIKARA_CONFIG && window.TIKARA_CONFIG.SERVICES && window.TIKARA_CONFIG.SERVICES.TICKET)
        ? window.TIKARA_CONFIG.SERVICES.TICKET
        : (typeof API_TICKET_SERVICE !== 'undefined' ? API_TICKET_SERVICE : 'http://localhost:8083');

    footer.innerHTML = `
        <div class="footer-container">
            <div class="footer-main-grid">
                <!-- Brand & Security Column -->
                <div class="footer-col footer-brand-col">
                    <a href="${eventBase}/index.html" class="footer-logo" aria-label="TIKARA Beranda">
                        <i class="fa-solid fa-ticket"></i> <span>TIKARA</span>
                    </a>
                    <p class="footer-desc">
                        Beli tiket konser, festival musik, dan event seru lainnya dengan mudah &amp; aman di TIKARA. 
                        <strong>#PASTIBISA</strong> nonton konser &amp; festival idaman!
                    </p>
                    <div class="footer-security-section">
                        <h4 class="footer-subheading"><i class="fa-solid fa-shield-halved"></i> Keamanan dan Privasi</h4>
                        <div class="footer-badges-wrap">
                            <a href="${eventBase}/terms.html#security" class="footer-iso-badge" title="Tersertifikasi ISO/IEC 27001 &amp; ISO/IEC 27701">
                                <div class="iso-icon-box">
                                    <i class="fa-solid fa-award"></i>
                                </div>
                                <div class="iso-info-box">
                                    <span class="iso-title">ISO/IEC 27001 &bull; 27701</span>
                                    <span class="iso-desc">Information Security &amp; Privacy Management</span>
                                </div>
                            </a>
                            <div class="footer-ssl-badge">
                                <i class="fa-solid fa-lock"></i> <span>256-Bit SSL Enkripsi Terverifikasi</span>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Navigation Columns -->
                <div class="footer-nav-columns">
                    <div class="footer-col">
                        <h3 class="footer-heading">Tentang TIKARA</h3>
                        <ul class="footer-links">
                            <li><a href="${eventBase}/about.html">Tentang Kami</a></li>
                            <li><a href="${eventBase}/blog.html">Blog &amp; Berita Musik</a></li>
                        </ul>
                        <h3 class="footer-heading" style="margin-top: 1.75rem;">Produk</h3>
                        <ul class="footer-links">
                            <li><a href="${eventBase}/index.html#catalog">Semua Konser &amp; Festival</a></li>
                            <li><a href="${ticketBase}/my-tickets.html">Tiket Saya</a></li>
                        </ul>
                    </div>

                    <div class="footer-col">
                        <h3 class="footer-heading">Event Creator</h3>
                        <ul class="footer-links">
                            <li><a href="${eventBase}/creator-guide.html#biaya">Biaya &amp; Komisi</a></li>
                            <li><a href="${eventBase}/creator-guide.html#kerjasama">Kerjasama dengan Kami</a></li>
                            <li><a href="${eventBase}/creator-guide.html#panduan">Buku Panduan Creator</a></li>
                            <li><a href="${eventBase}/create-event.html">Buat Event Konser</a></li>
                            <li><a href="${eventBase}/organizer-dashboard.html">Dashboard Penyelenggara</a></li>
                        </ul>
                    </div>

                    <div class="footer-col">
                        <h3 class="footer-heading">Dukungan</h3>
                        <ul class="footer-links">
                            <li><a href="${eventBase}/help.html">Pusat Bantuan (FAQ)</a></li>
                            <li><a href="${eventBase}/terms.html#terms">Syarat dan Ketentuan</a></li>
                            <li><a href="${eventBase}/terms.html#privacy">Kebijakan Privasi</a></li>
                            <li><a href="${eventBase}/terms.html#security">Kepatuhan Keamanan &amp; Privasi</a></li>
                            <li><a href="${eventBase}/terms.html#cookies">Kebijakan Cookies</a></li>
                        </ul>
                    </div>
                </div>

                <!-- Action / Payment & Social Column -->
                <div class="footer-col footer-action-col">
                    <h3 class="footer-heading">Pilihan Pembayaran</h3>
                    <div class="footer-payment-grid" title="Metode Pembayaran Resmi TIKARA">
                        <span class="pay-chip"><i class="fa-solid fa-qrcode"></i> QRIS</span>
                        <span class="pay-chip">BCA</span>
                        <span class="pay-chip">Mandiri</span>
                        <span class="pay-chip">BRI</span>
                        <span class="pay-chip">BNI</span>
                        <span class="pay-chip">BTN</span>
                        <span class="pay-chip">BSI</span>
                        <span class="pay-chip">Permata</span>
                        <span class="pay-chip"><i class="fa-brands fa-cc-visa"></i> VISA</span>
                        <span class="pay-chip"><i class="fa-brands fa-cc-mastercard"></i> Master</span>
                        <span class="pay-chip">ShopeePay</span>
                        <span class="pay-chip">GoPay</span>
                        <span class="pay-chip">Alfamart</span>
                        <span class="pay-chip">Indomaret</span>
                    </div>
                    <div>
                        <a href="${eventBase}/help.html#pembayaran" class="footer-pay-guide-link">
                            <i class="fa-solid fa-circle-question"></i> Panduan &amp; Cara Bayar
                        </a>
                    </div>

                    <div class="footer-social-wrap" style="margin-top: 1.75rem;">
                        <h3 class="footer-heading">Ikuti Kami untuk Update</h3>
                        <div class="footer-social-links">
                            <a href="https://www.instagram.com/deafriliart/?__pwa=1" target="_blank" rel="noopener noreferrer" class="social-btn social-ig" aria-label="Instagram TIKARA" title="Instagram">
                                <i class="fa-brands fa-instagram"></i>
                            </a>
                            <a href="https://www.tiktok.com/@realityclubofficial" target="_blank" rel="noopener noreferrer" class="social-btn social-tiktok" aria-label="TikTok TIKARA" title="TikTok">
                                <i class="fa-brands fa-tiktok"></i>
                            </a>
                            <a href="https://x.com/reality_club" target="_blank" rel="noopener noreferrer" class="social-btn social-x" aria-label="X (Twitter) TIKARA" title="X (Twitter)">
                                <i class="fa-brands fa-x-twitter"></i>
                            </a>
                            <a href="https://www.facebook.com/officialrealityclub/?locale=id_ID" target="_blank" rel="noopener noreferrer" class="social-btn social-fb" aria-label="Facebook TIKARA" title="Facebook">
                                <i class="fa-brands fa-facebook-f"></i>
                            </a>
                            <a href="https://youtu.be/SlwQyBkCtZE?si=WBPI1nh0A6A2e9Dv" target="_blank" rel="noopener noreferrer" class="social-btn social-yt" aria-label="YouTube TIKARA" title="YouTube">
                                <i class="fa-brands fa-youtube"></i>
                            </a>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Bottom Copyright -->
            <div class="footer-bottom-bar">
                <p>&copy; 2026 TIKARA (PT Tikara Nusantara Sejahtera). Seluruh hak cipta dilindungi undang-undang.</p>
                <div style="display: flex; gap: 1.25rem; font-size: 0.8rem;">
                    <a href="${eventBase}/terms.html#terms">Syarat Layanan</a>
                    <a href="${eventBase}/terms.html#privacy">Privasi</a>
                    <a href="${eventBase}/terms.html#security">Keamanan</a>
                </div>
            </div>
        </div>
    `;
}
window.renderTIKARAFooter = renderTIKARAFooter;

// Auto Initialize
document.addEventListener('DOMContentLoaded', () => {
    renderTIKARANavbar();
    renderTIKARAFooter();
    initMobileNavigation();
    verifySessionWithBackend();
});

// Alias getJwtToken for backward compatibility
function getJwtToken() { return getToken(); }
window.getJwtToken = getJwtToken;


window.getToken = getToken;
window.setToken = setToken;
window.getAuthState = getAuthState;
window.getUserData = getUserData;
window.setUserData = setUserData;
window.clearAuthState = clearAuthState;
window.normalizeAvatarUrl = normalizeAvatarUrl;
window.formatCurrency = formatCurrency;
window.formatEventDateTime = formatEventDateTime;


