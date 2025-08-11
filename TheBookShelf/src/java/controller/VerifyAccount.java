package controller;

import model.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.User;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Anne
 */
@WebServlet(name = "VerifyAccount", urlPatterns = {"/VerifyAccount"})
public class VerifyAccount extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession ses = request.getSession();

        if (ses.getAttribute("email") == null) {
            responseObject.addProperty("message", "Email Not Found");
        } else {

            String email = ses.getAttribute("email").toString();

            JsonObject userCode = gson.fromJson(request.getReader(), JsonObject.class);
            String verificationCode = userCode.get("verificationcode").getAsString();

            Session s = null;
            org.hibernate.Transaction tx = null;

            try {
                s = HibernateUtil.getSessionFactory().openSession();

                Criteria c1 = s.createCriteria(User.class);
                c1.add(Restrictions.eq("email", email));
                c1.add(Restrictions.eq("verification", verificationCode));

                @SuppressWarnings("unchecked")
                java.util.List<User> userList = c1.list();

                if (userList.isEmpty()) {
                    responseObject.addProperty("message", "Invalid Verification Code");
                } else {
                    User user = userList.get(0);

                    tx = s.beginTransaction();
                    user.setVerification("verified");
                    s.update(user);
                    tx.commit();

                    ses.setAttribute("user", user);

                    responseObject.addProperty("status", true);
                    responseObject.addProperty("message", "Verification Success!");
                }
            } catch (Exception e) {
                if (tx != null) {
                    tx.rollback();
                }
                e.printStackTrace();
                responseObject.addProperty("message", "Server error.");
            } finally {
                if (s != null && s.isOpen()) {
                    s.close();
                }
            }
        }

        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);
    }

}
