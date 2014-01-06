import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

	private String version = "";
	private String path = "";
	private String query = "";
	private String root = "";
	private String responseHeaders;
	private HttpRequest.httpResponseCode httpResponseCode;
	private HttpParser.Method method;
	private Map<String, String> requestHeaders;
	private Map<String, String> responsetHeaders;
	private Map<String, String> params;
	private Map<String, String> cookies;
	private boolean isChunked = false;
	private File responseFile;
	private byte[] fileInBytes;
	private int ContentLength;
	private String ContentType;
	private static final String CHUNKED = "chunked: yes";
	private static final String USERMAIL = "usermail";
	private static final String RESPONSE_CHUNK_HEADER = "transfer-encoding";
	private static final String CRLF = "\r\n";
	private static final String NOT_IMPLEMENTED_PAGE = "/notImplementedRequst.html";
	private static final String BAD_PAGE = "/badRequst.html";
	private static final String INTERNAL_SERVER_ERROR_PAGE = "/internalRequst.html";
	private static final String NOT_FOUND_PAGE = "/notFoundRequst.html";
	private static final String PARAMS_INFO = "/params_info.html";
	private StringBuilder response;

	private RemindersDB remindersDB;
	private TasksDB taskDB;
	private PollDB pollDB;
	private ConfigManager configM;

	public HttpResponse(HttpRequest request) throws SQLException {
		this.root = request.getRoot();
		this.version = request.getParser().getVersion();
		this.method = request.getParser().getMethod();
		this.path = request.getParser().getPath();
		this.query = request.getParser().getQuery();
		this.httpResponseCode = request.getHttpResponseCode();
		this.requestHeaders = request.getParser().getHeaders();
		this.params = request.getParser().getParams();
		this.cookies = request.getParser().getCookies();
		this.response = new StringBuilder();
		this.responsetHeaders = new HashMap<>();
		this.ContentLength = 0;
		this.ContentType = "application/octet-stream";
		this.remindersDB = request.getReminderDB();
		this.taskDB = request.getTaskDB();
		this.pollDB = request.getPollDB();
		this.configM = request.getConfigM();
		setResponse();
	}

	/**
	 * According to the http response code of the reuest and the given method,
	 * create a full response (header and body) that will be send to the client.
	 * 
	 * @throws SQLException
	 */
	private void setResponse() throws SQLException {

		HtmlBuilder<Reminder> reminderHtmlBuilder = new HtmlBuilder<Reminder>();
		HtmlBuilder<Task> taskHtmlBuilder = new HtmlBuilder<Task>();
		HtmlBuilder<Poll> pollHtmlBuilder = new HtmlBuilder<Poll>();

		String body = null;

		// Response first line
		response.append(buildResponseHeader(this.httpResponseCode));
		response.append(CRLF);

		switch (this.httpResponseCode) {
		case OK:
			switch (method) {
			case GET:
				responseFile = new File(root + path);
				if (path.equals(PARAMS_INFO) && !params.isEmpty()) {
					body = buildFileWithParams(responseFile);

					// reminder.html
				} else if (path.equals("/" + Consts.REMINDERS_PAGE)) {
					String userName = cookies.get(Consts.USERMAIL);
					if (query != null && query.contains("id")) {
						remindersDB.deleteById((long) getId());
					}
					body = reminderHtmlBuilder.buildTable(
							remindersDB.getReminders(userName), path);

				} else if (path.equals("/" + Consts.REMINDERS_EDITOR)) {
					Reminder reminderToEdit = null;
					if (params.containsKey("id")) {
						int id = getId();
						if (id != -1) {
							reminderToEdit = remindersDB
									.getReminderById((long) id);
						}
					}
					body = reminderHtmlBuilder.buildEditor(
							Consts.REMINDERS_EDITOR, reminderToEdit);

					// Tasks
				} else if (path.equals("/" + Consts.TASKS_PAGE)) {
					String userName = cookies.get(Consts.USERMAIL);
					if (query != null && query.contains("id")) {
						taskDB.deleteById((long) getId());
					}
					body = taskHtmlBuilder.buildTable(
							taskDB.getTasks(userName), path);
				} else if (path.equals("/" + Consts.TASK_EDITOR)) {
					body = taskHtmlBuilder
							.buildTaskEditor(Consts.REMINDERS_EDITOR);
				} else if (path.equals("/" + Consts.TASK_REPLY)) {

					if (params.containsKey("id")) {
						int id = getId();
						if (id > 0) {
							Task currentTask = null;
							try {
								currentTask = taskDB.getTaskById(id);
							} catch (SQLException e) {
								System.err.println("Error getting a task by Id while sending the email to the creator");
							}
							sendMailToTaskCreator(currentTask);
							updateTaskAsCompleted(currentTask);
						}
					}

					// Polls
				} else if (path.equals("/" + Consts.POLLS_PAGE)) {
					String userName = cookies.get(Consts.USERMAIL);
					if (query != null && query.contains("id")) {
						pollDB.deleteById((long) getId());
					}
					body = pollHtmlBuilder.buildTable(
							pollDB.getPolls(userName), path);
				} else if (path.equals("/" + Consts.POLL_EDITOR)) {
					body = pollHtmlBuilder.buildPollEditor(Consts.POLL_EDITOR);
				} else if (path.equals("/" + Consts.POLL_REPLY)) {
					if (params.containsKey("id")
							&& params.containsKey("answer")
							&& params.containsKey("answerer")) {
						String userName = params.get("answerer");
						String answer = params.get("answer");
						if (userName != null && answer != null) {
							int id = getId();
							if (id > 0) {
								body = updatePoll(userName, answer, id);
								sendMailToPollCreator(userName, answer, id);
							}
						}
					}
				}
				break;
			case POST:
				responseFile = new File(root + path);
				if (path.equals(PARAMS_INFO) && !params.isEmpty()) {
					body = buildFileWithParams(responseFile);
				}
				break;
			case TRACE:
				responseFile = new File(root + path);
				body = getTraceResponse();
				break;
			case OPTIONS:
				getOptionsResponse();
				break;
			case HEAD:
				responseFile = new File(root + path);
				break;
			}
			break;
		case NOT_IMPLEMENTED:
			responseFile = new File(root + NOT_IMPLEMENTED_PAGE);
			break;
		case BAD:
			responseFile = new File(root + BAD_PAGE);
			break;
		case INTERNAL_SERVER_ERROR:
			responseFile = new File(root + INTERNAL_SERVER_ERROR_PAGE);
			break;
		case NOT_FOUND:
			responseFile = new File(root + NOT_FOUND_PAGE);
			break;
		case FOUND:
			addHeader("Location", path);
			responseFile = null;
			break;

		}

		if (responseFile != null) {
			fileInBytes = readFile(responseFile);
			ContentLength = fileInBytes.length;
			ContentType = getContentType(responseFile);
		}

		if (body != null) {
			ContentLength = body.length();
			if (ContentType == null) {
				ContentType = "application/octet-stream";
			}
		}

		// write the headers
		setBasicHeaders();
		response.append(writeHeaders());
		responseHeaders = response.toString();

		// write the body
		String bodyContent = buildBody(body);
		if (bodyContent != null && method != HttpParser.Method.HEAD) {
			response.append(buildBody(body));
			response.append(CRLF);
		}
	}

	private int getId() {
		String idString = params.get("id");
		try {
			return Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			System.out.println("not a number");
			return -1;
		}
	}

	/**
	 * Adds a header with the allowing methods for the option method.
	 */
	private void getOptionsResponse() {

		StringBuilder builder = new StringBuilder();
		for (HttpParser.Method option : HttpParser.Method.values()) {
			builder.append(option.name() + ", ");
		}

		String options = builder.toString();
		addHeader("allow", options.substring(0, options.length() - 2));

	}

	/**
	 * Write a html page with a table that consist the given request parmaters.
	 * 
	 * @param responseFile
	 * @return
	 */
	private String buildFileWithParams(File responseFile) {
		ArrayList<String> keys = new ArrayList<String>(params.keySet());
		StringBuilder builder = new StringBuilder();

		builder.append("<html><head><title>params_info</title></head>");
		builder.append(CRLF);
		builder.append("<body>");
		builder.append(CRLF);
		builder.append("<table border='1'><tr><th>Key</th><th>Value</th></tr>");
		builder.append(CRLF);
		for (String key : keys) {
			builder.append("<tr><td>" + key + "</td><td>" + params.get(key)
					+ "</td></tr>");
			builder.append(CRLF);
		}
		builder.append(CRLF);
		builder.append("</table>");
		builder.append(CRLF);
		builder.append("</body></html>");
		String content = builder.toString();
		ContentLength = content.length();
		return content;
	}

	/**
	 * The first line of the response
	 * 
	 * @param responseCode
	 * @return
	 */
	private String buildResponseHeader(HttpRequest.httpResponseCode responseCode) {
		return String.format("HTTP/%s %s", version, responseCode.getValue());
	}

	private void setBasicHeaders() {

		// Check the headers for "chunking"
		if (requestHeaders != null) {
			for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
				String currentHeader = header.getKey() + ": "
						+ header.getValue();
				if (currentHeader.equals(CHUNKED)) {
					addHeader(RESPONSE_CHUNK_HEADER, "chunked");
					this.isChunked = true;
				}
			}
		}

		addHeader("content-type", ContentType);
		addHeader("content-length", Integer.toString(ContentLength));

		// Add the user mail cookie.
		if (params.containsKey(USERMAIL)) {
			addHeader("Set-Cookie", USERMAIL + "=" + params.get(USERMAIL));
			webServer.addValueToUserMailCookies(params.get(USERMAIL));
		}

	}

	/**
	 * Write all the headers from the headers map.
	 * 
	 * @return
	 */
	private String writeHeaders() {
		StringBuilder builder = new StringBuilder();
		if (responsetHeaders != null) {
			for (Map.Entry<String, String> header : responsetHeaders.entrySet()) {
				if (header.getValue() != null) {
					builder.append(header.getKey() + ": " + header.getValue()
							+ CRLF);
				}
			}
		}
		builder.append(CRLF);
		return builder.toString();
	}

	/**
	 * Add a header to the headers map
	 * 
	 * @param header
	 * @param value
	 */
	private void addHeader(String header, String value) {
		responsetHeaders.put(header, value);
	}

	/**
	 * Write the body of the response.
	 * 
	 * @param body
	 * @return
	 */
	private String buildBody(String body) {

		String responseBody = "";

		if (body != null) {
			responseBody = body;
		} else if (responseFile != null) {
			responseBody = converByteArrayToString(fileInBytes);
		}

		if (isChunked) {
			String[] chunks = responseBody.split("\n");
			StringBuilder builder = new StringBuilder();
			for (String line : chunks) {
				builder.append(Integer.toHexString(line.length())).append(CRLF);
				builder.append(line).append(CRLF);
			}
			return builder.toString();
		}

		return responseBody;
	}

	/**
	 * convert byte array to string
	 * 
	 */
	public String converByteArrayToString(byte[] byteArray) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < byteArray.length; i++) {
			builder.append((char) byteArray[i]);
		}
		return builder.toString();
	}

	private String getContentType(File file) {
		String fileName = file.getName().toLowerCase();
		int index = fileName.indexOf(".");
		if (index != -1) {
			fileName = fileName.substring(index + 1, fileName.length());
		}

		switch (fileName) {
		case "png":
			return "image";
		case "jpg":
			return "image";
		case "gip":
			return "image";
		case "bmp":
			return "image";
		case "ico":
			return "icon";
		case "html":
			return "text/html";
		default:
			return "application/octet-stream";
		}
	}

	private byte[] readFile(File file) {
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] bFile = new byte[(int) file.length()];
			while (fis.available() != 0) {
				fis.read(bFile, 0, bFile.length);
			}
			fis.close();
			return bFile;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
		return null;
	}

	/**
	 * Write the headers of the request to the body of the response.
	 */
	private String getTraceResponse() {
		StringBuilder builder = new StringBuilder();
		builder.append("TRACE " + this.responseFile + " " + this.version + CRLF);
		if (requestHeaders != null) {
			for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
				if (header.getValue() != null) {
					builder.append(header.getKey() + ": " + header.getValue()
							+ CRLF);
				}
			}
		}
		String request = builder.toString();
		return request.getBytes().length + CRLF + request;

	}

	public String getResponse() {
		return response.toString();
	}

	public String getHeaders() {
		return responseHeaders.toString();
	}

	public String updatePoll(String answererUserName, String answer, long id)
			throws SQLException {
		
		String body = null;
		
		Poll currentPoll = pollDB.getPollById((long) id);
		
		if (currentPoll != null && currentPoll.getStatus() != Consts.PollStatus.COMPLETED) {
			
			currentPoll.setRecipientsReplies(answererUserName, answer);
			
			if (currentPoll.getRecipientsRepliesAsMap().size() == currentPoll.getRecipientsAsList().size()) {
				
				// Before we set it as completed send the mail to the creator
				sendMailToPollCreator(answererUserName, answer, id);
				pollDB.updatePollAnswers(Consts.PollStatus.COMPLETED, currentPoll.getRecipientsReplies(), (long)id);
			} else {
				pollDB.updatePollAnswers(Consts.PollStatus.IN_PROGRESS, currentPoll.getRecipientsReplies(), (long)id);
			}
		} else {
			body = "<html><head><title>poll_reply.html</title></head><body>"
					+ "The poll is not avilable<form action=\"polls.html\"><input type=\"submit\" value=\"Go back\">"
					+ "</form></body></html>";
		}
		return body;
	}

	public void sendMailToPollCreator(String answererUserName, String answer,
			long id) {
		Poll currentPoll = null;
		try {
			currentPoll = pollDB.getPollById(id);
		} catch (SQLException e) {
			System.err.println("Error getting a poll by Id while sending the email to the creator");
		}

		if (currentPoll.getStatus() != Consts.PollStatus.COMPLETED) {
			
			StringBuilder mailContent = new StringBuilder();
			mailContent.append(Consts.CRLF);
			mailContent.append(Consts.POLL_ANSWER_CONTENT + answer);
			mailContent.append(Consts.CRLF);
			mailContent.append(Consts.POLL_CURRENT_STATE);
			mailContent.append(Consts.CRLF);
			mailContent.append(currentPoll.getRecipientsReplies());

			SMTPclient SmtpToCreator = new SMTPclient(
					configM.GetValue(Consts.CONFIG_SMTPUSERNAME),
					configM.GetValue(Consts.CONFIG_SMTPPASSWORD),
					currentPoll.getTitle() + " answered by " + answererUserName,
					answererUserName, currentPoll.getUserName(), mailContent.toString(), configM.GetValue(Consts.CONFIG_SMTPNAME),
					Integer.parseInt(configM.GetValue(Consts.CONFIG_SMTPPORT)),
					Boolean.parseBoolean(configM.GetValue(Consts.CONFIG_ISAUTHLOGIN)));
			SmtpToCreator.sendSmtpMessage();
		}
	}

	public void sendMailToTaskCreator(Task currentTask) {
		if (currentTask.getStatus() == Consts.TaskStatus.IN_PROGRESS) {
			
			SMTPclient SmtpToCreator = new SMTPclient(
					configM.GetValue(Consts.CONFIG_SMTPUSERNAME),
					configM.GetValue(Consts.CONFIG_SMTPPASSWORD),
					currentTask.getTitle() + " is completed",
					currentTask.getRecipient(), currentTask.getUserName(),
					Consts.TASK_COMPLETED,
					configM.GetValue(Consts.CONFIG_SMTPNAME),
					Integer.parseInt(configM.GetValue(Consts.CONFIG_SMTPPORT)),
					Boolean.parseBoolean(configM
							.GetValue(Consts.CONFIG_ISAUTHLOGIN)));
			SmtpToCreator.sendSmtpMessage();
			
		}
	}

	public void updateTaskAsCompleted(Task currentTask) throws SQLException {
		if (currentTask.getStatus() == Consts.TaskStatus.IN_PROGRESS) {
		taskDB.updateTask(Consts.TaskStatus.COMPLETED, (long)currentTask.getId());
		}
	}
}
