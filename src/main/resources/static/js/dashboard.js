(function () {
    const habitsContainer = document.getElementById("dashboardHabits");
    const remindersContainer = document.getElementById("dashboardReminders");
    const reminderHabits = [];
    let reminderActionsBound = false;
    let habits = [];

    async function loadReminders() {
        try {
            const reminders = await window.HabitForgeApi.getReminders();
            window.HabitForgeUI.notifyReminders(reminders);
            reminderHabits.splice(0, reminderHabits.length, ...reminders.map(window.HabitForgeUI.reminderHabit));
            remindersContainer.innerHTML = reminders.length
                ? window.HabitForgeUI.renderReminderCards(reminders)
                : '<p class="muted-copy">No pending reminders.</p>';
            if (!reminderActionsBound) {
                window.HabitForgeUI.bindCompletionActions(remindersContainer, reminderHabits, async () => {
                    await loadReminders();
                    loadDashboard();
                });
                reminderActionsBound = true;
            }
        } catch (error) {
            remindersContainer.innerHTML = `<div class="inline-error"><strong>We couldn't load reminders.</strong><p>${window.HabitForgeUI.escapeHTML(error.message)}</p></div>`;
        }
    }

    async function updateProgress() {
        try {
            const stats = await window.HabitForgeApi.getDashboard();
            const total = stats.totalHabits || 0;
            const completed = stats.completedToday || 0;
            const percentage = total ? Math.round((completed / total) * 100) : 0;
            
            document.getElementById("totalHabits").textContent = total;
            const completedHabitsEl = document.getElementById("completedHabits");
            if (completedHabitsEl) completedHabitsEl.textContent = completed;
            
            const progressCount = document.getElementById("progressCount");
            if (progressCount) progressCount.textContent = `${completed} of ${total}`;
            
            const progressBar = document.getElementById("progressBar");
            if (progressBar) progressBar.style.width = `${percentage}%`;
            
            const progressTrack = document.querySelector(".progress-track");
            if (progressTrack) progressTrack.setAttribute("aria-valuenow", percentage);
            
            return stats;
        } catch (e) {
            console.error("Failed to load dashboard stats", e);
            return null;
        }
    }

    function renderActivity(records) {
        const container = document.getElementById("recentActivity");

        if (!records || !records.length) {
            container.innerHTML = '<p class="muted-copy">Your completed habits will show up here.</p>';
            return;
        }

        container.innerHTML = records.slice(0, 4).map(record => `<div class="activity-item"><span class="activity-check" aria-hidden="true">✓</span><div><strong>${window.HabitForgeUI.escapeHTML(record.name)}</strong><span>Completed on ${window.HabitForgeUI.escapeHTML(record.date)}</span></div></div>`).join("");
    }

    async function loadDashboard() {
        try {
            // Get first page of habits for the quick list
            const result = await window.HabitForgeApi.getHabits("", "", 0, 3);
            if (!result || !Array.isArray(result.content)) throw new Error("The habits response could not be read.");
            habits = result.content;
            


            if (!habits.length) {
                habitsContainer.innerHTML = '<div class="inline-empty"><span aria-hidden="true">✳</span><div><strong>Your first small step starts here.</strong><p>Create a habit to begin tracking your routine.</p></div><a class="text-link" href="/add-habits">Add a habit →</a></div>';
            } else {
                habitsContainer.innerHTML = habits.map(window.HabitForgeUI.renderHabitCard).join("");
                window.HabitForgeUI.bindCompletionActions(habitsContainer, habits, async () => {
                    const stats = await updateProgress();
                    loadReminders();
                    if (stats) renderActivity(stats.recentCompletions);
                    habitsContainer.querySelectorAll("[data-status-id]").forEach(status => {
                        if (window.HabitForgeUI.hasCompletedToday(status.dataset.statusId)) {
                            status.className = "status-pill status-complete";
                            status.innerHTML = "<i></i>Completed today";
                        }
                    });
                });
            }
        } catch (error) {
            document.getElementById("totalHabits").textContent = "--";
            habitsContainer.innerHTML = `<div class="inline-error"><strong>We couldn't load your habits.</strong><p>${window.HabitForgeUI.escapeHTML(error.message)}</p><button class="text-button" type="button" id="retryDashboard">Try again</button></div>`;
            document.getElementById("retryDashboard").addEventListener("click", loadDashboard, { once: true });
        }

        const stats = await updateProgress();
        if (stats) renderActivity(stats.recentCompletions);
    }

    const today = new Date();
    document.getElementById("todayLabel").textContent = today.toLocaleDateString(undefined, { weekday: "long", month: "long", day: "numeric" }).toUpperCase();
    loadDashboard();
    loadReminders();
    window.setInterval(loadReminders, 15000);
})();