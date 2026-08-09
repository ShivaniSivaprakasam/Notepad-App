/**
 * Handles the Home page's live clock, formatted date, and time-of-day
 * greeting. Computed entirely client-side using the browser's local
 * clock, so it reflects the user's actual local time regardless of
 * where the server is hosted.
 */
document.addEventListener('DOMContentLoaded', function () {
    const greetingEl = document.getElementById('greetingText');
    const dateEl = document.getElementById('dateText');
    const timerEl = document.getElementById('miniTimer');

    function ordinalSuffix(n) {
        const j = n % 10, k = n % 100;
        if (j === 1 && k !== 11) return 'st';
        if (j === 2 && k !== 12) return 'nd';
        if (j === 3 && k !== 13) return 'rd';
        return 'th';
    }

    function partOfDay(hour) {
        if (hour >= 5 && hour < 12) return 'Morning';
        if (hour >= 12 && hour < 17) return 'Afternoon';
        if (hour >= 17 && hour < 21) return 'Evening';
        return 'Night';
    }

    function updateGreetingAndDate() {
        const now = new Date();
        const username = greetingEl ? greetingEl.getAttribute('data-username') : '';

        if (greetingEl) {
            greetingEl.textContent = `Welcome, ${username}. Have a good ${partOfDay(now.getHours())}.`;
        }

        if (dateEl) {
            const months = ['January', 'February', 'March', 'April', 'May', 'June',
                             'July', 'August', 'September', 'October', 'November', 'December'];
            const day = now.getDate();
            dateEl.textContent = `${day}${ordinalSuffix(day)} ${months[now.getMonth()]} ${now.getFullYear()}`;
        }
    }

    function updateTimer() {
        if (!timerEl) return;
        const now = new Date();
        const pad = n => n.toString().padStart(2, '0');
        timerEl.textContent = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
    }

    updateGreetingAndDate();
    updateTimer();
    setInterval(updateTimer, 1000);
    setInterval(updateGreetingAndDate, 60000);
});