// {{CopyrightNotice}}

package com.ss.speedtransfer.util.sql;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.IFormattingStrategy;

import com.ss.speedtransfer.util.SQLHelper;
import com.ss.speedtransfer.util.SQLWarningDialog;
import com.ss.speedtransfer.util.SSUtil;
import com.ss.speedtransfer.util.StringHelper;
import com.ss.speedtransfer.util.parser.sql.ParseException;
import com.ss.speedtransfer.util.parser.sql.SQLParser;
import com.ss.speedtransfer.util.parser.sql.SQLParserTokenManager;
import com.ss.speedtransfer.util.parser.sql.SQLVisitorFormatFlattener;
import com.ss.speedtransfer.util.parser.sql.SimpleCharStream;
import com.ss.speedtransfer.util.parser.sql.StatementTree;


/**
 * The <code>SQLFormatter</code> class
 */
public class SQLFormatter implements IContentFormatter {

	/**
	 * Initializes a newly created <code>SQLFormatter</code>
	 */
	public SQLFormatter() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.formatter.IContentFormatter#format(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.IRegion)
	 */
	public void format(IDocument document, IRegion region) {
		String text = document.get();
		document.set(formatText(text));

	}

	public static String formatText(String text) {

		boolean warning = false;
		String message = "";
		StringBuilder sb = new StringBuilder();

		if (SQLHelper.hasMultipleStatements(text)) {
			List<String> stmts = SQLHelper.getStatements(text);
			for (String stmt : stmts) {

				if (stmt.endsWith("\n"))
					stmt = stmt.substring(0, stmt.length() - 1);
				if (stmt.endsWith("\r"))
					stmt = stmt.substring(0, stmt.length() - 1);
				if (stmt.startsWith("\r"))
					stmt = stmt.substring(1, stmt.length());
				if (stmt.startsWith("\n"))
					stmt = stmt.substring(1, stmt.length());

				String formattedSQL = stmt;
				try {
					formattedSQL = parseAndFormat(stmt);
				} catch (Exception e) {
					if (warning == false)
						message = SSUtil.getMessage(e);
					warning = true;
				}

				if (formattedSQL.trim().length() > 0)
					sb.append(formattedSQL);
				else
					sb.append(stmt);

				sb.append(";");
				sb.append(StringHelper.getNewLine());

			}
		} else {
			String formattedSQL = text;
			try {
				formattedSQL = parseAndFormat(text);
			} catch (Exception e) {
				if (warning == false)
					message = SSUtil.getMessage(e);
				warning = true;
			}

			if (formattedSQL.trim().length() > 0)
				sb.append(formattedSQL);
			else
				sb.append(text);
		}

		boolean doFormat = true;
		if (warning) {
			SQLWarningDialog dialog = new SQLWarningDialog("Unable to format all or parts of the SQL. The SQL could potentially have errors." + StringHelper.getNewLine() + StringHelper.getNewLine() + "Continue the format operation?", message);
			int response = dialog.open();
			if (response == 1)
				doFormat = false;
		}

		if (doFormat)
			return sb.toString();
		else
			return text;

	}

	protected static String parseAndFormat(String sql) throws ParseException {
		ByteArrayInputStream in = new ByteArrayInputStream(sql.getBytes());
		SimpleCharStream stream = new SimpleCharStream(in, 1, 1);
		SQLParserTokenManager lexer = new SQLParserTokenManager(stream);
		SQLParser parser = new SQLParser(lexer);
		StatementTree tree = parser.Statement();
		SQLVisitorFormatFlattener visitor = new SQLVisitorFormatFlattener();
		tree.acceptVisitor(visitor, null);

		return visitor.getSQL();

	}

	protected static String indentKeyword(String originalString, String keyword, int indent) {
		keyword = keyword.toUpperCase();
		String scanString = originalString.toUpperCase();
		StringBuffer buffer = new StringBuffer(originalString);

		// String lftab = getReturn() + getLineFeed();
		String lftab = getReturn();
		for (int i = 0; i < indent; i++)
			lftab += getTab();

		int idx = scanString.indexOf(" " + keyword + " ");
		while (idx >= 0) {
			buffer.insert(idx, lftab);
			scanString = buffer.toString().toUpperCase();
			idx = scanString.indexOf(" " + keyword + " ", idx + keyword.length() + lftab.length());
		}

		return buffer.toString();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.text.formatter.IContentFormatter#getFormattingStrategy(java.lang.String)
	 */
	public IFormattingStrategy getFormattingStrategy(String contentType) {
		return null;
	}

	protected static String getReturn() {
		return "\r";
	}

	protected static String getLineFeed() {
		return "\n";
	}

	protected static String getTab() {
		return "\t";
	}

}
