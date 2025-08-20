package model;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {

    private static SessionFactory sessionFactory;

    static {
        createSessionFactory();
    }

    private static void createSessionFactory() {
        try {
            System.out.println("Building SessionFactory...");
            Configuration configure = new Configuration();
            configure.configure("hibernate.cfg.xml");
            sessionFactory = configure.buildSessionFactory();
            System.out.println("SessionFactory built successfully.");
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed.");
            ex.printStackTrace(); // ✅ Print full stack trace
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
