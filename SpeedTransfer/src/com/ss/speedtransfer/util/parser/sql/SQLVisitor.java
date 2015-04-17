//{{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

/**
 * The <code>SQLVisitor</code> class
 */
public interface SQLVisitor {

	public boolean visit(StatementTree stmt, Object context);

	public void visitEnd(StatementTree stmt, Object context);

	public boolean visit(TableSelectExpression stmt, Object context);

	public void visitEnd(TableSelectExpression stmt, Object context);

	public boolean visit(FromClause stmt, Object context);

	public void visitEnd(FromClause stmt, Object context);

	public boolean visit(SearchExpression stmt, Object context);

	public void visitEnd(SearchExpression stmt, Object context);
}
