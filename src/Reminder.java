import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.Map;

public class Reminder extends Job {

	private String dateReminding;
	private Date dateRemindingDateFormat;

	public Reminder(Map<String, String> params, String userName) {
		super(params, userName);
		setDateRemniding(params.get(Consts.REMINDING_DATE.toLowerCase()),
				params.get(Consts.REMINDING_TIME.toLowerCase()));
		dateRemindingDateFormat = Consts.convertFromStringToDate(this.dateReminding);
	}

	private void setDateRemniding(String date, String time) {
		if ((date != null) && (time != null)) {
			dateReminding = date + " " + time;
		} else {
			super.setValid(false);
		}
	}

	public Reminder(String userName, String title, String content,
			String dateRemniding) {
		super((long) -1, title, content, userName, null);
		this.dateReminding = dateRemniding;
	}

	public Reminder(long id, String userName, String title, String content,
			Date creation_time, String dateRemniding) {
		super(id, userName, title, content, creation_time);
		this.dateReminding = dateRemniding;
	}
	
	public Reminder(long id, String userName, String title, String content,
			Date creation_time, Date dateRemniding) {
		super(id, userName, title, content, creation_time);
		this.dateRemindingDateFormat = dateRemniding;
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

		SimpleDateFormat dateFormat = new SimpleDateFormat(Consts.DATE_FORMAT);
		return true;
	}

	
	public Date getDateRemindingDate() {
		return dateRemindingDateFormat;
	}
	
	public String getDateRemindingString() {
		return new SimpleDateFormat(Consts.DATE_FORMAT).format(dateRemindingDateFormat.getTime());
	}

	// Todo: validation
	public void setDateRemniding(String date) {
		this.dateReminding = date;
	}

}
