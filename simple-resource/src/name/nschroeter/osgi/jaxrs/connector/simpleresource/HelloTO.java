package name.nschroeter.osgi.jaxrs.connector.simpleresource;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HelloTO {

	@JsonProperty("message")
	private String message = "Hello World";

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
