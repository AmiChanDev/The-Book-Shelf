package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.User;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/ChangePassword")
public class ChangePassword extends HttpServlet {

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JsonObject res = new JsonObject();
        Gson gson = new Gson();

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            res.addProperty("status", false);
            res.addProperty("message", "You must be logged in.");
        } else {
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);
            String oldPw = body.get("oldPassword").getAsString();
            String newPw = body.get("newPassword").getAsString();

            if (!user.getPassword().equals(oldPw)) {
                res.addProperty("status", false);
                res.addProperty("message", "Incorrect current password.");
            } else {
                Session hSession = HibernateUtil.getSessionFactory().openSession();
                Transaction tx = hSession.beginTransaction();

                user.setPassword(newPw);
                hSession.update(user);
                tx.commit();
                hSession.close();

                res.addProperty("status", true);
                res.addProperty("message", "Password updated successfully.");
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(res.toString());
    }
}
