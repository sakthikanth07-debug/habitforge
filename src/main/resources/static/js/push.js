(function () {
    function showMessage(message, type) {
        if (window.HabitForgeUI && window.HabitForgeUI.showToast) {
            window.HabitForgeUI.showToast(message, type);
        } else {
            console.warn(message);
        }
    }

    function decodeBase64Url(value) {
        const padding = "=".repeat((4 - value.length % 4) % 4);
        const base64 = (value + padding).replace(/-/g, "+").replace(/_/g, "/");
        const raw = window.atob(base64);
        return Uint8Array.from(raw, character => character.charCodeAt(0));
    }

    function encodeBase64(buffer) {
        return window.btoa(String.fromCharCode(...new Uint8Array(buffer)));
    }

    async function enablePush() {
        if (!("serviceWorker" in navigator) || !("PushManager" in window) || !("Notification" in window)) {
            showMessage("This browser does not support desktop reminders.", "error");
            return;
        }

        const configResponse = await fetch("/push-config", { credentials: "include" });
        const config = await configResponse.json();
        if (!config.enabled || !config.publicKey) {
            showMessage("Desktop reminders need VAPID keys configured on the server.", "error");
            return;
        }

        const permission = Notification.permission === "granted"
            ? "granted"
            : await Notification.requestPermission();
        if (permission !== "granted") {
            showMessage("Desktop reminder permission was not granted.", "error");
            return;
        }

        const registration = await navigator.serviceWorker.register("/sw.js");
        const subscription = await registration.pushManager.getSubscription()
            || await registration.pushManager.subscribe({
                userVisibleOnly: true,
                applicationServerKey: decodeBase64Url(config.publicKey)
            });
        const key = subscription.getKey("p256dh");
        const auth = subscription.getKey("auth");
        const response = await fetch("/push-subscriptions", {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json", Accept: "application/json" },
            body: JSON.stringify({
                endpoint: subscription.endpoint,
                p256dh: encodeBase64(key),
                auth: encodeBase64(auth)
            })
        });
        if (!response.ok) throw new Error("The push subscription could not be saved.");
        showMessage("Desktop reminders are enabled.", "success");
    }

    document.querySelectorAll("[data-enable-push]").forEach(button => {
        button.addEventListener("click", () => enablePush().catch(error => showMessage(error.message, "error")));
    });
})();