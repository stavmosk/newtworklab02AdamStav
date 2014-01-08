import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;

public class webServer {
	
	private static LinkedList<String> userMailCookies;
	
	private static void startListening(ConfigManager configM) {
		TasksDB taskManager = new TasksDB(configM.GetValue(Consts.CONFIG_ROOT) + configM.GetValue(Consts.CONFIG_TASKFILEPATH));
		RemindersDB reminderManager = new RemindersDB(configM.GetValue(Consts.CONFIG_ROOT) + 
				configM.GetValue(Consts.CONFIG_REMINDERFILE));
		PollDB pollManager = new PollDB(configM.GetValue(Consts.CONFIG_ROOT) + 
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
			System.err.println("Error loading the current reminder or tasks at load up");
		}

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
				if (task.getStatus() == Consts.TaskStatus.IN_PROGRESS) {
					Timer currentTimer = new Timer();
					currentTimer.schedule(new JobTimerTask(task,
							reminderManager, configM), Consts
							.convertFromStringToDate(task.getDueDate()));
				}
			}
		}

		System.out.println(Consts.STARTING_SERVER);
		ServerSocket server = null;
		ActiveThread[] threadsPool = null;
		try {

			threadsPool = new ActiveThread[Integer.parseInt(configM.GetValue(Consts.CONFIG_MAXTHREAD))];
			LinkedList<SocketThread> list = new LinkedList<SocketThread>();

			for (int i = 0; i < threadsPool.length; i++) {
				threadsPool[i] = new ActiveThread(list);
				threadsPool[i].start();
			}

			Socket currentClient;
			server = new ServerSocket(Integer.parseInt(configM.GetValue(Consts.CONFIG_PORT)));
			System.out.println("listening on port: " + Integer.parseInt(configM.GetValue(Consts.CONFIG_PORT)));

			while (true) {

				currentClient = server.accept();

				synchronized (list) {
					list.addLast(new SocketThread(configM, currentClient, reminderManager, taskManager, pollManager));
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

	public static void main(String[] args) {
		userMailCookies = new LinkedList<String>();

		try {
			ConfigManager configM = new ConfigManager();
			
			if(!configM.validateConfigFile()) { 
				System.err.println("Config file isn't valid");
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
