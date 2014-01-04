import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Gets a request from the client and send back to the client a response.
 * 
 * @author stavmosk
 * 
 */
public class SocketThread implements Runnable {

	private final static String CRLF = "\r\n";
	private Socket socket;
	private HttpRequest request;
	private HttpResponse response;
	private String root;
	private String defaultPage;
	private RemindersDB remindersDB;
	private TasksDB taskDB;
	private PollDB pollDB;
	private ConfigManager configM;

	public SocketThread(String root, String defaultPage, Socket socket, RemindersDB remindersDB, TasksDB tasksDB, PollDB pollDB) {
		this.root = root;
		this.defaultPage = defaultPage;
		this.socket = socket;
		this.remindersDB = remindersDB;
		this.taskDB = tasksDB;
		this.pollDB = pollDB;
	}
	
	public SocketThread(ConfigManager configM, Socket socket, RemindersDB remindersDB, TasksDB tasksDB, PollDB pollsDB) { 
		this.configM = configM;
		this.socket = socket;
		this.remindersDB = remindersDB;
		this.taskDB = tasksDB;
		this.pollDB = pollsDB;
	}

	/**
	 * read the request from the client. print the headers. gets a response and
	 * send it back to the client. print the response headers.
	 * 
	 */
	@Override
	public void run() {		
		try {

			String requestFromClient = getClientRequest();
			//request = new HttpRequest(requestFromClient, defaultPage, root, remindersDB, taskDB, pollDB);
			request = new HttpRequest(requestFromClient, configM, remindersDB, taskDB, pollDB);
			response = new HttpResponse(request);
			sendResponeToClient(response);
			printResponseHeaders();

		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.err
					.println("Error in the connection between the server and client");
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * read a request from the client.
	 * 
	 */
	private String getClientRequest() {
		String request = "";

		try {
			InputStreamReader in = new InputStreamReader(
					socket.getInputStream());
			BufferedReader reader = new BufferedReader(in);
			StringBuilder builder = new StringBuilder();
			int contentLength = 0;
			String headerLine = reader.readLine();
			builder.append(headerLine + CRLF);

			// Read the headers
			String line = reader.readLine();
			while (!line.isEmpty()) {
				if (line.startsWith("Content-Length")) {
					String contentLen = line.substring(line.indexOf(" ") + 1,
							line.length());
					try {
						contentLength = Integer.parseInt(contentLen);
					} catch (Exception e) {
						System.err.println("content length isnt valid");
					}
				}
				builder.append(line);
				builder.append(CRLF);
				line = reader.readLine();
			}

			printRequestHeaders(builder);
			builder.append(CRLF);

			// Read the body
			if (headerLine.startsWith("POST")) {
				for (int i = 0; i < contentLength; i++) {
					builder.append((char) reader.read());
				}
			}

			request = builder.toString();

		} catch (IOException e) {
			System.err.println("Cant read the request");
		} catch (Exception e) {
		}

		return request;
	}

	/**
	 * send response to the client
	 * 
	 * @param response
	 */
	private void sendResponeToClient(HttpResponse response) {
		try {
			DataOutputStream out = new DataOutputStream(
					this.socket.getOutputStream());
			out.writeBytes(response.getResponse());
			out.close();

		} catch (IOException e) {
			System.out.println("cant send response");
		}
	}

	public void printRequestHeaders(StringBuilder builder) {
		System.out.println("-------Request-------");
		System.out.println(builder.toString());
	}

	public void printResponseHeaders() {
		System.out.println("-------Response-------");
		System.out.println(response.getHeaders());

	}
}
