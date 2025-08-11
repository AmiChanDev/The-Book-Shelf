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
                        Mail.sendMail(reg.email, "The Book Shelf Verification Code: ", "<!DOCTYPE html>\n"
                                + "<html lang=\"en\">\n"
                                + "<head>\n"
                                + "    <meta charset=\"UTF-8\">\n"
                                + "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                                + "    <title>The Book Shelf Verification Code</title>\n"
                                + "    <style>\n"
                                + "        body {\n"
                                + "            margin: 0;\n"
                                + "            padding: 0;\n"
                                + "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;\n"
                                + "            background-color: #f4f4f4;\n"
                                + "        }\n"
                                + "        .container {\n"
                                + "            max-width: 600px;\n"
                                + "            margin: 20px auto;\n"
                                + "            background-color: #ffffff;\n"
                                + "            border-radius: 8px;\n"
                                + "            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);\n"
                                + "            overflow: hidden;\n"
                                + "        }\n"
                                + "        .header {\n"
                                + "            background-color: #2c3e50;\n"
                                + "            padding: 20px;\n"
                                + "            text-align: center;\n"
                                + "            color: #ffffff;\n"
                                + "        }\n"
                                + "        .header h1 {\n"
                                + "            margin: 0;\n"
                                + "            font-size: 24px;\n"
                                + "            font-weight: 600;\n"
                                + "        }\n"
                                + "        .content {\n"
                                + "            padding: 30px;\n"
                                + "            text-align: center;\n"
                                + "        }\n"
                                + "        .code-box {\n"
                                + "            background-color: #f8f9fa;\n"
                                + "            padding: 20px;\n"
                                + "            margin: 20px 0;\n"
                                + "            border-radius: 6px;\n"
                                + "            border: 1px solid #e9ecef;\n"
                                + "        }\n"
                                + "        .code {\n"
                                + "            font-size: 32px;\n"
                                + "            font-weight: bold;\n"
                                + "            color: #2c3e50;\n"
                                + "            letter-spacing: 4px;\n"
                                + "            margin: 0;\n"
                                + "        }\n"
                                + "        .instructions {\n"
                                + "            color: #555;\n"
                                + "            font-size: 16px;\n"
                                + "            line-height: 1.5;\n"
                                + "            margin: 20px 0;\n"
                                + "        }\n"
                                + "        .footer {\n"
                                + "            background-color: #f8f9fa;\n"
                                + "            padding: 20px;\n"
                                + "            text-align: center;\n"
                                + "            font-size: 14px;\n"
                                + "            color: #777;\n"
                                + "            border-top: 1px solid #e9ecef;\n"
                                + "        }\n"
                                + "        .button {\n"
                                + "            display: inline-block;\n"
                                + "            padding: 12px 24px;\n"
                                + "            background-color: #3498db;\n"
                                + "            color: #ffffff;\n"
                                + "            text-decoration: none;\n"
                                + "            border-radius: 4px;\n"
                                + "            font-weight: 600;\n"
                                + "            margin: 10px 0;\n"
                                + "        }\n"
                                + "        .button:hover {\n"
                                + "            background-color: #2980b9;\n"
                                + "        }\n"
                                + "        @media only screen and (max-width: 600px) {\n"
                                + "            .container {\n"
                                + "                margin: 10px;\n"
                                + "                border-radius: 6px;\n"
                                + "            }\n"
                                + "            .code {\n"
                                + "                font-size: 24px;\n"
                                + "                letter-spacing: 2px;\n"
                                + "            }\n"
                                + "        }\n"
                                + "    </style>\n"
                                + "</head>\n"
                                + "<body>\n"
                                + "    <div class=\"container\">\n"
                                + "        <div class=\"header\">\n"
                                + "            <h1>The Book Shelf</h1>\n"
                                + "        </div>\n"
                                + "        <div class=\"content\">\n"
                                + "            <h2>Your Verification Code</h2>\n"
                                + "            <p class=\"instructions\">Please use the code below to verify your email address:</p>\n"
                                + "            <div class=\"code-box\">\n"
                                + "                <h1 class=\"code\">" + generatedCode + "</h1>\n"
                                + "            </div>\n"
                                + "            <p class=\"instructions\">\n"
                                + "                Enter this code in the verification field to complete your registration.<br>\n"
                                + "            </p>\n"
                                + "        </div>\n"
                                + "        <div class=\"footer\">\n"
                                + "            <p>Thank you for joining The Book Shelf!<br>\n"
                                + "            If you didn't request this code, please ignore this email.</p>\n"
                                + "            <p>&copy; 2025 The Book Shelf. All rights reserved.</p>\n"
                                + "        </div>\n"
                                + "    </div>\n"
                                + "</body>\n"
                                + "</html>");
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
