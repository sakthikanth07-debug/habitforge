(function () {
    const params = new URLSearchParams(window.location.search);
    const habitId = params.get("id");
    const loading = document.getElementById("detailsLoading");
    const errorPanel = document.getElementById("detailsError");
    const content = document.getElementById("detailsContent");
    let habit = null;
    let visibleMonth = new Date(new Date().getFullYear(), new Date().getMonth(), 1);

    function setError(message) {
        loading.hidden = true;
        content.hidden = true;
        errorPanel.hidden = false;
        document.getElementById("detailsErrorMessage").textContent = message;
    }

    function isoDate(date) {
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        return `${date.getFullYear()}-${month}-${day}`;
    }

    function isRequiredDay(date) {
        if (habit.frequency !== "SPECIFIC_WEEKDAYS") return true;
        const days = String(habit.weekdays || "").split(",").map(day => day.trim().toUpperCase());
        const weekday = ["SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"][date.getDay()];
        return days.includes(weekday);
    }

    function renderCalendar() {
        const title = visibleMonth.toLocaleDateString(undefined, { month: "long", year: "numeric" });
        document.getElementById("calendarMonth").textContent = title;
        const grid = document.getElementById("calendarGrid");
        const firstDay = new Date(visibleMonth.getFullYear(), visibleMonth.getMonth(), 1);
        const dayCount = new Date(visibleMonth.getFullYear(), visibleMonth.getMonth() + 1, 0).getDate();
        const mondayOffset = (firstDay.getDay() + 6) % 7;
        const today = window.HabitForgeUI.todayISO();
        const canShowSessionCompletions = visibleMonth.getFullYear() === new Date().getFullYear()
            && visibleMonth.getMonth() === new Date().getMonth();
        const completedToday = canShowSessionCompletions && window.HabitForgeUI.hasCompletedToday(habit.id);
        const cells = Array.from({ length: mondayOffset }, () => '<span class="calendar-empty" aria-hidden="true"></span>');

        for (let day = 1; day <= dayCount; day += 1) {
            const date = new Date(visibleMonth.getFullYear(), visibleMonth.getMonth(), day);
            const dateKey = isoDate(date);
            const required = isRequiredDay(date);
            const isToday = dateKey === today;
            const isFuture = dateKey > today;
            const isComplete = isToday && completedToday;
            const isUnknown = required && dateKey < today && !isComplete;
            const classes = ["calendar-day"];
            if (isToday) classes.push("is-today");
            if (isComplete) classes.push("is-complete");
            else if (isUnknown) classes.push("is-unknown");
            else if (required) classes.push("is-required");
            else classes.push("is-rest");
            if (isFuture) classes.push("is-future");

            let status = isComplete ? "completed" : !required ? "not required" : isFuture ? "required, upcoming" : "required, history unavailable";
            if (isToday && !completedToday) status = required ? "today, not completed" : "today, not required";
            cells.push(`<span class="${classes.join(" ")}" role="gridcell" aria-label="${date.toLocaleDateString(undefined, { month: "long", day: "numeric" })}: ${status}" title="${status}">${day}</span>`);
        }

        grid.innerHTML = cells.join("");
    }

    function renderHistory() {
        const list = document.getElementById("completionHistory");
        const records = window.HabitForgeUI.getTodayCompletions().filter(record => String(record.habitId) === String(habit.id)).reverse();
        list.innerHTML = records.length
            ? records.map(record => `<div class="history-item"><span class="history-check" aria-hidden="true">✓</span><div><strong>${window.HabitForgeUI.escapeHTML(record.date)}</strong><span>Completed at ${window.HabitForgeUI.escapeHTML(record.time)}</span></div></div>`).join("")
            : '<p class="history-empty">No completions confirmed in this session yet.</p>';
        document.getElementById("todayStatus").textContent = window.HabitForgeUI.hasCompletedToday(habit.id) ? "Completed" : "Not completed";
    }

    function renderHabit(result) {
        habit = result;
        document.title = `${habit.name} | HabitForge`;
        document.getElementById("detailName").textContent = habit.name;
        document.getElementById("detailDescription").textContent = habit.description || "A habit worth making time for.";
        document.getElementById("detailFrequency").textContent = window.HabitForgeUI.frequencyLabel(habit);
        document.getElementById("detailReminder").textContent = habit.reminderTime ? `Reminder ${habit.reminderTime}` : "No reminder set";
        document.getElementById("currentStreak").textContent = Number.isFinite(habit.currentStreak) ? habit.currentStreak : "--";
        document.getElementById("bestStreak").textContent = Number.isFinite(habit.bestStreak) ? habit.bestStreak : "--";

        const completeButton = document.getElementById("detailCompleteButton");
        completeButton.dataset.completeId = habit.id;
        const completed = window.HabitForgeUI.hasCompletedToday(habit.id);
        const scheduled = window.HabitForgeUI.isScheduledToday(habit);
        window.HabitForgeUI.setButtonComplete(completeButton, completed);
        if (!scheduled && !completed) {
            completeButton.disabled = true;
            completeButton.querySelector(".button-label").textContent = "Not scheduled today";
        }

        renderHistory();
        renderCalendar();
        loading.hidden = true;
        content.hidden = false;
        window.HabitForgeUI.bindCompletionActions(content, [habit], () => {
            renderHistory();
            renderCalendar();
        });
    }

    document.getElementById("previousMonth").addEventListener("click", () => {
        visibleMonth = new Date(visibleMonth.getFullYear(), visibleMonth.getMonth() - 1, 1);
        renderCalendar();
    });
    document.getElementById("nextMonth").addEventListener("click", () => {
        visibleMonth = new Date(visibleMonth.getFullYear(), visibleMonth.getMonth() + 1, 1);
        renderCalendar();
    });

    if (!habitId || !/^\d+$/.test(habitId)) {
        setError("No valid habit was selected. Return to your habits and choose one to view.");
    } else {
        window.HabitForgeApi.getHabit(habitId).then(result => {
            if (!result || !result.id) throw new Error("That habit could not be found.");
            renderHabit(result);
        }).catch(error => setError(error.message || "We couldn't load this habit."));
    }
})();