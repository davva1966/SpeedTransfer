package com.ss.speedtransfer.util.parser.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.ss.speedtransfer.util.BackupGenerationStreamImpl;
import com.ss.speedtransfer.util.GenerationStream;
import com.ss.speedtransfer.util.StringHelper;


public class SQLVisitorFormatFlattener extends SQLVisitorFlattener {

	protected int depthCounter = 0;

	protected void flattenInsert(StatementTree stmt, Object context) {
		depthCounter++;
		stream.print("INSERT INTO ");

		// Add table name
		String tableName = (String) stmt.getObject("table_name");
		visitTableName(tableName, context);

		// Add the column list
		List<String> columns = (List<String>) stmt.getObject("col_list");
		if (columns != null && columns.size() > 0) {
			stream.print(" (");

			for (int j = 0; j < columns.size(); j++) {
				String col = columns.get(j);
				if (j > 0)
					stream.print(", ");

				stream.print(col);
			}
			stream.print(") ");
		} else
			stream.print(" ");

		// Add the data assignment
		List<List<Expression>> expressions = (List<List<Expression>>) stmt.getObject("data_list");
		if (expressions != null && expressions.size() > 0) {
			maybeNewLineAndTab();
			stream.print("VALUES (");
			for (int j = 0; j < expressions.size(); j++) {
				List<Expression> values = (List<Expression>) expressions.get(j);
				for (int k = 0; k < values.size(); k++) {
					if (k > 0)
						stream.print(", ");
					Expression exp = values.get(k);
					stream.print(exp.text().toString());

				}
			}
			stream.print(") ");
		}

		// Add the sub query
		StatementTree subquery = stmt.getStatementChild();
		if (subquery != null)
			visit(subquery, context);
		depthCounter--;
	}

	protected void flattenDelete(StatementTree stmt, Object context) {
		depthCounter++;
		stream.print("DELETE FROM ");

		// Add table name
		String tableName = (String) stmt.getObject("table_name");
		visitTableName(tableName, context);

		// Add where clause
		SearchExpression whereExp = (SearchExpression) stmt.getObject("where_clause");
		if (whereExp != null && whereExp.getFromExpression() != null) {
			maybeNewLineAndTab();
			stream.print(" WHERE ");
			visit(whereExp, context);
		}
		depthCounter--;
	}

	protected void flattenUpdate(StatementTree stmt, Object context) {
		depthCounter++;
		stream.print("UPDATE ");

		// Add table name
		String tableName = (String) stmt.getObject("table_name");
		visitTableName(tableName, context);

		// Add the data assignment
		List<Assignment> assignments = (List<Assignment>) stmt.getObject("assignments");
		if (assignments != null && assignments.size() > 0) {
			stream.print(" SET ");
			for (int i = 0; i < assignments.size(); i++) {
				Assignment asg = assignments.get(i);
				if (i > 0)
					stream.print(", ");
				visit(asg, context);
			}
		}

		// Add where clause
		SearchExpression whereExp = (SearchExpression) stmt.getObject("where_clause");
		if (whereExp != null && whereExp.getFromExpression() != null) {
			maybeNewLineAndTab();
			stream.print(" WHERE ");
			visit(whereExp, context);
		}
		depthCounter--;
	}

