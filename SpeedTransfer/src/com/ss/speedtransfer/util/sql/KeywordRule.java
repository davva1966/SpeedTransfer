// {{CopyrightNotice}}

package com.ss.speedtransfer.util.sql;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * The <code>KeywordRule</code> class
 */
public class KeywordRule extends WordRule {

	/** Buffer used for pattern detection */
	private StringBuffer fBuffer = new StringBuffer();

	/**
	 * Initializes a newly created <code>KeywordRule</code>
	 * 
	 * @param detector
	 */
	public KeywordRule(IWordDetector detector) {
		super(detector);
	}

	/**
	 * Initializes a newly created <code>KeywordRule</code>
	 * 
	 * @param detector
	 * @param defaultToken
	 */
	public KeywordRule(IWordDetector detector, IToken defaultToken) {
		super(detector, defaultToken);

	}

	/*
	 * @see IRule#evaluate(ICharacterScanner)
	 */
	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();
		if (fDetector.isWordStart((char) c)) {
			if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {

				fBuffer.setLength(0);
				do {
					fBuffer.append((char) c);
					c = scanner.read();
				} while (c != ICharacterScanner.EOF && fDetector.isWordPart((char) c));
				scanner.unread();

				IToken token = (IToken) fWords.get(fBuffer.toString().toUpperCase());
				if (token != null)
					return token;

				if (fDefaultToken.isUndefined())
					unreadBuffer(scanner);

				return fDefaultToken;
			}
		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.rules.WordRule#addWord(java.lang.String, org.eclipse.jface.text.rules.IToken)
	 */
	@Override
	public void addWord(String word, IToken token) {
		super.addWord(word.toUpperCase(), token);
	}

}
