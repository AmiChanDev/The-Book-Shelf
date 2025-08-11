async function signIn() {
    const popup = Notification();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();

    if (!email || !password) {
        popup.warning({title: "Warning", message: "Please fill in both fields"});
        return;
    }

    try {
        const response = await fetch("SignIn", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({email, password})
        });

        const result = await response.json();

        if (result.success) {
            popup.success({
                title: "Success",
                message: result.role === "ADMIN"
                        ? "Logged In as an admin successfully!"
                        : "Logged In Successfully!"
            });
            setTimeout(() => {
                window.location.href = result.redirect;
            }, 2000);
        } else {
            popup.error({title: "Error", message: result.message});
        }
    } catch (error) {
        console.error("SignIn request failed:", error);
        popup.error({title: "Error", message: "An error occurred while signing in"});
    }
}
