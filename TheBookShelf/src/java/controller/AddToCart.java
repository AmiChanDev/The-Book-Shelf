package controller;

import com.google.gson.JsonObject;
import hibernate.Book;
import hibernate.CartItem;
import hibernate.User;
import java.io.BufferedReader;
import java.io.IOException;
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
import com.google.gson.Gson;

@WebServlet(name = "AddToCart", urlPatterns = {"/AddToCart"})
public class AddToCart extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        JsonObject jsonRequest = new Gson().fromJson(sb.toString(), JsonObject.class);
        int bookId = jsonRequest.get("bookId").getAsInt();
        int quantity = jsonRequest.get("quantity").getAsInt();

        HttpSession session = request.getSession();
        User userObject = (User) session.getAttribute("user");

        if (userObject == null) {
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Please Login to Add Items To Cart");
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(jsonResponse));
            return;
        }

        int userId = userObject.getId();

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = hibernateSession.beginTransaction();

            Criteria criteria = hibernateSession.createCriteria(CartItem.class);
            criteria.add(Restrictions.eq("user.id", userId));
            criteria.add(Restrictions.eq("book.id", bookId));

            List<CartItem> existingItems = criteria.list();

            if (!existingItems.isEmpty()) {
                CartItem existingCartItem = existingItems.get(0);
                existingCartItem.setQuantity(existingCartItem.getQuantity() + quantity);
                hibernateSession.update(existingCartItem);

                // Optional: delete any duplicate entries
                for (int i = 1; i < existingItems.size(); i++) {
                    hibernateSession.delete(existingItems.get(i));
                }

            } else {
                User user = (User) hibernateSession.get(User.class, userId);
                Book book = (Book) hibernateSession.get(Book.class, bookId);

                if (user == null || book == null) {
                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("success", false);
                    jsonResponse.addProperty("message", "Invalid user or book.");
                    response.setContentType("application/json");
                    response.getWriter().write(new Gson().toJson(jsonResponse));
                    return;
                }

                CartItem cartItem = new CartItem();
                cartItem.setUser(user);
                cartItem.setBook(book);
                cartItem.setQuantity(quantity);

                hibernateSession.save(cartItem);
            }

            tx.commit();

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Book added to cart.");
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(jsonResponse));

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Failed to add book to cart.");
            response.setContentType("application/json");
            response.getWriter().write(new Gson().toJson(jsonResponse));
        }

    }
}
