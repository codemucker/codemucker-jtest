package org.codemucker.jtest;

public class JTestException extends RuntimeException {

	private static final long serialVersionUID = -8058778474987168811L;

	public JTestException(String message, Throwable cause) {
		super(message, cause);
	}

	public JTestException(String message) {
		super(message);
	}

	public JTestException(String message, Object... args) {
		super(String.format(message, args));
	}

}
