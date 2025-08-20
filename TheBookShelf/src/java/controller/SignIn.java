package controller;

import model.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/SignIn")
public class SignIn extends HttpServlet {

    private static class Credentials {

        String email;
        String password;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Gson gson = new Gson();
        BufferedReader reader = request.getReader();
        Credentials credentials = gson.fromJson(reader, Credentials.class);

        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        JsonObject res = new JsonObject();

        try {
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", credentials.email));
            criteria.add(Restrictions.eq("password", credentials.password));

            User user = (User) criteria.uniqueResult();

            if (user != null) {
                HttpSession httpSession = request.getSession();
                httpSession.setAttribute("user", user);
                httpSession.setAttribute("email", credentials.email);
                httpSession.setAttribute("userName", user.getName());
                httpSession.setAttribute("userId", user.getId());
                httpSession.setAttribute("role", user.getRole());

                res.addProperty("role", user.getRole());
                res.addProperty("success", true);
                res.addProperty("message", "success");
                if ("verified".equals(user.getVerification())) {
                    res.addProperty("redirect", user.getRole().equals("ADMIN") ? "admin-dashboard.html" : "index.html");
                } else {
                    res.addProperty("redirect", "verify-account.html");
                }
            } else {
                res.addProperty("success", false);
                res.addProperty("message", "Invalid email or password.");
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            res.addProperty("success", false);
            res.addProperty("message", "Server error.");
        } finally {
            session.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(res.toString());
    }

}
