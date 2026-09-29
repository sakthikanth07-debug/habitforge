self.addEventListener("push", event => {
    const data = event.data ? event.data.json() : {};
    event.waitUntil(self.registration.showNotification(data.title || "HabitForge reminder", {
        body: data.body || "You have a habit to complete today.",
        icon: "/favicon.ico",
        tag: data.tag || "habitforge-reminder",
        data: { url: data.url || "/habits-page" }
    }));
});

self.addEventListener("notificationclick", event => {
    event.notification.close();
    event.waitUntil(clients.matchAll({ type: "window", includeUncontrolled: true }).then(windowClients => {
        const target = new URL(event.notification.data.url, self.location.origin).href;
        const existing = windowClients.find(client => client.url === target);
        if (existing) return existing.focus();
        return clients.openWindow(target);
    }));
});