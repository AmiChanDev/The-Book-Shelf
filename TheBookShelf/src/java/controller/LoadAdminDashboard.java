package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Book;
import hibernate.Order;
import hibernate.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import model.OrderDTO;
import model.UserDTO;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;

/**
 *
 * @author AmiChan
 */
@WebServlet(name = "LoadAdminDashboard", urlPatterns = {"/LoadAdminDashboard"})
public class LoadAdminDashboard extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Gson gson = new Gson();

        Transaction tx = null;

        JsonObject responseObject = new JsonObject();

        try {
            Long orderCountLong = (Long) session.createCriteria(Order.class)
                    .setProjection(Projections.rowCount())
                    .uniqueResult();
            int orderCount = orderCountLong != null ? orderCountLong.intValue() : 0;

            Long bookCountLong = (Long) session.createCriteria(Book.class)
                    .setProjection(Projections.rowCount())
                    .uniqueResult();
            int bookCount = bookCountLong != null ? bookCountLong.intValue() : 0;

            Long userCountLong = (Long) session.createCriteria(User.class)
                    .setProjection(Projections.rowCount())
                    .uniqueResult();
            int userCount = userCountLong != null ? userCountLong.intValue() : 0;

            Double revenueCountDouble = (Double) session.createCriteria(Order.class)
                    .setProjection(Projections.sum("totalAmount"))
                    .uniqueResult();
            double revenueCount = revenueCountDouble != null ? revenueCountDouble : 0.0;

            Criteria criteria = session.createCriteria(Order.class);
            criteria.addOrder(org.hibernate.criterion.Order.desc("orderDate"));
            criteria.setMaxResults(5);

            List<Order> recentOrders = criteria.list();

            List<OrderDTO> dtoOrderList = new ArrayList<>();
            for (Order order : recentOrders) {
                dtoOrderList.add(new OrderDTO(
                        order.getId(),
                        order.getUser() != null ? order.getUser().getName() : "N/A",
                        order.getOrderDate() != null ? order.getOrderDate().toString() : "N/A",
                        order.getTotalAmount()
                ));
            }

            Criteria criteria2 = session.createCriteria(User.class);
            criteria2.addOrder(org.hibernate.criterion.Order.desc("role"));
            List<User> userList = criteria2.list();

            List<UserDTO> dtoUserList = new ArrayList<>();
            for (User user : userList) {
                String verificationRaw = user.getVerification();
                String verification = (verificationRaw != null && verificationRaw.equalsIgnoreCase("verified"))
                        ? "Verified" : "Unverified";
                String createdAt = user.getCreatedAt() != null ? user.getCreatedAt().toString() : "N/A";

                dtoUserList.add(new UserDTO(
                        user.getName(),
                        user.getEmail(),
                        user.getMobile(),
                        verification,
                        user.getRole(),
                        createdAt
                ));
            }

            responseObject.add("allUsers", gson.toJsonTree(dtoUserList));

            responseObject.addProperty("success", true);
            responseObject.addProperty("totalOrders", orderCount);
            responseObject.addProperty("booksInStock", bookCount);
            responseObject.addProperty("totalUsers", userCount);
            responseObject.add("recentOrders", gson.toJsonTree(dtoOrderList));
            responseObject.addProperty("totalRevenue", revenueCount);

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            // Print full stack trace here
            responseObject.addProperty("success", false);
            responseObject.addProperty("message", e.getMessage());
        }

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(responseObject));
    }

}
