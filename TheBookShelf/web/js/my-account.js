let globalAddresses = {};
let popup = Notification();
let popupDialog = Notification({
    position: 'center'
});

document.addEventListener("DOMContentLoaded", async () => {
    try {
        await loadCities();
        await loadGenres();
        await loadAccount();
        await loadUserBooks();
        await loadPurchaseHistory();
        setupUpdateSelectListener();
    } catch (err) {
        console.error("Error loading page data:", err);
    }
});

function setupUpdateSelectListener() {
    const selectUpdate = document.getElementById("selectAddressUpdate");
    if (!selectUpdate)
        return;

    selectUpdate.addEventListener("change", function () {
        const selectedId = this.value;
        if (!selectedId || !globalAddresses[selectedId])
            return;

        const address = globalAddresses[selectedId];

        document.getElementById("updateStreet").value = address.street;
        document.getElementById("updateZipCode").value = address.zip_code;

        const citySelect = document.getElementById("updateCitySelect");
        for (let i = 0; i < citySelect.options.length; i++) {
            if (citySelect.options[i].textContent === address.city) {
                citySelect.selectedIndex = i;
                break;
            }
        }
    });
}

// Load Functions
async function loadAccount() {
    return fetch("LoadAccount")
            .then(res => res.json())
            .then(data => {
                if (!data.status) {
                    popup.error({title: "Error", message: "Please Login"});
                    setTimeout(() => {
                        window.location.href = "sign-in.html";
                    }, 1500);
                    return;
                }

                globalAddresses = data.addresses;

                document.getElementById("username").textContent = data.user.name;
                document.getElementById("created_at").textContent = data.user.created_at;
                document.getElementById("name").textContent = data.user.name;
                document.getElementById("email").textContent = data.user.email;
                document.getElementById("mobile").textContent = data.user.mobile;
                document.getElementById("role").textContent = data.user.role;

                document.getElementById("bookAuthor").value = data.user.name;
                const addressDeleteSelect = document.getElementById("selectAddressDelete");
                const addressUpdateSelect = document.getElementById("selectAddressUpdate");

                addressDeleteSelect.innerHTML = "<option value='' disabled selected>Select Address to Delete</option>";
                addressUpdateSelect.innerHTML = "<option value='' disabled selected>Select Address to Update</option>";

                const list = document.getElementById("addresses");
                list.innerHTML = Object.keys(globalAddresses).length === 0
                        ? "<li>No addresses found</li>"
                        : "";

                for (let addressId in globalAddresses) {
                    const address = globalAddresses[addressId];

                    const deleteOption = document.createElement("option");
                    deleteOption.value = addressId;
                    deleteOption.textContent = `${address.street}, ${address.city}, ${address.zip_code}`;
                    addressDeleteSelect.appendChild(deleteOption);

                    const updateOption = document.createElement("option");
                    updateOption.value = addressId;
                    updateOption.textContent = `${address.street}, ${address.city}, ${address.zip_code}`;
                    addressUpdateSelect.appendChild(updateOption);

                    const li = document.createElement("li");
                    li.textContent = `${address.street}, ${address.city}, ${address.zip_code}`;
                    list.appendChild(li);
                }
            });
}

async function loadCities() {
    return fetch("GetCities")
            .then(res => res.json())
            .then(data => {
                const addSelect = document.getElementById("addCitySelect");
                const updateSelect = document.getElementById("updateCitySelect");

                addSelect.innerHTML = "<option value='' disabled selected>Select City</option>";
                updateSelect.innerHTML = "<option value='' disabled selected>Select City</option>";

                data.forEach(city => {
                    const opt1 = document.createElement("option");
                    opt1.value = city.id;
                    opt1.textContent = city.name;
                    addSelect.appendChild(opt1);

                    const opt2 = document.createElement("option");
                    opt2.value = city.id;
                    opt2.textContent = city.name;
                    updateSelect.appendChild(opt2);
                });
            });
}

async function loadGenres() {
    fetch("GetGenres")
            .then(res => res.json())
            .then(data => {
                const genreSelect = document.getElementById("bookGenre");
                genreSelect.innerHTML = '<option value="">Select Genre</option>';
                data.forEach(genre => {
                    const option = document.createElement("option");
                    option.value = genre.id;
                    option.textContent = genre.name;
                    genreSelect.appendChild(option);
                });
            })
            .catch(err => console.error("Failed to load genres:", err));
}

