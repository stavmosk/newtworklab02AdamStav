import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.Map;

public class Reminder extends job {

	private String dateRemniding;

	public Reminder(Map<String, String> params, String userName) {
		super(params, userName);
		setDateRemniding(params.get(Consts.REMINDING_DATE.toLowerCase()),
				params.get(Consts.REMINDING_TIME.toLowerCase()));
	}

	private void setDateRemniding(String date, String time) {
		if ((date != null) && (time != null)) {
			dateRemniding = date + " " + time;
		} else {
			super.setValid(false);
		}
	}

	public Reminder(String userName, String title, String content,
			String dateRemniding) {
		super((long) -1, title, content, userName, null);
		this.dateRemniding = dateRemniding;
	}

	public Reminder(long id, String userName, String title, String content,
			Date creation_time, String dateRemniding) {
		super(id, userName, title, content, creation_time);
		this.dateRemniding = dateRemniding;
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

	public String getDateRemnidingString() {
		return dateRemniding;
	}

	// Todo: validation
	public void setDateRemniding(String date) {
		this.dateRemniding = date;
	}



}
