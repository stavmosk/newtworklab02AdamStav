import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle the parsing of HTTP Request.
 * 
 * @author stavmosk
 */
public class HttpParser {

	private final static String CRLF = "\r\n";

	/**
	 * The possible Methods to handle.
	 * 
	 * @author stavmosk
	 * 
	 */
	public enum Method {
		HEAD, GET, POST, TRACE, OPTIONS
	};

	private String version;
	private Map<String, String> cookies;
	private Map<String, String> headers;
	private Map<String, String> params;
	private Method method;
	private String path;
	private String query;
	private int contentLen = -1;
	private String[] headerRequestSeperatedToLines;
	private String body;

	public HttpParser() {
		method = null;
		params = new HashMap<String, String>();
		headers = new HashMap<String, String>();
		cookies = new HashMap<String, String>();
	}

	/**
	 * parse the given query and insert each set of key and value (key=value) to
	 * the params map.
	 * 
	 * @param query
	 */
	private void parseParams(String query) {

		String decode = null;
		try {
			decode = URLDecoder.decode(query, "utf-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (decode != null) {
			Pattern pattern = Pattern.compile("(\\w+)=([^&]+)&?");
			Matcher matcher = pattern.matcher(decode);
			while (matcher.find()) {
				if (matcher.group(1) != null && matcher.group(2) != null)
					if (params.get(matcher.group(1).toLowerCase()) == null) {
						params.put(matcher.group(1).toLowerCase(), matcher
								.group(2).toLowerCase());
					}
			}
		}
	}

	/**
	 * Read and parse an HTTP request.
	 * 
	 */
	public boolean parse(String httpRequest) {
		String pathAndQuery = null;

		// Separate the request to lines.
		if (!seperateRequestToLines(httpRequest)) {
			return false;
		}

		// Gets the header line
		String firstLine = headerRequestSeperatedToLines[0];
		if (firstLine == null) {
			return false;
		}

		// Parse the header line
		Pattern urlPattern = Pattern
				.compile("^([A-Z]+) (/.*) HTTP/([0-9]\\.[0-9])$");
		Matcher matcher = urlPattern.matcher(firstLine);
		if (matcher.find()) {
			method = Method.valueOf(matcher.group(1).trim());
			pathAndQuery = matcher.group(2).trim();
			version = matcher.group(3).trim();
		} else {
			return false;
		}

		// Splits the URL to path and query
		if (pathAndQuery != null) {
			if (pathAndQuery.contains("?")) {
				int index = pathAndQuery.indexOf("?");
				query = pathAndQuery.substring(index + 1);
				path = pathAndQuery.substring(0, index);
				parseParams(query);
			} else {
				path = pathAndQuery;
			}
		}

		// Parse the headers.
		if (!parseHeaders(headerRequestSeperatedToLines)) {
			return false;
		}
		
		addCookie();

		// Parse the parameters in the body.
		if (method.name().equals("POST") && body != null) {
			if (contentLen != -1) {
				String result = setPostBodyToQuery(body
						.substring(0, contentLen));
				parseParams(result);
			}
		}

		return true;
	}

	/**
	 * Change the body of post (key=value crlf key=value) to the query form
	 * (key=value&key=value)
	 * 
	 * @param body
	 * @return
	 */
	private String setPostBodyToQuery(String body) {
		String result = body.replaceAll(CRLF, "&");
		return result;
	}

	private boolean seperateRequestToLines(String request) {
		String[] requestAndBody = request.split(CRLF + CRLF); // separates the
																// request part
																// from
																// the body part
		headerRequestSeperatedToLines = requestAndBody[0].split(CRLF); // splits
																		// the
																		// request
																		// to
		if (requestAndBody.length > 1) {
			body = requestAndBody[1];
		}

		if (requestAndBody.length > 2) {
			return false;
		}

		return true;
	}

	/**
	 * Read and parse the headers into the headers map.
	 * 
	 */
	protected boolean parseHeaders(String[] httpRequest) {
		Pattern p = Pattern.compile("([^:]+): ?([^(\r\n)]+)");

		String line;
		for (int i = 1; i < httpRequest.length; i++) {
			line = httpRequest[i];
			Matcher m = p.matcher(line);
			if (m.find()) {
				if (m.group(1) != null && m.group(2) != null) {
					headers.put(m.group(1).toLowerCase(), m.group(2));
				}
			}
		}

		if (headers.containsKey("content-length")) {
			try {
				contentLen = Integer.parseInt(headers.get("content-length"));
			} catch (Exception e) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Return the request method.
	 * 
	 * @return
	 */
	public Method getMethod() {
		return method;
	}

	/**
	 * Return the request path.
	 * 
	 * @return
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Return the request query.
	 * 
	 * @return
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * Return the request version.
	 * 
	 * @return
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Return the value of a parameter.
	 * 
	 */
	public String getParam(String param) {
		return params.get(param);
	}

	public String getHeader(String header) {
		return headers.get(header.toLowerCase());
	}

	public Map<String, String> getParams() {
		return params;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setPath(String value) {
		path = value;
	}

	public Map<String, String> getCookies() {
		return cookies;
	}

	public void setCookies(Map<String, String> cookies) {
		this.cookies = cookies;
	}

	public void addCookie() {
		if (headers.containsKey("cookie")) {
			Pattern pattern = Pattern.compile("(\\w+)=([^;]+);?");
			Matcher matcher = pattern.matcher(headers.get("cookie"));
			while (matcher.find()) {
				if (matcher.group(1) != null && matcher.group(2) != null) {
					if (matcher.group(1).equals(Consts.USERMAIL)) {
						cookies.put(Consts.USERMAIL, matcher.group(2));
					}
				}
			}
		}
	}
}
