package controller;

import com.google.gson.JsonObject;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author AmiChan
 */
@WebServlet(name = "CheckSession", urlPatterns = {"/CheckSession"})
public class CheckSession extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        HttpSession session = request.getSession(false);
        JsonObject res = new JsonObject();

        if (session != null && session.getAttribute("user") != null) {
            res.addProperty("status", true);
        } else {
            res.addProperty("status", false);
        }

        response.setContentType("application/json");
        response.getWriter().write(res.toString());
    }
}
