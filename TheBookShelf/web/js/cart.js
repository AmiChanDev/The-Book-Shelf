document.addEventListener("DOMContentLoaded", function () {
    loadCart();
    document.getElementById('checkoutBtn').addEventListener('click', handleCheckout);
});

function loadCart() {
    fetch('/TheBookShelf/LoadCart')
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    if (data.cartItems && data.cartItems.length > 0) {
                        renderCartItems(data.cartItems);

                    } else {
                        showNotification("Your cart is empty.", "info");
                    }
                } else {
                    showNotification(data.message || "Failed to load cart.", "warning");
                }
            })
            .catch(err => {
                console.error("Error loading cart:", err);
                showNotification("An error occurred while loading cart.", "error");
            });
}

function renderCartItems(cartItems) {
    const cartItemsContainer = document.getElementById('cartItems');
    cartItemsContainer.innerHTML = '';

    let totalItems = 0;
    let totalPrice = 0;

    cartItems.forEach(item => {
        if (!item.bookTitle || isNaN(item.price) || isNaN(item.quantity))
            return;

        const quantity = item.quantity;
        const price = item.price;
        const total = price * quantity;
        const imageUrl = item.imagePath ? item.imagePath : 'images/placeholder.png';

        totalItems += quantity;
        totalPrice += total;

        const cartItem = document.createElement('li');
        cartItem.className = 'list-group-item d-flex justify-content-between align-items-center';

        cartItem.innerHTML = `
            <div class="d-flex align-items-center">
                <img src="${imageUrl}" alt="${item.bookTitle}" class="me-3" style="width: 60px; height: 90px; object-fit: cover;">
                <div>
                    <div class="fw-bold">${item.bookTitle}</div>
                   <div>
    Quantity: <input type="number" class="form-control form-control-sm quantity-input" data-title="${item.bookTitle}" value="${quantity}" min="1" style="width: 70px; display: inline-block;">
                </div>
                </div>
            </div>
            <div class="text-end">
                <span class="fw-semibold">${total.toFixed(2)} LKR</span>
            </div>
        `;

        cartItemsContainer.appendChild(cartItem);
    });

    document.getElementById('totalItems').textContent = totalItems;
    document.getElementById('totalPrice').textContent = totalPrice.toFixed(2);
}

function handleCheckout() {
    const totalItems = parseInt(document.getElementById('totalItems').textContent);
    if (totalItems === 0) {
        showNotification("Your cart is empty!", "warning");
    } else {
        window.location.href = "checkout.html";
    }
}

