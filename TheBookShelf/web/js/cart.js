document.addEventListener("DOMContentLoaded", function () {
    loadCart();
    document.getElementById('checkoutBtn').addEventListener('click', handleCheckout);
});

let popup = Notification();
let popupDialog = Notification({
    position: 'center'
});

function loadCart() {
    fetch('/TheBookShelf/LoadCart')
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    if (data.cartItems && data.cartItems.length > 0) {
                        renderCartItems(data.cartItems);
                    } else {
                        const cartItemsContainer = document.getElementById('cartItems');
                        cartItemsContainer.innerHTML = '';
                        updateSummary(0, 0);
                        popup.info({title: "Info", message: "Your cart is empty."});
                    }
                } else {
                    popup.warning({title: "Warning", message: data.message || "Failed to load cart."});
                }
            })
            .catch(err => {
                console.error("Error loading cart:", err);
                popup.error({title: "Error", message: "An error occurred while loading cart."});
            });
}

function renderCartItems(cartItems) {
    console.log(cartItems);
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
                        Quantity:
                        <input type="number"
                               class="form-control form-control-sm quantity-input"
                               data-book-id="${item.bookId}"
                               value="${quantity}"
                               min="1"
                               style="width: 70px; display: inline-block;" 
                               onkeydown="return false;">
                    </div>
                </div>
            </div>
            <div class="text-end">
                <span class="fw-semibold item-total">${total.toFixed(2)} LKR</span>
                <button class="btn btn-danger btn-sm ms-2" data-book-id="${item.bookId}" onclick="removeFromCart(${item.bookId})">
                    <i class="fas fa-trash-alt"></i></button>
            </div>
        `;

        cartItemsContainer.appendChild(cartItem);
    });

    updateSummary(totalItems, totalPrice);

    document.querySelectorAll('.quantity-input').forEach(input => {
        input.addEventListener('change', function () {
            const newQuantity = parseInt(input.value);
            const bookId = parseInt(input.dataset.bookId);

            if (isNaN(newQuantity) || newQuantity < 1) {
                input.value = 1;
                return;
            }

            updateQuantity(bookId, newQuantity);
        });
    });
}

function updateQuantity(bookId, quantity) {
    fetch('/TheBookShelf/UpdateCartQuantity', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            bookId: bookId,
            quantity: quantity
        })
    })
            .then(response => response.json())
            .then(data => {
                if (data.success) {
                    loadCart();
                    popup.info({title: "Info", message: "Quantity updated."});
                } else {
                    popup.warning({title: "Warning", message: data.message || "Failed to update cart."});
                }
            })
            .catch(err => {
                console.error("Error updating cart quantity:", err);
                popup.error({title: "Error", message: "An error occurred while updating quantity."});
            });
}

function updateSummary(totalItems, totalPrice) {
    document.getElementById('totalItems').textContent = totalItems;
    document.getElementById('totalPrice').textContent = totalPrice.toFixed(2);
}

function handleCheckout() {
    const totalItems = parseInt(document.getElementById('totalItems').textContent);
    if (totalItems === 0) {
        popup.warning({title: "Warning", message: "Your cart is empty!"});
    } else {
        window.location.href = "checkout.html";
    }
}

function removeFromCart(bookId) {

    popupDialog.dialog({
        title: "Remove Confirmation",
        message: "Are you sure you want to remove this item?",
        callback: function (response) {
            if (response === 'ok') {
                console.log("User confirmed removing item from cart");
                fetch('/TheBookShelf/RemoveCartItem', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({bookId: bookId})
                })
                        .then(response => response.json())
                        .then(data => {
                            if (data.success) {
                                popup.success({title: "Success", message: data.message});
                                loadCart();
                            } else {
                                popup.error({title: "Error", message: data.message});
                            }
                        })
                        .catch(err => {
                            console.error("Error removing item from cart:", err);
                            popup.error({title: "Error", message: "An error occurred while removing the item."});
                        });
            } else if (response === 'cancel') {
                console.log("User canceled  removing item from cart");
            }
        },
        validFunc: function () {
            return true;
        }
    });

}
