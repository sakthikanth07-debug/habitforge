(function () {
    const container = document.getElementById("habitsContainer");
    const emptyState = document.getElementById("emptyState");
    const summary = document.getElementById("resultsSummary");
    const search = document.getElementById("habitSearch");
    const frequencyFilter = document.getElementById("frequencyFilter");
    const statusFilter = document.getElementById("statusFilter");
    const remindersPanel = document.getElementById("remindersPanel");
    const remindersContainer = document.getElementById("remindersContainer");
    
    let habits = [];
    let currentPage = 0;
    const pageSize = 10;
    let debounceTimer;
    const reminderHabits = [];
    let reminderActionsBound = false;

    async function loadReminders() {
        try {
            const reminders = await window.HabitForgeApi.getReminders();
            window.HabitForgeUI.notifyReminders(reminders);
            reminderHabits.splice(0, reminderHabits.length, ...reminders.map(window.HabitForgeUI.reminderHabit));
            remindersPanel.hidden = reminders.length === 0;
            remindersContainer.innerHTML = window.HabitForgeUI.renderReminderCards(reminders);
            if (!reminderActionsBound) {
                window.HabitForgeUI.bindCompletionActions(remindersContainer, reminderHabits, async () => {
                    await loadReminders();
                    loadHabits();
                });
                reminderActionsBound = true;
            }
        } catch (error) {
            remindersPanel.hidden = false;
            remindersContainer.innerHTML = `<div class="inline-error"><strong>We couldn't load reminders.</strong><p>${window.HabitForgeUI.escapeHTML(error.message)}</p></div>`;
        }
    }

    async function loadHabits() {
        container.innerHTML = '<div class="loading-state"><span class="spinner"></span> Loading your habits...</div>';
        emptyState.hidden = true;
        try {
            const query = search.value.trim().toLowerCase();
            const frequency = frequencyFilter.value === "ALL" ? "" : frequencyFilter.value;
            
            const result = await window.HabitForgeApi.getHabits(query, frequency, currentPage, pageSize);
            
            if (!result || !Array.isArray(result.content)) throw new Error("The habits response could not be read.");
            habits = result.content;
            
            const status = statusFilter.value;
            const filtered = habits.filter(habit => {
                const completed = window.HabitForgeUI.hasCompletedToday(habit.id);
                return status === "ALL" || (status === "COMPLETED" ? completed : !completed);
            });
            
            summary.textContent = `${filtered.length} habit(s) shown of ${result.totalElements} total`;
            
            container.innerHTML = filtered.map(window.HabitForgeUI.renderHabitCard).join("");
            emptyState.hidden = filtered.length > 0;
            
            if (result.totalPages > 1) {
                const paginationHtml = `
                    <div class="pagination" style="margin-top: 20px; display: flex; justify-content: space-between; align-items: center;">
                        <button class="secondary-button" id="prevPage" ${result.first ? 'disabled' : ''}>Previous</button>
                        <span>Page ${result.number + 1} of ${result.totalPages}</span>
                        <button class="secondary-button" id="nextPage" ${result.last ? 'disabled' : ''}>Next</button>
                    </div>
                `;
                container.innerHTML += paginationHtml;
                
                const prevPageBtn = document.getElementById("prevPage");
                if (prevPageBtn) prevPageBtn.addEventListener("click", () => { currentPage--; loadHabits(); });
                
                const nextPageBtn = document.getElementById("nextPage");
                if (nextPageBtn) nextPageBtn.addEventListener("click", () => { currentPage++; loadHabits(); });
            }

            if (!habits.length && !query && !frequency) {
                document.getElementById("emptyTitle").textContent = "No habits yet";
                document.getElementById("emptyDescription").textContent = "Start with one small promise to yourself.";
            } else if (!filtered.length) {
                document.getElementById("emptyTitle").textContent = "No habits match";
                document.getElementById("emptyDescription").textContent = "Try another search or change your filters.";
                if (emptyState.querySelector(".primary-button")) {
                    emptyState.querySelector(".primary-button").hidden = true;
                }
            } else {
                if (emptyState.querySelector(".primary-button")) {
                    emptyState.querySelector(".primary-button").hidden = false;
                }
            }
            
            window.HabitForgeUI.bindCompletionActions(container, habits, (habit) => {
                const statusEl = container.querySelector(`[data-status-id="${CSS.escape(String(habit.id))}"]`);
                if (statusEl) {
                    statusEl.className = "status-pill status-complete";
                    statusEl.innerHTML = "<i></i>Completed today";
                }
                loadHabits();
            });
        } catch (error) {
            summary.textContent = "";
            container.innerHTML = `<div class="inline-error"><strong>We couldn't load your habits.</strong><p>${window.HabitForgeUI.escapeHTML(error.message)}</p><button class="text-button" type="button" id="retryHabits">Try again</button></div>`;
            document.getElementById("retryHabits").addEventListener("click", loadHabits, { once: true });
        }
    }
    
    function triggerLoad() {
        currentPage = 0;
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => {
            loadHabits();
        }, 300);
    }

    search.addEventListener("input", triggerLoad);
    frequencyFilter.addEventListener("change", triggerLoad);
    statusFilter.addEventListener("change", () => { currentPage = 0; loadHabits(); });
    
    loadHabits();
    loadReminders();
    window.setInterval(loadReminders, 15000);
})();