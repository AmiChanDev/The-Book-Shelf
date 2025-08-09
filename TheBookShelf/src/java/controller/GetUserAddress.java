package controller;

import com.google.gson.JsonObject;
import hibernate.Address;
import hibernate.City;
import hibernate.User;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/GetUserAddress")
public class GetUserAddress extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonObject json = new JsonObject();
            json.addProperty("success", false);
            json.addProperty("message", "User not logged in.");
            out.print(json.toString());
            return;
        }

        Integer userId = (Integer) session.getAttribute("userId");
        Session hibSession = null;

        try {
            hibSession = HibernateUtil.getSessionFactory().openSession();

            User user = (User) hibSession.get(User.class, userId);
            if (user == null) {
                JsonObject json = new JsonObject();
                json.addProperty("success", false);
                json.addProperty("message", "User not found.");
                out.print(json.toString());
                return;
            }

            Criteria criteria = hibSession.createCriteria(Address.class);
            criteria.add(Restrictions.eq("user.id", userId));
            criteria.addOrder(Order.desc("id"));
            criteria.setMaxResults(1);
            Address address = (Address) criteria.uniqueResult();

            if (address == null) {
                JsonObject json = new JsonObject();
                json.addProperty("success", false);
                json.addProperty("message", "Address not found.");
                out.print(json.toString());
                return;
            }

            JsonObject addrJson = new JsonObject();

            String fullName = user.getName() != null ? user.getName().trim() : "";
            String firstName = "";
            String lastName = "";
            if (!fullName.isEmpty()) {
                String[] parts = fullName.split("\\s+", 2);
                firstName = parts[0];
                lastName = parts.length > 1 ? parts[1] : "";
            }

            addrJson.addProperty("firstName", firstName);
            addrJson.addProperty("lastName", lastName);
            addrJson.addProperty("street", address.getStreet());
            addrJson.addProperty("postalCode", address.getZipCode());
            addrJson.addProperty("mobile", user.getMobile());
            City city = address.getCity();
            addrJson.addProperty("cityId", city.getId());

            JsonObject json = new JsonObject();
            json.addProperty("success", true);
            json.add("address", addrJson);

            out.print(json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject json = new JsonObject();
            json.addProperty("success", false);
            json.addProperty("message", "Failed to load address.");
            out.print(json.toString());
        } finally {
            if (hibSession != null && hibSession.isOpen()) {
                hibSession.close();
            }
        }
    }
}
