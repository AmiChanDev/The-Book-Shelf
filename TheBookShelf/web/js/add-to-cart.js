async function addToCart(bookIdNum) {
    const popup = Notification();
    const quantity = 1;

    const requestData = {
        bookId: bookIdNum,
        quantity: quantity
    };

    try {
        const response = await fetch('AddToCart', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        const result = await response.json();

        if (result.success) {
            popup.success({title: "Success", message: "Item Added To Cart"});
        } else {
            throw new Error(result.message || "Failed to add the book to the cart");
        }

    } catch (error) {
        console.error("Error:", error);
        popup.error({title: "Error", message: error.message});
    }
}
