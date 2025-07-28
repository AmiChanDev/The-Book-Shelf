async function signIn() {
    const popup = Notification();

    const email = document.getElementById("email").value.trim();
    const password = document.getElementById("password").value.trim();
    const errorMsg = document.getElementById("errorMsg");

    if (!email || !password) {
        popup.warning({title: "Warning", message: "Please fill in both fields"});
//        errorMsg.innerText = "Please fill in both fields.";
        return;
    }

    fetch("SignIn", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({email, password}),
    })
            .then((response) => response.json())
            .then((result) => {
                if (result.success) {
//                    document.getElementById("errorMsg").textContent = "";
//                    document.getElementById("successMsg").textContent = "Logged In Successfully";
                    popup.success({title: "Success", message: "Logged In Successfully!"});
                    setTimeout(() => {
                        window.location.href = result.redirect;

                    }, 2000);
                } else {
                    popup.error({title: "Error", message: result.message});
//                    errorMsg.innerText = result.message;
                }
            })
            .catch(() => {
                popup.error({title: "Error", message: "An error occurred while signing in"});
            });
}
