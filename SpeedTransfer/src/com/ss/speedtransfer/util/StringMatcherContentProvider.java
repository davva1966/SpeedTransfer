package com.ss.speedtransfer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;

import org.eclipse.core.internal.content.ContentMessages;
import org.eclipse.core.internal.content.TextContentDescriber;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.osgi.util.NLS;

/**
 * A content describer for XHTML.
 */
@SuppressWarnings("restriction")
public class StringMatcherContentProvider extends TextContentDescriber implements IExecutableExtension {

	private static final String STRING_TO_MATCH = "string"; //$NON-NLS-1$

	private static final String CHARS_TO_READ = "charsToRead"; //$NON-NLS-1$

	private static final String IGNORE_CASE = "ignoreCase"; //$NON-NLS-1$

	private static final String IGNORE_WHITE_SPACE = "ignoreSpace"; //$NON-NLS-1$

	public static final int BUFFER_SIZE = 256;

	protected String string = null;

	protected int charsToRead = 1024;

	protected boolean ignoreCase = true;

	protected boolean ignoreWhiteSpace = true;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.content.IContentDescriber#describe(java.io. InputStream, org.eclipse.core.runtime.content.IContentDescription)
	 */
	public int describe(InputStream contents, IContentDescription description) throws IOException {

		if (description != null)
			return VALID;

		BufferedReader reader = new BufferedReader(new InputStreamReader(contents));
		return describe(reader, description);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.content.ITextContentDescriber#describe(java. io.Reader, org.eclipse.core.runtime.content.IContentDescription)
	 */
	public int describe(Reader contents, IContentDescription description) throws IOException {
		if (description != null)
			return VALID;

		String searchString = string;

		if (ignoreCase)
			searchString = searchString.toUpperCase();

		if (ignoreWhiteSpace)
			searchString = searchString.replace(" ", "");

		char[] buffer = new char[BUFFER_SIZE];
		int count = 0;
		int totalCharsRead = 0;
		int marker = 0;
		char currentChar = ' ';

		int numCharsRead;
		while ((numCharsRead = contents.read(buffer)) > 0) {
			totalCharsRead += numCharsRead;
			for (int c = 0; c < numCharsRead; c++) {

				if (ignoreWhiteSpace && (Character.toString(buffer[c]).trim().length() == 0))
					continue;

				if (ignoreCase)
					currentChar = Character.toUpperCase(buffer[c]);
				else
					currentChar = buffer[c];

				if (currentChar == searchString.charAt(count)) {
					if (count == 0)
						marker = c;
					count++;
				} else {
					if (count > 0)
						c = marker;
					count = 0;

				}

				if (count == searchString.length())
					return VALID;

			}

			if (totalCharsRead >= charsToRead)
				break;
		}
		return INVALID;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.content.IContentDescriber#getSupportedOptions()
	 */
	public QualifiedName[] getSupportedOptions() {
		return new QualifiedName[0];
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if (data instanceof String) {
			string = (String) data;
		} else if (data instanceof Hashtable) {
			Hashtable parameters = (Hashtable) data;
			string = (String) parameters.get(STRING_TO_MATCH);
			try {
				String charsToReadStr = (String) parameters.get(CHARS_TO_READ);
				if (charsToReadStr != null)
					charsToRead = Integer.parseInt(charsToReadStr);
			} catch (Exception e) {
			}
			try {
				String ignoreCaseStr = (String) parameters.get(IGNORE_CASE);
				if (ignoreCaseStr != null)
					ignoreCase = ignoreCaseStr.equalsIgnoreCase("true") || ignoreCaseStr.equalsIgnoreCase("1");
			} catch (Exception e) {
			}
			try {
				String ignoreWhiteSpaceStr = (String) parameters.get(IGNORE_WHITE_SPACE);
				if (ignoreWhiteSpaceStr != null)
					ignoreWhiteSpace = ignoreWhiteSpaceStr.equalsIgnoreCase("true") || ignoreWhiteSpaceStr.equalsIgnoreCase("1");
			} catch (Exception e) {
			}

			if (ignoreCase)
				string = string.toUpperCase();

			if (ignoreWhiteSpace)
				string = string.replace(" ", "");

		}

		if (string == null || string.trim().length() == 0) {
			String message = NLS.bind(ContentMessages.content_badInitializationData, StringMatcherContentProvider.class.getName());
			throw new CoreException(new Status(IStatus.ERROR, ContentMessages.OWNER_NAME, 0, message, null));
		}

	}
}