(function () {
    const container = document.getElementById("habitsContainer");
    const emptyState = document.getElementById("emptyState");
    const summary = document.getElementById("resultsSummary");
    const search = document.getElementById("habitSearch");
    const frequencyFilter = document.getElementById("frequencyFilter");
    const statusFilter = document.getElementById("statusFilter");
    let habits = [];

    function render() {
        const query = search.value.trim().toLowerCase();
        const frequency = frequencyFilter.value;
        const status = statusFilter.value;
        const filtered = habits.filter(habit => {
            const matchesSearch = `${habit.name || ""} ${habit.description || ""}`.toLowerCase().includes(query);
            const matchesFrequency = frequency === "ALL" || habit.frequency === frequency;
            const completed = window.HabitForgeUI.hasCompletedToday(habit.id);
            const matchesStatus = status === "ALL" || (status === "COMPLETED" ? completed : !completed);
            return matchesSearch && matchesFrequency && matchesStatus;
        });

        summary.textContent = `${filtered.length} ${filtered.length === 1 ? "habit" : "habits"}${filtered.length !== habits.length ? ` shown of ${habits.length}` : ""}`;
        container.innerHTML = filtered.map(window.HabitForgeUI.renderHabitCard).join("");
        emptyState.hidden = filtered.length > 0;

        if (!habits.length) {
            document.getElementById("emptyTitle").textContent = "No habits yet";
            document.getElementById("emptyDescription").textContent = "Start with one small promise to yourself.";
        } else if (!filtered.length) {
            document.getElementById("emptyTitle").textContent = "No habits match";
            document.getElementById("emptyDescription").textContent = "Try another search or change your filters.";
            emptyState.querySelector(".primary-button").hidden = true;
        } else {
            emptyState.querySelector(".primary-button").hidden = false;
        }
    }

    async function loadHabits() {
        container.innerHTML = '<div class="loading-state"><span class="spinner"></span> Loading your habits...</div>';
        emptyState.hidden = true;
        try {
            const result = await window.HabitForgeApi.getHabits();
            if (!Array.isArray(result)) throw new Error("The habits response could not be read.");
            habits = result;
            render();
            window.HabitForgeUI.bindCompletionActions(container, habits, (habit) => {
                const status = container.querySelector(`[data-status-id="${CSS.escape(String(habit.id))}"]`);
                if (status) {
                    status.className = "status-pill status-complete";
                    status.innerHTML = "<i></i>Completed today";
                }
                render();
            });
        } catch (error) {
            summary.textContent = "";
            container.innerHTML = `<div class="inline-error"><strong>We couldn't load your habits.</strong><p>${window.HabitForgeUI.escapeHTML(error.message)}</p><button class="text-button" type="button" id="retryHabits">Try again</button></div>`;
            document.getElementById("retryHabits").addEventListener("click", loadHabits, { once: true });
        }
    }

    search.addEventListener("input", render);
    frequencyFilter.addEventListener("change", render);
    statusFilter.addEventListener("change", render);
    loadHabits();
})();