async function verifyAccount() {
    const popup =Notification();
    const verificationCode = document.getElementById("verificationCode").value.trim();
//    const errorMsg = document.getElementById("errorMsg");
//    const successMsg = document.getElementById("successMsg");

    if (!verificationCode) {
         popup.warning({title: "Warning", message: "Please enter the verification code."});
//        errorMsg.innerText = "Please enter the verification code.";
//        successMsg.innerText = "";
        return;
    }

    fetch("VerifyAccount", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({verificationcode: verificationCode}),
    })
            .then((response) => response.json())
            .then((result) => {
                if (result.status) {
                    popup.success({title: "Success", message: "Account Verified Successfully"});
//                    successMsg.innerText = "Account Verified Successfully";
//                    errorMsg.innerText = "";

                    setTimeout(() => {
                        window.location.href = "index.html";
                    }, 2000);
                } else {
                     popup.error({title: "Error", message: result.message});
//                    errorMsg.innerText = result.message;
//                    successMsg.innerText = "";
                }
            })
            .catch(() => {
                        popup.error({title: "Error", message: "An error occurred while verifying the account"});
//                errorMsg.innerText = "An error occurred while verifying the account.";
//                successMsg.innerText = "";
            });
}


