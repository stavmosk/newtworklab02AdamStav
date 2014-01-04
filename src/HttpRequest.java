import java.io.File;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle the http request.
 * 
 * @author stavmosk
 * 
 */
public class HttpRequest {

	private final static String CRLF = "\r\n";

	/**
	 * Holds the possibles response code.
	 * 
	 * @author stavmosk
	 * 
	 */
	public enum httpResponseCode {
		OK("200 OK"), BAD("400 Bad Request"), NOT_FOUND("404 Not Found"), REQUEST_TIMEOUT(
				"408 Request Timeout"), INTERNAL_SERVER_ERROR(
				"500 Internal Server Error"), NOT_IMPLEMENTED(
				"501 Not Implemented"), FOUND("302 Found");

		private String description;

		private httpResponseCode(String description) {
			this.description = description;
		}

		public String getValue() {
			return description;
		}
	}

	private String request;
	private httpResponseCode httpResponseCode;
	private String defaultPage;
	private String root;
	private HttpParser parser;
	private RemindersDB reminderDB;
	private TasksDB taskDB;
	private PollDB pollDB;

	public HttpRequest(String request, ConfigManager configM, RemindersDB reminderDB, TasksDB taskDB, PollDB pollDB) { 
		this.request = request;
		
		this.defaultPage = configM.GetValue(Consts.CONFIG_DEFAULTPAGE);
		this.root = configM.GetValue(Consts.CONFIG_ROOT);
		this.reminderDB = reminderDB;
		this.taskDB = taskDB;
		this.pollDB = pollDB;
		parser = new HttpParser();
		httpResponseCode = httpResponseCode.OK;

		if (parseRequest()) {
			validateRequest();
		}
	}
	
	/**
	 * Given a request, parse it using http parser and validate its headers and
	 * body. Decide the http response code each request gets.
	 * 
	 */
	public HttpRequest(String request, String defaultPage, String root,
			RemindersDB reminderDB, TasksDB taskDB, PollDB pollDB) {
		this.request = request;
		this.defaultPage = defaultPage;
		this.root = root;
		this.reminderDB = reminderDB;
		this.taskDB = taskDB;
		this.pollDB = pollDB;
		parser = new HttpParser();
		httpResponseCode = httpResponseCode.OK;

		if (parseRequest()) {
			validateRequest();
		}
	}

	/**
	 * Parse the given request using httpParser.
	 * 
	 * @return true if the format of the request is correct. otherwise false
	 */
	private boolean parseRequest() {

		if (!parser.parse(request)) {
			httpResponseCode = httpResponseCode.BAD;
			return false;
		}

		setPathtoDefaultIfNeeded();
		return true;
	}

	/**
	 * sets the default page if necessary.
	 */
	private void setPathtoDefaultIfNeeded() {
		if (parser.getPath().equals("/")) {
			parser.setPath("/" + this.defaultPage);
		}
	}

	/**
	 * Validate the request method, version, path and headers.
	 */
	private void validateRequest() {
		validateMethod();
		validateVersion();
		validatePath();
		validateHeaders();
		validateCookie();

		// Add or update a new reminder
		if (parser.getPath().equals("/" + Consts.REMINDERS_SUBMIT)) {
			try {
				validateReminder();
			} catch (SQLException e) {
				System.err.println("The reminder isnt valid");
				httpResponseCode = httpResponseCode.BAD;
			}
		}

		if (parser.getPath().equals("/" + Consts.TASK_SUBMIT)) {
			try {
				validateTask();
			} catch (SQLException e) {
				System.err.println("The task isnt valid");
				httpResponseCode = httpResponseCode.BAD;
			}
		}

		if (parser.getPath().equals("/" + Consts.POLL_SUBMIT)) {
			try {
				validatePoll();
			} catch (SQLException e) {
				System.err.println("The poll isnt valid");
				httpResponseCode = httpResponseCode.BAD;

			}
		}



		if (parser.getPath().equals("/logout.html")) {
			httpResponseCode = httpResponseCode.FOUND;
			parser.setPath("/index.html");
			parser.getParams().put(Consts.USERMAIL, "");
		}
	}

	private void validateCookie() {
		// Check if the cookie is valid or needs redirecting to index.html
		if (!parser.getHeaders().containsKey(Consts.HDR_COOKIE.toLowerCase())
				|| !isValidCookieHeader(parser.getHeader(Consts.HDR_COOKIE
						.toLowerCase()))) {
			if (!parser.getPath().equals("/index.html")) {
				httpResponseCode = httpResponseCode.FOUND;
				parser.setPath("/index.html");
			}
		} else if (parser.getPath().equals("/index.html")) {
			httpResponseCode = httpResponseCode.FOUND;
			parser.setPath("/main.html");
		}
	}

	private boolean isValidCookieHeader(String string) {

		Pattern pattern = Pattern.compile("(\\w+)=([^;]+);?");
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			if (matcher.group(1) != null && matcher.group(2) != null) {
				if (matcher.group(1).equals(Consts.USERMAIL)
						&& webServer.getUserMailCookies().contains(
								matcher.group(2))) {
					return true;
				}
			}
		}

