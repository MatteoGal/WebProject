package servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.owasp.encoder.Encode;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.RSAkeys;
import util.Token;
import util.Util;

/**
 * Servlet implementation class SendMailServlet
 */
@WebServlet("/SendMailServlet")
public class SendMailServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Connection conn;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendMailServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
    public void init() throws ServletException {
    	conn = Util.initDbConnection();
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");

		Token.checkToken(request, response);

		String sender = request.getParameter("email").replace("'", "''");
		String receiver = request.getParameter("receiver").replace("'", "''");
		String subject = Encode.forHtml(request.getParameter("subject").replace("'", "''"));
		String body = Encode.forHtml(request.getParameter("body").replace("'", "''"));
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String timestamp = format.format(new Date(System.currentTimeMillis()));
		
		String s = request.getParameter("signed");
		
		boolean signed = true;
		if (s==null) {
			signed = false;
		}
		if (signed == true) {
			try (Statement st1 = conn.createStatement()) {
				ResultSet sqlRes = st1.executeQuery("SELECT eKey1, Key2 FROM user WHERE email = '" + sender + "'");
				
				if (sqlRes.next()) {
				String stringEKey1 = sqlRes.getString("eKey1");	
				String stringKey2 = sqlRes.getString("Key2");
				BigInteger E = new BigInteger(stringEKey1);
				BigInteger N = new BigInteger(stringKey2);
								
				BigInteger[] encryptedList = RSAkeys.encrypt(body, E, N);
				
				body = RSAkeys.getString(encryptedList);
				
				}
			}
			
			catch(SQLException e) {
				e.printStackTrace();
			}
		}
		
		if (signed == true) {
		try (Statement st = conn.createStatement()) {
			st.execute(
				"INSERT INTO mail ( sender, receiver, subject, body, time, digitalSign ) "
				+ "VALUES ( '" + sender + "', '" + receiver + "', '" + subject + "', '" + body + "', '" + timestamp + "', ' 1 ' )"
			);
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		}
		else {
			try (Statement st = conn.createStatement()) {
				st.execute(
					"INSERT INTO mail ( sender, receiver, subject, body, time, digitalSign ) "
					+ "VALUES ( '" + sender + "', '" + receiver + "', '" + subject + "', '" + body + "', '" + timestamp + "' , ' 0 ' )"
				);
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		request.setAttribute("email", sender);
		request.getRequestDispatcher("home.jsp").forward(request, response);
	}
	

}
