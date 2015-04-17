// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

public class SQLFilterSyntaxToken {

	public static final int LPAR = 1;

	public static final int RPAR = 2;

	public static final int OPER = 3;

	public int type = 0;

	public String value = "";

	public SQLFilterSyntaxToken(String value, int type) {
		super();
		this.type = type;
		this.value = value;

	}

	public String toString() {
		return value;

	}

}