		return false;

	}

	/**
	 * If the given version is http 1.1 - a host header is needed. set http
	 * response code to bad if no host.
	 */
	private void validateHeaders() {
		if (parser.getVersion().equals("1.1")
				&& parser.getHeaders().get("host") == null) {
			httpResponseCode = httpResponseCode.BAD;
		}

	}

	/**
	 * Valid the path remain in the server root else set http response code to
	 * bad. check if the path exist else set http response code to not found
	 */
	private void validatePath() {
		if (parser.getPath().contains("..")) {
			httpResponseCode = httpResponseCode.BAD;
			return;
		}

		File file = new File(root + parser.getPath());
		if (!file.exists() || file.isDirectory()) {
			httpResponseCode = httpResponseCode.NOT_FOUND;
		}
	}

	/**
	 * Valid the version of http (1.1 or 1.0) else set http response code to not
	 * implemnted.
	 */
	private void validateVersion() {
		if (!parser.getVersion().equals("1.1")
				&& !parser.getVersion().equals("1.0")) {
			httpResponseCode = httpResponseCode.NOT_IMPLEMENTED;
		}
	}

	/**
	 * Valid the method else set http response code to not implemented.
	 */
	private void validateMethod() {
		for (HttpParser.Method method : HttpParser.Method.values()) {
			if (method.equals(parser.getMethod())) {
				return;
			}
		}
		httpResponseCode = httpResponseCode.NOT_IMPLEMENTED;
	}

	public String getRoot() {
		return root;
	}

	public HttpParser getParser() {
		return parser;
	}

	public httpResponseCode getHttpResponseCode() {
		return httpResponseCode;
	}

	public RemindersDB getReminderDB() {
		return reminderDB;
	}

	public void setReminderDB(RemindersDB reminderDB) {
		this.reminderDB = reminderDB;
	}

	public TasksDB getTaskDB() {
		return taskDB;
	}

	public void setTaskDB(TasksDB taskDB) {
		this.taskDB = taskDB;
	}

	private void validateReminder() throws SQLException {
		Reminder reminder = new Reminder(parser.getParams(), parser
				.getCookies().get(Consts.USERMAIL));
		if (reminder.getValid()) {
			reminderDB.addOrupdateReminder(reminder);
			setTimerForReminder(reminder, reminderDB);
			httpResponseCode = httpResponseCode.FOUND;
			parser.setPath("/reminders.html");
		}
	}
	
	public void setTimerForReminder(Reminder currentReminder, RemindersDB manager ) { 
		Timer reminderTimer = new Timer();
		reminderTimer.schedule(new JobTimerTask(currentReminder, manager), currentReminder.getDateRemindingDate());
	}

	private void validateTask() throws SQLException {
		Task task = new Task(parser.getParams(), parser.getCookies().get(
				Consts.USERMAIL));
		if (task.getValid()) {
			taskDB.createTask(task);
			sendMailToTaskRecipient(task);
			setTimerForTask(task, taskDB);
			httpResponseCode = httpResponseCode.FOUND;
			parser.setPath("/tasks.html");
		}
	}
	
	private void setTimerForTask(Task currentTask, TasksDB manager ) { 
		Timer reminderTimer = new Timer();
		reminderTimer.schedule(new JobTimerTask(currentTask, manager), Consts.convertFromStringToDate(currentTask.getDueDate()));
	}
	
	private void sendMailToTaskRecipient(Task task){
		String currentContent = task.getContent() + "ServerName\task_reply.html?id=" + task.getId();
		SMTPclient smtpTaskToRecipient = new SMTPclient("tasker@cscidc.ac.il", "password", Consts.TASK_TITLE + task.getTitle(), task.getUserName(), task.getRecipient(), currentContent, "compnet.idc.ac.il", 25, true);
		smtpTaskToRecipient.sendSmtpMessage();
	}

	private void validatePoll() throws SQLException {

		Poll poll = new Poll(parser.getParams(), parser.getCookies().get(
				Consts.USERMAIL));
		
		if (poll.getValid()) {
			pollDB.createPoll(poll);
			SendMailToPollParticipants(poll);
			httpResponseCode = httpResponseCode.FOUND;
			parser.setPath("/polls.html");
		}
	}
	
	private void SendMailToPollParticipants(Poll poll) { 
		LinkedList<String> answers = poll.getAnswersAsList();
		
		StringBuilder contentBuilder = new StringBuilder();
		contentBuilder.append(poll.getContent());
		contentBuilder.append("\n");
		
		SMTPclient currentSmtpClient;
		LinkedList<String> recipients = poll.getRecipientsAsList();
		for (String answer : answers) {
			
		// need to handle the url right
		contentBuilder.append(String.format("<a href=\"ServerName/poll_reply.html?id=%d&answer=%s\">%s</a>\n", poll.getId(), answer, answer));
		}
		
		for (String recipient : recipients) {
			currentSmtpClient = new SMTPclient("tasker@cscidc.ac.il", "password", Consts.POLL_TITLE + poll.getTitle(), poll.getUserName(), recipient, contentBuilder.toString(), "compnet.idc.ac.il", 25, true);
			currentSmtpClient.sendSmtpMessage();
		}	
	}

	public PollDB getPollDB() {
		return pollDB;
	}

	public void setPollDB(PollDB pollDB) {
		this.pollDB = pollDB;
	}

	private int getId() {
		String idString = parser.getParam("id");
		try {
			return Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			System.out.println("not a number");
			return -1;
		}
	}
}
