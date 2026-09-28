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
            feedback.textContent = "This form is ready, but account authentication has not been connected yet. Nothing was sent.";
            feedback.hidden = false;
        });
    });
})();