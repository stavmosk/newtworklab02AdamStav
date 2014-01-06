import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Consts {

	public enum TaskStatus {
		IN_PROGRESS, COMPLETED, TIME_IS_DUE
	};

	public enum PollStatus {
		IN_PROGRESS, COMPLETED
	};
	
	public enum ReminderStatus {
		NOT_SENT, SENT
	};

	public final static String CRLF = "\r\n";
	public static final String REMINDERS_PAGE = "reminders.html";
	public static final String TASKS_PAGE = "tasks.html";
	public static final String POLLS_PAGE = "polls.html";
	
	public static final String TASK_REPLY = "task_reply.html";
	public static final String POLL_REPLY = "poll_reply.html";



	public static final String REMINDERS_EDITOR = "reminder_editor.html";
	public static final String TASK_EDITOR = "task_editor.html";
	public static final String POLL_EDITOR = "poll_editor.html";
	
	public static final String REMINDERS_SUBMIT = "submit_reminder.html";
	public static final String TASK_SUBMIT = "submit_task.html";
	public static final String POLL_SUBMIT = "submit_poll.html";

	public static final String REMINDERS_TABLE = "ReminderTable";
	public static final String TASKS_TABLE = "TasksTable";
	public static final String POLLS_TABLE = "PollsTable";
	public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss.s";
	public static final String USER_NAME = "userName";
	public static final String CREATION_DATE = "craetionDate";
	public static final String DUE_DATE = "dueDate";
	public static final String DUE_TIME = "dueTime";

	public static final String TITLE = "title";
	public static final String CONTENT = "content";
	public static final String STATUS = "status";
	public static final String REMINDING_DATE = "remindingDate";
	public static final String REMINDING_TIME = "remindingTime";

	public static final String RECIPIENT = "recipient";
	public static final String RECIPIENTS = "recipients";
	public static final String ANSWERS = "answers";
	public static final String RECIPIENT_REPLAY = "recipientReplay";
	public static final String USERMAIL = "usermail";

	public final static String HDR_COOKIE = "cookie";
	
	public final static String TASK_COMPLETED = "The task is completed";
	public final static String TASK_TIME_IS_DUE = "The tasks' time is due";
	public final static String TASK_TITLE = "Task: ";
	
	public final static String POLL_TITLE = "Poll: ";
	public final static String POLL_ANSWER_CONTENT = "My answer to your poll is: ";
	
	public static final String CONFIG_PORT = "port";
	public static final String CONFIG_ROOT = "root";
	public static final String CONFIG_MAXTHREAD = "maxThreads";
	public static final String CONFIG_DEFAULTPAGE = "defaultPage";
	public static final String CONFIG_SMTPNAME = "SMTPName";
	public static final String CONFIG_SMTPPORT = "SMTPPort";
	public static final String CONFIG_SERVERNAME = "ServerName";
	public static final String CONFIG_SMTPUSERNAME = "SMTPUsername";
	public static final String CONFIG_ISAUTHLOGIN = "SMTPIsAuthLogin";
	public static final String CONFIG_REMINDERFILE = "reminderFilePath";
	public static final String CONFIG_TASKFILEPATH = "taskFilePath";
	public static final String CONFIG_POLLFILEPATH = "pollFilePath";
	public static final String CONFIG_SMTPPASSWORD ="SMTPPassword";
	
	public static final String POLL_MAIL_ANSWER_LINE = "For %s Press - %s";
	public static final String POLL_CURRENT_STATE = "The current answers state is - ";
	
	public static String replaceApostrophes(String toReplace) { 
		String toReturn = toReplace.replace("'", "''");
		return toReturn;
	}


	public static Date convertFromStringToDate(String date) {
		Date result;
		SimpleDateFormat dateFormat = new SimpleDateFormat(Consts.DATE_FORMAT);
		try {
			result = dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			System.err.println("Date Format isnt Correct");
			return null;
		}

		return result;
	}

	public static String convertFromDateToString(Date date) {
		return new SimpleDateFormat(Consts.DATE_FORMAT).format(date.getTime());
	}

}
