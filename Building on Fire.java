import java.io.*;
import java.sql.*;

public class HW2 {

	private static final int BUILDING = 0;
	private static final int FIRE_HYDRANT = 1;
	private static final int FIRE_BUILDING = 2;
	private static final String URL = "jdbc:oracle:thin:localhost:1521:orcl";
	private static final String ID = "scott";
	private static final String PASSWORD = "tiger";

	public HW2() {
	}

	public static Connection connectDB() {
		try {
			System.out.println("Oracle JDBA Driver...Loading...");
			DriverManager.registerDriver(new oracle.jdbc.OracleDriver());
			System.out.println("Connecting to DB");
			Connection conn = DriverManager.getConnection(URL, ID, PASSWORD);
			System.out.println("Connection Successful!!");
			return conn;
		} catch (Exception e) {
			System.out.println(e.toString());
			System.exit(-1);
			return null;
		}
	}

	public static void main(String[] args) {
		try {
			Connection conn = connectDB();
			if (args[0].equalsIgnoreCase("window")) {
				window(args, conn);
			} else if (args[0].equalsIgnoreCase("within")) {
				within(args, conn);
			} else if (args[0].equals("nn")) {
				nn(args, conn);
			} else if (args[0].equals("demo")) {
				demos(args, conn);
			} else {
				System.out.println("Please enter a valid command!");
				return;
			}
			return;
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public static void window_q(int obj_type, float x1, float y1, float x2,
			float y2, Connection conn) throws Exception {
		String window_query = "";
		if (obj_type == BUILDING) {
			window_query = "SELECT B.B_ID"
					+ "FROM BUILDING B"
					+ "WHERE B.B_NAME NOT IN (SELECT F_NAME FROM FIRE_BUILDING) "
					+ "AND SDO_INSIDE(B.B_SHAPE, SDO_GEOMETRY(2003,NULL,NULL, SDO_ELEM_INFO_ARRAY(1,1003,3), SDO_ORDINATE_ARRAY("
					+ x1 + "," + y1 +

					"," + x2 + "," + y2 + ")))='TRUE'";
		} else if (obj_type == FIRE_HYDRANT) {
			window_query = "SELECT F.F_ID FROM FIRE_HYDRANT F WHERE SDO_INSIDE(F.F_SHAPE,"
					+ "SDO_GEOMETRY(2003,NULL,NULL, SDO_ELEM_INFO_ARRAY(1,1003,3),SDO_ORDINATE_ARRAY("
					+ x1 + "," + y1 + "," + x2 + "," + y2 + ")))='TRUE'";
		} else {
			window_query = "SELECT B.B_ID FROM BUILDING B, FIRE_BUILDING F WHERE B.B_NAME=F.F_NAME "
					+ "AND SDO_INSIDE(B.B_SHAPE, SDO_GEOMETRY(2003,NULL,NULL, SDO_ELEM_INFO_ARRAY(1,1003,3),SDO_ORDINATE_ARRAY("
					+ x1 + "," + y1 + "," + x2 + "," + y2 + ")))='TRUE'";
		}
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(window_query);
		String result = "";
		while (rs.next()) {
			result += (rs.getObject(1) + "\n");
		}
		System.out.println("Output:");
		if (result.equals("")) {
			System.out.println("No rows found!!!");
		} else {
			System.out.println(result);
		}
	}

	public static void window(String[] args, Connection conn)
			throws NumberFormatException, Exception {
		int obj_type = -1;
		if (args.length != 6) {
			System.out.println("Number of arguments is more!!!");
		}
		if (args[1].equalsIgnoreCase("building")) {
			obj_type = BUILDING;
		} else if (args[1].equalsIgnoreCase("firehydrant")) {
			obj_type = FIRE_HYDRANT;
		} else if (args[1].equalsIgnoreCase("firebuilding")) {
			obj_type = FIRE_BUILDING;
		} else {
			System.out.println("Invalid argument for obj_type");
			return;
		}
		window_q(obj_type, Float.parseFloat(args[2]),
				Float.parseFloat(args[3]), Float.parseFloat(args[4]),
				Float.parseFloat(args[5]), conn);
	}

	public static void within_q(int obj_type, String B_NAME, float d,
			Connection conn) throws Exception {
		String within_query = "";
		if (obj_type == BUILDING) {
			within_query = "SELECT B1.B_ID FROM BUILDING B1,BUILDING B2 WHERE B2.B_NAME='"
					+ B_NAME
					+ "' AND B1.B_ID!=B2.B_ID"
					+ "AND B1.B_NAME NOT IN (SELECT F_NAME FROM FIRE_BUILDING)"
					+ "AND SDO_WITHIN_DISTANCE(B1.B_SHAPE,B2.B_SHAPE,"
					+ "'DISTANCE=" + d + "')='TRUE'";
		} else if (obj_type == FIRE_HYDRANT) {
			within_query = "SELECT F.F_ID FROM FIRE_HYDRANT F,BUILDING B WHERE B.B_NAME='"
					+ B_NAME
					+ "' AND  SDO_WITHIN_DISTANCE(F.F_SHAPE, B.B_SHAPE,'DISTANCE="
					+ d + "')='TRUE'";
		} else if (obj_type == FIRE_BUILDING) {
			within_query = "SELECT B1.B_ID FROM BUILDING B1,BUILDING B2, FIRE_BUILDING F WHERE B1.B_NAME=F.F_NAME AND B2.B_NAME='"
					+ B_NAME
					+ "' AND SDO_WITHIN_DISTANCE(B2.B_SHAPE, B1.B_SHAPE,'DISTANCE="
					+ d + "')='TRUE' AND B1.B_ID!=B2.B_ID";

		}
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(within_query);
		String result = "";
		while (rs.next()) {
			result += (rs.getObject(1) + "\n");
		}
		System.out.println("Output: ");
		if (result.equals("")) {
			System.out.println("No rows found.");
		} else {
			System.out.println(result);
		}
	}

	public static void within(String[] args, Connection conn)
			throws NumberFormatException, Exception {
		int obj_type = -1;
		if (args.length != 4) {
			System.out.println("Number of arguments is more!!!");
		}
		if (args[1].equalsIgnoreCase("building")) {
			obj_type = BUILDING;
		} else if (args[1].equalsIgnoreCase("firehydrant")) {
			obj_type = FIRE_HYDRANT;
		} else if (args[1].equalsIgnoreCase("firebuilding")) {
			obj_type = FIRE_BUILDING;
		} else {
			System.out.println("Invalid argument for obj_type!!!");
			return;
		}
		within_q(obj_type, args[2].toString(), Integer.parseInt(args[3]), conn);
	}

	public static void nn_q(int obj_type, String B_ID, int num, Connection conn)
			throws Exception {
		String nearest_query = "";
		if (obj_type == BUILDING) {
			nearest_query = "SELECT B1.B_ID FROM BUILDING B1,BUILDING B2 WHERE B2.B_ID='"
					+ B_ID
					+ "' AND B1.B_NAME NOT IN (SELECT F_NAME FROM FIRE_BUILDING) AND B1.B_ID!=B2.B_ID "
					+ "AND SDO_NN(B1.B_SHAPE,B2.B_SHAPE,'SDO_NUM_RES="
					+ (num + 1) + "')='TRUE'";

		} else if (obj_type == FIRE_HYDRANT) {
			nearest_query = "SELECT F.F_ID FROM BUILDING B,FIRE_HYDRANT F WHERE B.B_ID='"
					+ B_ID
					+ "' AND SDO_NN(F.F_SHAPE, B.B_SHAPE,'SDO_NUM_RES="
					+ num + "')='TRUE'";

		} else if (obj_type == FIRE_BUILDING) {
			nearest_query = "SELECT TEMP.B_ID FROM (SELECT B1.B_ID, SDO_NN_DISTANCE(1) DIST FROM BUILDING B2,"
					+ "BUILDING B1  WHERE B2.B_ID='"
					+ B_ID
					+ "' AND B1.B_ID!=B2.B_ID AND B1.B_NAME IN (SELECT F_NAME FROM FIRE_BUILDING) "
					+ "AND SDO_NN(B1.B_SHAPE, B2.B_SHAPE,1)='TRUE' ORDER BY DIST ) TEMP WHERE ROWNUM<="
					+ num;
		}
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(nearest_query);
		String result = "";
		while (rs.next()) {
			result += (rs.getObject(1) + "\n");
		}
		System.out.println("Output: ");
		if (result.equals("")) {
			System.out.println("No rows found!!!");
		} else {
			System.out.println(result);
		}
	}

	public static void nn(String[] args, Connection conn)
			throws NumberFormatException, Exception {
		int obj_type = -1;
		if (args.length != 4) {
			System.out.println("Number of arguments is more!!!");
		}
		if (args[1].equalsIgnoreCase("building")) {
			obj_type = BUILDING;
		} else if (args[1].equalsIgnoreCase("firehydrant")) {
			obj_type = FIRE_HYDRANT;
		} else if (args[1].equalsIgnoreCase("firebuilding")) {
			obj_type = FIRE_BUILDING;
		} else {
			System.out.println("Invalid argument for obj_type!!!");
			return;
		}
		nn_q(obj_type, args[2].toString(), Integer.parseInt(args[3]), conn);
	}

	public static void demo1(Connection conn) throws SQLException {
		String demo1_query = "";
		demo1_query = "select DISTINCT  b.B_NAME from building b where b.B_NAME like 'S%' and b.B_NAME not in (select f.F_NAME from FIRE_BUILDING f)";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(demo1_query);
		String result = "";
		while (rs.next()) {
			result += (rs.getObject(1) + "\n");
		}
		System.out.println("Output of Demo 1: ");
		if (result.equals("")) {
			System.out.println("No rows found!!!");
		} else {
			System.out.println(result);
		}
	}

	public static void demo2(Connection conn) throws SQLException {
		String demo2_query = "";
		demo2_query = "SELECT B.B_NAME, F.F_ID FROM BUILDING B, FIRE_HYDRANT F, FIRE_BUILDING FB WHERE B.B_NAME=FB.F_NAME AND SDO_NN(F.F_SHAPE, B.B_SHAPE,'SDO_NUM_RES=5')='TRUE'";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(demo2_query);
		String result = "";
		while (rs.next()) {
			result += (rs.getObject(1) + " " + rs.getObject(2) + "\n");
		}
		System.out.println("Output of Demo 2: ");
		if (result.equals("")) {
			System.out.println("No rows found!!!");
		} else {
			System.out.println(result);
		}
	}

	public static void demo3(Connection conn) throws SQLException {
		String demo3_query = "";
		demo3_query = "SELECT F.F_ID FROM FIRE_HYDRANT F,BUILDING B WHERE SDO_WITHIN_DISTANCE(F.F_SHAPE, B.B_SHAPE,'DISTANCE=120')='TRUE' group by F.F_ID having count(*)= (SELECT max(count(*)) FROM FIRE_HYDRANT F,BUILDING B  WHERE SDO_WITHIN_DISTANCE(F.F_SHAPE, B.B_SHAPE,'DISTANCE=120')='TRUE' group by F.F_ID)";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(demo3_query);
		String result = "";
		while (rs.next()) {
			result += (rs.getObject(1) + "\n");
		}
		System.out.println("Output of Demo 3:");
		if (result.equals("")) {
			System.out.println("No rows found!!!");
		} else {
			System.out.println(result);
		}
	}

	public static void demo4(Connection conn) throws SQLException {
		String demo4_query = "";
		demo4_query = "select * from (SELECT  f.F_ID, count(*) FROM BUILDING B, FIRE_HYDRANT F WHERE SDO_NN( F.F_SHAPE, b.B_SHAPE, 'SDO_NUM_RES=1')='TRUE'group by f.F_ID order by count(*) desc) where rownum<=5";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(demo4_query);
		String result = "";
		while (rs.next()) {
			result += (rs.getObject(1) + " " + rs.getObject(2) + "\n");
		}
		System.out.println("Output of Query 4:");
		if (result.equals("")) {
			System.out.println("No rows found!!!");
		} else {
			System.out.println(result);
		}
	}

	public static void demo5(Connection conn) throws SQLException {
		String demo5_query = "";
		demo5_query = "SELECT SDO_AGGR_MBR(b.B_SHAPE).get_wkt() AS clobCol FROM building b WHERE b.B_NAME like '%HE'";
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery(demo5_query);
		while (rs.next()) {
			StringBuffer strOut = new StringBuffer();
			String aux;
			try {
				BufferedReader br = new BufferedReader(rs.getClob("clobCol")
						.getCharacterStream());
				while ((aux = br.readLine()) != null) {
					strOut.append(aux);
					strOut.append(System.getProperty("line.separator"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			String clobStr = strOut.toString();
			String[] parts = clobStr.split(",");
			String temp = parts[0];
			System.out.println("Lower Left: "
					+ temp.substring(10, temp.length()));
			System.out.println("Upper Right: " + parts[2]);
		}
	}

	public static void demos(String[] args, Connection conn)
			throws NumberFormatException, Exception {
		int obj_type = Integer.parseInt(args[1]);
		if (obj_type == 1) {
			demo1(conn);
		} else if (obj_type == 2) {
			demo2(conn);
		} else if (obj_type == 3) {
			demo3(conn);
		} else if (obj_type == 4) {
			demo4(conn);
		} else if (obj_type == 5) {
			demo5(conn);
		} else {
			System.out.println("Invalid argument for obj_type!!!");
			return;
		}
	}
}