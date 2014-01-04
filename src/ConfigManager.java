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
	 * Given a key Return the config value.
	 */
	public String GetValue(String key) {
		return configMap.get(key);
	}
}