async function loadUserBooks() {
    try {
        const response = await fetch("GetUserBooks");
        if (!response.ok) {
            throw new Error("Unauthorized or failed to fetch books.");
        }

        const books = await response.json();
        const list = document.getElementById("uploadedBooksList");
        list.innerHTML = "";

        if (books.length === 0) {
            list.innerHTML = "<div class='col-12'><div class='alert alert-info' role='alert'>No books uploaded yet.</div></div>";
            return;
        }

        books.forEach(book => {
            const cardCol = document.createElement("div");
//            cardCol.classList.add("col-md-4", "mb-4");

            const card = document.createElement("div");
            card.classList.add("card", "h-100");

            const cardImage = document.createElement("img");
            cardImage.classList.add("card-img-top", "book-image");
            cardImage.src = book.imagePath;
            cardImage.alt = book.title;

            const cardBody = document.createElement("div");
            cardBody.classList.add("card-body");

            const cardTitle = document.createElement("h5");
            cardTitle.classList.add("card-title", "text-center", "fw-bold");
            cardTitle.textContent = book.title;

            const cardAuthor = document.createElement("p");
            cardAuthor.classList.add("fw-bold");
            cardAuthor.textContent = `Author: ${book.authorName}`;

            const cardPrice = document.createElement("p");
            cardPrice.classList.add("fw-bold");
            cardPrice.textContent = `Price: ${book.price.toFixed(2)} LKR`;

            const cardGenres = document.createElement("p");
            cardGenres.innerHTML = `<strong>Genres:</strong> ${book.genres.map(g => g.name).join(", ")}`;

            const cardDescription = document.createElement("p");
            cardDescription.textContent = book.description;

            cardBody.append(cardTitle, cardAuthor, cardPrice, cardGenres, cardDescription);
            card.append(cardImage, cardBody);
            cardCol.appendChild(card);
            list.appendChild(cardCol);
        });
    } catch (error) {
        const list = document.getElementById("uploadedBooksList");
        list.innerHTML = `<div class="col-12"><div class="alert alert-danger" role="alert">${error.message}</div></div>`;
    }
}




// Functions
async function changePassword() {
    const oldPassword = document.getElementById("oldPassword").value.trim();
    const newPassword = document.getElementById("newPassword").value.trim();

    if (!oldPassword || !newPassword) {
        document.getElementById("messageText").textContent = "Both Fields are required";
        return;
    }

    popupDialog.dialog({
        title: "Change Password",
        message: "Are you sure you want to change your password?",
        callback: function (response) {
            if (response === "ok") {
                fetch("ChangePassword", {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        oldPassword: oldPassword,
                        newPassword: newPassword
                    })
                })
                        .then(res => res.json())
                        .then(data => {
                            if (data.status) {
                                document.getElementById("oldPassword").value = "";
                                document.getElementById("newPassword").value = "";
                                popup.success({title: "Success", message: data.message});
                                setTimeout(() => {
                                    location.reload(true);
                                }, 2000);
                            } else {
                                popup.error({title: "Error", message: data.message});
                            }
                        })
                        .catch(err => {
                            console.error("Password change failed:", err);
                            console.log("Something went wrong!");
                        });
            } else {
                console.log("User cancelled Password Change");
                document.getElementById("oldPassword").value = "";
                document.getElementById("newPassword").value = "";
            }
        },
        validFunc: function () {
            return true;
        }
    });
}

async function addAddress() {
    const street = document.getElementById("addStreet").value.trim();
    const zip = document.getElementById("addZipCode").value.trim();
    const cityId = document.getElementById("addCitySelect").value;

    if (!street || !zip || !cityId) {
        document.getElementById("addAddressMessage").textContent = "Please fill in all fields.";
        return;
    }

    popupDialog.dialog({
        title: "Add Address",
        message: "Are you sure you want to add this address?",
        callback: function (response) {
            if (response === 'ok') {
                fetch("AddAddress", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        street: street,
                        zipCode: zip,
                        cityId: cityId
                    })
                })
                        .then(res => res.json())
                        .then(data => {
                            document.getElementById("addAddressMessage").textContent = "";
                            if (data.success) {
                                popup.success({title: "Success", message: data.message});
                                document.getElementById("addAddressForm").reset();
                                setTimeout(async () => {
                                    await loadAccount();
                                    clearForms();
                                }, 1000);
                            } else {
                                popup.error({title: "Error", message: data.message});
                            }
                        })
                        .catch(err => {
                            popup.error({title: "Error", message: "Something went wrong"});
                            console.error(err);
                        });
            } else if (response === 'cancel') {
                console.log("User canceled deletion");
            }
        },
        validFunc: function () {
            return true;
        }
    });
}

