import java.sql.SQLException;
import java.util.ArrayList;

public class HtmlBuilder<T extends Job> {
	private String[] remindersTitle = { "Title", "Creation Date",
			"Reminder Date" };
	private String[] taskTitle = { "Title", "Creation Date", "task Date",
			"Status" };
	private String[] pollTitle = { "Title", "Creation Date",
			"Recipients replies" };

	public String buildTable(ArrayList<T> table, String pageName)
			throws SQLException {
		StringBuilder builder = new StringBuilder();

		builder.append("<html><head><title>" + pageName + "</title></head>");
		builder.append(Consts.CRLF);
		builder.append("<body>");
		builder.append(Consts.CRLF);
		builder.append("<table border='1'>");
		builder.append(Consts.CRLF);

		String jobName = pageName.split("\\.")[0].substring(1);
		if (table == null || table.size() == 0) {
			builder.append("No " + jobName + "s added yet. please enter new "
					+ jobName);
		} else {

			builder.append("</tr>");
			builder.append(buildTitles(table.get(0), pageName));
			builder.append("</tr>");

			// The rows.
			builder.append("</tr>");
			builder.append(Consts.CRLF);
			for (int i = 0; i < table.size(); i++) {
				builder.append("<tr>");
				builder.append(buildTableRow(table.get(i)));
				builder.append("</tr>");
				builder.append(Consts.CRLF);
			}

		}
		builder.append(Consts.CRLF);
		builder.append("</table>");
		builder.append(buildNewButton(jobName));
		builder.append(buildHomeButton());
		builder.append(Consts.CRLF);
		builder.append("</body></html>");
		String content = builder.toString();
		return content;
	}

