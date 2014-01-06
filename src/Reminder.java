import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.Map;

public class Reminder extends Job {

	private String dateReminding;
	private Date dateRemindingDateFormat;
	private Consts.ReminderStatus status;

	public Reminder(Map<String, String> params, String userName) {
		super(params, userName);
		setStatus(Consts.ReminderStatus.NOT_SENT);
		setDateReminding(params.get(Consts.REMINDING_DATE.toLowerCase()),
				params.get(Consts.REMINDING_TIME.toLowerCase()));
		dateRemindingDateFormat = Consts.convertFromStringToDate(this.dateReminding);
	}

	private void setDateReminding(String date, String time) {
		if ((date != null) && (time != null)) {
			dateReminding = date + " " + time;
		} else {
			super.setValid(false);
		}
	}

	public Reminder(String userName, String title, String content, String status,
			String dateRemniding) {
		super((long) -1, title, content, userName, null);
		setStatus(status);
		this.dateReminding = dateRemniding;
	}
	
	public Reminder(String userName, String title, String content,
			String dateRemniding) {
		super((long) -1, title, content, userName, null);
		setStatus(status);
		this.dateReminding = dateRemniding;
	}
	
	public Reminder(long id, String userName, String title, String content,
			Date creation_time, Date dateRemniding) {
		super(id, userName, title, content, creation_time);
		setStatus(status);
		this.dateRemindingDateFormat = dateRemniding;
	}
	
	public Reminder(long id, String userName, String title, String content, String status,
			Date creation_time, Date dateRemniding) {
		super(id, userName, title, content, creation_time);
		setStatus(status);
		this.dateRemindingDateFormat = dateRemniding;
	}
	
	public void setStatus(String status) {
		this.status = Consts.ReminderStatus.valueOf(status);
	}
	
	public Consts.ReminderStatus getStatus() {
		return this.status;
	}

	public String getStatusString() {
		return this.status.name();
	}

	public void setStatus(Consts.ReminderStatus status) {
		this.status = status;
	}

	/**
	 * Validate the date and time of the reminding
	 * 
	 * @param date
	 * @return
	 */
	private Boolean validateDateReminding(String date) {
		if (date == null) {
			return false;
		}

		if(Consts.convertFromStringToDate(date) == null) { 
			return false;
		}
		
		return true;
	}

	
	public Date getDateRemindingDate() {
		return dateRemindingDateFormat;
	}
	
	public String getDateRemindingString() {
		return new SimpleDateFormat(Consts.DATE_FORMAT).format(dateRemindingDateFormat.getTime());
	}

	public void setDateRemniding(String date) {
		if(validateDateReminding(date)) { 
		this.dateReminding = date;
		}
	}

}
