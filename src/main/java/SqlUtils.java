import java.sql.*;

public class SqlUtils {

	private static String url = "jdbc:mysql://172.16.0.105:3306";
	
	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static Connection getConnnection(String user,String password){
		try {
			return DriverManager.getConnection(SqlUtils.url, user, password);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void closeConnection(Connection conn) throws SQLException{
		if(conn!=null){
			conn.close();			
		}
	}
	
	public static ResultSet getResultSet(Connection conn ,String sql){
		try {
			Statement stat = conn.createStatement();
			return stat.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
