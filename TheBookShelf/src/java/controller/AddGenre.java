package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.Genre;
import model.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "AddGenre", urlPatterns = {"/AddGenre"})
public class AddGenre extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();

        try {
            JsonObject genreData = gson.fromJson(req.getReader(), JsonObject.class);
            String genreName = genreData.get("genreName").getAsString();

            Genre genre = new Genre();
            genre.setName(genreName);

            SessionFactory sf = HibernateUtil.getSessionFactory();
            Session session = sf.openSession();
            Transaction tx = session.beginTransaction();
            session.save(genre);
            tx.commit();

            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("status", "success");
            responseJson.addProperty("message", "Genre added successfully");
            resp.getWriter().write(gson.toJson(responseJson));

        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject errorJson = new JsonObject();
            errorJson.addProperty("status", "error");
            errorJson.addProperty("message", "Failed to add genre: " + e.getMessage());
            resp.getWriter().write(gson.toJson(errorJson));
        }
    }
}
