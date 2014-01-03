
import java.util.Date;
import java.util.Map;

public class Task extends job {

	private String dueDate;
	private Consts.TaskStatus status;
	private String recipient;

	public Task(Map<String, String> params, String userName) {
		super(params, userName);
		setStatus(Consts.TaskStatus.IN_PROGRESS);
		if (getValid()) {
			setDueDate(params.get(Consts.DUE_DATE.toLowerCase()),
					params.get(Consts.DUE_TIME.toLowerCase()));
			setRecipient(params.get(Consts.RECIPIENT.toLowerCase()));
		}
	}

	public Task(long id, String userName, String title, String content,
			String recipent, String status, Date createDate, String dueDate) {
		super(id, userName, title, content, createDate);
		this.dueDate = dueDate;
		setStatus(status);
		this.recipient = recipent;
	}

	public String getDueDate() {
		return dueDate;
	}

	public void setDueDate(String dueDate) {
		this.dueDate = dueDate;
	}

	public void setDueDate(String dueDate, String dueTime) {
		if (dueDate != null && dueTime != null) {
			this.dueDate = dueDate + " " + dueTime;
		} else {
			setValid(false);
		}
	}

	public Consts.TaskStatus getStatus() {
		return status;
	}

	public String getStatusString() {
		return status.name();
	}

	public void setStatus(Consts.TaskStatus status) {
		this.status = status;
	}

	public void setStatus(String status) {
		this.status = Consts.TaskStatus.valueOf(status);
	}

	public String getRecipient() {
		return recipient;
	}

	public void setRecipient(String recipient) {
		if (recipient == null) {
			this.setValid(false);
		} else {
			this.recipient = recipient;
		}
	}


}
