package controller;

import model.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.User;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import model.Mail;
import model.Util;

@WebServlet("/SignUp")
public class SignUp extends HttpServlet {
    
    private static class Registration {
        
        String firstName;
        String lastName;
        String email;
        String mobile;
        String password;
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        Gson gson = new Gson();
        BufferedReader reader = request.getReader();
        Registration reg = gson.fromJson(reader, Registration.class);
        
        JsonObject res = new JsonObject();
        
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        
        try {
            tx = session.beginTransaction();
            
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("email", reg.email));
            User existing = (User) criteria.uniqueResult();
            
            if (existing != null) {
                res.addProperty("success", false);
                res.addProperty("message", "Email already registered.");
            } else {
                User user = new User();
                user.setName(reg.firstName + " " + reg.lastName);
                user.setEmail(reg.email);
                user.setMobile(reg.mobile);
                user.setPassword(reg.password);
                String generatedCode = Util.generateCode();
                user.setVerification(generatedCode);
                user.setRole("USER");
                
                session.save(user);
                tx.commit();

                //send email
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Mail.sendMail(reg.email, "The Book Shelf Verification Code: ", "<h1>" + generatedCode + "</h1>");
                    }
                }).start();
                HttpSession httpSession = request.getSession();
                httpSession.setAttribute("email", reg.email);
                
                res.addProperty("success", true);
                res.addProperty("redirect", "verify-account.html");
            }
            
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            res.addProperty("success", false);
            res.addProperty("message", "Registration failed.");
            e.printStackTrace();
        } finally {
            session.close();
        }
        
        response.setContentType("application/json");
        response.getWriter().write(res.toString());
    }
}
