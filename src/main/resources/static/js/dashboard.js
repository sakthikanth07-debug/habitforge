(function () {
    const habitsContainer = document.getElementById("dashboardHabits");
    let habits = [];

    function updateProgress() {
        const completed = habits.filter(habit => window.HabitForgeUI.hasCompletedToday(habit.id)).length;
        const total = habits.length;
        const percentage = total ? Math.round((completed / total) * 100) : 0;
        document.getElementById("totalHabits").textContent = total;
        document.getElementById("completedHabits").textContent = completed;
        document.getElementById("progressCount").textContent = `${completed} of ${total}`;
        document.getElementById("progressBar").style.width = `${percentage}%`;
        document.querySelector(".progress-track").setAttribute("aria-valuenow", percentage);
    }

    function renderActivity() {
        const container = document.getElementById("recentActivity");
        const records = window.HabitForgeUI.getTodayCompletions().slice().reverse().slice(0, 4);

        if (!records.length) {
            container.innerHTML = '<p class="muted-copy">Your completed habits will show up here.</p>';
            return;
        }

        container.innerHTML = records.map(record => `<div class="activity-item"><span class="activity-check" aria-hidden="true">✓</span><div><strong>${window.HabitForgeUI.escapeHTML(record.name)}</strong><span>Completed at ${window.HabitForgeUI.escapeHTML(record.time)}</span></div></div>`).join("");
    }

    async function loadDashboard() {
        try {
            const result = await window.HabitForgeApi.getHabits();
            if (!Array.isArray(result)) throw new Error("The habits response could not be read.");
            habits = result;
            updateProgress();

            if (!habits.length) {
                habitsContainer.innerHTML = '<div class="inline-empty"><span aria-hidden="true">✳</span><div><strong>Your first small step starts here.</strong><p>Create a habit to begin tracking your routine.</p></div><a class="text-link" href="/add-habits">Add a habit →</a></div>';
            } else {
                habitsContainer.innerHTML = habits.slice(0, 3).map(window.HabitForgeUI.renderHabitCard).join("");
                window.HabitForgeUI.bindCompletionActions(habitsContainer, habits, () => {
                    updateProgress();
                    renderActivity();
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

        renderActivity();
    }

    const today = new Date();
    document.getElementById("todayLabel").textContent = today.toLocaleDateString(undefined, { weekday: "long", month: "long", day: "numeric" }).toUpperCase();
    loadDashboard();
})();