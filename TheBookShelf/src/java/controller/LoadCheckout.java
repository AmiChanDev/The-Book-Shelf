package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.Book;
import hibernate.CartItem;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.List;

@WebServlet("/LoadCheckout")
public class LoadCheckout extends HttpServlet {

    private static final double SHIPPING_CHARGE = 1000.00;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("Session Attributes:");
            Enumeration<String> attrNames = session.getAttributeNames();
            while (attrNames.hasMoreElements()) {
                String attrName = attrNames.nextElement();
                Object attrValue = session.getAttribute(attrName);
            }
        } else {
            System.out.println("No active session found.");
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        if (session == null || session.getAttribute("user") == null) {
            JsonObject json = new JsonObject();
            json.addProperty("success", false);
            json.addProperty("message", "User not logged in.");
            out.print(json.toString());
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            JsonObject json = new JsonObject();
            json.addProperty("success", false);
            json.addProperty("message", "User ID not found in session.");
            out.print(json.toString());
            return;
        }

        Session hibSession = null;

        try {
            hibSession = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = hibSession.createCriteria(CartItem.class);
            criteria.add(Restrictions.eq("user.id", userId));

            List<CartItem> cartItems = criteria.list();

            JsonArray itemsArray = new JsonArray();
            double subtotal = 0.0;

            for (CartItem item : cartItems) {
                JsonObject itemJson = new JsonObject();
                Book book = item.getBook();

                if (book == null) {
                    continue;
                }

                itemJson.addProperty("title", book.getTitle());
                itemJson.addProperty("quantity", item.getQuantity());
                itemJson.addProperty("price", book.getPrice());

                itemsArray.add(itemJson);

                subtotal += book.getPrice() * item.getQuantity();
            }

            double shipping = cartItems.isEmpty() ? 0.0 : SHIPPING_CHARGE;
            double total = subtotal + shipping;

            JsonObject json = new JsonObject();
            json.addProperty("success", true);
            json.add("cartItems", itemsArray);
            json.addProperty("subtotal", subtotal);
            json.addProperty("shipping", shipping);
            json.addProperty("total", total);

            out.print(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject json = new JsonObject();
            json.addProperty("success", false);
            json.addProperty("message", "Failed to load checkout data.");
            out.print(json.toString());
        } finally {
            if (hibSession != null && hibSession.isOpen()) {
                hibSession.close();
            }
        }
    }

}
