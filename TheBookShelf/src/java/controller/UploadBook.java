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

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session session = sf.openSession();
        Transaction tx = null;

        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        try {
            tx = session.beginTransaction();

            String title = request.getParameter("title");
            String isbn = request.getParameter("isbn");
            Double price = Double.valueOf(request.getParameter("price"));
            int stock = Integer.parseInt(request.getParameter("stock"));
            String description = request.getParameter("description");
            String genreId = request.getParameter("genreId");

            String authorName = request.getParameter("authorName");

            if (authorName == null || authorName.trim().isEmpty()) {
                out.print("Author name cannot be empty.");
                return;
            }

            if (isbn != null && isbn.matches("\\d+")) {
                String reversedIsbn = new StringBuilder(isbn).reverse().toString();
                System.out.println("Reversed ISBN: " + reversedIsbn);
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
            book.setCreatedAt(Timestamp.from(Instant.now()));

            Part imagePart = request.getPart("image");
            if (imagePart != null && imagePart.getSize() > 0) {
                String imageFileName = System.currentTimeMillis() + "_" + imagePart.getSubmittedFileName();
                String uploadPath = getServletContext().getRealPath("/images/products/");
                File uploadDir = new File(uploadPath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                File file = new File(uploadPath + File.separator + imageFileName);
                try (InputStream in = imagePart.getInputStream(); FileOutputStream fos = new FileOutputStream(file)) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                }

                book.setImagePath("images/products/" + imageFileName);
            }

            String[] genreIds = request.getParameterValues("genres");
            Set<Genre> genresSet = new HashSet<>();
            if (genreIds != null) {
                for (String gid : genreIds) {
                    Genre genre = (Genre) session.get(Genre.class, Integer.valueOf(gid));
                    if (genre != null) {
                        genresSet.add(genre);
                    }
                }
            }

            List<Genre> genresList = new ArrayList<>(genresSet);
            book.setGenres(genresList);

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
            session.close();
        }
    }
}
