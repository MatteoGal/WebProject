package servlet;

import jakarta.servlet.http.HttpServlet;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Hashing;
import util.RSAkeys;
import util.Token;
import util.Util;

/**
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/RegisterServlet")
public class RegisterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Connection conn;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterServlet() {
        super();
    }
    
    public void init() throws ServletException {
    	conn = Util.initDbConnection();
    }

 
    
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		
		String name = request.getParameter("name").replace("'", "''");
		String surname = request.getParameter("surname").replace("'", "''");
		String email = request.getParameter("email").replace("'", "''");
		String pwdToHash = request.getParameter("password").replace("'", "''");
		
		RSAkeys key = RSAkeys.getKey();
		
		BigInteger eKey1 = key.geteKey1();
		BigInteger dKey1 = key.getdKey1();
		BigInteger key2 = key.getkey2();

		
		String pwd = Hashing.getDigest(pwdToHash);
		
		try (Statement st = conn.createStatement()) {

			PreparedStatement statement = conn.prepareStatement("SELECT * FROM user WHERE email = ?");
			statement.setString(1, email);
			ResultSet sqlRes = statement.executeQuery();
			
			
			if (sqlRes.next()) {
				System.out.println("Email already registered!");
				request.getRequestDispatcher("register.html").forward(request, response);
				
			} else {				
				st.execute(
					"INSERT INTO user ( name, surname, email, password, eKey1, dKey1, Key2 ) "
					+ "VALUES ( '" + name + "', '" + surname + "', '" + email + "', '" + pwd + "', '" + eKey1 + "' , '" + dKey1 + "', '" + key2 + "' )"
				);
				
				request.setAttribute("email", email);
				request.setAttribute("password", pwd);
				
				System.out.println("Registration succeeded!");
				Token.createToken(request, response);

				request.getRequestDispatcher("home.jsp").forward(request, response);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			e.getCause();
			request.getRequestDispatcher("register.html").forward(request, response);
		}
	}

}
