package controller;

import com.google.gson.Gson;
import hibernate.Book;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
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

            List<Book> books = criteria.list();

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print(new Gson().toJson(books));
            out.flush();
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
