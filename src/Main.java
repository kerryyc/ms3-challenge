import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
	static final String CSVPATH = "";
	
	public static void main (String[] args) {
		// use default path if the path is not specified in argument
		String filename = "";
		if (args.length != 1)
			filename = CSVPATH;
		else	
			filename = args[0];
		
		ParseIntoDB p = new ParseIntoDB(filename);
		p.execute();
		
		// testing database 
//		try {
//			Statement s = p.getConnection().createStatement();
//			ResultSet rs = s.executeQuery("SELECT * FROM X");
//			ResultSetMetaData rsmd = rs.getMetaData();
//			int columnsNumber = rsmd.getColumnCount();
//			while (rs.next()) {
//				for(int i = 1 ; i <= columnsNumber; i++){
//				      System.out.print(rs.getString(i) + " "); 
//				}
//				System.out.println();
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
	}
}