package controller;

import model.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hibernate.User;
import hibernate.Book;
import hibernate.CartItem;
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

                // Merge cart
                String sessionCartHeader = request.getHeader("Session-Cart");
                if (sessionCartHeader != null && !sessionCartHeader.isEmpty()) {
                    mergeSessionCart(sessionCartHeader, user, session);
                    res.addProperty("cartMerged", true);
                }

                res.addProperty("success", true);
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

    private void mergeSessionCart(String cartJson, User user, Session session) {
        JsonArray cartArray = JsonParser.parseString(cartJson).getAsJsonArray();

        for (int i = 0; i < cartArray.size(); i++) {
            JsonObject item = cartArray.get(i).getAsJsonObject();
            int bookId = item.get("bookId").getAsInt();
            int quantity = item.get("quantity").getAsInt();

            Criteria existing = session.createCriteria(CartItem.class);
            existing.add(Restrictions.eq("user.id", user.getId()));
            existing.add(Restrictions.eq("book.id", bookId));
            CartItem existingItem = (CartItem) existing.uniqueResult();

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                session.update(existingItem);
            } else {
                Book book = (Book) session.get(Book.class, bookId);
                if (book != null) {
                    CartItem newItem = new CartItem();
                    newItem.setUser(user);
                    newItem.setBook(book);
                    newItem.setQuantity(quantity);
                    session.save(newItem);
                }
            }
        }
    }
}
