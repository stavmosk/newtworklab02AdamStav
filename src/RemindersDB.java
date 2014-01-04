import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class RemindersDB extends DBManager {

	public RemindersDB(String path) {
		super(path);
	}

	String getDbFile() {
		//return "C:\\Users\\user\\Downloads\\reminder1.data";
		return "C:\\Users\\user\\workspaceHTML\\newtworklab02AdamStav\\"+ path;
	}

	public void createDb() throws Exception {
		Class.forName("org.h2.Driver");
		connection = DriverManager.getConnection(String.format("jdbc:h2:%s",
				getDbFile()));

		Statement statement = null;

		try {
			statement = connection.createStatement();

			try {
				/*
				statement.execute(String.format("create table %s(id int primary key auto_increment, user_name varchar(255), title varchar(255), "
										+ "content varchar(1000), reminder varchar(1000), creation_time datetime)",
										Consts.REMINDERS_TABLE));*/
				statement.execute(String.format("create table %s(id int primary key auto_increment, user_name varchar(255), title varchar(255), "
						+ "content varchar(1000), reminding_time datetime, creation_time datetime)",
						Consts.REMINDERS_TABLE));
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

	public void addOrupdateReminder(Reminder reminder) throws SQLException {
		Statement statement = null;

		try {
			statement = connection.createStatement();

			synchronized (lock) {

				// Create new reminder
				if (reminder.getId() < 1) {
					statement
							.execute(String
									//.format("insert into %s values(null, '%s', '%s', '%s', '%s', parsedatetime('%s', '%s'))",
									.format("insert into %s values(null, '%s', '%s', '%s', parsedatetime('%s', '%s'), parsedatetime('%s', '%s'))",
											Consts.REMINDERS_TABLE,
											reminder.getUserName(),
											reminder.getTitle(),
											reminder.getContent(),
											reminder.getDateRemindingString(),
											Consts.DATE_FORMAT,
											reminder.getCreationDateAndTimeString(),
											Consts.DATE_FORMAT));
				} else {

					// update the reminder
					statement
							.execute(String
									//.format("update %s set title='%s', content='%s', reminder='%s' where id=%d",
									.format("update %s set title='%s', content='%s', reminding_time=parsedatetime('%s', '%s') where id=%d",
											Consts.REMINDERS_TABLE,
											reminder.getTitle(),
											reminder.getContent(),
											reminder.getDateRemindingString(),
											Consts.DATE_FORMAT,
											reminder.getId()));
				}
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	
	public ArrayList<Reminder> getReminders(String userName)
			throws SQLException {

		Statement statement = null;

		try {
			ArrayList<Reminder> results = new ArrayList<>();
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(String.format(
					"select * from %s where user_name = '%s'", Consts.REMINDERS_TABLE,
					userName));

			while (rs.next()) {
				results.add(new Reminder(rs.getLong("id"), rs
						.getString("user_name"), rs.getString("title"), rs
						.getString("content"),
						rs.getTimestamp("creation_time"), 
						//rs.getString("reminder")));
						rs.getTimestamp("reminding_time")));

			}

			return results;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	
	public ArrayList<Reminder> getRemindersBeforeCurrentTime()
			throws SQLException {

		Statement statement = null;

		try {
			ArrayList<Reminder> results = new ArrayList<>();
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(String.format(
					"select * from %s where reminding_time <= curdate()'", Consts.REMINDERS_TABLE));

			while (rs.next()) {
				results.add(new Reminder(rs.getLong("id"), rs
						.getString("user_name"), rs.getString("title"), rs
						.getString("content"),
						rs.getTimestamp("creation_time"),
						//rs.getString("reminder")));
						rs.getTimestamp("reminding_time")));
			}

			return results;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	public Reminder getReminderById(long id) throws SQLException {

		Statement statement = null;

		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(String.format(
					"select * from %s where id=%d", Consts.REMINDERS_TABLE, id));
			rs.next();
			return new Reminder(id, rs.getString("user_name"),
					rs.getString("title"), rs.getString("content"),
					rs.getTimestamp("creation_time"), 
					//rs.getString("reminder"));
					rs.getTimestamp("reminding_time"));
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}


    public void deleteById(long id) throws SQLException {
    	deleteById(id, Consts.REMINDERS_TABLE);
    }
}
