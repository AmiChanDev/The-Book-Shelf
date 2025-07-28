package controller;

import model.HibernateUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hibernate.User;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author Anne
 */
@WebServlet(name = "VerifyAccount", urlPatterns = {"/VerifyAccount"})
public class VerifyAccount extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Gson gson = new Gson();
        
        JsonObject responseObject = new JsonObject();
        responseObject.addProperty("status", false);

        HttpSession ses = request.getSession();

        if (ses.getAttribute("email") == null) {
            responseObject.addProperty("message", "Email Not Found");
        } else {

            String email = ses.getAttribute("email").toString();

            
            JsonObject userCode = gson.fromJson(request.getReader(), JsonObject.class);

            String verificationCode = userCode.get("verificationcode").getAsString();
 
            Session s = HibernateUtil.getSessionFactory().openSession();
            
            Criteria c1 = s.createCriteria(User.class);
            
            Criterion crt1 = Restrictions.eq("email",email);
            Criterion crt2 = Restrictions.eq("verification",verificationCode);
            
            c1.add(crt1);
            c1.add(crt2);
            
            if(c1.list().isEmpty()){
                responseObject.addProperty("message", "Invalid Verification Code");
            }else{
                
               User user = (User)c1.list().get(0);
               
               user.setVerification("verified");
               
               s.update(user);
               s.beginTransaction().commit();
               s.close();
               
               ses.setAttribute("user", user);
               
               responseObject.addProperty("status",true);
               responseObject.addProperty("message","Verification Successs !");
               
                
            }
               
        }
        
        String responseText = gson.toJson(responseObject);
        response.setContentType("application/json");
        response.getWriter().write(responseText);

    }

}
