package controller;

import com.google.gson.Gson;
import hibernate.Order;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/GetPurchaseHistory")
public class GetPurchaseHistory extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = (Integer) req.getSession().getAttribute("userId");
        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session session = null;
        try {
            session = sessionFactory.openSession();

            Criteria criteria = session.createCriteria(Order.class);
            criteria.add(Restrictions.eq("user.id", userId));

            List<Order> orders = criteria.list();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            List<Map<String, Object>> orderList = new ArrayList<>();
            for (Order o : orders) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", o.getId());
                map.put("orderDate", sdf.format(o.getOrderDate()));
                map.put("totalAmount", o.getTotalAmount());
                orderList.add(map);
            }

            Gson gson = new Gson();
            String purchaseHistoryJson = gson.toJson(orderList);

            resp.setContentType("application/json");
            resp.getWriter().write(purchaseHistoryJson);
            resp.getWriter().flush();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
