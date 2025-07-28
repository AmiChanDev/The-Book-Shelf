package controller;

import com.google.gson.JsonObject;
import hibernate.Address;
import hibernate.City;
import hibernate.User;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import model.HibernateUtil;

@WebServlet("/LoadAccount")
public class LoadAccount extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        JsonObject jsonResponse = new JsonObject();

        if (user == null) {
            jsonResponse.addProperty("status", false);//
            jsonResponse.addProperty("message", "User is not logged in.");
        } else {
            jsonResponse.addProperty("status", true);//
            jsonResponse.addProperty("message", "User data loaded successfully.");

            JsonObject userJson = new JsonObject();
            userJson.addProperty("name", user.getName());
            userJson.addProperty("email", user.getEmail());
            userJson.addProperty("mobile", user.getMobile());
            userJson.addProperty("role", user.getRole());
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String formattedDate = user.getCreatedAt().toLocalDateTime().format(formatter);
            userJson.addProperty("created_at", formattedDate);
            
            userJson.addProperty("verification", user.getVerification());

            jsonResponse.add("user", userJson);//

            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            List<Address> addressList = hibernateSession.createCriteria(Address.class)
                    .add(Restrictions.eq("user", user))
                    .list();

            JsonObject addressJson = new JsonObject();
            addressList.forEach(address -> {
                JsonObject addressDetails = new JsonObject();
                addressDetails.addProperty("street", address.getStreet());
                addressDetails.addProperty("zip_code", address.getZipCode());

                City city = address.getCity();
                addressDetails.addProperty("city", city != null ? city.getName() : "N/A");

                addressJson.add(String.valueOf(address.getId()), addressDetails);
            });

            jsonResponse.add("addresses", addressJson);//
            hibernateSession.close();
        }

        response.setContentType("application/json");
        response.getWriter().write(jsonResponse.toString());
    }
}
