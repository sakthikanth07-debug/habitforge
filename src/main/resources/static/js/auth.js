(function () {
    document.querySelectorAll("[data-password-target]").forEach(button => {
        button.addEventListener("click", () => {
            const input = document.getElementById(button.dataset.passwordTarget);
            const isVisible = input.type === "text";
            input.type = isVisible ? "password" : "text";
            button.textContent = isVisible ? "Show" : "Hide";
            button.setAttribute("aria-label", `${isVisible ? "Show" : "Hide"} password`);
        });
    });

    document.querySelectorAll("[data-auth-form]").forEach(form => {
        const feedback = form.querySelector(".auth-feedback");
        const password = form.querySelector("[name='password']");
        const confirmation = form.querySelector("[name='confirmPassword']");

        if (confirmation) {
            const validateConfirmation = () => {
                confirmation.setCustomValidity(confirmation.value && confirmation.value !== password.value
                    ? "Passwords do not match."
                    : "");
            };
            password.addEventListener("input", validateConfirmation);
            confirmation.addEventListener("input", validateConfirmation);
        }

        form.addEventListener("input", () => {
            feedback.hidden = true;
            feedback.textContent = "";
        });

        form.addEventListener("submit", event => {
            event.preventDefault();
            if (!form.reportValidity()) return;

            const isSignup = Boolean(confirmation);
            const payload = Object.fromEntries(new FormData(form).entries());
            delete payload.confirmPassword;
            feedback.textContent = "Connecting...";
            feedback.hidden = false;

            fetch(isSignup ? "/api/auth/signup" : "/api/auth/login", {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json", Accept: "application/json" },
                body: JSON.stringify(payload)
            })
                .then(async response => {
                    const data = await response.json().catch(() => ({}));
                    if (!response.ok) throw new Error(data.message || "We could not authenticate your account.");
                    window.location.assign("/habits-page");
                })
                .catch(error => {
                    feedback.textContent = error.message;
                    feedback.hidden = false;
                });
        });
    });
})();