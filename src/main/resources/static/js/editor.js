/**
 * Initializes the Quill rich text editor and wires it up to the hidden
 * textarea so its HTML content is submitted along with the rest of the
 * form.
 *
 * Runs on both create-note.html and edit-note.html — on edit, the
 * hidden textarea is pre-populated by Thymeleaf with the note's existing
 * content, so we load that into Quill on init.
 */
document.addEventListener('DOMContentLoaded', function () {
    const hiddenField = document.getElementById('content');
    const editorContainer = document.getElementById('editor');
    const form = document.getElementById('noteForm');

    if (!editorContainer || !hiddenField || !form) {
        // Not on a page with the editor — nothing to do.
        return;
    }

    // Toolbar configuration: matches the "bold, italics, highlighting,
    // fonts" requirement from the project spec.
    const toolbarOptions = [
        [{ 'font': [] }],
        ['bold', 'italic', 'underline', 'strike'],
        [{ 'color': [] }, { 'background': [] }], // background = highlighting
        [{ 'header': [1, 2, 3, false] }],
        [{ 'list': 'ordered' }, { 'list': 'bullet' }],
        [{ 'align': [] }],
        ['link', 'blockquote'],
        ['clean'] // remove formatting button
    ];

    const quill = window.quillInstance = new Quill('#editor', {
        theme: 'snow',
        modules: {
            toolbar: toolbarOptions
        }
    });

    // If editing an existing note, the hidden textarea already contains
    // the saved HTML content (rendered server-side by Thymeleaf) — load
    // it into Quill so the user sees their existing content.
    if (hiddenField.value && hiddenField.value.trim() !== '') {
        quill.root.innerHTML = hiddenField.value;
    }

    // Before the form submits, copy Quill's current HTML content into the
    // hidden textarea so it's included in the POST request.
    form.addEventListener('submit', function () {
        hiddenField.value = quill.root.innerHTML;
    });
});