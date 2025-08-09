/* global payhere */

let popup = Notification();
let popupDialog = Notification({position: 'center'});

document.addEventListener("DOMContentLoaded", () => {
    loadCities()
            .then(loadOrderSummary)
            .catch(err => {
                console.error(err);
                popup.error({title: "Error", message: "Initialization failed."});
            });

    const checkbox = document.getElementById("same-address-checkbox");
    checkbox.addEventListener("change", function () {
        if (this.checked) {
            loadUserAddress();
            // Disable inputs
            document.getElementById("first-name").disabled = true;
            document.getElementById("last-name").disabled = true;
            document.getElementById("street").disabled = true;
            document.getElementById("postal-code").disabled = true;
            document.getElementById("mobile").disabled = true;
            document.getElementById("city-select").disabled = true;
        } else {
            // Enable inputs
            document.getElementById("first-name").disabled = false;
            document.getElementById("last-name").disabled = false;
            document.getElementById("street").disabled = false;
            document.getElementById("postal-code").disabled = false;
            document.getElementById("mobile").disabled = false;
            document.getElementById("city-select").disabled = false;
            clearAddressFields();
        }
    });
});

async function loadCities() {
    try {
        const res = await fetch("GetCities");
        if (!res.ok)
            throw new Error("Failed to fetch cities");
        const cities = await res.json();

        const citySelect = document.getElementById("city-select");
        citySelect.innerHTML = `<option value="">Select</option>`;

        cities.forEach(city => {
            const opt = document.createElement("option");
            opt.value = city.id;
            opt.textContent = city.name;
            citySelect.appendChild(opt);
        });
    } catch (err) {
        console.error(err);
        popup.error({title: "Error", message: "Could not load cities."});
    }
}

async function loadOrderSummary() {
    try {
        const res = await fetch("LoadCheckout");
        if (!res.ok)
            throw new Error("Failed to load checkout data");
        const data = await res.json();

        if (!data.success) {
            popup.error({title: "Error", message: data.message || "Failed to load checkout data."});
            return;
        }

        const tbody = document.getElementById("summary-tbody");
        tbody.innerHTML = "";

        data.cartItems.forEach(item => {
            const tr = document.createElement("tr");
            tr.classList.add("order-product");
            tr.innerHTML = `
        <td>${item.title} x <span class="quantity">${item.quantity}</span></td>
        <td>Rs. ${item.price.toLocaleString()}</td>
      `;
            tbody.appendChild(tr);
        });

        tbody.innerHTML += `
<tr class="order-subtotal">
    <td>Subtotal</td>
    <td>Rs. ${data.subtotal.toLocaleString()}</td>
</tr>
<tr class="order-shipping">
    <td>Shipping Charges</td>
    <td>
        <span class="amount">Rs. ${data.shipping.toLocaleString()}</span>
    </td>
</tr>
<tr class="order-total">
    <td>Total</td>
    <td class="order-total-amount">Rs. ${data.total.toLocaleString()}</td>
</tr>

    `;
    } catch (err) {
        console.error(err);
        popup.error({title: "Error", message: "Could not load order summary."});
    }
}

async function loadUserAddress() {
    try {
        const res = await fetch("GetUserAddress");
        if (!res.ok)
            throw new Error("Failed to load user address");
        const data = await res.json();

        if (!data.success || !data.address) {
            popup.error({title: "Error", message: "Could not load your saved address."});
            return;
        }

        const addr = data.address;
        document.getElementById("first-name").value = addr.firstName || "";
        document.getElementById("last-name").value = addr.lastName || "";
        document.getElementById("street").value = addr.street || "";
        document.getElementById("postal-code").value = addr.postalCode || "";
        document.getElementById("mobile").value = addr.mobile || "";

        const citySelect = document.getElementById("city-select");
        [...citySelect.options].forEach(opt => {
            opt.selected = (opt.value == addr.cityId);
        });

    } catch (err) {
        console.error(err);
        popup.error({title: "Error", message: "Failed to load address."});
    }
}

function clearAddressFields() {
    document.getElementById("first-name").value = "";
    document.getElementById("last-name").value = "";
    document.getElementById("street").value = "";
    document.getElementById("postal-code").value = "";
    document.getElementById("mobile").value = "";
    document.getElementById("city-select").selectedIndex = 0;
}

async function checkout() {
    const citySelect = document.getElementById("city-select");
    const firstName = document.getElementById("first-name").value.trim();
    const lastName = document.getElementById("last-name").value.trim();
    const cityId = citySelect.value;
    const street = document.getElementById("street").value.trim();
    const postalCode = document.getElementById("postal-code").value.trim();
    const mobile = document.getElementById("mobile").value.trim();

    const orderData = {
        firstName,
        lastName,
        cityId,
        street,
        postalCode,
        mobile,
        email: document.getElementById("email")?.value.trim() || "",
        paymentMethod: document.querySelector('input[name="payment"]:checked')?.id || "payment-radio"
    };

    if (!firstName || !lastName || !cityId || !street || !postalCode || !mobile) {
        popup.error({title: "Validation Error", message: "Please fill in all required fields."});
        return;
    }
    try {
        const response = await fetch("PayHereCheckout", {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(orderData)
        });

        const payhereParams = await response.json();
        const tempOrderId = payhereParams.order_id;

        if (!payhereParams.success) {
            popup.error({title: "Error", message: payhereParams.message || "Failed to initiate payment."});
            return;
        }

        const payment = {
            sandbox: true,
            merchant_id: payhereParams.merchant_id,
            return_url: window.location.origin + "/order-confirmation.html",
            cancel_url: window.location.origin + "/checkout.html",
            notify_url: window.location.origin + "/PayHereNotify",
            order_id: payhereParams.order_id,
            items: payhereParams.items || "Order Payment",
            amount: payhereParams.amount,
            currency: "LKR",
            first_name: orderData.firstName,
            last_name: orderData.lastName,
            email: orderData.email,
            phone: orderData.mobile,
            address: orderData.street,
            city: citySelect.options[citySelect.selectedIndex]?.text || "",
            country: "Sri Lanka",
            hash: payhereParams.hash
        };

        payhere.onCompleted = async function (orderId) {
            popup.success({title: "Success", message: "Payment completed! Saving order..."});

            try {
                const confirmResponse = await fetch("Checkout", {
                    method: "POST",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify({
                        ...orderData,
                        tempOrderId: tempOrderId
                    })
                });
                const confirmResult = await confirmResponse.json();

                if (confirmResult.success) {
                    setTimeout(() => {
                        window.location.href = "index.html";
                    }, 2000);

                } else {
                    popup.error({title: "Error", message: confirmResult.message || "Failed to confirm order."});
                }
            } catch (err) {
                popup.error({title: "Error", message: "Error confirming order: " + err.message});
            }
        };

        payhere.onDismissed = function () {
            popup.error({title: "Cancelled", message: "Payment was cancelled."});
        };

        payhere.onError = function (error) {
            popup.error({title: "Error", message: "Payment error: " + error});
        };

        // Start PayHere payment popup
        payhere.startPayment(payment);

    } catch (err) {
        popup.error({title: "Error", message: "Could not start payment."});
        console.error("Checkout error:", err);
    }
}
