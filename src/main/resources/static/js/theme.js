/**
 * Handles dark mode toggling and persistence.
 *
 * The chosen theme is stored in a cookie (not localStorage/sessionStorage)
 * so that it's available to the server on every page load without a
 * flash of the wrong theme, and works consistently across all pages
 * without relying on browser storage APIs that aren't appropriate here.
 *
 * This script is included on every page (via the navbar include being
 * present everywhere), so the toggle button and theme state are
 * consistent across the whole app.
 */
(function () {
    const COOKIE_NAME = 'notepad_theme';
    const DARK_CLASS = 'dark-mode';

    function getCookie(name) {
        const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
        return match ? match[2] : null;
    }

    function setCookie(name, value, days) {
        const expires = new Date(Date.now() + days * 24 * 60 * 60 * 1000).toUTCString();
        document.cookie = `${name}=${value}; expires=${expires}; path=/`;
    }

    function applyTheme(isDark) {
        document.body.classList.toggle(DARK_CLASS, isDark);
        const btn = document.getElementById('themeToggleBtn');
        if (btn) {
            btn.textContent = isDark ? '☀️ Light Mode' : '🌙 Dark Mode';
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        const savedTheme = getCookie(COOKIE_NAME);
        applyTheme(savedTheme === 'dark');

        const toggleBtn = document.getElementById('themeToggleBtn');
        if (toggleBtn) {
            toggleBtn.addEventListener('click', function () {
                const isDarkNow = document.body.classList.contains(DARK_CLASS);
                const newIsDark = !isDarkNow;
                applyTheme(newIsDark);
                setCookie(COOKIE_NAME, newIsDark ? 'dark' : 'light', 365);
            });
        }
    });
})();