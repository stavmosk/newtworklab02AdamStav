import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class PollDB extends DBManager {

	String getDbFile() {
		return "C:\\Users\\stavmosk\\Downloads\\poll3.data";
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
										+ "due_time varchar(255))", Consts.POLLS_TABLE));
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


	public void createPoll(Poll poll) throws SQLException {
		Statement statement = null;

		try {
			statement = connection.createStatement();

			synchronized (lock) {
				statement
						.execute(String
								.format("insert into %s values(null, '%s', '%s', '%s', '%s', '%s' , '%s' , '%s', parsedatetime('%s', '%s'), '%s')",
										Consts.POLLS_TABLE, 
										poll.getUserName(),
										poll.getTitle(),
										poll.getContent(),
										poll.getRecipients(),
										poll.getAnswers(),
										poll.getRecipientsReplies(),
										poll.getStatusString(),
										poll.getCreationDateAndTimeString(),
										Consts.DATE_FORMAT,
										poll.getCreationDateAndTimeString()));

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
					"select * from %s where user_name = '%s'", Consts.POLLS_TABLE,
					userName));

			while (rs.next()) {
				Poll p = new Poll(rs.getLong("id"), rs
						.getString("user_name"), rs.getString("title"), rs
						.getString("content"), rs.getNString("recipient") , rs.getNString("answers") 
						, rs.getNString("recipientsReplies") ,rs
						.getString("status"), rs.getTimestamp("creation_time"),
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
}
