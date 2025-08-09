package controller;

import com.google.gson.Gson;
import hibernate.Book;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/GetSingleBook")
public class GetSingleBook extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        res.setContentType("application/json");
        PrintWriter out = res.getWriter();

        String idParam = req.getParameter("id");

        if (idParam == null || idParam.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Book ID is required\"}");
            return;
        }

        int bookId;
        try {
            bookId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"error\":\"Invalid Book ID\"}");
            return;
        }

        Session session = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Criteria criteria = session.createCriteria(Book.class);
            criteria.add(Restrictions.eq("id", bookId));
            Book book = (Book) criteria.uniqueResult();

            if (book == null) {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.write("{\"error\":\"Book not found\"}");
            } else {
                Gson gson = new Gson();
                String json = gson.toJson(book);
                out.write(json);
            }

        } catch (Exception e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"" + e.getMessage() + "\"}");
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
