package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hibernate.Address;
import hibernate.City;
import hibernate.User;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/AddAddress")
public class AddAddress extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonObject json = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        String street = json.get("street").getAsString();
        String zipCode = json.get("zipCode").getAsString();
        int cityId = json.get("cityId").getAsInt();

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        JsonObject result = new JsonObject();

        if (user == null) {
            result.addProperty("success", false);
            result.addProperty("message", "User not logged in.");
        } else {
            Session hibSession = null;
            try {
                hibSession = HibernateUtil.getSessionFactory().openSession();
                hibSession.beginTransaction();

                City city = (City) hibSession.get(City.class, cityId);

                Criteria criteria = hibSession.createCriteria(Address.class);
                criteria.add(Restrictions.eq("street", street));
                criteria.add(Restrictions.eq("zipCode", zipCode));
                criteria.add(Restrictions.eq("city", city));
                criteria.add(Restrictions.eq("user", user));

                Address existingAddress = (Address) criteria.uniqueResult();

                if (existingAddress != null) {
                    result.addProperty("success", false);
                    result.addProperty("message", "Address already exists.");
                } else {
                    Address address = new Address();
                    address.setStreet(street);
                    address.setZipCode(zipCode);
                    address.setCity(city);
                    address.setUser(user);

                    hibSession.save(address);
                    hibSession.getTransaction().commit();

                    result.addProperty("success", true);
                    result.addProperty("message", "Address added successfully.");
                }

            } catch (Exception e) {
                if (hibSession != null) {
                    hibSession.getTransaction().rollback();
                }
                e.printStackTrace();
                result.addProperty("success", false);
                result.addProperty("message", "Failed to add address.");
            } finally {
                if (hibSession != null) {
                    hibSession.close();
                }
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(result.toString());
    }
}