	public boolean visit(TableSelectExpression stmt, Object context) {

		depthCounter++;

		stream.print("SELECT ");

		if (stmt.distinct)
			stream.print("DISTINCT ");

		if (stmt.top > 0)
			stream.print("TOP(" + stmt.top + ") ");

		ArrayList columns = stmt.getColumns();
		GenerationStream savedStream = stream;
		stream = new BackupGenerationStreamImpl();

		for (int j = 0; j < columns.size(); j++) {
			SelectColumn selcol = (SelectColumn) columns.get(j);
			if (j > 0)
				stream.print(", ");

			visit(selcol, context);
		}

		String colString = ((BackupGenerationStreamImpl) stream).getBuffer();
		colString = StringHelper.wordWrapString(colString, 80, 14);

		savedStream.print(colString);
		stream = savedStream;

		maybeNewLineAndTab();
		stream.print(" FROM ");

		FromClause fromClause = stmt.getFrom_clause();
		fromClause.acceptVisitor(this, context);

		SearchExpression whereExp = (SearchExpression) stmt.getWhere_clause();
		if (whereExp != null && whereExp.getFromExpression() != null) {
			maybeNewLineAndTab();
			stream.print(" WHERE ");
			whereExp.acceptVisitor(this, context);
		}

		if (stmt.getNext_composite() != null) {
			if (stmt.getComposite_function() == CompositeTable.UNION) {
				maybeNewLineAndTab();
				stream.print(" UNION ");
				if (stmt.is_composite_all)
					stream.print("ALL ");
				visit(stmt.getNext_composite(), context);

			}
		}

		ArrayList<ByColumn> groupby = (ArrayList<ByColumn>) stmt.getGroup_by();
		if (groupby != null && groupby.size() > 0) {
			maybeNewLineAndTab();
			stream.print(" GROUP BY ");
			hasGroupBy = true;
		}
		for (int i = 0; groupby != null && i < groupby.size(); i++) {
			ByColumn col = groupby.get(i);
			if (i > 0)
				stream.print(", ");
			visit(col, context);
		}

		if (groupby != null) {
			SearchExpression havingExp = (SearchExpression) stmt.having_clause;
			if (havingExp != null && havingExp.getFromExpression() != null) {
				maybeNewLineAndTab();
				stream.print(" HAVING ");
				havingExp.acceptVisitor(this, context);
			}
		}

		depthCounter--;

		return true;
	}

	public boolean visit(FromClause stmt, Object context) {

		JoiningSet joinset = stmt.getJoinSet();
		TableName tablename = joinset.getFirstTable();
		FromTableDef def = null;

		String key = tablename.getName();
		if (key != null && key.length() > 0) {
			Iterator<FromTableDef> iter = stmt.allTables().iterator();
			while (iter.hasNext()) {
				def = iter.next();
				if (key.equals(def.getUniqueKey())) {
					break;
				}
				def = null;
			}
		}

		if (def != null)
			visit(def, context);
		else
			visit(tablename, context);

		Collection tables = stmt.allTables();
		int count = joinset.getTableCount();
		for (int i = 1; i < count; i++) {
			hasJoin = true;
			int type = joinset.getJoinType(i - 1);
			tablename = joinset.getTable(i);
			if (type == JoiningSet.LEFT_OUTER_JOIN) {
				maybeNewLineAndTab(2);
				stream.print(" LEFT OUTER JOIN ");
			} else if (type == JoiningSet.RIGHT_OUTER_JOIN) {
				maybeNewLineAndTab(2);
				stream.print(" RIGHT OUTER JOIN ");
			} else if (type == JoiningSet.FULL_OUTER_JOIN) {
				maybeNewLineAndTab(2);
				stream.print(" FULL OUTER JOIN ");
			} else if (type == JoiningSet.INNER_JOIN && joinset.getOnExpression(i - 1) != null) {
				maybeNewLineAndTab(2);
				stream.print(" INNER JOIN ");
			} else {
				stream.print(", ");
			}

			Iterator iter = tables.iterator();
			while (iter.hasNext()) {
				def = (FromTableDef) iter.next();
				String key2 = def.getUniqueKey();
				if (key2.equals(tablename.getName())) {
					visit(def, context);

					Expression onexp = joinset.getOnExpression(i - 1);
					if (onexp != null) {
						stream.print(" ON ");
						visit(onexp, new Stack());
						stream.print(" ");
					}
					break;
				}
			}
		}

		return true;
	}

	public void visitEnd(StatementTree stmt, Object context) {
		StatementTree stmtChild = stmt.getStatementChild();
		ArrayList<ByColumn> orderby = (ArrayList<ByColumn>) stmt.getObject("order_by");

		for (int i = 0; orderby != null && i < orderby.size(); i++) {
			if (i == 0) {
				maybeNewLineAndTab();
				stream.print(" ORDER BY ");
			}

			ByColumn col = orderby.get(i);

			if (i > 0)
				stream.print(", ");

			visit(col, context);
		}
		if (stmtChild != null)
			visitEnd(stmtChild, context);

	}

	protected void maybeNewLineAndTab() {
		maybeNewLineAndTab(1);

	}

	protected void maybeNewLineAndTab(int tabs) {
		if (depthCounter <= 1) {
			stream.println();
			for (int i = 0; i < tabs; i++) {
				stream.print(getTab());
			}

		}
	}

	protected static String getTab() {
		return "\t";
	}

}
