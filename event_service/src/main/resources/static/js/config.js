/* ==========================================================================
   TIKARA Centralized Environment & Security Configuration
   ========================================================================== */

(function () {
    const isDev = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

    window.TIKARA_CONFIG = {
        ENV: isDev ? 'development' : 'production',

        // Microservices Endpoints
        SERVICES: {
            USER: isDev ? 'http://localhost:8081' : window.location.origin,
            EVENT: isDev ? 'http://localhost:8082' : window.location.origin,
            TICKET: isDev ? 'http://localhost:8083' : window.location.origin,
            ORDER: isDev ? 'http://localhost:8084' : window.location.origin,
            ADMIN: isDev ? 'http://localhost:8089' : window.location.origin
        },

        // Open Redirect Whitelist (Allowed target hosts for login redirects)
        ALLOWED_HOSTS: [
            window.location.host,
            'localhost:8081',
            'localhost:8082',
            'localhost:8083',
            'localhost:8084',
            'localhost:8089',
            '127.0.0.1:8081',
            '127.0.0.2:8082',
            'tikara.com',
            'www.tikara.com',
            'api.tikara.com'
        ],

        /**
         * Authoritative Client-Side Open Redirect Mitigation
         * Strictly sanitizes redirect parameters to prevent phishing.
         */
        sanitizeRedirectUrl(rawUrl, defaultFallback = '/index.html') {
            if (!rawUrl || typeof rawUrl !== 'string') return defaultFallback;
            const trimmed = rawUrl.trim();

            // Allow relative paths starting with single '/'
            if (trimmed.startsWith('/') && !trimmed.startsWith('//') && !trimmed.includes('\\')) {
                return trimmed;
            }

            // If absolute URL, check host whitelist
            try {
                const parsed = new URL(trimmed, window.location.origin);
                if (this.ALLOWED_HOSTS.includes(parsed.host)) {
                    return parsed.href;
                }
            } catch (e) {
                // Malformed URL, reject
            }

            return defaultFallback;
        },

        getLoginUrl(returnTarget) {
            const target = returnTarget || (window.location.pathname + window.location.search);
            const sanitized = this.sanitizeRedirectUrl(target);
            return `${this.SERVICES.USER}/login.html?redirect=${encodeURIComponent(sanitized)}`;
        },

        getRegisterUrl() {
            return `${this.SERVICES.USER}/register.html`;
        }
    };
})();
