(() => {
    const overlay = document.getElementById('achievement-notification-overlay');
    if (!overlay) return;

    const icon = document.getElementById('achievement-notification-icon');
    const title = document.getElementById('achievement-notification-title');
    const description = document.getElementById('achievement-notification-description');
    const okButton = document.getElementById('achievement-notification-ok');

    let notificationOpen = false;
    let requestInProgress = false;

    function showNotification(notification) {
        icon.textContent = notification.icon || '🏆';
        title.textContent = notification.title || 'Новое достижение';
        description.textContent = notification.description || 'Поздравляем!';
        notificationOpen = true;
        overlay.classList.add('is-visible');
        overlay.setAttribute('aria-hidden', 'false');
        okButton.focus();
    }

    function hideNotification() {
        overlay.classList.remove('is-visible');
        overlay.setAttribute('aria-hidden', 'true');
        notificationOpen = false;
        // После закрытия сразу проверяем, не ожидает ли следующее достижение.
        window.setTimeout(loadNextNotification, 250);
    }

    async function loadNextNotification() {
        if (notificationOpen || requestInProgress) return;
        requestInProgress = true;

        try {
            const response = await fetch('/achievements/notifications/next', {
                method: 'GET',
                credentials: 'same-origin',
                headers: { 'Accept': 'application/json' },
                cache: 'no-store'
            });

            if (response.status === 204 || !response.ok) return;

            const notification = await response.json();
            if (notification && notification.title) showNotification(notification);
        } catch (error) {
            // При временной сетевой ошибке следующая проверка повторит запрос.
        } finally {
            requestInProgress = false;
        }
    }

    okButton.addEventListener('click', hideNotification);
    overlay.addEventListener('click', event => {
        // Не закрываем окно кликом по затемнённому фону: только кнопкой «Отлично».
        if (event.target === overlay) event.preventDefault();
    });

    loadNextNotification();
    window.setInterval(() => {
        if (!notificationOpen) loadNextNotification();
    }, 5000);
})();
