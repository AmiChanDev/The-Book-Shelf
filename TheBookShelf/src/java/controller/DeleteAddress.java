package controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import hibernate.Address;
import hibernate.User;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;

@WebServlet("/DeleteAddress")
public class DeleteAddress extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonObject json = JsonParser.parseReader(request.getReader()).getAsJsonObject();
        int addressId = json.get("addressId").getAsInt();

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        JsonObject result = new JsonObject();

        if (user == null) {
            result.addProperty("success", false);
            result.addProperty("message", "User not logged in.");
        } else {
            Session hibSession = null;
            Transaction transaction = null;

            try {
                hibSession = HibernateUtil.getSessionFactory().openSession();
                transaction = hibSession.beginTransaction();

                Address address = (Address) hibSession.get(Address.class, addressId);
                if (address != null && address.getUser().getId() == user.getId()) {
                    hibSession.delete(address);  // Delete the address from the database
                    transaction.commit();

                    result.addProperty("success", true);
                    result.addProperty("message", "Address deleted successfully.");
                } else {
                    result.addProperty("success", false);
                    result.addProperty("message", "Address not found or does not belong to this user.");
                }

            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                e.printStackTrace();
                result.addProperty("success", false);
                result.addProperty("message", "Failed to delete address.");
            } finally {
                if (hibSession != null && hibSession.isOpen()) {
                    hibSession.close();
                }

            }
        }

        response.setContentType("application/json");
        response.getWriter().write(result.toString());
    }
}
