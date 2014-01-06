import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;


public class Poll extends Job {


	private LinkedList<String> recipients;
	private LinkedList<String> answers;
	private Map<String, String> recipientsReplies;
	private Consts.PollStatus status;

	public Poll(Map<String, String> params, String userName) {
		super(params, userName);
		this.recipients = new LinkedList<String>();
		this.answers = new LinkedList<String>();
		this.recipientsReplies = new HashMap<String, String>();

		if (getValid()) {
			setRecipients(params.get(Consts.RECIPIENTS.toLowerCase()));
			setAnswers(params.get(Consts.ANSWERS.toLowerCase()));
			setStatus(Consts.PollStatus.IN_PROGRESS);
		}
	}

	public Poll(long id, String userName, String title, String content,
			String recipent, String answers, String recipientsReplies,
			String status, Date creationTime, String dueDate) {
		super(id, userName, title, content, creationTime);
		this.recipients = new LinkedList<String>();
		this.answers = new LinkedList<String>();
		this.recipientsReplies = new HashMap<String, String>();
		//setRecipients(recipent);
		//setAnswers(answers);
		setRecipients(replaceSpaceWithCRLF(recipent));
		setAnswers(replaceSpaceWithCRLF(answers));
		setRecipientsReplies(recipientsReplies);
		setStatus(status);
	}
	
	private String replaceSpaceWithCRLF(String toReplace) { 
		String toReturn = toReplace.replace(" ", Consts.CRLF);
		return toReturn;
	}
	
	public LinkedList<String> getRecipientsAsList() { 
		return recipients;
	}

	public String getRecipients() {
		if (recipients != null && !recipients.isEmpty()) {
			StringBuilder builder = new StringBuilder();

			ListIterator<String> itr = recipients.listIterator();
			while (itr.hasNext()) {
				builder.append(itr.next() + " ");
				//builder.append(itr.next() + Consts.CRLF);
			}

			// Removes the last space.
			builder.deleteCharAt(builder.length() - 1);
			//builder.deleteCharAt(builder.length() - 2);
			return builder.toString();
		}
		return "";
	}

	public void setRecipients(String receptients) {
		if (receptients == null || receptients.length() == 0) {
			setValid(false);
		} else {
			String[] receptientsArray = receptients.split(Consts.CRLF);
			for (int i = 0; i < receptientsArray.length; i++) {
				this.recipients.add(receptientsArray[i]);
			}
		}
	}
	
	public LinkedList<String> getAnswersAsList() { 
		return answers;
	}

	public String getAnswers() {
		if (answers != null && !answers.isEmpty()) {

			StringBuilder builder = new StringBuilder();
			ListIterator<String> itr = answers.listIterator();
			while (itr.hasNext()) {
				builder.append(itr.next() + " ");
				//builder.append(itr.next() + Consts.CRLF);
			}

			// Removes the last space.
			builder.deleteCharAt(builder.length() - 1);
			//builder.deleteCharAt(builder.length() - 2);
			return builder.toString();
		}
		return "";
	}

	public void setAnswers(String answers) {
		if (answers == null || answers.length() == 0) {
			setValid(false);
		} else {

			String[] answersArray = answers.split(Consts.CRLF);
			for (int i = 0; i < answersArray.length; i++) {
				this.answers.add(answersArray[i]);
			}
		}
	}
	
	public Map<String,String> getRecipientsRepliesAsMap() {
	return recipientsReplies;
	}
	
	public String getRecipientsReplies() { 
		if (recipientsReplies == null || recipientsReplies.isEmpty()) { 
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String,String> entry : recipientsReplies.entrySet()) {
		   builder.append(entry.getKey());
		   builder.append(":");
		   builder.append(entry.getValue());
		   builder.append(",");
		}
		return builder.toString();
	}
	
	public void setRecipientsReplies(String recipientsReplies) {
		if (recipientsReplies == null || recipientsReplies.length() == 0) {
			setValid(false);
		} else {
			String[] reciReplay = recipientsReplies.split(",");
			for (int i = 0; i < reciReplay.length; i++) {
				String[] reciAnswer = reciReplay[i].split(":");
				this.recipientsReplies.put(reciAnswer[0], reciAnswer[1]);
			}
		}

	}
	
	public void setRecipientsReplies(String userName, String answer) {
		if (userName != null && answer != null) {
			this.recipientsReplies.put(userName, answer);
		} else {
			setValid(false);
		}

	}

	public Consts.PollStatus getStatus() {
		return status;
	}

	public void setStatus(Consts.PollStatus status) {
		this.status = status;
	}

	public void setStatus(String status) {
		this.status = Consts.PollStatus.valueOf(status);
	}

	public String getStatusString() {
		return status.name();
	}


}
