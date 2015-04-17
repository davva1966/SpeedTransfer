package com.ss.speedtransfer.util.sql;

import java.lang.reflect.Field;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

import com.ss.speedtransfer.util.parser.sql.SQLParserConstants;


public class SQLScanner extends RuleBasedScanner {

	public SQLScanner(SQLColorManager manager) {

		IToken stringToken = new Token(new TextAttribute(manager.getColor(ISQLColorConstants.STRING)));
		IToken numberToken = new Token(new TextAttribute(manager.getColor(ISQLColorConstants.NUMBER)));
		IToken keywordToken = new Token(new TextAttribute(manager.getColor(ISQLColorConstants.KEYWORD)));

		IWhitespaceDetector whiteSpaceDetector = new IWhitespaceDetector() {

			public boolean isWhitespace(char c) {
				return Character.isWhitespace(c);
			}
		};

		IRule keywordRule = buildKeywordRule(keywordToken);

		IRule[] rules = new IRule[5];
		rules[0] = new MultiLineRule("\"", "\"", stringToken, '\\');
		rules[1] = new MultiLineRule("'", "'", stringToken, '\\');
		rules[2] = new WhitespaceRule(whiteSpaceDetector);
		rules[3] = new NumberRule(numberToken);
		rules[4] = keywordRule;

		setRules(rules);

	}

	protected IRule buildKeywordRule(IToken keywordToken) {

		KeywordRule rule = new KeywordRule(new IWordDetector() {

			public boolean isWordStart(char c) {
				return Character.isJavaIdentifierStart(c);
			}

			public boolean isWordPart(char c) {
				return Character.isJavaIdentifierPart(c);
			}
		});

		Field[] keywordFields = SQLParserConstants.class.getDeclaredFields();
		for (int i = 0; i < keywordFields.length; i++) {
			Field field = keywordFields[i];
			String name = field.getName();
			rule.addWord(name, keywordToken);
		}

		rule.addWord("group", keywordToken);
		rule.addWord("order", keywordToken);
		rule.addWord("by", keywordToken);

		return rule;

	}

}
