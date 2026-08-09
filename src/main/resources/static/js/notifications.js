/**
 * Polls the server for today's reminders and shows an in-app banner if
 * any exist. This is a "digest" style notification — showing a summary
 * of today's reminders while the dashboard is open — rather than a true
 * real-time push notification, which would require WebSockets/SSE.
 *
 * Runs once on page load, then every 60 seconds while the dashboard
 * remains open.
 */
document.addEventListener('DOMContentLoaded', function () {
    const banner = document.getElementById('notificationBanner');
    const textEl = document.getElementById('notificationText');

    if (!banner || !textEl) {
        return; // Not on the dashboard page.
    }

    async function checkNotifications() {
        try {
            const response = await fetch('/reminders/api/today');
            if (!response.ok) return;

            const reminders = await response.json();
            if (reminders.length === 0) {
                banner.style.display = 'none';
                return;
            }

            const upcoming = reminders.filter(r => !r.isPast);
            const summary = upcoming.length > 0
                ? `You have ${upcoming.length} reminder(s) coming up today: ` +
                  upcoming.map(r => `${r.title} at ${r.time}`).join(', ')
                : `You had ${reminders.length} reminder(s) today.`;

            textEl.textContent = summary;
            banner.style.display = 'block';
        } catch (err) {
            // Fail silently — a broken notification check shouldn't
            // disrupt the rest of the dashboard.
        }
    }

    checkNotifications();
    setInterval(checkNotifications, 60000);
});