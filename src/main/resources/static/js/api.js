const API_BASE_URL = "http://localhost:8080";

async function apiRequest(path, options = {}) {

    let response;

    try {
        response = await fetch(`${API_BASE_URL}${path}`, {
            ...options,
            headers: {
                Accept: "application/json",
                ...(options.body
                    ? { "Content-Type": "application/json" }
                    : {}),
                ...options.headers
            }
        });
    } catch (error) {
        throw new Error(
            "HabitForge could not reach the server. Check your connection and try again."
        );
    }

    const responseText = await response.text();

    let data = null;

    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = responseText;
        }
    }

    if (!response.ok) {

        const detail =
            typeof data === "string"
                ? data
                : data?.message || data?.detail || data?.error;

        const fallbackMessages = {
            400: "Please check the information and try again.",
            404: "That habit could not be found.",
            409: "This habit is already marked complete.",
            500: "The server could not complete your request. Please try again."
        };

        throw new Error(
            detail ||
            fallbackMessages[response.status] ||
            "Something went wrong. Please try again."
        );
    }

    return data;
}

function getHabits() {
    return apiRequest("/habits");
}

function createHabit(habit) {
    return apiRequest("/habits", {
        method: "POST",
        body: JSON.stringify(habit)
    });
}

function getHabit(id) {
    return apiRequest("/habits/get", {
        method: "POST",
        body: JSON.stringify({
            id: Number(id)
        })
    });
}

function completeHabit(habitId, date) {
    return apiRequest("/habits/complete", {
        method: "POST",
        body: JSON.stringify({
            habitId: Number(habitId),
            date: date
        })
    });
}

function getCalendar(habitId) {
    return apiRequest("/habits/calendar", {
        method: "POST",
        body: JSON.stringify({
            id: Number(habitId)
        })
    });
}

function getStreak(habitId) {
    return apiRequest("/habits/streak", {
        method: "POST",
        body: JSON.stringify({
            id: Number(habitId)
        })
    });
}

window.API_BASE_URL = API_BASE_URL;

window.HabitForgeApi = Object.freeze({
    getHabits,
    createHabit,
    getHabit,
    completeHabit,
    getCalendar,
    getStreak
});