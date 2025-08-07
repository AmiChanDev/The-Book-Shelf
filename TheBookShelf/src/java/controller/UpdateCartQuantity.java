package controller;

import com.google.gson.Gson;
import hibernate.Book;
import hibernate.CartItem;
import hibernate.User;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import model.HibernateUtil;

@WebServlet(name = "UpdateCartQuantity", urlPatterns = {"/UpdateCartQuantity"})
public class UpdateCartQuantity extends HttpServlet {

    private static class RequestBody {

        int bookId;
        int quantity;
    }

    private static class JsonResponse {

        boolean success;
        String message;

        JsonResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        Gson gson = new Gson();

        BufferedReader reader = request.getReader();
        RequestBody body = gson.fromJson(reader, RequestBody.class);

        if (body == null || body.bookId <= 0 || body.quantity < 1) {
            response.getWriter().write(gson.toJson(new JsonResponse(false, "Invalid book ID or quantity")));
            return;
        }

        User user = (User) request.getSession().getAttribute("user");
        if (user == null) {
            response.getWriter().write(gson.toJson(new JsonResponse(false, "User not logged in")));
            return;
        }

        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.openSession();
        Transaction tx = session.beginTransaction();

        Criteria criteria = session.createCriteria(CartItem.class)
                .add(Restrictions.eq("user", user))
                .add(Restrictions.eq("book.id", body.bookId));

        CartItem cartItem = (CartItem) criteria.uniqueResult();

        if (cartItem != null) {
            cartItem.setQuantity(body.quantity);
            session.update(cartItem);
        } else {
            Book book = (Book) session.get(Book.class, body.bookId);
            if (book == null) {
                response.getWriter().write(gson.toJson(new JsonResponse(false, "Book not found")));
                return;
            }
            cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setBook(book);
            cartItem.setQuantity(body.quantity);
            session.save(cartItem);
        }

        tx.commit();
        session.close();

        response.getWriter().write(gson.toJson(new JsonResponse(true, "Quantity updated")));
    }
}
