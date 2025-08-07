package controller;

import model.HibernateUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.CartItem;
import hibernate.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/LoadCart")
public class LoadCart extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession httpSession = request.getSession(false); 
        JsonObject res = new JsonObject();

        if (httpSession == null || httpSession.getAttribute("user") == null) {
            res.addProperty("success", false);
            res.addProperty("message", "User not logged in.");
            response.setContentType("application/json");
            response.getWriter().write(res.toString());
            return;
        }

        User user = (User) httpSession.getAttribute("user");
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            Criteria criteria = session.createCriteria(CartItem.class);
            criteria.add(Restrictions.eq("user", user)); 

            List<CartItem> cartItems = criteria.list();
            tx.commit();

            if (cartItems == null || cartItems.isEmpty()) {
                res.addProperty("success", true);
                res.addProperty("message", "Cart is empty.");
            } else {
                res.addProperty("success", true);

                JsonArray cartArray = new JsonArray();

                for (CartItem cartItem : cartItems) {
                    JsonObject itemJson = new JsonObject();
                    itemJson.addProperty("bookId", cartItem.getBook().getId());
                    itemJson.addProperty("bookTitle", cartItem.getBook().getTitle());
                    itemJson.addProperty("price", cartItem.getBook().getPrice());
                    itemJson.addProperty("quantity", cartItem.getQuantity());
                    itemJson.addProperty("imagePath", cartItem.getBook().getImagePath());
                    cartArray.add(itemJson);
                }

                res.add("cartItems", cartArray);
            }

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
