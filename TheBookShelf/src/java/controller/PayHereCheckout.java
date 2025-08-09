package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.*;
import model.PayHere;
import model.HibernateUtil;
import org.hibernate.*;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/PayHereCheckout")
public class PayHereCheckout extends HttpServlet {

    private static final String MERCHANT_ID = "1224902";
    private static final String MERCHANT_SECRET = "MzQ4MzI5NDM1MTMyMjk0OTAxOTk3ODcyMzQ0ODcyMzk2OTcwMDMz";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        HttpSession httpSession = req.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "User not logged in");
            resp.getWriter().print(jsonResponse.toString());
            return;
        }
        User user = (User) httpSession.getAttribute("user");

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();

            Criteria cartCriteria = session.createCriteria(CartItem.class);
            cartCriteria.add(Restrictions.eq("user", user));
            List<CartItem> cartItems = cartCriteria.list();

            if (cartItems.isEmpty()) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Cart is empty");
                resp.getWriter().print(jsonResponse.toString());
                return;
            }

            double totalAmount = 0;
            for (CartItem item : cartItems) {
                Double price = item.getBook().getPrice();
                if (price == null) {
                    price = 0.0;
                }
                totalAmount += price * item.getQuantity();
            }

            totalAmount += 1000;
            String tempOrderId = String.valueOf(System.currentTimeMillis()) + user.getId();

            String amountStr = String.format("%.2f", totalAmount);
            String currency = "LKR";

            String hash = PayHere.getMd5(MERCHANT_ID + tempOrderId + amountStr + currency + PayHere.getMd5(MERCHANT_SECRET));

            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("merchant_id", MERCHANT_ID);
            jsonResponse.addProperty("order_id", tempOrderId);
            jsonResponse.addProperty("amount", totalAmount);
            jsonResponse.addProperty("currency", currency);
            jsonResponse.addProperty("hash", hash);
            jsonResponse.addProperty("message", "Payment data prepared");

        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Internal server error: " + e.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }

        resp.getWriter().print(jsonResponse.toString());
    }
}
