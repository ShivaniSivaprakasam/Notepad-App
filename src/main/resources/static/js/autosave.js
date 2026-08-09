/**
 * Handles periodic auto-saving of note content while editing, similar
 * to Google Docs' "Saving... / Saved" indicator.
 *
 * Only active on the edit-note page (a note must already have an ID
 * to auto-save against). Saves every 5 seconds, but only if the
 * content has actually changed since the last save — avoids sending
 * unnecessary requests while the user is idle or just reading.
 */
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('noteForm');
    const noteIdField = document.querySelector('input[name="id"]');

    // Only run on the edit page — the create page has no note ID yet,
    // and its form action is different (POST /notes, not /notes/update).
    if (!form || !noteIdField || !noteIdField.value) {
        return;
    }

    const AUTOSAVE_INTERVAL_MS = 5000;
    let lastSavedContent = null;
    let statusEl = document.getElementById('autosaveStatus');

    // Create the status indicator element if it doesn't already exist.
    if (!statusEl) {
        statusEl = document.createElement('span');
        statusEl.id = 'autosaveStatus';
        statusEl.style.marginLeft = '12px';
        statusEl.style.fontSize = '0.85em';
        statusEl.style.color = '#777';
        const saveButton = form.querySelector('button[type="submit"]');
        if (saveButton) {
            saveButton.insertAdjacentElement('afterend', statusEl);
        }
    }

    function getFormData() {
        // Quill's content is synced into the hidden #content textarea by
        // editor.js's own submit handler — but auto-save happens outside
        // a real submit, so we need to sync it manually here too.
        const hiddenContent = document.getElementById('content');
        const quillEditor = document.getElementById('editor');
        if (hiddenContent && quillEditor && window.quillInstance) {
            hiddenContent.value = window.quillInstance.root.innerHTML;
        }
        return new FormData(form);
    }

    async function autoSave() {
        const formData = getFormData();
        const currentContent = formData.get('content') + '|' + formData.get('title');

        // Skip the request entirely if nothing has changed — reduces
        // server load and avoids a "Saving..." flicker on every tick.
        if (currentContent === lastSavedContent) {
            return;
        }

        statusEl.textContent = 'Saving...';

        try {
            const params = new URLSearchParams();
            for (const [key, value] of formData.entries()) {
                params.append(key, value);
            }

            const response = await fetch(form.action.replace('/update', '/autosave'), {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params.toString()
            });

            const result = await response.json();

            if (result.status === 'success') {
                lastSavedContent = currentContent;
                statusEl.textContent = 'Saved ✓';
            } else {
                statusEl.textContent = 'Save failed';
            }
        } catch (err) {
            statusEl.textContent = 'Save failed (offline?)';
        }
    }

    setInterval(autoSave, AUTOSAVE_INTERVAL_MS);
});