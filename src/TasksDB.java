import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class TasksDB extends DBManager {


	String getDbFile() {
		return "C:\\Users\\stavmosk\\Downloads\\task2.data";
	}
	
	public void createDb() throws Exception {
		Class.forName("org.h2.Driver");
		connection = DriverManager.getConnection(String.format("jdbc:h2:%s",
				getDbFile()));

		Statement statement = null;

		try {
			statement = connection.createStatement();

			try {
				statement
						.execute(String
								.format("create table %s(id int primary key auto_increment, user_name varchar(255), "
										+ "title varchar(255), content varchar(1000), recipient varchar(255), "
										+ "status varchar(255), creation_time datetime, due_time varchar(255))",
										Consts.TASKS_TABLE));
			} catch (SQLException e) {
				if (e.getErrorCode() == TABLE_EXISTS_ERROR_CODE) {
				} else {
					throw e;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	public void createTask(Task task) throws SQLException {
		Statement statement = null;

		try {
			statement = connection.createStatement();

			synchronized (lock) {
				statement
						.execute(String
								.format("insert into %s values(null, '%s', '%s', '%s', '%s', '%s' , parsedatetime('%s', '%s'), '%s')",
										Consts.TASKS_TABLE, task.getUserName(),
										task.getTitle(),
										task.getContent(),
										task.getRecipient(),
										task.getStatusString(),
										task.getCreationDateAndTimeString(),
										Consts.DATE_FORMAT,
										task.getDueDate()));

			}
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	public void deleteById(long id) throws SQLException {
		 deleteById(id, Consts.TASKS_TABLE);
	}

	public ArrayList<Task> getTasks(String userName) throws SQLException {
		Statement statement = null;

		try {
			ArrayList<Task> results = new ArrayList<>();
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(String.format(
					"select * from %s where user_name = '%s'", Consts.TASKS_TABLE,
					userName));

			while (rs.next()) {
				results.add(new Task(rs.getLong("id"), rs
						.getString("user_name"), rs.getString("title"), rs
						.getString("content"), rs.getNString("recipient"), rs
						.getString("status"), rs.getTimestamp("creation_time"),
						rs.getString("due_time")));

			}

			return results;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}

	}
}
