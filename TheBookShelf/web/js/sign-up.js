async function handleSignUp() {
    const popup = Notification();

    const firstName = document.getElementById("firstName").value.trim();
    const lastName = document.getElementById("lastName").value.trim();
    const email = document.getElementById("email").value.trim();
    const mobile = document.getElementById("mobile").value.trim();
    const password = document.getElementById("password").value.trim();

//    const messageBox = document.getElementById("signup-message");
//    messageBox.textContent = "";

    if (!firstName || !lastName || !email || !password || !mobile) {
        popup.warning({title: "Warning", message: "All fields are required"});
        return;
    }

    fetch("SignUp", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            firstName,
            lastName,
            email,
            mobile,
            password
        })
    })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
//                    document.getElementById("signup-message").textContent = "";
//                    document.getElementById("successMsg").textContent = "Signed Up Successfully";
                    popup.success({title: "Success", message: "Signed Up Successfully"});
                    setTimeout(() => {
                        window.location.href = data.redirect;
                    }, 2000);
                } else {
                    popup.error({title: "Error", message: data.message});
//                    messageBox.textContent = data.message;
                }
            })
            .catch(() => {
                popup.error({title: "Error", message: "Something went wrong"});
//                messageBox.textContent = "Something went wrong.";
            });
}
