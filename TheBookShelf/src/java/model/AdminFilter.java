package model;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author AmiChan
 */
@WebFilter(urlPatterns = {"/admin-dashboard.html"})
public class AdminFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            if (session.getAttribute("role").equals("ADMIN")) {
                chain.doFilter(request, response);
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                resp.sendRedirect("index.html");
            }
        } else {
            resp.sendRedirect("sign-in.html");
        }

    }

    @Override
    public void destroy() {
    }

}
