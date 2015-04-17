package com.ss.speedtransfer.util.sql;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class SQLWhitespaceDetector implements IWhitespaceDetector {

	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
