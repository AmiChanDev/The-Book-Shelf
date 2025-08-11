package controller;

import com.google.gson.Gson;
import hibernate.Book;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 *
 * @author AmiChan
 */
@WebServlet(name = "LoadAdminBooks", urlPatterns = {"/LoadAdminBooks"})
public class LoadAdminBooks extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userName") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        Session hibernateSession = null;

        try {
            hibernateSession = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = hibernateSession.createCriteria(Book.class);

            List<Book> books = criteria.list();
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(books));
        } finally {
            if (hibernateSession != null && hibernateSession.isOpen()) {
                hibernateSession.close();
            }
        }
    }

}
