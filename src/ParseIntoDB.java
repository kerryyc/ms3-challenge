import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.CSVReader;

public class ParseIntoDB {
	// class variables
	String fn = "";
	Connection conn = null;
	
	// SQL Statements
	String table = "CREATE TABLE IF NOT EXISTS X (\n"
            + "	id integer PRIMARY KEY NOT NULL,\n"
            + "	A VARCHAR NOT NULL,\n"
            + "	B VARCHAR NOT NULL, \n"
            + " C VARCHAR NOT NULL, \n"
            + " D VARCHAR NOT NULL, \n"
            + " E VARCHAR NOT NULL, \n"
            + " F VARCHAR NOT NULL, \n"
            + " G FLOAT NOT NULL, \n"
            + " H BINARY NOT NULL, \n"
            + " I BINARY NOT NULL, \n"
            + " J BINARY NOT NULL"
            + ");";
	String insert = "INSERT INTO X VALUES(";
	
	public ParseIntoDB(String filename) {
		fn = filename;
		
		//intialize database
		try {
			conn = DriverManager.getConnection("jdbc:sqlite::memory:");
		    if (conn != null) {
		    	Statement smt = conn.createStatement();
		        smt.execute(table);
		    }
		
		} catch (SQLException e) {
		    System.out.println(e.getMessage());
		}
	}
	
	public void execute() {
		// reads csv file, adds successful lines to database, and writes log and bad record files
		
		CSVReader reader = null;
		Statement smt = null;
		int counter = 1;
		String insertStr = "";
		
		int fail = 0;
		ArrayList<String[]> failLines = new ArrayList<String[]>();
		
        try {
            reader = new CSVReaderBuilder(new FileReader(fn)).build();
            reader.readNext(); // skip first line that contains only headers
            smt = conn.createStatement();
            String [] nextLine;
            
            while ((nextLine = reader.readNext()) != null) {
            	// create insert string
            	insertStr = createInsertStr(nextLine, counter);
                
            	// if insert string was created successfully, add it to SQLite database
                if (insertStr.length() > 0) {
                	insertStr = insert + insertStr.substring(0, insertStr.length()-1) + ")";
                	smt.executeUpdate(insertStr);
                }
                // otherwise, add failed line to ArrayList
                else {
                	failLines.add(nextLine);
                	fail++;
                }
                counter++;
            }
            
            reader.close();
        }
        catch (SQLException e) {
        	System.out.println(insertStr);
		    System.out.println(e.getMessage());
		}
        catch (Exception e) {
            e.printStackTrace();
        }
        
        //write bad lines to csv file
        writeBadRecords(failLines);
        
        //write log file
        writeLogFile(counter, fail);
	}
	
	public Connection getConnection() {
		return conn;
	}
	
	private String createInsertStr(String[] nextLine, int counter) {
		//create string used to insert into SQLite database
		
		// if number of columns does not match, return empty string
		if (nextLine.length != 10) 
			return "";
		
		String insertStr = "" + counter + ",";
		
        for(String token : nextLine) {
        	// if token is empty, return empty string
        	if (token.length() < 1)
        		return "";
        	
        	// insert elements with "," with double quotes
        	else if (token.contains(","))
        		insertStr += "\"" + token + "\",";
        	
        	// insert money
        	else if (token.contains("$"))
        		insertStr += token.substring(1) + ",";
        	
        	// insert boolean values
        	else if (token == "TRUE")
        		insertStr += true + ",";
        	else if (token == "False")
        		insertStr += false + ",";
        	
        	// fix apostrophe in string
        	else if (token.contains("\'"))
        		insertStr += "\'" + token.replace("\'", "\'\'") + "\',";
        	
        	// otherwise, insert string
        	else
        		insertStr += "\'" + token + "\',";
        }
        
        return insertStr;
	}
	
	private void writeBadRecords(ArrayList<String[]> lines) {
		// write all bad records to csv file
		
		Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());
        String path = ".\\bad-data-" + ts.toString().replaceAll(" ", "").replaceAll(":", "-") + ".csv";
        CSVWriter cw = null;
        
        try {
        	Writer writer = Files.newBufferedWriter(Paths.get(path));
			cw = new CSVWriter(writer);
			String[] header = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
			cw.writeNext(header);
			
			for (String[] s : lines) {
				cw.writeNext(s);
			}
			
			cw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void writeLogFile(int total, int fail) {
		// write statistics to log file 
		
		String path = ".\\log.txt";
		File file = new File(path);
		try {
			FileWriter writer = new FileWriter(file);
			writer.write("# of Records Received: " + total + "\n");
			writer.write("# of Records Successful: " + (total - fail) + "\n");
			writer.write("# of Records Failed: " + fail + "\n");
			writer.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