async function deleteAddress() {
    const addressId = document.getElementById("selectAddressDelete").value;

    if (!addressId) {
        document.getElementById("deleteAddressMessage").textContent = "Please select an address to delete.";
        document.getElementById("deleteAddressMessage").className = "text-danger";
        return;
    }

    popupDialog.dialog({
        title: "Delete Confirmation",
        message: "Are you sure you want to delete this address?",
        callback: function (response) {
            if (response === 'ok') {
                fetch("DeleteAddress", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({addressId: addressId})
                })
                        .then(res => res.json())
                        .then(data => {
                            document.getElementById("deleteAddressMessage").textContent = "";
                            if (data.success) {
                                popup.success({title: "Success", message: data.message});
                                setTimeout(async () => {
                                    await loadAccount();
                                    clearForms();
                                }, 1000);
                            } else {
                                popup.error({title: "Error", message: data.message});
                            }
                        })
                        .catch(err => {
                            popup.error({title: "Error", message: "Something went wrong"});
                            console.error(err);
                        });
            } else if (response === 'cancel') {
                console.log("User canceled deletion");
            }
        },
        validFunc: function () {
            return true;
        }
    });
}

async function updateAddress() {
    const addressId = document.getElementById("selectAddressUpdate").value;
    const newStreet = document.getElementById("updateStreet").value;
    const newZip = document.getElementById("updateZipCode").value;
    const newCityId = document.getElementById("updateCitySelect").value;

    if (!addressId || !newStreet || !newZip || !newCityId) {
        document.getElementById("updateAddressMessage").className = "text-danger";
        document.getElementById("updateAddressMessage").textContent = "All fields are required.";
        return;
    }

    popupDialog.dialog({
        title: "Update Confirmation",
        message: "Are you sure you want to update this address?",
        callback: function (response) {
            if (response === 'ok') {
                fetch("UpdateAddress", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({
                        id: addressId,
                        street: newStreet,
                        zip_code: newZip,
                        city_id: newCityId
                    })
                })
                        .then(res => res.json())
                        .then(data => {
                            document.getElementById("updateAddressMessage").textContent = "";
                            if (data.status) {
                                popup.success({title: "Success", message: data.message});
                                setTimeout(async () => {
                                    await loadAccount();
                                    clearForms();
                                }, 1000);
                            } else {
                                popup.error({title: "Error", message: data.message});
                            }
                        })
                        .catch(err => {
                            popup.error({title: "Error", message: "Something went wrong"});
                            console.error("Update address failed:", err);
                        });
            } else if (response === 'cancel') {
                console.log("User canceled updation");
            }
        },
        validFunc: function () {
            return true;
        }
    });
}

async function uploadBook() {
    popupDialog.dialog({
        title: "Add Confirmation",
        message: "Are you sure you want to add this book to listing?",
        callback: async function (response) {
            if (response === 'ok') {
                const title = document.getElementById("bookTitle").value.trim();
                const author = document.getElementById("bookAuthor").value.trim();
                const isbn = document.getElementById("bookISBN").value.trim();
                const price = document.getElementById("bookPrice").value.trim();
                const stock = document.getElementById("bookStock").value.trim();
                const description = document.getElementById("bookDescription").value.trim();
                const genreId = Array.from(document.getElementById("bookGenre").selectedOptions).map(option => option.value);
                const imageFile = document.getElementById("bookImage").files[0];

                const message = document.getElementById("uploadMessage");
                message.textContent = "";

                if (!title || !author || !isbn || !price || !stock || !description || genreId.length === 0) {
                    message.textContent = "Please fill in all fields.";
                    return;
                }

                if (isNaN(price) || parseFloat(price) < 0) {
                    message.textContent = "Price must be a valid non-negative number.";
                    return;
                }

                if (isNaN(stock) || parseInt(stock) < 0) {
                    message.textContent = "Stock must be a valid non-negative integer.";
                    return;
                }

                if (!imageFile) {
                    message.textContent = "Please select an image for the book.";
                    return;
                }

                const allowedTypes = ["image/jpeg", "image/png", "image/webp"];
                if (!allowedTypes.includes(imageFile.type)) {
                    message.textContent = "Only JPG, PNG, or WebP images are allowed.";
                    return;
                }

                const formData = new FormData();
                formData.append("title", title);
                formData.append("authorName", author);
                formData.append("isbn", isbn);
                formData.append("price", price);
                formData.append("stock", stock);
                formData.append("description", description);
                genreId.forEach(gid => formData.append("genres", gid));
                formData.append("image", imageFile);

                try {
                    const response = await fetch("UploadBook", {
                        method: "POST",
                        body: formData
                    });

                    const result = await response.text();
                    if (result.toLowerCase().includes("success")) {
                        popup.success({title: "Success", message: "Book added to listing successfully!"});

                        document.getElementById("uploadBookForm").reset();
                        document.getElementById("bookAuthor").value = document.getElementById("name").textContent;

                        await loadUserBooks();
                    } else {
                        popup.error({title: "Error", message: result});
                    }
                } catch (err) {
                    message.textContent = "Upload failed due to network error.";
                    message.classList.remove("text-success");
                    message.classList.add("text-danger");
                    console.error(err);
                }
            } else if (response === 'cancel') {
                console.log("User canceled upload");
            }
        },
        validFunc: function () {
            return true;
        }
    });
}

