package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author AmiChan
 */
@WebServlet(name = "SignOut", urlPatterns = {"/SignOut"})
public class SignOut extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);
        HttpSession session = req.getSession(false);

        if (session != null) {
            session.invalidate();
            responseObject.addProperty("status", true);
        }

        Gson gson = new Gson();
        String toJson = gson.toJson(responseObject);
        resp.setContentType("application/json");
        resp.getWriter().write(toJson);
    }

}
