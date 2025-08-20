package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "Error404Servlet", urlPatterns = "/error-404")
public class Error404Servlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setStatus(404);
        response.setContentType("text/html;charset=UTF-8");

        String requestedURL = (String) request.getAttribute("javax.servlet.error.request_uri");
        if (requestedURL == null) {
            requestedURL = request.getRequestURI();
        }

        response.sendRedirect("404-page.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