	private String buildNewButton(String jobName) {
		String newPage = null;
		if (jobName.equals("reminders")) {
			newPage = Consts.REMINDERS_EDITOR;
		} else if (jobName.equals("tasks")) {
			newPage = Consts.TASK_EDITOR;
		} else if (jobName.equals("polls")) {
			newPage = Consts.POLL_EDITOR;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("<form name=\"New\" method=\"GET\" action=\"" + newPage
				+ "\">");
		builder.append("<input type=\"submit\" value=\"New\">");
		builder.append("</form>");
		return builder.toString();
	}

	private String buildHomeButton() {
		StringBuilder builder = new StringBuilder();
		builder.append("<form name=\"Home\" method=\"GET\" action=\"main.html\">");
		builder.append("<input type=\"submit\" value=\"Home\">");
		builder.append("</form>");
		return builder.toString();
	}

	private String buildTitles(T job, String pageName) {

		String jobName = pageName.split("\\.")[0].substring(1);
		if (job == null) {
			return "No " + jobName + "s added yet. please enter new " + jobName;
		}
		String[] title = null;
		if (job instanceof Reminder) {
			title = remindersTitle;
		} else if (job instanceof Task) {
			title = taskTitle;
		} else if (job instanceof Poll) {
			title = pollTitle;
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < title.length; i++) {
			builder.append("<td>");
			builder.append(title[i]);
			builder.append("</td>");
			builder.append(Consts.CRLF);
		}

		return builder.toString();
	}

	private String buildTableRow(T job) {
		if (job instanceof Reminder) {
			return reminderBuildTableRow((Reminder) job);
		} else if (job instanceof Task) {
			return taskBuildTableRow((Task) job);
		} else if (job instanceof Poll) {
			return pollBuildTableRow((Poll) job);
		}
		return "";
	}

	private String reminderBuildTableRow(Reminder reminder) {
		StringBuilder builder = new StringBuilder();

		builder.append("<td>");
		builder.append(reminder.getTitle());
		builder.append("</td>");
		builder.append("<td>");
		builder.append(reminder.getCreationDateAndTimeString());
		builder.append("</td>");
		builder.append("<td>");
		builder.append(reminder.getDateRemindingString());
		builder.append("</td>");
		builder.append("<td>");
		builder.append("<form name=\"Edit\" method=\"GET\" action=\""
				+ Consts.REMINDERS_EDITOR + "\">");
		builder.append("<input type=\"hidden\" name=\"id\" value=\""
				+ reminder.getId() + "\" />");
		builder.append("<input type=\"submit\" value=\"Edit\">");
		builder.append("</form>");
		builder.append("</td>");
		builder.append("<td>");
		builder.append("<form name=\"Delete\" method=\"GET\" action=\""
				+ Consts.REMINDERS_PAGE + "\">");
		builder.append("<input type=\"hidden\" name=\"id\" value=\""
				+ reminder.getId() + "\" />");
		builder.append("<input type=\"submit\" value=\"Delete\">");
		builder.append("</form>");
		builder.append("</td>");

		return builder.toString();

	}

	private String taskBuildTableRow(Task task) {
		StringBuilder builder = new StringBuilder();
		builder.append("<td>");
		builder.append(task.getTitle());
		builder.append("</td>");
		builder.append("<td>");
		builder.append(task.getCreationDateAndTimeString());
		builder.append("</td>");
		builder.append("<td>");
		builder.append(task.getDueDate());
		builder.append("</td>");
		builder.append("<td>");
		builder.append(task.getStatusString());
		builder.append("</td>");
		builder.append("<td>");
		if (task.getStatus() == Consts.TaskStatus.IN_PROGRESS) { 
		builder.append("<form name=\"Delete\" method=\"GET\" action=\""
				+ Consts.TASKS_PAGE + "\">");
		builder.append("<input type=\"hidden\" name=\"id\" value=\""
				+ task.getId() + "\" />");
		builder.append("<input type=\"submit\" value=\"Delete\">");
		builder.append("</form>");
		}
		builder.append("</td>");

		return builder.toString();
	}

	private String pollBuildTableRow(Poll poll) {
		StringBuilder builder = new StringBuilder();

		builder.append("<td>");
		builder.append(poll.getTitle());
		builder.append("</td>");
		builder.append("<td>");
		builder.append(poll.getCreationDateAndTimeString());
		builder.append("</td>");
		builder.append("<td>");
		builder.append(poll.getRecipientsReplies());
		builder.append("</td>");
		builder.append("<td>");
		builder.append("<form name=\"Delete\" method=\"GET\" action=\""
				+ Consts.POLLS_PAGE + "\">");
		builder.append("<input type=\"hidden\" name=\"id\" value=\""
				+ poll.getId() + "\" />");
		builder.append("<input type=\"submit\" value=\"Delete\">");
		builder.append("</form>");
		builder.append("</td>");

		return builder.toString();
	}

	public String buildEditor(String pageName, Reminder reminder) {

		String subject = "";
		String content = "";
		String date = "";
		String time = "";
		long id = -1;
		if (reminder != null) {
			id = reminder.getId();
			subject = reminder.getTitle();
			content = reminder.getContent();
			date = reminder.getDateRemindingString();
			time = reminder.getDateRemindingString();
		}
		
		StringBuilder builder = new StringBuilder();

		builder.append("<html><head><title>" + pageName + "</title></head>");
		builder.append(Consts.CRLF);
		builder.append("<body>");
		builder.append(Consts.CRLF);
		builder.append("<form name=\"ReminderEdit\" method=\"POST\" "
				+ "action=\"submit_reminder.html\">");
		builder.append("<input type=\"hidden\" name=\"id\" value=\""
				+ id + "\" />");

		builder.append("subject: <input type=\"text\" name=\""+ Consts.TITLE +"\" value=\"" + subject +
				"\"><br>");
		builder.append("content: <textarea cols=\"40\" rows=\"5\" name=\"" + Consts.CONTENT +
				"\">"  + content);
		builder.append("</textarea><br>");
		builder.append("date(Format DD-MM-YYYY): <input type=\"text\" name=\"" + Consts.REMINDING_DATE + "\" value=\"" + date+
				"\"><br>");
		builder.append("time(Format HH:MM:SS.S): <input type=\"text\" name=\"" + Consts.REMINDING_TIME + "\" value=\"" + time +
				"\"><br>");
		builder.append("<input type=\"submit\" value=\"Save\">");
		builder.append("</form>");
		
		builder.append("<form action=\"reminders.html\">"
				+ "<input type=\"submit\" value=\"Cancle\"></form>");


		builder.append(Consts.CRLF);
		builder.append(Consts.CRLF);
		builder.append("</body></html>");
		return builder.toString();
	}
	
	public String buildTaskEditor(String pageName) {
		
		StringBuilder builder = new StringBuilder();

		builder.append("<html><head><title>" + pageName + "</title></head>");
		builder.append(Consts.CRLF);
		builder.append("<body>");
		builder.append(Consts.CRLF);
		builder.append("<form name=\"TaskEdit\" method=\"POST\" "
				+ "action=\"submit_task.html\">");
		builder.append("subject: <input type=\"text\" name=\""+ Consts.TITLE +"\"><br>");
		builder.append("content: <textarea cols=\"40\" rows=\"5\" name=\"" + Consts.CONTENT +
				"\">");
		builder.append("</textarea><br>");
		builder.append("recipient: <input type=\"text\" name=\"" + Consts.RECIPIENT + "\"><br>");
		builder.append("date(Format DD-MM-YYYY): <input type=\"text\" name=\"" + Consts.DUE_DATE + "\"><br>");
		builder.append("time(Format HH:MM:SS.S): <input type=\"text\" name=\"" + Consts.DUE_TIME + "\"><br>");
		builder.append("<input type=\"submit\" value=\"Save\">");
		builder.append("</form>");
		builder.append("<form action=\"tasks.html\">"
				+ "<input type=\"submit\" value=\"Cancle\"></form>");


		builder.append(Consts.CRLF);
		builder.append(Consts.CRLF);
		builder.append("</body></html>");
		return builder.toString();
	}
	
	public String buildPollEditor(String pageName) {
		
		StringBuilder builder = new StringBuilder();

		builder.append("<html><head><title>" + pageName + "</title></head>");
		builder.append(Consts.CRLF);
		builder.append("<body>");
		builder.append(Consts.CRLF);
		builder.append("<form name=\"PollEdit\" method=\"POST\" "
				+ "action=\"submit_poll.html\">");
		builder.append("subject: <input type=\"text\" name=\""+ Consts.TITLE +"\"><br>");
		builder.append("content: <textarea cols=\"40\" rows=\"5\" name=\"" + Consts.CONTENT +
				"\">");
		builder.append("</textarea><br>");
		builder.append("recipients: <textarea cols=\"40\" rows=\"5\" name=\"" + Consts.RECIPIENTS +
				"\">");
		builder.append("</textarea><br>");
		builder.append("answers: <textarea cols=\"40\" rows=\"5\" name=\"" + Consts.ANSWERS +
				"\">");
		builder.append("</textarea><br>");
		builder.append("<input type=\"submit\" value=\"Save\">");
		builder.append("</form>");
		builder.append("<form action=\"polls.html\">"
				+ "<input type=\"submit\" value=\"Cancle\"></form>");


		builder.append(Consts.CRLF);
		builder.append(Consts.CRLF);
		builder.append("</body></html>");
		return builder.toString();
	}
}
