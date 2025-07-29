document.addEventListener("DOMContentLoaded", () => {
    loadGenres();
    fetchAndDisplayBooks();
});

async function fetchAndDisplayBooks(filters = {}) {
    try {
        const params = new URLSearchParams(filters);
        const response = await fetch(`GetBooks?${params.toString()}`);
        if (!response.ok)
            throw new Error("Failed to fetch books.");

        const books = await response.json();
        const container = document.getElementById("booksContainer");
        container.innerHTML = "";

        if (books.length === 0) {
            container.innerHTML = `<div class="col-12"><div class="alert alert-info">No books found.</div></div>`;
            return;
        }

        books.forEach(book => {
            const cardCol = document.createElement("div");
            cardCol.classList.add("col-md-4", "mb-4");

            const cardLink = document.createElement("a");
            cardLink.href = `single-product.html?id=${book.id}`;
            cardLink.classList.add("text-decoration-none");

            const card = document.createElement("div");
            card.classList.add("card", "h-100", "book-card");

            const img = document.createElement("img");
            img.classList.add("card-img-top");
            img.src = book.imagePath || "images/default.jpg";
            img.alt = book.title;

            const body = document.createElement("div");
            body.classList.add("card-body");

            const title = document.createElement("h5");
            title.classList.add("card-title", "text-center");
            title.textContent = book.title;

            const bookId = document.createElement("div");
            bookId.classList.add("hidden");
            bookId.setAttribute("data-book-id", book.id);

            const author = document.createElement("p");
            author.innerHTML = `<strong>Author:</strong> ${book.authorName}`;

            const isbn = document.createElement("p");
            isbn.innerHTML = `<strong>ISBN:</strong> ${book.isbn}`;

            const genre = document.createElement("p");
            genre.innerHTML = `<strong>Genres:</strong> ${book.genres.map(g => g.name).join(", ")}`;

            const price = document.createElement("p");
            price.innerHTML = `<strong>Price:</strong> $${book.price.toFixed(2)}`;

            const btn = document.createElement("button");
            btn.classList.add("btn", "btn-success", "mt-2", "w-100");
            btn.textContent = "Add to Cart";
            btn.onclick = (event) => {
                event.preventDefault();
                addToCart(book.id);
            };

            body.append(title, author, isbn, genre, price, btn, bookId);
            card.append(img, body);
            cardLink.appendChild(card);
            cardCol.appendChild(cardLink);


            container.appendChild(cardCol);
        });



    } catch (err) {
        console.error(err);
        document.getElementById("booksContainer").innerHTML =
                `<div class="col-12"><div class="alert alert-danger">${err.message}</div></div>`;
}
}

async function loadGenres() {
    try {
        const response = await fetch("GetGenres");
        const data = await response.json();

        const genreSelect = document.getElementById("searchGenre");
        genreSelect.innerHTML = '<option value="">Select Genre</option>';

        data.forEach(genre => {
            const option = document.createElement("option");
            option.value = genre.id;
            option.textContent = genre.name;
            genreSelect.appendChild(option);
        });
    } catch (err) {
        console.error("Failed to load genres:", err);
    }
}

function searchBooks() {
    const genreSelect = document.getElementById("searchGenre");
    const genre = genreSelect.selectedIndex === 0 ? null : genreSelect.options[genreSelect.selectedIndex].text.trim();

    const filters = {
        title: document.getElementById("searchTitle").value.trim(),
        isbn: document.getElementById("searchISBN").value.trim(),
        author: document.getElementById("searchAuthor").value.trim(),
        genre: genre
    };

    for (const key in filters) {
        if (!filters[key]) {
            delete filters[key];
        }
    }

    fetchAndDisplayBooks(filters);
}

function clearSearch() {
    document.getElementById("searchTitle").value = "";
    document.getElementById("searchISBN").value = "";
    document.getElementById("searchAuthor").value = "";
    document.getElementById("searchGenre").selectedIndex = 0;

    fetchAndDisplayBooks();
}
