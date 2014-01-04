import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.xml.bind.DatatypeConverter;

public class SMTPclient {

	private final static String CRLF = "\r\n";
	private final static String fromName = "Mr. Tasker";

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

	/**
	 * 
	 */
	public void sendSmtpMessage() {
		connectingToServer();
		sendMessage();
	}

	/**
	 * 
	 */
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

	/**
	 * 
	 * @return
	 */
	private boolean checkSmtpServerResponse() {
		String responseLine;
		try {
			if ((responseLine = reader.readLine()) != null) {
				if (responseLine.indexOf("OK") == -1 && responseLine.indexOf("220") == -1) {
					System.err.println(responseLine);
					return false;
				}
				System.out.println(responseLine);
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

	/**
	 * 
	 */
	private void sendMessage() {
		if (smtpSocket != null && os != null && is != null) {
			String encodeUserName = null;
			String encodePassword = null;

			try {

				if (authLogin) {
					os.writeBytes("EHLO stavMoskovich adamRozental" + CRLF);
					if (!checkSmtpServerResponse()) {
						return;
					}
					System.out.println(reader.readLine());
					System.out.println(reader.readLine());
					System.out.println(reader.readLine());
					
					os.writeBytes("AUTH LOGIN" + CRLF);
					System.out.println(reader.readLine());
					// //... 334
					encodeUserName = DatatypeConverter.printBase64Binary(userName.getBytes());
					encodePassword = DatatypeConverter.printBase64Binary(password.getBytes());
					os.writeBytes(encodeUserName + CRLF);
					System.out.println(reader.readLine());
					// /... 334
					os.writeBytes(encodePassword + CRLF);
					System.out.println(reader.readLine());
					// /.. 235

				} else {
					// after helo what should be??????
					os.writeBytes("HELO " + "tasker" + CRLF);
					if (!checkSmtpServerResponse()) {
						return;
					}
				}

				os.writeBytes("MAIL FROM: " + mailFrom + CRLF);
				if (!checkSmtpServerResponse()) {
					return;
				}
				os.writeBytes("RCPT TO: " + rcptTo + CRLF);
				if (!checkSmtpServerResponse()) {
					return;
				}
				os.writeBytes("DATA" + CRLF);
				if (!checkSmtpServerResponse()) {
					return;
				}

				// Message Data:
				os.writeBytes("Subject: " + subject + CRLF);
				os.writeBytes("From: " + fromName + CRLF);
				// check what should be written after the sender
				os.writeBytes("Sender: " + mailFrom + CRLF);
				os.writeBytes(messageData);
				os.writeBytes(CRLF + "." + CRLF);

				System.out.println(reader.readLine());
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
