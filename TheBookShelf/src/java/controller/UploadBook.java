package controller;

import hibernate.Book;
import hibernate.Genre;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

@WebServlet(name = "UploadBook", urlPatterns = {"/UploadBook"})
@MultipartConfig
public class UploadBook extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Session session = null;
        Transaction tx = null;

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            String title = request.getParameter("title");
            String isbn = request.getParameter("isbn");
            String priceStr = request.getParameter("price");
            String stockStr = request.getParameter("stock");
            String description = request.getParameter("description");
            String authorName = request.getParameter("authorName");
            String[] genreIds = request.getParameterValues("genres");

            // Basic validations
            if (authorName == null || authorName.trim().isEmpty()) {
                out.print("Author name cannot be empty.");
                return;
            }
            if (title == null || title.trim().isEmpty()) {
                out.print("Title cannot be empty.");
                return;
            }
            if (isbn == null || isbn.trim().isEmpty()) {
                out.print("ISBN cannot be empty.");
                return;
            }
            double price;
            int stock;
            try {
                price = Double.parseDouble(priceStr);
                stock = Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                out.print("Invalid price or stock number.");
                return;
            }

            Criteria criteria = session.createCriteria(Book.class);
            criteria.add(Restrictions.eq("isbn", isbn));
            List<Book> existingBooks = criteria.list();
            if (!existingBooks.isEmpty()) {
                out.print("The book with ISBN " + isbn + " is already listed.");
                return;
            }

            Book book = new Book();
            book.setAuthorName(authorName);
            book.setTitle(title);
            book.setIsbn(isbn);
            book.setPrice(price);
            book.setStock(stock);
            book.setDescription(description);
            book.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

            // Handle image upload
            Part imagePart = request.getPart("image");
            if (imagePart != null && imagePart.getSize() > 0) {
                String imageFileName = System.currentTimeMillis() + "_" + imagePart.getSubmittedFileName();
                String uploadPath = getServletContext().getRealPath("/images/products/");
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                File file = new File(uploadDir, imageFileName);
                try (InputStream in = imagePart.getInputStream(); FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }
                book.setImagePath("images/products/" + imageFileName);
            }

            // Set genres
            if (genreIds != null) {
                Set<Genre> genresSet = new HashSet<>();
                for (String gid : genreIds) {
                    Genre genre = (Genre) session.get(Genre.class, Integer.valueOf(gid));
                    if (genre != null) {
                        genresSet.add(genre);
                    }
                }
                book.setGenres(new ArrayList<>(genresSet));
            }

            session.save(book);
            tx.commit();

            out.print("Book uploaded successfully!");

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            out.print("Failed to upload book.");
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
