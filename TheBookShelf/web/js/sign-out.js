async function signOut() {
    const popup = Notification();

    try {
        const sessionCheck = await fetch("CheckSession");
        const sessionJson = await sessionCheck.json();

        if (!sessionJson.status) {
            popup.warning({title: "Warning", message: "You are not logged in."});
            return;
        }

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
            popup.error({title: "Error", message: "Logout request failed."});
        }

    } catch (err) {
        console.error("Error during sign out:", err);
        popup.error({title: "Error", message: "Something went wrong during logout."});
    }
}
