/* ============================================================================
   SwapEd — Interactive Client Utilities
   ============================================================================ */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Show / Hide Password Toggle
    document.querySelectorAll('[data-toggle-password]').forEach(button => {
        button.addEventListener('click', () => {
            const targetId = button.getAttribute('data-toggle-password');
            const input = document.getElementById(targetId);
            if (!input) return;
            if (input.type === 'password') {
                input.type = 'text';
                button.textContent = 'Hide';
            } else {
                input.type = 'password';
                button.textContent = 'Show';
            }
        });
    });

    // 2. Product Image Upload Preview
    const imageInput = document.getElementById('imageFileInput');
    const previewContainer = document.getElementById('imagePreviewContainer');
    const previewImage = document.getElementById('imagePreviewElement');

    if (imageInput && previewContainer && previewImage) {
        imageInput.addEventListener('change', (event) => {
            const file = event.target.files && event.target.files[0];
            if (!file) return;
            if (file.size > 5 * 1024 * 1024) {
                alert('Selected image exceeds 5 MB. Please choose a smaller image.');
                imageInput.value = '';
                return;
            }
            const reader = new FileReader();
            reader.onload = (e) => {
                previewImage.src = e.target.result;
                previewContainer.classList.remove('d-none');
            };
            reader.readAsDataURL(file);
        });
    }

    // 3. Demo Account Quick-Fill on Login Page (for Project Evaluation Convenience)
    document.querySelectorAll('[data-demo-email]').forEach(btn => {
        btn.addEventListener('click', () => {
            const emailInput = document.getElementById('loginEmail');
            const passInput = document.getElementById('loginPassword');
            if (emailInput && passInput) {
                emailInput.value = btn.getAttribute('data-demo-email');
                passInput.value = btn.getAttribute('data-demo-pass');
                emailInput.focus();
            }
        });
    });
});
