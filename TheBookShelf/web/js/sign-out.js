async function signOut() {
    const popup = Notification();
    const response = await fetch("SignOut");

    if (response.ok) {
        const json = await response.json();
        if (json.status) {
            popup.info({title: "Info", message: "User Logged Out Successfully"});
            setTimeout(() => {
                window.location.href = "sign-in.html";
            }, 2000);
        } else {
            window.location.reload();
        }
    } else {
        console.log("Logout Failed!");
    }
}
