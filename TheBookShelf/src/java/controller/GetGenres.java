package controller;

import com.google.gson.Gson;
import hibernate.Genre;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;

/**
 *
 * @author AmiChan
 */
@WebServlet(name = "GetGenres", urlPatterns = {"/GetGenres"})
public class GetGenres extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SessionFactory sf = HibernateUtil.getSessionFactory();
        Session session = sf.openSession();
        Transaction tx = null;

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            tx = session.beginTransaction();

            Criteria cr = session.createCriteria(Genre.class);
            cr.addOrder(Order.asc("name"));  
            List<Genre> genreList = cr.list();

            Gson gson = new Gson();
            String json = gson.toJson(genreList);
            out.print(json);

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            session.close();
            out.close();
        }
    }

}
