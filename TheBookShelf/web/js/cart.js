document.addEventListener("DOMContentLoaded", async () => {
    await loadCart();
    checkOut();
});

async function loadCart() {
    try {
        const response = await fetch("GetCart");  
        if (!response.ok)
            throw new Error("Failed to load cart");

        const cartData = await response.json();
        displayCart(cartData);
    } catch (err) {
        console.error("Error loading cart:", err);
    }
}

function displayCart(cartItems) {
    const cartContainer = document.getElementById("cartItems");
    const totalItems = document.getElementById("totalItems");
    const totalPrice = document.getElementById("totalPrice");

    cartContainer.innerHTML = "";
    let total = 0;
    let count = 0;

    cartItems.forEach(item => {
        const itemTotal = item.price * item.quantity;
        total += itemTotal;
        count += item.quantity;

        const itemElement = document.createElement("div");
        itemElement.className = "list-group-item d-flex justify-content-between align-items-center flex-wrap";

        itemElement.innerHTML = `
            <div class="d-flex align-items-center">
                <img src="${item.image}" alt="${item.title}" class="me-3" style="width: 60px; height: 90px; object-fit: cover;">
                <div>
                    <h5 class="mb-1">${item.title}</h5>
                    <p class="mb-1 text-muted">Price: $${item.price.toFixed(2)}</p>
                    <div class="input-group input-group-sm w-auto">
                        <span class="input-group-text">Qty</span>
                        <input type="number" class="form-control quantity-input" value="${item.quantity}" min="1" data-id="${item.bookId}">
                        <button class="btn btn-outline-danger btn-sm ms-2 remove-btn" data-id="${item.bookId}"><i class="fas fa-trash-alt"></i></button>
                    </div>
                </div>
            </div>
        `;
        cartContainer.appendChild(itemElement);
    });

    totalItems.textContent = count;
    totalPrice.textContent = total.toFixed(2);

    quantityListeners();
    removeListeners();
}

function quantityListeners() {
    document.querySelectorAll(".quantity-input").forEach(input => {
        input.addEventListener("change", async () => {
            const id = input.dataset.id;
            const qty = parseInt(input.value);
            if (qty <= 0)
                return;

            await fetch(`UpdateCart`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({bookId: id, quantity: qty})
            });

            await loadCart(); // refresh display
        });
    });
}

function removeListeners() {
    document.querySelectorAll(".remove-btn").forEach(btn => {
        btn.addEventListener("click", async () => {
            const id = btn.dataset.id;

            await fetch(`RemoveFromCart`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify({bookId: id})
            });

            await loadCart(); // refresh display
        });
    });
}

function checkOut() {
    document.getElementById("checkoutBtn").addEventListener("click", () => {
        window.location.href = "checkout.html"; 
    });
}
