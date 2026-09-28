(function () {
    const form = document.getElementById("habitForm");
    const frequency = document.getElementById("frequency");
    const weekdaysContainer = document.getElementById("weekdaysContainer");
    const message = document.getElementById("formMessage");
    const submitButton = document.getElementById("submitButton");

    function clearErrors() {
        document.querySelectorAll(".field-error").forEach(field => { field.textContent = ""; });
        message.hidden = true;
        message.className = "form-message";
        message.textContent = "";
    }

    function updateWeekdayVisibility() {
        const visible = frequency.value === "SPECIFIC_WEEKDAYS";
        weekdaysContainer.hidden = !visible;
        weekdaysContainer.querySelectorAll("input").forEach(input => { input.required = visible; });
    }

    frequency.addEventListener("change", () => {
        clearErrors();
        updateWeekdayVisibility();
    });

    form.addEventListener("input", event => {
        if (event.target.matches("input, textarea, select")) clearErrors();
    });

    form.addEventListener("submit", async event => {
        event.preventDefault();
        clearErrors();

        const name = document.getElementById("name").value.trim();
        const selectedWeekdays = Array.from(form.querySelectorAll('input[name="weekdays"]:checked')).map(input => input.value);
        let invalid = false;

        if (!name) {
            document.getElementById("nameError").textContent = "Give your habit a name.";
            invalid = true;
        }
        if (!frequency.value) {
            document.getElementById("frequencyError").textContent = "Choose how often this habit happens.";
            invalid = true;
        }
        if (frequency.value === "SPECIFIC_WEEKDAYS" && !selectedWeekdays.length) {
            document.getElementById("weekdaysError").textContent = "Choose at least one required day.";
            invalid = true;
        }
        if (invalid) return;

        const habit = {
            name,
            description: document.getElementById("description").value.trim(),
            frequency: frequency.value,
            weekdays: frequency.value === "DAILY" ? "" : selectedWeekdays.join(","),
            reminderTime: document.getElementById("reminderTime").value
        };

        submitButton.disabled = true;
        submitButton.classList.add("is-loading");

        try {
            await window.HabitForgeApi.createHabit(habit);
            message.hidden = false;
            message.className = "form-message is-success";
            message.textContent = "Your habit is in. Taking the first step counts.";
            window.HabitForgeUI.showToast("Habit created successfully.", "success");
            form.reset();
            updateWeekdayVisibility();
            window.setTimeout(() => { window.location.href = "/habits-page"; }, 900);
        } catch (error) {
            message.hidden = false;
            message.className = "form-message is-error";
            message.textContent = error.message || "We couldn't create your habit. Please try again.";
        } finally {
            submitButton.disabled = false;
            submitButton.classList.remove("is-loading");
        }
    });

    updateWeekdayVisibility();
})();