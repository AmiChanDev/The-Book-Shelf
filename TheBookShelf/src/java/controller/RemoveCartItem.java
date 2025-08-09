package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.CartItem;
import hibernate.User;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/RemoveCartItem")
public class RemoveCartItem extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        JsonObject res = new JsonObject();

        if (session == null || session.getAttribute("user") == null) {
            res.addProperty("success", false);
            res.addProperty("message", "User not logged in.");
            response.setContentType("application/json");
            response.getWriter().write(res.toString());
            return;
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }
        JsonObject jsonObject = new Gson().fromJson(sb.toString(), JsonObject.class);
        int bookId = jsonObject.get("bookId").getAsInt();

        User user = (User) session.getAttribute("user");
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = hibernateSession.beginTransaction();

            Criteria criteria = hibernateSession.createCriteria(CartItem.class);
            criteria.add(Restrictions.eq("user", user));
            criteria.add(Restrictions.eq("book.id", bookId));

            CartItem cartItem = (CartItem) criteria.uniqueResult();

            if (cartItem != null) {
                hibernateSession.delete(cartItem);
                tx.commit();
                res.addProperty("success", true);
                res.addProperty("message", "Item removed from cart.");
            } else {
                res.addProperty("success", false);
                res.addProperty("message", "Item not found in cart.");
            }

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            res.addProperty("success", false);
            res.addProperty("message", "Error occurred while removing item.");
        } finally {
            hibernateSession.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(res.toString());
    }

}
