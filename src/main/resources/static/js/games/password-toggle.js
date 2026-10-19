/* =========================================================
   👁 Переключение видимости пароля
   ========================================================= */

document.addEventListener('DOMContentLoaded', () => {
    const toggles = document.querySelectorAll('[data-password-toggle]');

    toggles.forEach((btn) => {
        // Ищем input в том же .password-field
        const container = btn.closest('.password-field');
        if (!container) return;

        const input = container.querySelector('input');
        if (!input) return;

        btn.addEventListener('click', () => {
            const isHidden = input.type === 'password';

            input.type = isHidden ? 'text' : 'password';

            btn.classList.toggle('is-active', isHidden);

            // Меняем иконку и подписи
            if (isHidden) {
                btn.textContent = '🙈';
                btn.setAttribute('aria-label', 'Скрыть пароль');
                btn.setAttribute('title', 'Скрыть пароль');
            } else {
                btn.textContent = '🐵';
                btn.setAttribute('aria-label', 'Показать пароль');
                btn.setAttribute('title', 'Показать пароль');
            }
        });
    });
});