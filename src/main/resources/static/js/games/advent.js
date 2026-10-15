(function () {
    "use strict";

    // ---------- CSRF ----------
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

    const CSRF_TOKEN = csrfMeta ? csrfMeta.content : null;
    const CSRF_HEADER = csrfHeaderMeta ? csrfHeaderMeta.content : null;

    // ---------- Элементы модалки ----------
    const modal = document.getElementById("adventModal");
    const backdrop = document.getElementById("adventModalBackdrop");
    const closeBtn = document.getElementById("adventModalClose");
    const modalBody = document.getElementById("adventModalBody");

    if (!modal || !backdrop || !closeBtn || !modalBody) {
        return; // мы не на странице адвента
    }

    // ---------- Состояние ----------
    let isFetching = false;

    // =========================================================
    //  ОТКРЫТИЕ МОДАЛКИ
    // =========================================================

    function openModal() {
        modal.hidden = false;
        document.body.style.overflow = "hidden";
    }

    function closeModal() {
        modal.hidden = true;
        modalBody.innerHTML = "";
        document.body.style.overflow = "";
        isFetching = false;
    }

    // =========================================================
    //  КЛИК ПО ЯЧЕЙКЕ
    // =========================================================

    const daysContainer = document.querySelector(".advent-days");

    if (daysContainer) {
        daysContainer.addEventListener("click", function (event) {
            const cell = event.target.closest(".advent-day");

            if (!cell) return;

            // Пустые ячейки / будущие / призрачные — игнорируем
            if (cell.classList.contains("advent-day--empty")) return;
            if (cell.classList.contains("advent-day--future")) return;
            if (cell.classList.contains("advent-day--ghost")) return;

            const date = cell.dataset.adventDate;
            if (!date) return;

            if (isFetching) return;
            isFetching = true;

            const alreadyOpened = cell.dataset.opened === "true";

            // Анимация открытия — только если ячейка ещё не открыта
            if (!alreadyOpened) {
                cell.classList.add("is-opening");
                setTimeout(() => {
                    cell.classList.remove("is-opening");
                }, 700);
            }

            // AJAX-запрос к серверу
            fetch(`/advent/open/${date}`, {
                method: "POST",
                headers: buildHeaders(),
                credentials: "same-origin"
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error("Не удалось открыть ячейку");
                    }
                    return response.text();
                })
                .then(html => {
                    modalBody.innerHTML = html;

                    // Обновляем состояние ячейки
                    if (!alreadyOpened) {
                        cell.dataset.opened = "true";

                        // Обновляем иконку
                        const iconEl = cell.querySelector(".advent-day__icon");
                        if (iconEl) {
                            // Иконка задания уже должна быть в data-атрибуте,
                            // но мы её не знаем на фронте до загрузки.
                            // Положимся на то, что сервер вернёт HTML,
                            // а мы после закрытия модалки перезагрузим страницу.
                        }
                    }

                    openModal();
                    bindCompleteButton();

                    isFetching = false;
                })
                .catch(error => {
                    console.error(error);
                    isFetching = false;
                    alert("Не удалось открыть ячейку. Попробуйте ещё раз.");
                });
        });
    }

    // =========================================================
    //  КНОПКА «ВЫПОЛНЕНО»
    // =========================================================

    function bindCompleteButton() {
        const btn = modalBody.querySelector(".advent-complete-button");

        if (!btn) return;

        // Уже выполнено — ничего не делаем
        if (btn.dataset.completed === "true") {
            btn.disabled = true;
            return;
        }

        btn.addEventListener("click", function () {
            const date = btn.dataset.adventDate;
            if (!date) return;

            btn.disabled = true;
            const originalText = btn.textContent;
            btn.textContent = "Сохранение...";

            fetch(`/advent/complete/${date}`, {
                method: "POST",
                headers: buildHeaders(),
                credentials: "same-origin"
            })
                .then(response => {
                    if (response.status === 403) {
                        throw new Error("AUTH_REQUIRED");
                    }
                    if (!response.ok) {
                        throw new Error("SERVER_ERROR");
                    }
                    return response.text();
                })
                .then(status => {
                    if (status === "SUCCESS" || status === "ALREADY_COMPLETED") {
                        // Меняем UI кнопки
                        btn.textContent = "✓ Выполнено";
                        btn.classList.add("is-completed");
                        btn.dataset.completed = "true";

                        // Обновляем ячейку в календаре
                        const cell = document.querySelector(
                            `.advent-day[data-advent-date="${date}"]`
                        );
                        if (cell) {
                            cell.dataset.completed = "true";

                            if (!cell.querySelector(".advent-day__check")) {
                                const check = document.createElement("span");
                                check.className = "advent-day__check";
                                check.textContent = "✓";
                                cell.appendChild(check);
                            }
                        }

                        // Обновляем прогресс-бар (опционально)
                        updateProgress();

                        // Закрываем модалку через 1 сек
                        setTimeout(closeModal, 1000);
                    } else {
                        throw new Error("UNKNOWN_STATUS");
                    }
                })
                .catch(error => {
                    console.error(error);
                    btn.disabled = false;
                    btn.textContent = originalText;
                    alert("Не удалось сохранить. Попробуйте ещё раз.");
                });
        });
    }

    // =========================================================
    //  ПРОГРЕСС
    // =========================================================

    function updateProgress() {
        const fill = document.querySelector(".advent-progress-fill");
        const text = document.querySelector(".advent-progress-text");

        if (!fill) return;

        // Пересчитываем количество открытых ячеек
        const total = document.querySelectorAll(".advent-day[data-advent-date]").length;
        const opened = document.querySelectorAll(
            '.advent-day[data-opened="true"]'
        ).length;
        const completed = document.querySelectorAll(
            '.advent-day[data-completed="true"]'
        ).length;

        if (total > 0) {
            fill.style.width = (opened * 100 / total) + "%";
        }

        if (text) {
            text.innerHTML =
                `Открыто <strong>${opened}</strong> из <strong>${total}</strong> · ` +
                `Выполнено <strong>${completed}</strong>`;
        }
    }

    // =========================================================
    //  ЗАКРЫТИЕ МОДАЛКИ
    // =========================================================

    closeBtn.addEventListener("click", closeModal);
    backdrop.addEventListener("click", closeModal);

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape" && !modal.hidden) {
            closeModal();
        }
    });

    // =========================================================
    //  ВСПОМОГАТЕЛЬНОЕ
    // =========================================================

    function buildHeaders() {
        const headers = {
            "Accept": "text/html"
        };

        if (CSRF_TOKEN && CSRF_HEADER) {
            headers[CSRF_HEADER] = CSRF_TOKEN;
        }

        return headers;
    }

})();