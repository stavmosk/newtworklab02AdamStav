import java.sql.SQLException;
import java.util.TimerTask;

public class JobTimerTask extends TimerTask {

	Job currentJob;
	DBManager manager;
	ConfigManager configM;

	public JobTimerTask(Job currentJob, DBManager manager, ConfigManager configM) {
		this.currentJob = currentJob;
		this.manager = manager;
		this.configM = configM;
	}

	@Override
	public void run() {
		if (currentJob instanceof Reminder && manager instanceof RemindersDB ) {
			
			if (((Reminder)currentJob).getStatus() == Consts.ReminderStatus.NOT_SENT) { 
			
			SMTPclient currentSmtpClient = new SMTPclient(
					configM.GetValue(Consts.CONFIG_SMTPUSERNAME),
					configM.GetValue(Consts.CONFIG_SMTPPASSWORD),
					currentJob.getTitle(),
					currentJob.getUserName(), currentJob.getUserName(), currentJob.getContent(),
					configM.GetValue(Consts.CONFIG_SMTPNAME),
					Integer.parseInt(configM.GetValue(Consts.CONFIG_SMTPPORT)),
					Boolean.parseBoolean(configM
							.GetValue(Consts.CONFIG_ISAUTHLOGIN)));
			
			currentSmtpClient.sendSmtpMessage();
			
			try {
				((RemindersDB) manager).updateRemindersStatus(Consts.ReminderStatus.SENT, currentJob.getId());
			} catch (SQLException e) {
				System.err.println("Error updating a job as mail sent");
				e.printStackTrace();
			}
			
			}

		} else {
			if (currentJob instanceof Task && manager instanceof TasksDB) {
				
			if (((Task)currentJob).getStatus() == Consts.TaskStatus.IN_PROGRESS) { 

				SMTPclient SmtpToReceiver = new SMTPclient(
						configM.GetValue(Consts.CONFIG_SMTPUSERNAME),
						configM.GetValue(Consts.CONFIG_SMTPPASSWORD),
						Consts.TASK_TITLE
						+ currentJob.getTitle() + " time is due",
						currentJob.getUserName(),
						((Task) currentJob).getRecipient(), Consts.TASK_TIME_IS_DUE,
						configM.GetValue(Consts.CONFIG_SMTPNAME),
						Integer.parseInt(configM.GetValue(Consts.CONFIG_SMTPPORT)),
						Boolean.parseBoolean(configM
								.GetValue(Consts.CONFIG_ISAUTHLOGIN)));
				
				SmtpToReceiver.sendSmtpMessage();
				
				SMTPclient SmtpToOpener = new SMTPclient(
						configM.GetValue(Consts.CONFIG_SMTPUSERNAME),
						configM.GetValue(Consts.CONFIG_SMTPPASSWORD),
						Consts.TASK_TITLE + currentJob.getTitle()
						+ " time is due",
						((Task) currentJob).getRecipient(), currentJob.getUserName(),
						 Consts.TASK_TIME_IS_DUE,
						configM.GetValue(Consts.CONFIG_SMTPNAME),
						Integer.parseInt(configM.GetValue(Consts.CONFIG_SMTPPORT)),
						Boolean.parseBoolean(configM
								.GetValue(Consts.CONFIG_ISAUTHLOGIN)));		
				SmtpToOpener.sendSmtpMessage();
				

				try {
					((TasksDB) manager).updateTask(Consts.TaskStatus.TIME_IS_DUE, currentJob.getId());
				} catch (SQLException e) {
					System.err
							.println("Error updating a task as over due time");
				}
				// Update the table as over the time
				// I need jobs to have their database
			}
			}
		}

	}

}
