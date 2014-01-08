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
		return path;
	}

	public void createDb() throws Exception {
		Class.forName("org.h2.Driver");
		connection = DriverManager.getConnection(String.format("jdbc:h2:%s",
				getDbFile()));

		Statement statement = null;

		try {
			statement = connection.createStatement();

			try {
				 statement.execute(String.format(
						  "create table %s(id int primary key auto_increment, user_name varchar(255), title varchar(255), " +
						  "content varchar(1000), status varchar(255), reminding_time datetime, creation_time datetime)"
						  , Consts.REMINDERS_TABLE));
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

	public Long addOrupdateReminder(Reminder reminder) throws SQLException {
		Statement statement = null;

		try {
			statement = connection.createStatement();

			synchronized (lock) {

				if (reminder.getId() < 1) {
					statement
							.execute(String
									.format("insert into %s values(null, '%s', '%s', '%s', '%s', parsedatetime('%s', '%s'), parsedatetime('%s', '%s'))",
											Consts.REMINDERS_TABLE,
											reminder.getUserName(),
											Consts.replaceApostrophes(reminder.getTitle()),
											Consts.replaceApostrophes(reminder.getContent()),
											reminder.getStatusString(), 
											reminder.getDateRemindingString(),
											Consts.DATE_FORMAT,
											reminder.getCreationDateAndTimeString(),
											Consts.DATE_FORMAT));

					statement = connection.createStatement();
					ResultSet rs = statement.getGeneratedKeys();
					rs.next();

					return rs.getLong(1);
				} else {

					// update the reminder
					statement
							.execute(String
									.format("update %s set title='%s', content='%s', reminding_time=parsedatetime('%s', '%s') where id=%d",
											Consts.REMINDERS_TABLE,
											Consts.replaceApostrophes(reminder.getTitle()),
											Consts.replaceApostrophes(reminder.getContent()),
											reminder.getDateRemindingString(),
											Consts.DATE_FORMAT,
											reminder.getId()));
					return reminder.getId();
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
					"select * from %s where user_name = '%s'",
					Consts.REMINDERS_TABLE, userName));

			while (rs.next()) {
				results.add(new Reminder(rs.getLong("id"), rs
						.getString("user_name"), rs.getString("title"), rs
						.getString("content"), rs.getString("status"), 											
						rs.getTimestamp("creation_time"), rs
								.getTimestamp("reminding_time")));
			}

			return results;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	public ArrayList<Reminder> getAllReminders() throws SQLException {

		Statement statement = null;

		try {
			ArrayList<Reminder> results = new ArrayList<>();
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(String.format(
					"select * from %s", Consts.REMINDERS_TABLE));

			while (rs.next()) {
				results.add(new Reminder(rs.getLong("id"), rs
						.getString("user_name"), rs.getString("title"), rs
						.getString("content"), rs.getString("status"), 
						rs.getTimestamp("creation_time"), rs
								.getTimestamp("reminding_time")));
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
			ResultSet rs = statement
					.executeQuery(String.format("select * from %s where id=%d",
							Consts.REMINDERS_TABLE, id));
			rs.next();
			return new Reminder(id, rs.getString("user_name"),
					rs.getString("title"),
					rs.getString("content"),
					rs.getString("status"), 
					rs.getTimestamp("creation_time"),
					rs.getTimestamp("reminding_time"));
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	public void updateRemindersStatus(Consts.ReminderStatus reminderStatus,
			long id) throws SQLException {
		String status = reminderStatus.name();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(String.format("update %s set status='%s' where id=%d", Consts.REMINDERS_TABLE, status, id));
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
