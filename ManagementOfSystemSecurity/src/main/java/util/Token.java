package util;
import java.util.Random;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class Token {

	
	public static void createToken(HttpServletRequest request, HttpServletResponse response) {
		Random rand = new Random();
		int content = rand.nextInt(100000);
		String token = content+"";
		HttpSession session = request.getSession(false);
		session.setAttribute("randomToken", token);
	}
	
	public static boolean checkToken(HttpServletRequest request, HttpServletResponse response) {
		String CSRFtoken = request.getParameter("CSRFtoken");
		System.out.println(CSRFtoken);
        HttpSession session=request.getSession(false);  
        String attribute = (String)session.getAttribute("randomToken");
        if (!CSRFtoken.equals(attribute) || CSRFtoken == null) {
        	System.out.println("Entry not valid");
        	return false;
        }
        else {
        	return true;
        }
	}
}
