import java.io.*;
import java.util.*;

/**
 * Manage the Config File.
 * 
 * @author stavmosk
 *
 */
public class ConfigManager {

	private final String CONFIGFILE_NAME = "config.ini";
	private final String SPLITER = "=";
	private Map<String, String> configMap;

	/**
	 * A configuration manager. 
	 * Accept a file with root, port, default page and max number of threads.
	 */
	public ConfigManager() throws Exception {
		configMap = new HashMap<String, String>();

		try {
			// Open the file.
			FileInputStream fileIn = new FileInputStream(CONFIGFILE_NAME);

			// Get the object to read from the file
			DataInputStream reader = new DataInputStream(fileIn);
			BufferedReader buffer = new BufferedReader(new InputStreamReader(reader));
			String line;

			// Reads Line by Line
			while ((line = buffer.readLine()) != null) {
				String[] keyAndValue = line.split(SPLITER);

				if (keyAndValue.length > 2) {
					throw new Exception("Wrong Format");
				}
				
				if (keyAndValue[0].trim().length() == 0) {
					throw new Exception("Incorrect key");
				}

				if (keyAndValue.length == 2) {
					configMap.put(keyAndValue[0].trim(), keyAndValue[1].trim());
				}
				
				if (keyAndValue.length == 1 || keyAndValue[1].trim().length() == 0) {
					configMap.put(keyAndValue[0].trim(), "");
				}
			}
			
			reader.close();
			buffer.close();
		}
		catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Validate the default page exist
	 * 
	 */
	private boolean isValideDefaultPage(String root, String defaultPage) {
		try {
			File file = new File(root + "\\" + defaultPage);
			if (file.exists()) {
				return true;
			}
		} catch (Exception e) {
			System.err.println("Can't find the default page");
		}
		return false;
	}

	/**
	 * Validate port exist
	 */
	private boolean isValidePort(String portAsString) {
		int port;
		try { 
		port = Integer.parseInt(portAsString);
		} catch (NumberFormatException e) { 
			System.err.println("Port must be a number!");
			return false;
		}
		if (port < 0 || port > 65353) {
			System.err.println(Consts.PORT_OUT_OF_RANGE);
			return false;
		}
		return true;
	}

	/**
	 * Validate the root folder exist
	 */
	private boolean isValideRoot(String root) {
		try {
			File file = new File(root);
			if (file.exists()) {
				return true;
			}
			System.err.println("Can't find root folder");
		} catch (Exception e) {
			System.err.println("Can't find root folder");
		}
		return false;
	}
	
	private boolean notNullOrEmpty(String toCheck) { 
		if (toCheck == null || toCheck.isEmpty()) { 
			return false;
		}
			return true;
	}

	/**
	 * Validate the number of max thread is in the range.
	 */
	private boolean isValideMaxThreads(String maxThreadsAsString) {
		int maxThreads;
		try { 
			maxThreads = Integer.parseInt(maxThreadsAsString);
		} catch (NumberFormatException e) { 
			System.err.println("maxThreads must be a number!");
			return false;
		}
		if (maxThreads < 1) {
			System.err.println(Consts.MAX_THREAD_OUT_OF_RANGE);
			return false;
		}
		return true;
	}
	
	private boolean areSmtpConfigsValide() { 
		if(!notNullOrEmpty(configMap.get(Consts.CONFIG_SERVERNAME))) { 
			System.err.println("Server name can not be empty");
			return false;
		}
		
		if(!notNullOrEmpty(configMap.get(Consts.CONFIG_SMTPNAME))) { 
			System.err.println("SMTP name can not be empty");
			return false;
		}
		
		if(!notNullOrEmpty(configMap.get(Consts.CONFIG_TASKFILEPATH))) { 
			System.err.println("Tasks databse file(taskFilePath) path can not be empty");
			return false;
		}
		
		if(!notNullOrEmpty(configMap.get(Consts.CONFIG_REMINDERFILE))) { 
			System.err.println("Reminders databse file(reminderFilePath) path can not be empty");
			return false;
		}
		
		if(!notNullOrEmpty(configMap.get(Consts.CONFIG_POLLFILEPATH))) { 
			System.err.println("Polls databse file(pollFilePath) path can not be empty");
			return false;
		}
		
		return true;	
	}
	
	private boolean isAuthLoginValide() { 
		if (!configMap.get(Consts.CONFIG_ISAUTHLOGIN).equalsIgnoreCase("true") && !configMap.get(Consts.CONFIG_ISAUTHLOGIN).equalsIgnoreCase("false") ) { 
			System.err.println("SMTPIsAuthLogin must be TRUE or FALSE");
		}
		
		Boolean isAuth = Boolean.parseBoolean(configMap.get(Consts.CONFIG_ISAUTHLOGIN));
		
		if (isAuth) {
			
			if(!notNullOrEmpty(configMap.get(Consts.CONFIG_SMTPUSERNAME))) { 
				System.err.println("SMTPUsername can not be empty if isAuthLogin is TRUE");
				return false;
			}
			
			if(!notNullOrEmpty(configMap.get(Consts.CONFIG_SMTPPASSWORD))) { 
				System.err.println("SMTPPassword can not be empty if isAuthLogin is TRUE");
				return false;
			}
		}
		
		return true;
	}
	
	public boolean validateConfigFile() {
		
		if(!isValidePort(configMap.get(Consts.CONFIG_PORT)) || !isValideRoot(configMap.get(Consts.CONFIG_ROOT))
				|| !isValideMaxThreads(configMap.get(Consts.CONFIG_MAXTHREAD)) || !isValideDefaultPage(configMap.get(Consts.CONFIG_ROOT), configMap.get(Consts.CONFIG_DEFAULTPAGE))
				|| !isValidePort(configMap.get(Consts.CONFIG_SMTPPORT))) { 
			return false;	
		}
		
		if(!areSmtpConfigsValide()) { 
			return false;
		}
		
		if(!isAuthLoginValide()) { 
			return false;
		}
		
		return true;
	}

	/**
	 * Given a key Return the config value.
	 */
	public String GetValue(String key) {
		return configMap.get(key);
	}
}
