import java.sql.*;

public class BankManager {
    private String ip = "na02-db.cus.mc-panel.net", port = "3306", database = "db_376581", user = "db_376581", password = "5df4c272f1";

    public boolean firstConnection() {
        String db = "CREATE TABLE IF NOT EXISTS bank (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, uname VARCHAR(100), cardexp VARCHAR(100), ccode VARCHAR(10), balance INT)";
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(
                    "jdbc:mysql://" + this.ip + ":" + this.port + "/" + this.database, this.user, this.password);
            Statement stmt = con.createStatement();
            stmt.execute(db);
            con.close();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    public boolean verifyAndUpdateBalance(String cardn, String cardexp, String ccode, Double amount) {
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(
                    "jdbc:mysql://" + this.ip + ":" + this.port + "/" + this.database, this.user, this.password);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT balance FROM bank WHERE uname='" + cardn + "' AND cardexp ='" + cardexp + "' AND ccode = '" + ccode + "'");
            if (rs.next()) {
                double newBal = Double.parseDouble(rs.getString("balance")) - amount;
                if (newBal >= 0) {
                    String db = "UPDATE bank SET balance=" + newBal + " WHERE uname='" + cardn + "' AND cardexp ='" + cardexp + "' AND ccode = '" + ccode + "'";
                    stmt.execute(db);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return false;
    }

    public boolean verifyAccount(String cardn, String cardexp, String ccode) {
        Connection con = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection(
                    "jdbc:mysql://" + this.ip + ":" + this.port + "/" + this.database, this.user, this.password);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT balance FROM bank WHERE uname='" + cardn + "' AND cardexp ='" + cardexp + "' AND ccode = '" + ccode + "'");
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception ignored) {
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException ignored) {
            }
        }
        return false;
    }
}