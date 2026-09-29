(function () {
    const weekdays = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"];
    let memoryRecords = [];
    const notifiedReminderIds = new Set();
    const dayNames = {
        MONDAY: "Monday",
        TUESDAY: "Tuesday",
        WEDNESDAY: "Wednesday",
        THURSDAY: "Thursday",
        FRIDAY: "Friday",
        SATURDAY: "Saturday",
        SUNDAY: "Sunday"
    };

    function todayISO() {
        const date = new Date();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${date.getFullYear()}-${month}-${day}`;
    }

    function completionStorageKey() {
        return `habitforge:completions:${todayISO()}`;
    }

    function getTodayCompletions() {
        try {
            const records = JSON.parse(sessionStorage.getItem(completionStorageKey()) || "[]");
            memoryRecords = Array.isArray(records) ? records : [];
            return memoryRecords;
        } catch {
            return memoryRecords;
        }
    }

    function hasCompletedToday(habitId) {
        return getTodayCompletions().some(record => String(record.habitId) === String(habitId));
    }

    function recordCompletion(habit) {
        const records = getTodayCompletions();
        if (!records.some(record => String(record.habitId) === String(habit.id))) {
            records.push({
                habitId: habit.id,
                name: habit.name,
                date: todayISO(),
                time: new Date().toLocaleTimeString([], { hour: "numeric", minute: "2-digit" })
            });
            memoryRecords = records;
            try {
                sessionStorage.setItem(completionStorageKey(), JSON.stringify(records));
            } catch {
                memoryRecords = records;
            }
        }
    }

    function escapeHTML(value) {
        return String(value ?? "").replace(/[&<>"']/g, character => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            "\"": "&quot;",
            "'": "&#39;"
        })[character]);
    }

    function weekdayNames(value) {
        return String(value || "").split(",").map(day => dayNames[day.trim().toUpperCase()]).filter(Boolean);
    }

    function frequencyLabel(habit) {
        if (habit.frequency === "DAILY") return "Every day";
        const names = weekdayNames(habit.weekdays);
        return names.length ? names.join(", ") : "Specific weekdays";
    }

    function isScheduledToday(habit) {
        if (habit.frequency !== "SPECIFIC_WEEKDAYS") return true;
        return String(habit.weekdays || "").split(",").map(day => day.trim().toUpperCase()).includes(weekdays[new Date().getDay()]);
    }

    function setButtonComplete(button, completed) {
        button.classList.toggle("is-complete", completed);
        button.disabled = completed;
        const label = button.querySelector(".button-label");
        if (label) label.textContent = completed ? "Completed today" : "Mark complete";
    }

    function showToast(message, type = "success") {
        const region = document.getElementById("toastRegion");
        if (!region) return;

        const toast = document.createElement("div");
        toast.className = `toast toast-${type}`;
        toast.textContent = message;
        region.replaceChildren(toast);
        window.setTimeout(() => toast.remove(), 4200);
    }

    function notifyReminders(reminders) {
        reminders.forEach(reminder => {
            const reminderId = String(reminder.id);
            if (notifiedReminderIds.has(reminderId)) return;
            notifiedReminderIds.add(reminderId);

            if ("Notification" in window && Notification.permission === "granted") {
                new Notification("HabitForge reminder", { body: reminder.message });
            }
            showToast(reminder.message, "error");
        });
    }

    function bindCompletionActions(container, habits, onSuccess) {
        const pendingIds = new Set();

        container.addEventListener("click", async event => {
            const button = event.target.closest("[data-complete-id]");
            if (!button || !container.contains(button)) return;

            const habitId = button.dataset.completeId;
            const habit = habits.find(item => String(item.id) === String(habitId));
            if (!habit || pendingIds.has(String(habitId)) || hasCompletedToday(habitId)) return;

            if (!isScheduledToday(habit)) {
                showToast("This habit is not scheduled for today.", "error");
                return;
            }

            pendingIds.add(String(habitId));
            button.disabled = true;
            button.classList.add("is-loading");

            try {
                const result = await window.HabitForgeApi.completeHabit(habitId, todayISO());
                recordCompletion(habit);
                setButtonComplete(button, true);
                if (onSuccess) onSuccess(habit, result);
                showToast(`${habit.name} completed. Nice work.`, "success");
            } catch (error) {
                if (/already marked complete/i.test(error.message)) {
                    recordCompletion(habit);
                    setButtonComplete(button, true);
                    if (onSuccess) onSuccess(habit, null);
                    showToast("This habit is already marked complete for today.", "success");
                } else {
                    button.disabled = false;
                    showToast(error.message || "Unable to complete this habit.", "error");
                }
            } finally {
                pendingIds.delete(String(habitId));
                button.classList.remove("is-loading");
            }
        });
    }

    function renderHabitCard(habit) {
        const completed = hasCompletedToday(habit.id);
        const scheduled = isScheduledToday(habit);
        const statusClass = completed ? "status-complete" : scheduled ? "status-pending" : "status-rest";
        const statusText = completed ? "Completed today" : scheduled ? "Up next" : "Rest day";
        const description = habit.description || "A habit worth making time for.";
        const reminder = habit.reminderTime ? `Reminder ${escapeHTML(habit.reminderTime)}` : "No reminder";

        return `<article class="habit-card" data-habit-card="${escapeHTML(habit.id)}">
            <div class="habit-card-top"><span class="habit-mark" aria-hidden="true">✳</span><span class="status-pill ${statusClass}" data-status-id="${escapeHTML(habit.id)}"><i></i>${statusText}</span></div>
            <h3>${escapeHTML(habit.name)}</h3><p class="habit-description">${escapeHTML(description)}</p>
            <div class="habit-meta"><span class="meta-label">SCHEDULE</span><span>${escapeHTML(frequencyLabel(habit))}</span></div>
            <div class="habit-meta"><span class="meta-label">REMINDER</span><span>${reminder}</span></div>
            <div class="habit-streak-row"><span>Current <strong>${Number.isFinite(habit.currentStreak) ? habit.currentStreak : "--"}</strong></span><span>Best <strong>${Number.isFinite(habit.bestStreak) ? habit.bestStreak : "--"}</strong></span></div>
            <div class="habit-card-actions"><button class="complete-action ${completed ? "is-complete" : ""}" type="button" data-complete-id="${escapeHTML(habit.id)}" ${completed || !scheduled ? "disabled" : ""}><span class="button-label">${completed ? "Completed today" : scheduled ? "Mark complete" : "Not scheduled today"}</span><span class="button-spinner" aria-hidden="true"></span><span aria-hidden="true">✓</span></button><a class="details-link" href="/pages/habit-details.html?id=${encodeURIComponent(habit.id)}" aria-label="View ${escapeHTML(habit.name)} details">Details <span aria-hidden="true">→</span></a></div>
        </article>`;
    }

    function reminderHabit(reminder) {
        return {
            id: reminder.habitId,
            name: reminder.habitName,
            frequency: reminder.frequency,
            weekdays: reminder.weekdays
        };
    }

    function renderReminderCards(reminders) {
        return reminders.map(reminder => `<article class="reminder-item" data-reminder-id="${escapeHTML(reminder.id)}">
            <span class="reminder-mark" aria-hidden="true">!</span>
            <div class="reminder-copy"><strong>${escapeHTML(reminder.habitName)}</strong><span>${escapeHTML(reminder.message)}</span></div>
            <button class="complete-action reminder-action" type="button" data-complete-id="${escapeHTML(reminder.habitId)}"><span class="button-label">Mark complete</span><span class="button-spinner" aria-hidden="true"></span><span aria-hidden="true">✓</span></button>
        </article>`).join("");
    }

    window.HabitForgeUI = Object.freeze({
        todayISO,
        getTodayCompletions,
        hasCompletedToday,
        recordCompletion,
        escapeHTML,
        weekdayNames,
        frequencyLabel,
        isScheduledToday,
        setButtonComplete,
        showToast,
        notifyReminders,
        bindCompletionActions,
        renderHabitCard,
        reminderHabit,
        renderReminderCards
    });
})();