<%-- 
    Document   : session-action
    Created on : Aug 15, 2025, 3:23:17 PM
    Author     : AmiChan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="sessionAction" method="POST">
            Data<input type="text" name="data">
            <input type="submit" name="action" value="save"><!-- comment -->
            <input type="submit" name="action" value="delete"><!-- comment -->
        </form>
    </body>
</html>
