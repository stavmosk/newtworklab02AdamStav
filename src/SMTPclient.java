import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.bind.DatatypeConverter;

public class SMTPclient {

	private final static String CRLF = "\r\n";
	private final static String fromName = "Mr.Tasker";

	private String subject;
	private String mailFrom;
	private String rcptTo;
	private String messageData;
	private String smtpName;
	private int smtpPort;
	private String userName;
	private String password;
	private Boolean authLogin;
	
	Socket smtpSocket = null;
	DataOutputStream os = null;
	InputStreamReader is = null;
	BufferedReader reader = null;

	public SMTPclient(String userName, String password, String subject,
			String mailFrom, String rcptTo, String messageData,
			String smtpName, int smtpPort, Boolean authLogin) {
		this.userName = userName;
		this.password = password;
		this.subject = subject;
		this.mailFrom = mailFrom;
		this.rcptTo = rcptTo;
		this.messageData = messageData;
		this.smtpName = smtpName;
		this.smtpPort = smtpPort;
		this.authLogin = authLogin;
	}

	public void sendSmtpMessage() {
		connectingToServer();
		sendMessage();
	}

	private void connectingToServer() {
		try {
			smtpSocket = new Socket(smtpName, smtpPort);
			os = new DataOutputStream(smtpSocket.getOutputStream());
			is = new InputStreamReader(smtpSocket.getInputStream());
			reader = new BufferedReader(is);

		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: hostname");
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: hostname");
		}
	}

	private boolean checkSmtpServerResponse() {
		String responseLine;
		try {
			if ((responseLine = reader.readLine()) != null) {
				System.out.println(responseLine);
				if (responseLine.indexOf("OK") == -1 && responseLine.indexOf("220") == -1) {
					System.err.println(responseLine);
					return false;
				}
			} else {
				System.err.println("Smtp server isnt responding");
				return false;
			}

		} catch (IOException e) {
			System.err.println("error while reading");
			e.printStackTrace();
		}

		return true;
	}

	private void sendMessage() {
		if (smtpSocket != null && os != null && is != null) {
			String encodeUserName = null;
			String encodePassword = null;

			try {
				String currentLine = "";
				
				if (authLogin) {
					os.writeBytes("EHLO stavMoskovich adamRozental" + CRLF);
					if (!checkSmtpServerResponse()) {
						return;
					}
					System.out.println(reader.readLine());
					System.out.println(reader.readLine());
					System.out.println(reader.readLine());
					
					System.out.println("AUTH LOGIN" + CRLF);
					os.writeBytes("AUTH LOGIN" + CRLF);
					currentLine = reader.readLine();
					System.out.println(currentLine);
					if(currentLine.indexOf("334") == -1){
						return;
					}
					
					encodeUserName = DatatypeConverter.printBase64Binary(userName.getBytes());
					encodePassword = DatatypeConverter.printBase64Binary(password.getBytes());
					System.out.println(encodeUserName + CRLF);
					os.writeBytes(encodeUserName + CRLF);
					currentLine = reader.readLine();
					System.out.println(currentLine);
					if(currentLine.indexOf("334") == -1){
						return;
					}
					System.out.println(encodePassword + CRLF);
					os.writeBytes(encodePassword + CRLF);
					currentLine = reader.readLine();
					System.out.println(currentLine);
					if(currentLine.indexOf("235") == -1){
						return;
					}

				} else {
					System.out.println("HELO " + "tasker" + CRLF);
					os.writeBytes("HELO " + "tasker" + CRLF);
					if (!checkSmtpServerResponse()) {
						return;
					}
				}

				System.out.println("MAIL FROM: " + mailFrom + CRLF);
				os.writeBytes("MAIL FROM: " + mailFrom + CRLF);
				if (!checkSmtpServerResponse()) {
					return;
				}
				System.out.println("RCPT TO: " + rcptTo + CRLF);
				os.writeBytes("RCPT TO: " + rcptTo + CRLF);
				if (!checkSmtpServerResponse()) {
					return;
				}
				System.out.println("DATA" + CRLF);
				os.writeBytes("DATA" + CRLF);
				if (!checkSmtpServerResponse()) {
					return;
				}

				// Message Data:
				System.out.println("Subject: " + subject + CRLF + "From: " + fromName + CRLF + "Sender: " + mailFrom + CRLF + CRLF + messageData +CRLF + "." + CRLF);
				os.writeBytes("Subject: " + subject + CRLF);
				os.writeBytes("From: " + fromName + CRLF);
				os.writeBytes("Sender: " + mailFrom + CRLF);
				os.writeBytes(CRLF);
				os.writeBytes(messageData);
				os.writeBytes(CRLF + "." + CRLF);

				System.out.println(reader.readLine());
				System.out.println("QUIT");
				os.writeBytes("QUIT");
				os.close();
				is.close();
				reader.close();
				smtpSocket.close();
			} catch (UnknownHostException e) {
				System.err.println("Trying to connect to unknown host: " + e);
			} catch (IOException e) {
				System.err.println("IOException:  " + e);
			}
		}
	}

}
