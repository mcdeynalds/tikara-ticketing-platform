/* ==========================================================================
   TIKARA Unified Frontend Engine & Cross-Service Auth Synchronizer (v3)
   Shared across user_service, event_service, ticket_service, order_service
   ========================================================================== */

const API_USER_SERVICE = 'http://localhost:8081';
const API_EVENT_SERVICE = 'http://localhost:8082';
const API_TICKET_SERVICE = 'http://localhost:8083';
const API_ORDER_SERVICE = 'http://localhost:8084';
const API_QUEUE_SERVICE = 'http://localhost:8085';
const API_NOTIFICATION_SERVICE = 'http://localhost:8086';
const API_CHECKIN_SERVICE = 'http://localhost:8087';
const API_PROMO_SERVICE = 'http://localhost:8088';
const API_ADMIN_SERVICE = 'http://localhost:8089';

// ==========================================================================
// 1. Cross-Port Cookie & Storage Synchronization Helpers
// ==========================================================================

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
    // Path=/ ensures cookie is accessible across all ports on localhost
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

function getToken() {
    let token = localStorage.getItem('jwt_token');
    if (!token) {
        // Fallback to shared cookie across localhost ports
        token = getCookie('tikara_token');
        if (token) {
            localStorage.setItem('jwt_token', token);
        }
    }
    return token;
}

function setUserData(userData) {
    if (!userData) return;
    const normalizedName = userData.nama || userData.namaLengkap || 'Pengguna TIKARA';
    const normalizedRole = (userData.role || userData.peran || 'PEMBELI').toUpperCase();
    const cleanUser = {
        id: userData.id,
        nama: normalizedName,
        namaLengkap: normalizedName,
        email: userData.email || '',
        role: normalizedRole,
        peran: normalizedRole,
        fotoProfil: userData.fotoProfil || userData.fotoProfilUrl || userData.fotoUrl || userData.avatarUrl || ''
    };
    const str = JSON.stringify(cleanUser);
    localStorage.setItem('user_data', str);
    localStorage.setItem('role', normalizedRole);
    setCookie('tikara_user', str, 7);
    setCookie('tikara_role', normalizedRole, 7);
}

function getUserData() {
    let data = localStorage.getItem('user_data');
    if (!data) {
        data = getCookie('tikara_user');
        if (data) {
            try {
                localStorage.setItem('user_data', data);
            } catch (e) {}
        }
    }
    try {
        return data ? JSON.parse(data) : null;
    } catch (e) {
        return null;
    }
}

function clearAuthState() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_data');
    localStorage.removeItem('role');
    sessionStorage.removeItem('resetToken');
    sessionStorage.clear();
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
        const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
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

