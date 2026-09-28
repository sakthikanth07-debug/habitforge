document.addEventListener("DOMContentLoaded", function () {

    const habitForm = document.getElementById("habitForm");

    if (habitForm) {
        habitForm.addEventListener("submit", async function (event) {

            event.preventDefault();

            const name = document.getElementById("name").value;
            const description = document.getElementById("description").value;
            const frequency = document.getElementById("frequency").value;
            const weekdaysElement = document.getElementById("weekdays");
            const reminderTimeElement = document.getElementById("reminderTime");

            const weekdays = weekdaysElement
                ? weekdaysElement.value
                : "";

            const reminderTime = reminderTimeElement
                ? reminderTimeElement.value
                : "";

            const habit = {
                name: name,
                description: description,
                frequency: frequency,
                weekdays: weekdays,
                reminderTime: reminderTime
            };

            try {

                const response = await fetch("http://localhost:8080/habits", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(habit)
                });

                if (!response.ok) {
                    const error = await response.text();
                    throw new Error(error);
                }

                const savedHabit = await response.json();

                alert("Habit created successfully!");

                habitForm.reset();

                console.log(savedHabit);

            } catch (error) {

                console.error(error);

                alert(
                    "HabitForge could not reach the server. Check that Spring Boot is running."
                );
            }
        });
    }
});