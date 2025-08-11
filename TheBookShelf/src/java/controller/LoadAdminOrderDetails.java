package controller;

import com.google.gson.Gson;
import hibernate.Book;
import hibernate.Order;
import hibernate.OrderItem;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

@WebServlet("/LoadAdminOrderDetails")
public class LoadAdminOrderDetails extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long orderId = null;
        try {
            orderId = Long.valueOf(req.getParameter("orderId"));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = null;

        try {
            session = sessionFactory.openSession();

            Order order = (Order) session.get(Order.class, orderId);
            if (order == null || order.getUser() == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Criteria criteria = session.createCriteria(OrderItem.class);
            criteria.add(Restrictions.eq("order.id", orderId));

            List<OrderItem> items = criteria.list();

            List<Map<String, Object>> result = new ArrayList<>();
            for (OrderItem item : items) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", orderId);
                map.put("orderDate", order.getOrderDate().toString());
                map.put("bookId", item.getBookId());

                Criteria criteria2 = session.createCriteria(Book.class);
                criteria2.add(Restrictions.eq("id", item.getBookId()));
                Book book = (Book) criteria2.uniqueResult();

                map.put("bookName", book.getTitle());
                map.put("quantity", item.getQuantity());
                map.put("price", item.getPrice());
                result.add(map);
            }

            Gson gson = new Gson();
            String json = gson.toJson(result);

            resp.setContentType("application/json");
            resp.getWriter().write(json);
            resp.getWriter().flush();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