// ==========================================================================
// 2. Authoritative Unified Auth State Engine
// ==========================================================================

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

    let user = getUserData();
    // If token exists but user data missing, extract from JWT payload
    if (!user) {
        const payload = parseJwt(token);
        if (payload) {
            const role = (payload.role || payload.peran || 'PEMBELI').toUpperCase();
            user = {
                id: payload.id || payload.userId || payload.sub,
                nama: payload.nama || payload.sub || 'Pengguna TIKARA',
                namaLengkap: payload.nama || payload.sub || 'Pengguna TIKARA',
                email: payload.email || payload.sub || '',
                role: role,
                peran: role
            };
            setUserData(user);
        }
    }

    const role = (user && (user.peran || user.role)) ? (user.peran || user.role).toUpperCase() : 'PEMBELI';

    return {
        isAuthenticated: true,
        user: user,
        token: token,
        role: role
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

// ==========================================================================
// 3. Unified TIKARA Navbar Renderer (Desktop & Mobile Drawer)
// ==========================================================================

function renderTIKARANavbar() {
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

    // 2. Desktop Navigation Menu Items
    if (navMenu) {
        let menuHtml = '';
        if (!auth.isAuthenticated || auth.role === 'GUEST') {
            menuHtml = `
                <li><a href="${API_EVENT_SERVICE}/index.html" class="navbar-link ${currentPort === '8082' && (currentPath === '/' || currentPath.endsWith('index.html')) ? 'active' : ''}"><i class="fa-solid fa-house"></i> Beranda</a></li>
                <li><a href="${API_EVENT_SERVICE}/index.html#catalog" class="navbar-link"><i class="fa-solid fa-compass"></i> Jelajah Event</a></li>
            `;
        } else if (auth.role === 'PEMBELI') {
            menuHtml = `
                <li><a href="${API_EVENT_SERVICE}/index.html" class="navbar-link ${currentPort === '8082' && (currentPath === '/' || currentPath.endsWith('index.html')) ? 'active' : ''}"><i class="fa-solid fa-house"></i> Beranda</a></li>
                <li><a href="${API_EVENT_SERVICE}/index.html#catalog" class="navbar-link"><i class="fa-solid fa-compass"></i> Jelajah Event</a></li>
                <li><a href="${API_ORDER_SERVICE}/my-tickets.html" class="navbar-link"><i class="fa-solid fa-ticket"></i> Tiket Saya</a></li>
                <li><a href="${API_ORDER_SERVICE}/my-orders.html" class="navbar-link"><i class="fa-solid fa-clock-rotate-left"></i> Pesanan Saya</a></li>
            `;
        } else if (auth.role === 'PENYELENGGARA') {
            menuHtml = `
                <li><a href="${API_EVENT_SERVICE}/organizer-dashboard.html" class="navbar-link ${currentPath.includes('organizer-dashboard') ? 'active' : ''}"><i class="fa-solid fa-chart-pie"></i> Dashboard</a></li>
                <li><a href="${API_EVENT_SERVICE}/my-events.html" class="navbar-link ${currentPath.includes('my-events') ? 'active' : ''}"><i class="fa-solid fa-calendar-days"></i> Event Saya</a></li>
                <li><a href="${API_EVENT_SERVICE}/create-event.html" class="navbar-link ${currentPath.includes('create-event') ? 'active' : ''}"><i class="fa-solid fa-plus-circle"></i> Buat Event</a></li>
                <li><a href="${API_EVENT_SERVICE}/index.html" class="navbar-link ${(currentPath === '/' || currentPath.endsWith('index.html')) ? 'active' : ''}"><i class="fa-solid fa-eye"></i> Mode Pembeli (Jelajah)</a></li>
            `;
        } else if (auth.role === 'ADMIN') {
            menuHtml = `
                <li><a href="${API_ADMIN_SERVICE}/admin-dashboard.html" class="navbar-link"><i class="fa-solid fa-shield-halved"></i> Dashboard Admin</a></li>
                <li><a href="${API_ADMIN_SERVICE}/admin-dashboard.html?tab=users" class="navbar-link"><i class="fa-solid fa-users"></i> Kelola Pengguna</a></li>
                <li><a href="${API_ADMIN_SERVICE}/admin-dashboard.html?tab=approvals" class="navbar-link"><i class="fa-solid fa-clipboard-check"></i> Verifikasi Event</a></li>
                <li><a href="${API_EVENT_SERVICE}/index.html" class="navbar-link"><i class="fa-solid fa-compass"></i> Katalog Publik</a></li>
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
                <a href="${loginUrl}" class="btn btn-outline" style="width:auto; padding:0.45rem 1.2rem; font-size:0.875rem; color:white; border-color:rgba(255,255,255,0.3);">Masuk</a>
                <a href="${regUrl}" class="btn btn-primary" style="width:auto; padding:0.45rem 1.2rem; font-size:0.875rem;">Daftar</a>
            `;
        } else {
            // Authenticated State: HIDE Masuk & Daftar, SHOW Loket.com-style User Pill Button
            const user = auth.user || {};
            const namaLengkap = user.nama || user.namaLengkap || 'Pengguna TIKARA';
            const firstName = namaLengkap.split(' ')[0] || 'Akun';
            const initial = firstName.charAt(0).toUpperCase() || 'U';
            const email = user.email || '';
            const role = auth.role;
            const avatarUrl = user.fotoProfil || user.fotoProfilUrl || user.fotoUrl || user.avatarUrl;

            const avatarHtml = avatarUrl
                ? `<img src="${avatarUrl}" alt="${namaLengkap}" class="avatar-img-round" onerror="this.outerHTML='<div class=\'avatar-img\'>${initial}</div>'">`
                : `<div class="avatar-img">${initial}</div>`;

            const dropAvatarHtml = avatarUrl
                ? `<img src="${avatarUrl}" alt="${namaLengkap}" class="big-drop-avatar-img" onerror="this.outerHTML='<div class=\'big-drop-avatar\'>${initial}</div>'">`
                : `<div class="big-drop-avatar">${initial}</div>`;

            let roleBadgeClass = 'role-badge-buyer';
            if (role === 'ADMIN') roleBadgeClass = 'role-badge-admin';
            if (role === 'PENYELENGGARA') roleBadgeClass = 'role-badge-organizer';

            let roleSpecificMenuItems = '';
            if (role === 'PEMBELI') {
                roleSpecificMenuItems = `
                    <a href="${API_ORDER_SERVICE}/my-tickets.html" class="dropdown-item"><i class="fa-solid fa-ticket"></i> Tiket Saya</a>
                    <a href="${API_ORDER_SERVICE}/my-orders.html" class="dropdown-item"><i class="fa-solid fa-clock-rotate-left"></i> Pesanan Saya</a>
                `;
            } else if (role === 'PENYELENGGARA') {
                roleSpecificMenuItems = `
                    <a href="${API_EVENT_SERVICE}/organizer-dashboard.html" class="dropdown-item"><i class="fa-solid fa-chart-pie"></i> Dashboard Penyelenggara</a>
                    <a href="${API_EVENT_SERVICE}/my-events.html" class="dropdown-item"><i class="fa-solid fa-calendar-days"></i> Event Saya</a>
                    <a href="${API_EVENT_SERVICE}/create-event.html" class="dropdown-item"><i class="fa-solid fa-plus-circle"></i> Buat Event Baru</a>
                `;
            } else if (role === 'ADMIN') {
                roleSpecificMenuItems = `
                    <a href="${API_ADMIN_SERVICE}/admin-dashboard.html" class="dropdown-item"><i class="fa-solid fa-shield-halved"></i> Dashboard Admin</a>
                    <a href="${API_ADMIN_SERVICE}/admin-dashboard.html?tab=users" class="dropdown-item"><i class="fa-solid fa-users"></i> Kelola Pengguna</a>
                    <a href="${API_ADMIN_SERVICE}/admin-dashboard.html?tab=approvals" class="dropdown-item"><i class="fa-solid fa-clipboard-check"></i> Verifikasi Event</a>
                `;
            }

            navUserBox.innerHTML = `
                <div class="user-nav-wrapper">
                    <button type="button" class="user-avatar-trigger" id="userAvatarTrigger" onclick="toggleUserDropdown(event)" aria-label="Menu Akun" aria-expanded="false">
                        ${avatarHtml}
                        <span class="user-display-name">${firstName}</span>
                        <i class="fa-solid fa-chevron-down nav-chevron"></i>
                    </button>

                    <div class="user-dropdown-menu" id="userDropdownMenu" role="menu">
                        <div class="dropdown-user-info">
                            ${dropAvatarHtml}
                            <div class="user-meta">
                                <span class="user-name" title="${namaLengkap}">${namaLengkap}</span>
                                <span class="user-email" title="${email}">${email}</span>
                                <span class="role-tag ${roleBadgeClass}">${role}</span>
                            </div>
                        </div>

                        <div class="dropdown-group">
                            <a href="${API_USER_SERVICE}/profile.html" class="dropdown-item"><i class="fa-solid fa-user-gear"></i> Informasi Akun / Profil</a>
                            ${roleSpecificMenuItems}
                        </div>

                        <div class="dropdown-divider"></div>

                        <div class="dropdown-group">
                            <button type="button" class="dropdown-item" onclick="handleTIKARALogout()" style="color:#EF4444; width:100%; border:none; background:none; cursor:pointer;">
                                <i class="fa-solid fa-arrow-right-from-bracket" style="color:#EF4444;"></i> Keluar dari Akun
                            </button>
                        </div>
                    </div>
                </div>
            `;
        }
    }

    // 4. Mobile Navigation Drawer
    if (drawer) {
        if (!auth.isAuthenticated || auth.role === 'GUEST') {
            const loginUrl = `${API_USER_SERVICE}/login.html?redirect=${encodeURIComponent(window.location.href)}`;
            const regUrl = `${API_USER_SERVICE}/register.html`;
            drawer.innerHTML = `
                <div class="mobile-drawer-links">
                    <a href="${API_EVENT_SERVICE}/index.html" class="mobile-drawer-link active"><i class="fa-solid fa-house"></i> Beranda</a>
                    <a href="${API_EVENT_SERVICE}/index.html#catalog" class="mobile-drawer-link"><i class="fa-solid fa-compass"></i> Jelajah Event</a>
                </div>
                <div class="mobile-drawer-divider"></div>
                <div class="mobile-drawer-actions">
                    <a href="${loginUrl}" class="btn btn-outline" style="width:100%; text-align:center;"><i class="fa-solid fa-arrow-right-to-bracket"></i> Masuk</a>
                    <a href="${regUrl}" class="btn btn-primary" style="width:100%; text-align:center;"><i class="fa-solid fa-user-plus"></i> Daftar Akun</a>
                </div>
            `;
        } else {
            const user = auth.user || {};
            const namaLengkap = user.nama || user.namaLengkap || 'Pengguna TIKARA';
            const initial = namaLengkap.charAt(0).toUpperCase() || 'U';
            const email = user.email || '';
            const role = auth.role;
            const avatarUrl = user.fotoProfil || user.fotoProfilUrl || user.fotoUrl || user.avatarUrl;

            const avatarDrawerHtml = avatarUrl
                ? `<img src="${avatarUrl}" alt="${namaLengkap}" class="avatar-img-round" style="width:44px;height:44px;" onerror="this.outerHTML='<div class=\'avatar-img\'>${initial}</div>'">`
                : `<div class="avatar-img" style="width:44px;height:44px;font-size:1.1rem;">${initial}</div>`;

            let roleBadgeClass = 'role-badge-buyer';
            if (role === 'ADMIN') roleBadgeClass = 'role-badge-admin';
            if (role === 'PENYELENGGARA') roleBadgeClass = 'role-badge-organizer';

            let roleSpecificDrawerLinks = '';
            if (role === 'PEMBELI') {
                roleSpecificDrawerLinks = `
                    <a href="${API_ORDER_SERVICE}/my-tickets.html" class="mobile-drawer-link"><i class="fa-solid fa-ticket"></i> Tiket Saya</a>
                    <a href="${API_ORDER_SERVICE}/my-orders.html" class="mobile-drawer-link"><i class="fa-solid fa-clock-rotate-left"></i> Pesanan Saya</a>
                `;
            } else if (role === 'PENYELENGGARA') {
                roleSpecificDrawerLinks = `
                    <a href="${API_EVENT_SERVICE}/organizer-dashboard.html" class="mobile-drawer-link"><i class="fa-solid fa-chart-pie"></i> Dashboard Penyelenggara</a>
                    <a href="${API_EVENT_SERVICE}/my-events.html" class="mobile-drawer-link"><i class="fa-solid fa-calendar-days"></i> Event Saya</a>
                    <a href="${API_EVENT_SERVICE}/create-event.html" class="mobile-drawer-link"><i class="fa-solid fa-plus-circle"></i> Buat Event Baru</a>
                `;
            } else if (role === 'ADMIN') {
                roleSpecificDrawerLinks = `
                    <a href="${API_ADMIN_SERVICE}/admin-dashboard.html" class="mobile-drawer-link"><i class="fa-solid fa-shield-halved"></i> Dashboard Admin</a>
                    <a href="${API_ADMIN_SERVICE}/admin-dashboard.html?tab=users" class="mobile-drawer-link"><i class="fa-solid fa-users"></i> Kelola Pengguna</a>
                    <a href="${API_ADMIN_SERVICE}/admin-dashboard.html?tab=approvals" class="mobile-drawer-link"><i class="fa-solid fa-clipboard-check"></i> Verifikasi Event</a>
                `;
            }

            drawer.innerHTML = `
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
                    <a href="${API_USER_SERVICE}/profile.html" class="mobile-drawer-link"><i class="fa-solid fa-user-gear"></i> Informasi Akun / Profil</a>
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

    if (!toggleBtn || !drawer) return;

    const closeDrawer = () => {
        drawer.classList.remove('open');
        toggleBtn.classList.remove('active');
        toggleBtn.setAttribute('aria-expanded', 'false');
    };

    toggleBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        const isOpen = drawer.classList.toggle('open');
        toggleBtn.classList.toggle('active', isOpen);
        toggleBtn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
    });

    document.addEventListener('click', (e) => {
        if (!drawer.contains(e.target) && !toggleBtn.contains(e.target)) {
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

// Auto Initialize
document.addEventListener('DOMContentLoaded', () => {
    renderTIKARANavbar();
    initMobileNavigation();
    verifySessionWithBackend();
});


// Alias getJwtToken for backward compatibility
function getJwtToken() { return getToken(); }
window.getJwtToken = getJwtToken;
