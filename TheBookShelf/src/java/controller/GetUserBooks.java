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
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author AmiChan
 */
@WebServlet("/GetUserBooks")
public class GetUserBooks extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userName") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String authorName = (String) session.getAttribute("userName");

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();

        try {
//            System.out.println("Author Name: " + authorName); 
            Criteria criteria = hibernateSession.createCriteria(Book.class)
                    .add(Restrictions.eq("authorName", authorName));

            List<Book> books = criteria.list();
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(books));
        } finally {
            hibernateSession.close();
        }
    }
}
