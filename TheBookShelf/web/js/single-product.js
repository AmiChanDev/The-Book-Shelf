document.addEventListener("DOMContentLoaded", () => {
    const bookId = getBookIdFromURL();
    if (!bookId) {
        showError("No book ID provided.");
        return;
    }
    fetchBookDetails(bookId);
});

document.querySelectorAll('a[href^="products.html"]').forEach(link => {
    link.addEventListener('click', e => {
        if (link.href === window.location.href) {
            e.preventDefault();
            window.location.reload();
        }
    });
});

function getBookIdFromURL() {
    const params = new URLSearchParams(window.location.search);
    return params.get("id");
}

async function fetchBookDetails(id) {
    try {
        const res = await fetch(`GetSingleBook?id=${id}`);
        if (!res.ok)
            throw new Error("Failed to fetch book details.");

        const book = await res.json();
        displayBookDetails(book);
    } catch (err) {
        showError(err.message);
    }
}

function displayBookDetails(book) {
    document.getElementById("productImage").src = book.imagePath || "images/default.jpg";
    document.getElementById("bookId").textContent = book.id;
    document.getElementById("productImage").alt = book.title;
    document.getElementById("productTitle").textContent = book.title;
    document.getElementById("productAuthor").textContent = book.authorName;
    document.getElementById("productISBN").textContent = book.isbn;
    document.getElementById("productGenres").textContent = book.genres.map(g => g.name).join(", ");
    document.getElementById("productDescription").textContent = book.description || "No description available.";
    document.getElementById("productPrice").textContent = `${book.price.toFixed(2)} LKR`;

    document.getElementById("addToCartBtn").onclick = () => addToCart(book.id);
//    document.getElementById("addToWishlistBtn").onclick = () => addToWishlist(book.id);
}

function addToCart(bookId) {
    alert(`Book ${bookId} added to cart.`);
}
