import java.sql.*;


public class DBManager {
    final int TABLE_EXISTS_ERROR_CODE = 42101;
    protected final Object lock = new Object();
    protected Connection connection;
    protected String path;
    
    public DBManager(String path) { 
    	this.path = path;
    }

    protected void deleteById(long id, String tableName) throws SQLException {
        Statement statement = null;

        try {
            statement = connection.createStatement();
            synchronized (lock) {
                statement.execute(String.format("delete from %s where id=%d", tableName, id));
            }
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public void closeDbConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
}
