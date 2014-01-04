import java.sql.SQLException;
import java.util.TimerTask;


public class JobTimerTask extends TimerTask {
	
	Job currentJob;
	DBManager manager;
	
	public JobTimerTask(Job currentJob, DBManager manager) { 
		this.currentJob = currentJob;
		this.manager = manager;
	}

	@Override
	public void run() {
		if (currentJob instanceof Reminder) {
			
		// need to change this to work with the config	
		SMTPclient currentSmtpClient = new SMTPclient("tasker@cscidc.ac.il", "password", currentJob.getTitle(), currentJob.getUserName(), currentJob.getUserName(), currentJob.getContent(), "compnet.idc.ac.il", 25, true);
		currentSmtpClient.sendSmtpMessage();
		
		} else { 
			if (currentJob instanceof Task && manager instanceof TasksDB) { 
				
				// need to change this to work with the config
				SMTPclient SmtpToReceiver =  new SMTPclient("tasker@cscidc.ac.il", "password", Consts.TASK_TITLE + currentJob.getTitle() + " time is due", currentJob.getUserName(), ((Task) currentJob).getRecipient(), Consts.TASK_TIME_IS_DUE, "compnet.idc.ac.il", 25, true);
				SmtpToReceiver.sendSmtpMessage();
				SMTPclient SmtpToOpener =  new SMTPclient("tasker@cscidc.ac.il", "password", Consts.TASK_TITLE + currentJob.getTitle() + " time is due", currentJob.getUserName(), currentJob.getUserName(), Consts.TASK_TIME_IS_DUE, "compnet.idc.ac.il", 25, true);
				SmtpToOpener.sendSmtpMessage();
				
				try {
					((TasksDB) manager).updateTask(Consts.TaskStatus.TIME_IS_DUE, currentJob.getId());
				} catch (SQLException e) {
					System.err.println("Error updating the task as over due time");
				}
				// Update the table as over the time
				// I need jobs to have their database
			}
		}
		
		
		
	}

}
