let currentPage = 1;
const pageSize = 8;

document.addEventListener("DOMContentLoaded", () => {
    loadGenres().then(() => {
        const filters = getFiltersFromURL();
        populateSearchInputs(filters);
        fetchAndDisplayBooks(filters, currentPage, pageSize);
    });
});

function getFiltersFromURL() {
    const urlParams = new URLSearchParams(window.location.search);
    const filters = {};
    if (urlParams.get("title"))
        filters.title = urlParams.get("title");
    if (urlParams.get("isbn"))
        filters.isbn = urlParams.get("isbn");
    if (urlParams.get("author"))
        filters.author = urlParams.get("author");
    if (urlParams.get("genre"))
        filters.genre = urlParams.get("genre");
    return filters;
}

function populateSearchInputs(filters) {
    if (filters.title)
        document.getElementById("searchTitle").value = filters.title;
    if (filters.isbn)
        document.getElementById("searchISBN").value = filters.isbn;
    if (filters.author)
        document.getElementById("searchAuthor").value = filters.author;
    if (filters.genre) {
        const genreSelect = document.getElementById("searchGenre");
        for (let i = 0; i < genreSelect.options.length; i++) {
            if (genreSelect.options[i].text === filters.genre || genreSelect.options[i].value === filters.genre) {
                genreSelect.selectedIndex = i;
                break;
            }
        }
    }
}

function createPagination(totalCount, currentPage, pageSize, filters) {
    const totalPages = Math.ceil(totalCount / pageSize);
    const paginationContainer = document.getElementById("pagination");

    paginationContainer.innerHTML = "";

    for (let i = 1; i <= totalPages; i++) {
        const btn = document.createElement("button");
        btn.textContent = i;
        btn.className = "btn btn-sm mx-1 " + (i === currentPage ? "btn-primary" : "btn-outline-primary");
        btn.onclick = () => {
            fetchAndDisplayBooks(filters, i, pageSize);
        };
        paginationContainer.appendChild(btn);
    }
}

async function fetchAndDisplayBooks(filters = {}, page = 1, pageSize = 8) {
    try {
        for (const key in filters) {
            if (!filters[key])
                delete filters[key];
        }

        filters.page = page;
        filters.pageSize = pageSize;

        const params = new URLSearchParams(filters);
        const response = await fetch(`GetBooks?${params.toString()}`);

        if (!response.ok)
            throw new Error("Failed to fetch books.");

        const data = await response.json();

        const books = data.books || [];
        const totalCount = data.totalCount || 0;

        const container = document.getElementById("booksContainer");
        container.innerHTML = "";

        if (books.length === 0) {
            container.innerHTML = `<div class="col-12"><div class="alert alert-info">No books found.</div></div>`;
            document.getElementById("pagination").innerHTML = "";
            return;
        }

        books.forEach(book => {
            const col = document.createElement("div");
            col.classList.add("col-md-4", "mb-4");

            const link = document.createElement("a");
            link.href = `single-product.html?id=${book.id}`;
            link.classList.add("text-decoration-none");

            const card = document.createElement("div");
            card.classList.add("card", "h-100");

            const img = document.createElement("img");
            img.classList.add("card-img-top");
            img.src = book.imagePath || "images/default.jpg";
            img.alt = book.title;

            const body = document.createElement("div");
            body.classList.add("card-body");

            const title = document.createElement("h5");
            title.classList.add("card-title", "text-center");
            title.textContent = book.title;

            const author = document.createElement("p");
            author.innerHTML = `<strong>Author:</strong> ${book.authorName}`;

            const isbn = document.createElement("p");
            isbn.innerHTML = `<strong>ISBN:</strong> ${book.isbn}`;

            const genre = document.createElement("p");
            genre.innerHTML = `<strong>Genres:</strong> ${book.genres.map(g => g.name).join(", ")}`;

            const price = document.createElement("p");
            price.innerHTML = `<strong>Price:</strong> ${book.price.toFixed(2)} LKR`;

            const btn = document.createElement("button");
            btn.classList.add("btn", "btn-success", "mt-2", "w-100");
            btn.textContent = "Add to Cart";
            btn.onclick = (e) => {
                e.preventDefault();
                addToCart(book.id);
            };

            body.append(title, author, isbn, genre, price, btn);
            card.append(img, body);
            link.appendChild(card);
            col.appendChild(link);
            container.appendChild(col);
        });

        currentPage = page;
        createPagination(totalCount, currentPage, pageSize, filters);

    } catch (err) {
        console.error(err);
        document.getElementById("booksContainer").innerHTML = `<div class="col-12"><div class="alert alert-danger">${err.message}</div></div>`;
        document.getElementById("pagination").innerHTML = "";
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
    } catch {
        console.error("Failed to load genres");
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
        if (!filters[key])
            delete filters[key];
    }

    const newUrl = window.location.pathname + (Object.keys(filters).length ? "?" + new URLSearchParams(filters).toString() : "");
    window.history.pushState({}, "", newUrl);

    currentPage = 1;
    fetchAndDisplayBooks(filters, currentPage, pageSize);
}

function clearSearch() {
    document.getElementById("searchTitle").value = "";
    document.getElementById("searchISBN").value = "";
    document.getElementById("searchAuthor").value = "";
    document.getElementById("searchGenre").selectedIndex = 0;

    window.history.pushState({}, "", window.location.pathname);

    currentPage = 1;
    fetchAndDisplayBooks({}, currentPage, pageSize);
}

