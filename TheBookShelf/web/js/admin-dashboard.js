/* global usersTableBody */

let popup = Notification();
let popupDialog = Notification({
    position: 'center'
});
document.addEventListener('DOMContentLoaded', () => {
    loadDashboard();
    loadGenresManagement();
    loadPurchaseHistory();
    loadGenres();
    loadBooksListing();
});

//Loading Functions
async function loadDashboard() {
    try {
        const response = await fetch('/TheBookShelf/LoadAdminDashboard');
        if (!response.ok)
            throw new Error('Failed to load dashboard');

        const data = await response.json();

        document.getElementById("total-orders").textContent = data.totalOrders || 0;
        document.getElementById("books-in-stock").textContent = data.booksInStock || 0;
        document.getElementById("total-customers").textContent = data.totalUsers || 0;
        document.getElementById("total-revenue").textContent = `${data.totalRevenue?.toFixed(2) || '0.00'} LKR`;

        const ordersTableBody = document.getElementById("orders-body");
        ordersTableBody.innerHTML = "";
        if (data.recentOrders && data.recentOrders.length) {
            data.recentOrders.forEach(order => {
                const row = document.createElement("tr");
                const orderDate = order.orderDate ? new Date(order.orderDate).toLocaleString() : "N/A";
                row.innerHTML = `
          <td>${order.id}</td>
          <td>${order.customerName}</td>
          <td>LKR ${order.totalAmount.toFixed(2)}</td>
          <td>${orderDate}</td>
        `;
                ordersTableBody.appendChild(row);
            });
        } else {
            ordersTableBody.innerHTML = `<tr><td colspan="4" class="text-center">No recent orders</td></tr>`;
        }

        const usersTableBody = document.getElementById("users-body");
        usersTableBody.innerHTML = "";
        if (data.allUsers && data.allUsers.length) {
            data.allUsers.forEach(user => {
                const row = document.createElement("tr");
                row.innerHTML = `
          <td>${user.name}</td>
          <td>${user.email}</td>
          <td>${user.mobile || 'N/A'}</td>
          <td>${user.verification}</td>
          <td>${user.role}</td>
          <td>${user.createdAt}</td>
        `;
                usersTableBody.appendChild(row);
            });
        } else {
            usersTableBody.innerHTML = `<tr><td colspan="6" class="text-center">No users found</td></tr>`;
        }

    } catch (error) {
        console.error("Error loading dashboard:", error);
    }
}

async function loadGenresManagement() {
    try {
        const response = await fetch('/TheBookShelf/GetGenres');
        if (!response.ok)
            throw new Error('Failed to fetch genres');

        const genres = await response.json();

        const genreList = document.getElementById('genreList');
        const genreMessage = document.getElementById('genreMessage');

        genreMessage.textContent = '';

        if (genres.length === 0) {
            genreList.innerHTML = '<li class="list-group-item">No genres available.</li>';
            return;
        }

        genreList.innerHTML = genres
                .map(g => `<li class="list-group-item d-flex justify-content-between align-items-center">
                          ${g.name}
                      </li>`)
                .join('');
    } catch (error) {
        console.error('Error loading genres for management:', error);
        const genreMessage = document.getElementById('genreMessage');
        genreMessage.textContent = 'Failed to load genres.';
        genreMessage.className = 'text-danger';
    }
}

