package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Address;
import hibernate.CartItem;
import hibernate.City;
import hibernate.Order;
import hibernate.OrderItem;
import hibernate.User;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author AmiChan
 */
@WebServlet(name = "Checkout", urlPatterns = {"/Checkout"})
public class Checkout extends HttpServlet {

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

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        JsonObject orderData = gson.fromJson(sb.toString(), JsonObject.class);

        String firstName = orderData.get("firstName").getAsString().trim();
        String lastName = orderData.get("lastName").getAsString().trim();
        Integer cityId = orderData.has("cityId") && !orderData.get("cityId").getAsString().isEmpty() ? orderData.get("cityId").getAsInt() : null;
        String street = orderData.get("street").getAsString().trim();
        String postalCode = orderData.get("postalCode").getAsString().trim();
        String mobile = orderData.get("mobile").getAsString().trim();

        String tempOrderIdStr = orderData.has("tempOrderId") ? orderData.get("tempOrderId").getAsString() : null;

        if (cityId == null || street.isEmpty() || mobile.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Missing required fields");
            resp.getWriter().print(jsonResponse.toString());
            return;
        }

        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            // Fetch city
            Criteria cityCriteria = session.createCriteria(City.class);
            cityCriteria.add(Restrictions.eq("id", cityId));
            City city = (City) cityCriteria.uniqueResult();
            if (city == null) {
                tx.rollback();
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Invalid city");
                resp.getWriter().print(jsonResponse.toString());
                return;
            }

            Criteria addrCriteria = session.createCriteria(Address.class);
            addrCriteria.add(Restrictions.eq("user", user));
            addrCriteria.add(Restrictions.eq("city", city));
            addrCriteria.add(Restrictions.eq("street", street));
            addrCriteria.add(Restrictions.eq("zipCode", postalCode));
            Address address = (Address) addrCriteria.uniqueResult();

            if (address == null) {
                address = new Address();
                address.setUser(user);
                address.setCity(city);
                address.setStreet(street);
                address.setZipCode(postalCode);
                session.save(address);
            }

            // Fetch cart items
            Criteria cartCriteria = session.createCriteria(CartItem.class);
            cartCriteria.add(Restrictions.eq("user", user));
            List<CartItem> cartItems = cartCriteria.list();

            if (cartItems.isEmpty()) {
                tx.rollback();
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
            Long tempOrderId = Long.valueOf(orderData.get("tempOrderId").getAsString());

            Order order = new Order();
            order.setId(tempOrderId);
            order.setUser(user);
            order.setAddress(address);
            order.setTotalAmount(totalAmount);
            order.setOrderDate(new Timestamp(System.currentTimeMillis()));
            session.save(order);

            for (CartItem item : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setBookId(item.getBook().getId());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(item.getBook().getPrice());
                session.save(orderItem);
            }

            // Clear cart
            for (CartItem item : cartItems) {
                session.delete(item);
            }

            tx.commit();

            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Order confirmed and saved");

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Internal server error: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }

        resp.getWriter().print(jsonResponse.toString());
    }

}
