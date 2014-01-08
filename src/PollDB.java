import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class PollDB extends DBManager {

	public PollDB(String path) {
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
				statement
						.execute(String
								.format("create table %s(id int primary key auto_increment, user_name varchar(255), title varchar(255), "
										+ "content varchar(1000), recipient varchar(1000), answers varchar(1000), recipientsReplies varchar(1000), "
										+ "status varchar(255), creation_time datetime, "
										+ "due_time varchar(255))",
										Consts.POLLS_TABLE));
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

	public Long createPoll(Poll poll) throws SQLException {
		Statement statement = null;

		try {
			statement = connection.createStatement();

			synchronized (lock) {
				statement
						.execute(String
								.format("insert into %s values(null, '%s', '%s', '%s', '%s', '%s' , '%s' , '%s', parsedatetime('%s', '%s'), '%s')",
										Consts.POLLS_TABLE, poll.getUserName(),
										Consts.replaceApostrophes(poll.getTitle()), Consts.replaceApostrophes(poll.getContent()),
										poll.getRecipients(),
										Consts.replaceApostrophes(poll.getAnswers()),
										Consts.replaceApostrophes(poll.getRecipientsReplies()),
										poll.getStatusString(),
										poll.getCreationDateAndTimeString(),
										Consts.DATE_FORMAT,
										poll.getCreationDateAndTimeString()));
				
				statement = connection.createStatement();
				ResultSet rs = statement.getGeneratedKeys();
				rs.next();

				return rs.getLong(1);
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	public void deleteById(long id) throws SQLException {
		deleteById(id, Consts.POLLS_TABLE);
	}

	public ArrayList<Poll> getPolls(String userName) throws SQLException {
		Statement statement = null;

		try {
			ArrayList<Poll> polls = new ArrayList<>();
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(String.format(
					"select * from %s where user_name = '%s'",
					Consts.POLLS_TABLE, userName));

			while (rs.next()) {
				Poll p = new Poll(rs.getLong("id"), rs.getString("user_name"),
						rs.getString("title"), rs.getString("content"),
						rs.getNString("recipient"), rs.getNString("answers"),
						rs.getNString("recipientsReplies"),
						rs.getString("status"),
						rs.getTimestamp("creation_time"),
						rs.getString("due_time"));
				polls.add(p);
			}
			return polls;
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	

	public void updatePoll(Consts.PollStatus pollStatus, long id)
			throws SQLException {
		String status = pollStatus.name();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(String.format(
					"update %s set status='%s' where id=%d",
					Consts.POLLS_TABLE, status, id));
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}
	
	public void updatePollAnswers(Consts.PollStatus pollStatus, String recipientsReplies, long id)
			throws SQLException {
		String status = pollStatus.name();
		Statement statement = null;
		try {
			statement = connection.createStatement();
			statement.execute(String.format(
					"update %s set status='%s', recipientsReplies='%s' where id=%d",
					Consts.POLLS_TABLE, status, recipientsReplies, id));
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

	public Poll getPollById(long id) throws SQLException {
		Statement statement = null;
		try {
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(String.format(
					"select * from %s where id=%d", Consts.POLLS_TABLE, id));
			if (rs.next()) {
				return new Poll(rs.getLong("id"), rs.getString("user_name"),
						rs.getString("title"), rs.getString("content"),
						rs.getNString("recipient"), rs.getNString("answers"),
						rs.getNString("recipientsReplies"),
						rs.getString("status"),
						rs.getTimestamp("creation_time"),
						rs.getString("due_time"));
			} else {
				return null;
			}
		} finally {
			if (statement != null) {
				statement.close();
			}
		}
	}

}