async function loadPurchaseHistory() {
    try {
        const response = await fetch('/TheBookShelf/LoadAdminOrders');
        if (!response.ok) {
            if (response.status === 401) {
                document.getElementById('orders').innerHTML = '<p class="text-danger">Please log in to view purchase history.</p>';
            } else {
                throw new Error('Failed to fetch purchase history');
            }
            return;
        }

        const orders = await response.json();
        const container = document.getElementById('orders');
        container.innerHTML = '<h2>Orders</h2>';

        if (orders.length === 0) {
            container.innerHTML += '<p>You have no purchase history.</p>';
            return;
        }

        const table = document.createElement('table');
        table.className = 'table table-striped';

        // Create table header
        const thead = document.createElement('thead');
        thead.innerHTML = `
          <tr>
            <th>Order ID</th>
            <th>User ID</th>
            <th>User Name</th>        
            <th>Order Date</th>
            <th>Total Amount (LKR)</th>
          </tr>
        `;

        const tbody = document.createElement('tbody');

        orders.forEach(order => {
            const tr = document.createElement('tr');

            const orderIdLink = document.createElement('a');
            orderIdLink.href = '#';
            orderIdLink.textContent = order.orderId;
            orderIdLink.addEventListener('click', async (e) => {
                e.preventDefault();
                await showOrderDetailsPopup(order.orderId);
            });

            const tdOrderId = document.createElement('td');
            tdOrderId.appendChild(orderIdLink);
            tr.appendChild(tdOrderId);

            const tdUserId = document.createElement('td');
            tdUserId.textContent = order.orderUid;
            tr.appendChild(tdUserId);

            const tdUserName = document.createElement('td');
            tdUserName.textContent = order.orderUName;
            tr.appendChild(tdUserName);

            const tdOrderDate = document.createElement('td');
            tdOrderDate.textContent = order.orderDate;
            tr.appendChild(tdOrderDate);

            const tdTotalAmount = document.createElement('td');
            tdTotalAmount.textContent = `LKR ${order.totalAmount.toLocaleString()}`;
            tr.appendChild(tdTotalAmount);

            tbody.appendChild(tr);
        });

        table.appendChild(thead);
        table.appendChild(tbody);

        container.appendChild(table);
    } catch (error) {
        console.error('Error loading purchase history:', error);
        document.getElementById('orders').innerHTML = '<p class="text-danger">Failed to load purchase history.</p>';
    }
}

async function showOrderDetailsPopup(orderId) {
    try {
        const response = await fetch(`/TheBookShelf/LoadAdminOrderDetails?orderId=${orderId}`);
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
                            <th>Book Name</th>
                            <th>Quantity</th>
                            <th>Price (LKR)</th>
                        </tr>
                    </thead>
                    <tbody>`;

        details.forEach(item => {
            html += `<tr>
                        <td>${item.bookId}</td>
                        <td>${item.bookName}</td>
                        <td>${item.quantity}</td>
                        <td>${item.price.toFixed(2)}</td>
                    </tr>`;
            total += item.price * item.quantity;
        });

        html += `</tbody></table>`;
        html += `<h6>Total Amount (including shipping): ${(total + shippingFee).toFixed(2)} LKR</h6>`;

        // Add a print button
        html += `<button id="printBtn" class="btn btn-primary mt-3" onclick="window.print()">Print Order</button>`;

        const width = 600;
        const height = 400;
        const left = window.screenX + (window.outerWidth - width) / 2;
        const top = window.screenY + (window.outerHeight - height) / 2;

        const popup = window.open(
                "",
                "OrderDetails",
                `width=${width},height=${height},left=${left},top=${top},scrollbars=yes`
                );

        popup.document.write(`
<html>
<head>
<title>Order Details</title>
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
<style>
/* Hide print button when printing */
@media print {
    #printBtn {
        display: none;
    }
}
</style>
</head>
<body class="p-3">${html}</body>
</html>
        `);
        popup.document.close();
    } catch (error) {
        alert("Could not load order details.");
        console.error(error);
    }
}

async function loadGenres() {
    fetch("/TheBookShelf/GetGenres")
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

async function loadBooksListing() {
    try {
        const response = await fetch("LoadAdminBooks");
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

//Upload Functions
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

                        setTimeout(async () => {
                            await loadUserBooks();
                            location.reload(true);
                        }, 1500);
                    } else {
                        popup.error({title: "Error", message: result});
                    }
                } catch (err) {
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

async function uploadGenre() {
    popupDialog.dialog({
        title: "Add Confirmation",
        message: "Are you sure you want to add this genre?",
        callback: async function (confirmed) {
            if (!confirmed)
                return;

            const genre = document.getElementById("newGenreInput").value.trim();

            if (!genre) {
                popup.error({title: "Error", message: "Please enter a genre to be added"});
                return;
            }

            try {
                const response = await fetch("/TheBookShelf/AddGenre", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({genreName: genre})
                });

                if (!response.ok) {
                    throw new Error("Failed to add genre");
                }

                const result = await response.json();

                popup.success({title: "Success", message: "Genre added successfully!"});

                document.getElementById("newGenreInput").value = "";

                loadGenresManagement();

            } catch (error) {
                console.error(error);
                popup.error({title: "Error", message: "Error adding genre"});
            }
        },
        validFunc: function () {
            return true;
        }
    });
}



