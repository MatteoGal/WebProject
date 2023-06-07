package servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.owasp.encoder.Encode;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.RSAkeys;
import util.Util;

/**
 * Servlet implementation class NavigationServlet
 */
@WebServlet("/NavigationServlet")
public class NavigationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Connection conn;
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public NavigationServlet() {
        super();
    }
    
    public void init() throws ServletException {
    	conn = Util.initDbConnection();
    }

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");


		String email = request.getParameter("email").replace("'", "''");
		String pwd = request.getParameter("password").replace("'", "''");
		
		String search = Encode.forHtml(request.getParameter("search"));
		
								
		if (request.getParameter("newMail") != null)
			request.setAttribute("content", getHtmlForNewMail(request, email, pwd));
		else if (request.getParameter("inbox") != null)
			request.setAttribute("content", getHtmlForInbox(email, pwd, search));
		else if (request.getParameter("sent") != null)
			request.setAttribute("content", getHtmlForSent(email, pwd, search));
		
		
		request.setAttribute("email", email);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}
	
	private String getHtmlForNewMail(HttpServletRequest request, String email, String pwd) {
		HttpSession session=request.getSession(false);  
        String attribute = (String)session.getAttribute("randomToken");
        String demo="Hello";
		return 
			"<form id=\"submitForm\" class=\"form-resize\" action=\"SendMailServlet\" method=\"post\">\r\n"
			+ "		<input type=\"hidden\" name=\"email\" value=\""+email+"\">\r\n"
			+ "		<input type=\"hidden\" name=\"password\" value=\""+pwd+"\">\r\n"
			+ "		<input type=\"hidden\" name=\"CSRFtoken\" name=\"CSRFtoken\" value=\""+attribute+"\">\r\n"
			+ "		<input class=\"single-row-input\" type=\"email\" name=\"receiver\" placeholder=\"Receiver\" required>\r\n"
			+ "		<input class=\"single-row-input\" type=\"text\"  name=\"subject\" placeholder=\"Subject\" required>\r\n"
			+ "		<textarea class=\"textarea-input\" name=\"body\" placeholder=\"Body\" wrap=\"hard\" required></textarea>\r\n"
			+ "		<input type=\"checkbox\" style=\"margin-right: 5 px; margin-top: 5px;\" name=\"signed\" id=\"signed\""
			+ "		<label style=\"margin-top: 5px;\"> Digitally sign </label>\r\n"
			+ "		<br>\r\n"	
			+ "		<input type=\"submit\" name=\"sent\" value=\"Send\">\r\n"
			+ "	</form>";
	}

	private String getHtmlForInbox(String receiver, String password, String sender) {
		try (Statement st = conn.createStatement()) {
			ResultSet sqlRes;
			PreparedStatement senderNull = conn.prepareStatement("SELECT * FROM mail WHERE receiver=? ORDER BY time DESC");
			senderNull.setString(1, receiver);
			PreparedStatement senderNotNull = conn.prepareStatement("SELECT * FROM mail WHERE receiver=? AND sender=? ORDER BY time DESC");
			senderNotNull.setString(1, receiver);
			senderNotNull.setString(2, sender);
			if (sender == null) {
				sqlRes = senderNull.executeQuery();
				System.out.println("Done");

			} else {
				sqlRes = senderNotNull.executeQuery();
				System.out.println("Done");

			}
			
			StringBuilder output = new StringBuilder();
			output.append("<div>\r\n");
			
			output.append("<form action=\"NavigationServlet\" method=\"post\">\r\n");
			output.append("		<input type=\"hidden\" name=\"email\" value=\""+receiver+"\">\r\n");
			output.append("		<input type=\"hidden\" name=\"password\" value=\""+password+"\">\r\n");
			output.append("		<input type=\"text\" placeholder=\"Search for sender\" name=\"search\" required>\r\n");
			output.append("		<input type=\"submit\" name=\"inbox\" value=\"Search\">\r\n");
			output.append("</form>\r\n");
			
			if (sender != null)
				output.append("<p>You searched for: " + sender + "</p>\r\n");
			
			while (sqlRes.next()) {
				String isEncoded = sqlRes.getString(6);
				String toDecode = sqlRes.getString(4);
				if (isEncoded.equals("1")) {

							output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
							output.append("FROM:&emsp;" + sqlRes.getString(1) + "&emsp;&emsp;AT:&emsp;" + sqlRes.getString(5));
							output.append("</span>");
							output.append("<br><b>" + sqlRes.getString(3) + "</b>\r\n");
							output.append("<br>" + NavigationServlet.decodeMessage(toDecode, sender) + "<br>");
							output.append("Encrypted with RSA");
							output.append("</div>\r\n");
							
							output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
							
					}

					
				
				else {
					output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
					output.append("FROM:&emsp;" + sqlRes.getString(1) + "&emsp;&emsp;AT:&emsp;" + sqlRes.getString(5));
					output.append("</span>");
					output.append("<br><b>" + sqlRes.getString(3) + "</b>\r\n");
					output.append("<br>" + sqlRes.getString(4));
					output.append("</div>\r\n");
					
					output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
				}

			}
			
			output.append("</div>");
			
			return output.toString();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR IN FETCHING INBOX MAILS!";
		}
	}
	
	private static String decodeMessage(String message, String sender) {
		String decryptedMessage =" ";
			try (Statement stkey = conn.createStatement()) {
				ResultSet getKey;
					getKey = stkey.executeQuery("SELECT dKey1, Key2 FROM user WHERE email='" + sender +"'");
					
					if(getKey.next()) {
						System.out.println(getKey.getString("dKey1"));
						
						String stringdKey1 = getKey.getString("dKey1");	
						String stringKey2 = getKey.getString("Key2");
						System.out.println(stringdKey1);
						System.out.println(stringKey2);
						BigInteger D = new BigInteger(stringdKey1);
						BigInteger N = new BigInteger(stringKey2);
						BigInteger [] bytesToDecrypt = RSAkeys.getList(message);
						decryptedMessage = RSAkeys.decrypt(bytesToDecrypt, D, N);
					}
				
					
			}
			catch (SQLException e){
				e.printStackTrace();
			}
	
		return decryptedMessage;
	}
	

	
	private String getHtmlForSent(String sender, String password, String receiver) {
		try (Statement st = conn.createStatement()) {
			ResultSet sqlRes;
			PreparedStatement receiverNull = conn.prepareStatement("SELECT * FROM mail WHERE sender=? ORDER BY time DESC");
			receiverNull.setString(1, sender);
			PreparedStatement receiverNotNull = conn.prepareStatement("SELECT * FROM mail WHERE sender=? AND receiver=? ORDER BY time DESC");
			receiverNotNull.setString(1, sender);
			receiverNotNull.setString(2, receiver);
			if (receiver == null) {
				sqlRes = receiverNull.executeQuery();
			} else {
				sqlRes = receiverNotNull.executeQuery();
			}

			StringBuilder output = new StringBuilder();
			output.append("<div>\r\n");
			
			output.append("<form action=\"NavigationServlet\" method=\"post\">\r\n");
			output.append("		<input type=\"hidden\" name=\"email\" value=\""+sender+"\">\r\n");
			output.append("		<input type=\"hidden\" name=\"password\" value=\""+password+"\">\r\n");
			output.append("		<input type=\"text\" placeholder=\"Search for receiver\" name=\"search\" required>\r\n");
			output.append("		<input type=\"submit\" name=\"sent\" value=\"Search\">\r\n");
			output.append("</form>\r\n");
			
			if (receiver != null)
				output.append("<p>You searched for: " + receiver + "</p>\r\n");
			
			while (sqlRes.next()) {
				String isEncoded = sqlRes.getString(6);
				String toDecode = sqlRes.getString(4);
				if (isEncoded.equals("1")) {

							output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
							output.append("FROM:&emsp;" + sqlRes.getString(1) + "&emsp;&emsp;AT:&emsp;" + sqlRes.getString(5));
							output.append("</span>");
							output.append("<br><b>" + sqlRes.getString(3) + "</b>\r\n");
							output.append("<br>" + NavigationServlet.decodeMessageSent(toDecode, sender) + "<br>");
							output.append("Encrypted with RSA");
							output.append("</div>\r\n");
							
							output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
							
					}

					
				
				else {
					output.append("<div style=\"white-space: pre-wrap;\"><span style=\"color:grey;\">");
					output.append("FROM:&emsp;" + sqlRes.getString(1) + "&emsp;&emsp;AT:&emsp;" + sqlRes.getString(5));
					output.append("</span>");
					output.append("<br><b>" + sqlRes.getString(3) + "</b>\r\n");
					output.append("<br>" + sqlRes.getString(4));
					output.append("</div>\r\n");
					
					output.append("<hr style=\"border-top: 2px solid black;\">\r\n");
				}

			}
			
			output.append("</div>");
			
			return output.toString();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return "ERROR IN FETCHING INBOX MAILS!";
		}
	}
	
	private static String decodeMessageSent(String message, String sender) {
		String decryptedMessage =" ";
			try (Statement stkey = conn.createStatement()) {
				ResultSet getKey;
					getKey = stkey.executeQuery("SELECT dKey1, Key2 FROM user WHERE email='" + sender +"'");
					
					if(getKey.next()) {
						
						String stringdKey1 = getKey.getString("dKey1");	
						String stringKey2 = getKey.getString("Key2");

						BigInteger D = new BigInteger(stringdKey1);
						BigInteger N = new BigInteger(stringKey2);
						BigInteger [] bytesToDecrypt = RSAkeys.getList(message);
						decryptedMessage = RSAkeys.decrypt(bytesToDecrypt, D, N);
					}
				
					
			}
			catch (SQLException e){
				e.printStackTrace();
			}
	
		return decryptedMessage;
	}
}