async function loadPurchaseHistory() {
    try {
        const response = await fetch("GetPurchaseHistory");
        if (!response.ok)
            throw new Error("Failed to fetch purchase history");
        const history = await response.json();
        const tbody = document.getElementById("orderHistoryBody");
        if (!tbody)
            return;
        tbody.innerHTML = "";
        if (history.length === 0) {
            tbody.innerHTML = `<tr><td colspan="3" class="text-center">No purchase history found.</td></tr>`;
            return;
        }
        history.forEach(item => {
            const tr = document.createElement("tr");

            const tdId = document.createElement("td");
            const a = document.createElement("a");
            a.href = "#";
            a.textContent = item.orderId;
            a.addEventListener("click", async e => {
                e.preventDefault();
                await showOrderDetailsPopup(item.orderId);
            });
            tdId.appendChild(a);

            const tdDate = document.createElement("td");
            tdDate.textContent = item.orderDate;

            const tdTotal = document.createElement("td");
            tdTotal.textContent = item.totalAmount.toFixed(2);

            tr.append(tdId, tdDate, tdTotal);
            tbody.appendChild(tr);
        });
    } catch (error) {
        const tbody = document.getElementById("orderHistoryBody");
        if (tbody) {
            tbody.innerHTML = `<tr><td colspan="3" class="text-danger text-center">Failed to load purchase history.</td></tr>`;
        }
        console.error(error);
    }
}
async function showOrderDetailsPopup(orderId) {
    try {
        const response = await fetch(`GetOrderDetails?orderId=${orderId}`);
        if (!response.ok)
            throw new Error("Failed to fetch order details");
        const details = await response.json();

        const shippingFee = 1000;
        let total = 0;

        let html = `<h4>Order Details (ID: ${orderId})</h4>
<h6>Shipping fee: ${shippingFee} LKR</h6>`;
        html += `<table class="table table-striped table-hover">
                    <thead>
                        <tr>
                            <th>Book ID</th>
                            <th>Quantity</th>
                            <th>Price (LKR)</th>
                        </tr>
                    </thead>
                    <tbody>`;

        details.forEach(item => {
            html += `<tr>
                        <td>${item.bookId}</td>
                        <td>${item.quantity}</td>
                        <td>${item.price.toFixed(2)}</td>
                    </tr>`;
            total += item.price * item.quantity;
        });

        html += `</tbody></table>`;

        html += `<h6>Total Amount (including shipping): ${(total + shippingFee).toFixed(2)} LKR</h6>`;

        const width = 600;
        const height = 400;
        const left = window.screenX + (window.outerWidth - width) / 2;
        const top = window.screenY + (window.outerHeight - height) / 2;

        const popup = window.open(
                "",
                "OrderDetails",
                `width=${width},height=${height},left=${left},top=${top},scrollbars=yes`
                );

        popup.document.write(`<html><head><title>Order Details</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet"></head>
            <body class="p-3">${html}</body></html>`);
        popup.document.close();
    } catch (error) {
        alert("Could not load order details.");
        console.error(error);
    }
}

// Clear Functions
function clearForms() {
    clearUpdateForm();
    clearDeleteForm();
}

function clearDeleteForm() {
    document.getElementById("selectAddressDelete").selectedIndex = 0;
    document.getElementById("deleteAddressMessage").textContent = "";
}

function clearUpdateForm() {
    document.getElementById("selectAddressUpdate").selectedIndex = 0;
    document.getElementById("updateStreet").value = "";
    document.getElementById("updateZipCode").value = "";
    document.getElementById("updateCitySelect").selectedIndex = 0;
    document.getElementById("updateAddressMessage").textContent = "";
}
