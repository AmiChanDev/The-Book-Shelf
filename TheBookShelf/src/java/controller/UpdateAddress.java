package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Address;
import hibernate.City;
import java.io.BufferedReader;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

@WebServlet(name = "UpdateAddress", urlPatterns = {"/UpdateAddress"})
public class UpdateAddress extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Gson gson = new Gson();
        JsonObject jsonResponse = new JsonObject();

        try (BufferedReader reader = request.getReader()) {
            JsonObject body = gson.fromJson(reader, JsonObject.class);

            int id = body.get("id").getAsInt();
            String street = body.get("street").getAsString();
            String zipCode = body.get("zip_code").getAsString();
            int cityId = body.get("city_id").getAsInt();

            SessionFactory factory = HibernateUtil.getSessionFactory();
            Session session = factory.openSession();
            Transaction tx = session.beginTransaction();

            Address address = (Address) session.get(Address.class, id);
            if (address != null) {
                City city = (City) session.get(City.class, cityId);

                if (city != null) {
                    address.setStreet(street);
                    address.setZipCode(zipCode);
                    address.setCity(city);
                    session.update(address);

                    tx.commit();
                    jsonResponse.addProperty("status", true);
                    jsonResponse.addProperty("message", "Address updated successfully.");
                } else {
                    jsonResponse.addProperty("status", false);
                    jsonResponse.addProperty("message", "Invalid city ID.");
                }
            } else {
                jsonResponse.addProperty("status", false);
                jsonResponse.addProperty("message", "Address not found.");
            }

            session.close();
        } catch (Exception e) {
            jsonResponse.addProperty("status", false);
            jsonResponse.addProperty("message", "Error: " + e.getMessage());
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}
