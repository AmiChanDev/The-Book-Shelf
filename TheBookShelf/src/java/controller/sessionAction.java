package controller;

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
@WebServlet(name = "sessionAction", urlPatterns = {"/sessionAction"})
public class sessionAction extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        String data = request.getParameter("data");
        String action = request.getParameter("action");
        
        if (action.equals("save")) {
            session.setAttribute("data", data);
            
            response.getWriter().write(data);
        } else if (action.equals("delete")) {
            session.removeAttribute("data");
            
            response.getWriter().write("session data cleared");
        }
    }
    
}
