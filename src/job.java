import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

public abstract class job {

	private String title;
	private String content;
	private String userName;
	private Date creationDateAndTime;
	private Boolean valid;
	private Long id;

	public job(Map<String, String> params, String userName) {
		if (params != null || !(params.isEmpty())) {
			this.valid = true;
			setId(params.get("id"));
			creationDateAndTime = new Date();
			setTitle(params.get(Consts.TITLE.toLowerCase()));
			setUserName(userName);
			setContent(params.get(Consts.CONETNT.toLowerCase()));
		} else {
			this.valid = false;
		}
	}

	public job(long id, String userName, String title, String content,
			Date creationDateAndTime) {
		super();
		this.title = title;
		this.content = content;
		this.userName = userName;
		this.creationDateAndTime = creationDateAndTime;
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		if (title != null) {
			this.title = title;
		} else {
			valid = false;
		}
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		if (content != null) {
			this.content = content;
		} else {
			valid = false;
		}
	}

	public Date getCreationDateAndTime() {
		return this.creationDateAndTime;
	}

	public String getCreationDateAndTimeString() {
		return new SimpleDateFormat(Consts.DATE_FORMAT)
				.format(getCreationDateAndTime().getTime());
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		if (userName != null) {
			this.userName = userName;
		} else {
			valid = false;
		}
	}

	public Boolean getValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setId(String id) {
		if (id != null) {
			if (id.equals("-1")) {
				this.id = (long) -1;
				return;
			}
			try {
				this.id = (long) Integer.parseInt(id);
			} catch (NumberFormatException e) {
				System.err.println("not a number");
				setValid(false);;
			}
		} else {
			this.id = (long) -1;
		}
	}

	public void setCreationDateAndTime(Date creationDateAndTime) {
		this.creationDateAndTime = creationDateAndTime;
	}

	public void setCreationDateAndTime(String date) {
		if (date == null) {
			setValid(false);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(Consts.DATE_FORMAT);
		try {
			this.creationDateAndTime = dateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			System.err.println("Date Format isnt Correct");
			setValid(false);
		}
	}

}
