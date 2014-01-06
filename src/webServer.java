import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;

/**
 * Parse the config file, and according to its parameters start to listen to the
 * given port. Each connection gets a thread that handle the connection between
 * the server and the client.
 * 
 * @author stavmosk
 * 
 */
public class webServer {

	private static final String ROOT = "root";
	private static final String PORT = "port";
	private static final String MAX_THREADS = "maxThreads";
	private static final String DEFAULT_PAGE = "defaultPage";
	private static final String STARTING_SERVER = "Starting server";
	private static final String PORT_OUT_OF_RANGE = "Port number is out of range";
	private static final String MAX_THREAD_OUT_OF_RANGE = "Max Threads is out of range";
	private static int port;
	private static int maxThreads;
	private static String root;
	private static String defaultPage;
	private static LinkedList<String> userMailCookies;

	private static void startListening(ConfigManager configM) {
		TasksDB taskManager = new TasksDB(
				configM.GetValue(Consts.CONFIG_TASKFILEPATH));
		RemindersDB reminderManager = new RemindersDB(
				configM.GetValue(Consts.CONFIG_REMINDERFILE));
		PollDB pollManager = new PollDB(
				configM.GetValue(Consts.CONFIG_POLLFILEPATH));
		try {
			reminderManager.createDb();
			taskManager.createDb();
			pollManager.createDb();
		} catch (Exception e1) {
			System.out.println("Error initlize Data base.");
		}

		ArrayList<Reminder> currentReminders = null;
		ArrayList<Task> currentTasks = null;
		try {
			currentReminders = reminderManager.getAllReminders();
			currentTasks = taskManager.getAllTasks();
		} catch (SQLException e2) {
			System.err
					.println("Error loading the current reminder or tasks at load up");
		}

		// will double send those who were sent when the server was on 
		if (currentReminders != null && currentReminders.size() != 0) {
			for (Reminder reminder : currentReminders) {
				if (reminder.getStatus() == Consts.ReminderStatus.NOT_SENT) { 
				Timer currentTimer = new Timer();
				currentTimer.schedule(new JobTimerTask(reminder,
						reminderManager, configM), reminder
						.getDateRemindingDate());
				}
			}
		}

		if (currentTasks != null && currentTasks.size() != 0) {
			for (Task task : currentTasks) {
				
				// need to think if this logic is enough
				if (task.getStatus() == Consts.TaskStatus.IN_PROGRESS) {
					Timer currentTimer = new Timer();
					currentTimer.schedule(new JobTimerTask(task,
							reminderManager, configM), Consts
							.convertFromStringToDate(task.getDueDate()));
				}
			}
		}

		System.out.println(STARTING_SERVER);
		ServerSocket server = null;
		ActiveThread[] threadsPool = null;
		try {

			threadsPool = new ActiveThread[maxThreads];
			LinkedList<SocketThread> list = new LinkedList<SocketThread>();

			for (int i = 0; i < threadsPool.length; i++) {
				threadsPool[i] = new ActiveThread(list);
				threadsPool[i].start();
			}

			Socket currentClient;
			server = new ServerSocket(port);
			System.out.println("listening on port: " + port);

			while (true) {

				currentClient = server.accept();

				synchronized (list) {
					list.addLast(new SocketThread(configM, currentClient,
							reminderManager, taskManager, pollManager));
					list.notify();
				}
			}
		} catch (IOException e1) {
			System.err.println("Could not create server socket");
			if (threadsPool != null) {
				for (int i = 0; i < threadsPool.length; i++) {
					threadsPool[i].interrupt();
				}
			}

			try {
				if (server != null) {
					server.close();
				}
			} catch (IOException e) {

			}
		}
	}

	/**
	 * Validate all the config file properties.
	 * 
	 * @return
	 */
	private static boolean validateConfigFile() {
		return isValidePort() && isValideMaxThreads() && isValideRoot()
				&& isValideDefaultPage();
	}

	/**
	 * Validate the default page exist
	 * 
	 */
	private static boolean isValideDefaultPage() {
		try {
			File file = new File(root + "\\" + defaultPage);
			if (file.exists()) {
				return true;
			}
		} catch (Exception e) {
			System.err.println("Cant find the default page");
		}
		return false;
	}

	/**
	 * Validate port exist
	 */
	private static boolean isValidePort() {
		if (port < 0 || port > 65353) {
			System.err.println(PORT_OUT_OF_RANGE);
			return false;
		}
		return true;
	}

	/**
	 * Validate the root folder exist
	 */
	private static boolean isValideRoot() {
		try {
			File file = new File(root);
			if (file.exists()) {
				return true;
			}
			System.err.println("cant find root folder");
		} catch (Exception e) {
			System.err.println("cant find root folder");
		}
		return false;
	}

	/**
	 * Validate the number of max thread is in the range.
	 */
	private static boolean isValideMaxThreads() {
		if (maxThreads < 1) {
			System.err.println(MAX_THREAD_OUT_OF_RANGE);
			return false;
		}
		return true;
	}

	public static void main(String[] args) {
		userMailCookies = new LinkedList<String>();

		try {
			ConfigManager configM = new ConfigManager();
			root = configM.GetValue(ROOT);
			defaultPage = configM.GetValue(DEFAULT_PAGE);

			try {
				maxThreads = Integer.parseInt(configM.GetValue(MAX_THREADS));
			} catch (Exception e) {
				System.out.println("config file isnt valid");
				System.exit(0);
			}

			try {
				port = Integer.parseInt(configM.GetValue(PORT));
			} catch (Exception e) {
				System.out.println("config file isnt valid");
				System.exit(0);
			}

			if (!validateConfigFile()) {
				System.err.println("config file isnt valid");
				System.exit(0);
			}

			startListening(configM);

		} catch (Exception e) {
			System.out.println("Error while running the server");
			System.exit(0);

		}
	}

	public static LinkedList<String> getUserMailCookies() {
		return userMailCookies;
	}

	public static void addValueToUserMailCookies(String value) {
		webServer.userMailCookies.add(value);
	}

}
