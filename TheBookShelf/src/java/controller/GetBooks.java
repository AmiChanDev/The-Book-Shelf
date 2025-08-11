package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Book;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/GetBooks")
public class GetBooks extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String title = request.getParameter("title");
        String isbn = request.getParameter("isbn");
        String author = request.getParameter("author");
        String genre = request.getParameter("genre");

        int page = 1;
        int pageSize = 8;

        try {
            page = Integer.parseInt(request.getParameter("page"));
            if (page < 1) {
                page = 1;
            }
        } catch (Exception ignored) {
        }

        try {
            pageSize = Integer.parseInt(request.getParameter("pageSize"));
            if (pageSize < 1) {
                pageSize = 8;
            }
        } catch (Exception ignored) {
        }

        Session session = HibernateUtil.getSessionFactory().openSession();

        try {
            Criteria criteria = session.createCriteria(Book.class);
            if (title != null && !title.isEmpty()) {
                criteria.add(Restrictions.ilike("title", title, MatchMode.ANYWHERE));
            }
            if (isbn != null && !isbn.isEmpty()) {
                criteria.add(Restrictions.ilike("isbn", isbn, MatchMode.ANYWHERE));
            }
            if (author != null && !author.isEmpty()) {
                criteria.add(Restrictions.ilike("authorName", author, MatchMode.ANYWHERE));
            }
            if (genre != null && !genre.isEmpty()) {
                criteria.createAlias("genres", "g");
                criteria.add(Restrictions.ilike("g.name", genre, MatchMode.ANYWHERE));
            }

            Criteria countCriteria = session.createCriteria(Book.class);
            if (title != null && !title.isEmpty()) {
                countCriteria.add(Restrictions.ilike("title", title, MatchMode.ANYWHERE));
            }
            if (isbn != null && !isbn.isEmpty()) {
                countCriteria.add(Restrictions.ilike("isbn", isbn, MatchMode.ANYWHERE));
            }
            if (author != null && !author.isEmpty()) {
                countCriteria.add(Restrictions.ilike("authorName", author, MatchMode.ANYWHERE));
            }
            if (genre != null && !genre.isEmpty()) {
                countCriteria.createAlias("genres", "g");
                countCriteria.add(Restrictions.ilike("g.name", genre, MatchMode.ANYWHERE));
            }
            countCriteria.setProjection(Projections.rowCount());
            Long totalCountLong = (Long) countCriteria.uniqueResult();
            int totalCount = totalCountLong != null ? totalCountLong.intValue() : 0;

            int offset = (page - 1) * pageSize;
            criteria.setFirstResult(offset);
            criteria.setMaxResults(pageSize);

            List<Book> books = criteria.list();

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.add("books", new Gson().toJsonTree(books));
            jsonResponse.addProperty("totalCount", totalCount);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print(jsonResponse.toString());
            }
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
