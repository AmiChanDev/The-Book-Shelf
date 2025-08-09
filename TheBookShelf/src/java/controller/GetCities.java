package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hibernate.City;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/GetCities")
public class GetCities extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        JsonArray cityArray = new JsonArray();
        Session hibSession = null;

        try {
            hibSession = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = hibSession.createCriteria(City.class);
            List<City> cities = criteria.list();

            for (City city : cities) {
                JsonObject cityJson = new JsonObject();
                cityJson.addProperty("id", city.getId());
                cityJson.addProperty("name", city.getName());
                cityArray.add(cityJson);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (hibSession != null && hibSession.isOpen()) {
                hibSession.close();
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(cityArray.toString());
    }
}
