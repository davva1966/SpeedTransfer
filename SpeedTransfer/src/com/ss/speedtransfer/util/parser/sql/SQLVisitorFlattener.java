// {{CopyrightNotice}}

package com.ss.speedtransfer.util.parser.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import com.ss.speedtransfer.util.BackupGenerationStreamImpl;
import com.ss.speedtransfer.util.GenerationStream;


/**
 * The <code>SQLVisitorFlattener</code> class
 */
public class SQLVisitorFlattener implements SQLVisitor {

	/** If the statement contains join */
	protected boolean hasJoin = false;

	/** If everything is selected from a table */
	protected boolean hasSelectAll = false;

	/** If the hasSelectAll variable has been set */
	protected boolean selectAllSet = false;

	/** If the statement contains group by */
	protected boolean hasGroupBy = false;

	/** The first table name */
	protected String firstTableName = null;

	/** The stream the flattener is writing to */
	protected GenerationStream stream = null;

	/**
	 * Initializes a newly created <code>SQLVisitorFlattener</code>
	 */
	public SQLVisitorFlattener() {
		this.stream = new BackupGenerationStreamImpl();
	}

	/**
	 * Initializes a newly created <code>SQLVisitorFlattener</code>
	 */
	public SQLVisitorFlattener(GenerationStream stream) {
		this.stream = stream;
	}

	/**
	 * Get the written SQL
	 * 
	 * @return
	 */
	public String getSQL() {
		if (stream instanceof BackupGenerationStreamImpl)
			return ((BackupGenerationStreamImpl) stream).getBuffer();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ibs.parser.sql.SQLVisitor#visit(net.ibs.parser.sql.StatementTree, java.lang.Object)
	 */
	public boolean visit(StatementTree stmt, Object context) {
		String type = stmt.getType();
		if (type.equalsIgnoreCase("select"))
			flattenSelect(stmt, context);
		else if (type.equalsIgnoreCase("insert"))
			flattenInsert(stmt, context);
		else if (type.equalsIgnoreCase("delete"))
			flattenDelete(stmt, context);
		else if (type.equalsIgnoreCase("updatetable"))
			flattenUpdate(stmt, context);
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ibs.parser.sql.SQLVisitor#visit(net.ibs.parser.sql.TableSelectExpression , java.lang.Object)
	 */
	public boolean visit(TableSelectExpression stmt, Object context) {

		stream.print("SELECT ");

		if (stmt.distinct)
			stream.print("DISTINCT ");

		if (stmt.top > 0)
			stream.print("TOP(" + stmt.top + ") ");

		ArrayList columns = stmt.getColumns();
		for (int j = 0; j < columns.size(); j++) {
			SelectColumn selcol = (SelectColumn) columns.get(j);
			if (j > 0)
				stream.print(",");

			visit(selcol, context);
		}

		stream.print(" FROM ");

		FromClause fromClause = stmt.getFrom_clause();
		fromClause.acceptVisitor(this, context);

		SearchExpression whereExp = (SearchExpression) stmt.getWhere_clause();
		if (whereExp != null && whereExp.getFromExpression() != null) {
			stream.print(" WHERE ");
			whereExp.acceptVisitor(this, context);
		}

		if (stmt.getNext_composite() != null) {
			if (stmt.getComposite_function() == CompositeTable.UNION) {
				stream.print(" UNION ");
				if (stmt.is_composite_all)
					stream.print("ALL ");
				visit(stmt.getNext_composite(), context);

			}
		}

		ArrayList<ByColumn> groupby = (ArrayList<ByColumn>) stmt.getGroup_by();
		if (groupby != null && groupby.size() > 0) {
			stream.print(" GROUP BY ");
			hasGroupBy = true;
		}
		for (int i = 0; groupby != null && i < groupby.size(); i++) {
			ByColumn col = groupby.get(i);
			if (i > 0)
				stream.print(",");
			visit(col, context);
		}

		if (groupby != null) {
			SearchExpression havingExp = (SearchExpression) stmt.having_clause;
			if (havingExp != null && havingExp.getFromExpression() != null) {
				stream.print(" HAVING ");
				havingExp.acceptVisitor(this, context);
			}
		}

		return true;
	}

	/**
	 * Visit FromClause
	 */
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
			if (type == JoiningSet.LEFT_OUTER_JOIN)
				stream.print(" LEFT OUTER JOIN ");
			else if (type == JoiningSet.RIGHT_OUTER_JOIN)
				stream.print(" RIGHT OUTER JOIN ");
			else if (type == JoiningSet.FULL_OUTER_JOIN)
				stream.print(" FULL OUTER JOIN ");
			else if (type == JoiningSet.INNER_JOIN && joinset.getOnExpression(i - 1) != null)
				stream.print(" INNER JOIN ");
			else
				stream.print(" , ");

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

	/**
	 * Visit end FromClause
	 */
	public void visitEnd(FromClause stmt, Object context) {

	}

	/**
	 * Visit order by column
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visit(SelectColumn stmt, Object context) {

		// Only set the flag for the first select clause
		if (!selectAllSet) {
			if (stmt.glob_name != null)
				hasSelectAll = true;
			else
				hasSelectAll = false;

			selectAllSet = true;
		}

		if (stmt.expression != null) {
			visit(stmt.expression, new Stack());
			if (stmt.alias != null && stmt.alias.length() > 0) {
				stream.print(" AS ");
				if (stmt.alias.trim().startsWith("\"") == false)
					stream.print("\"");
				stream.print(stmt.alias);
				if (stmt.alias.trim().endsWith("\"") == false)
					stream.print("\"");
			}
		} else if (stmt.glob_name != null) {
			stream.print(stmt.glob_name.toString());
		}

		return true;
	}

	/**
	 * Visit SearchExpression
	 */
	public boolean visit(SearchExpression stmt, Object context) {

		Expression exp = stmt.getFromExpression();
		if (exp != null) {
			visit(exp, new Stack());
		}

		return true;
	}

	/**
	 * Visit SearchExpression end
	 */
	public void visitEnd(SearchExpression stmt, Object context) {

	}

	/**
	 * Visit table name
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visit(TableName table, Object context) {

		String schema = table.getSchema();
		String name = table.getName();
		if (schema != null && schema.length() > 0)
			name = schema + "." + name;

		// if(firstTableName == null)
		// firstTableName = name;

		visitTableName(name, context);

		return true;
	}

	/**
	 * Visit from table
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visit(FromTableDef def, Object context) {

		// Subquery
		if (def.isSubQueryTable()) {
			visitTableObject(def.getTableSelectExpression(), context);
			// stream.print(" ");
		} else {
			visitTableName(def.getName(), context);
		}

		if (def.getAlias() != null && def.getAlias().length() > 0) {
			stream.print(" AS ");
			stream.print(def.getAlias());
			// if(firstTableName == null)
			// firstTableName = def.getAlias();
		} else {
			// if(firstTableName == null)
			// firstTableName = def.getName();
		}

		return true;
	}

	/**
	 * Visit expression
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	private void visitExpressionObject(Object stmt, Object context) {
		if (stmt instanceof Operator) {
			visit((Operator) stmt, context);
		} else if (stmt instanceof Variable) {
			visit((Variable) stmt, context);
		} else if (stmt instanceof TType) {
			visit((TType) stmt, context);
		} else if (stmt instanceof TObject) {
			visit((TObject) stmt, context);
		} else if (stmt instanceof FunctionDef) {
			visit((FunctionDef) stmt, context);
		} else if (stmt instanceof TableSelectExpression) {
			visit((TableSelectExpression) stmt, context);
		}
	}

	/**
	 * Visit expression
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	protected void visitExpressionObject(Operator oper, Variable var1, Variable var2, Object context) {
		visit(var2, context);
	}

	/**
	 * Visit a select statement inside another select statement
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visitTableObject(TableSelectExpression stmt, Object context) {
		stream.print("(");
		visit(stmt, context);
		stream.print(")");
		return true;
	}

	/**
	 * Visit a select statement inside another select statement
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visitParameterSubstitution(ParameterSubstitution stmt, Object context) {
		stream.print("?");
		return true;
	}

	/**
	 * Visit expression
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visit(Expression stmt, Stack context) {

		int size = stmt.size();
		if (size <= 1) {
			Object o = stmt.elementAt(0);
			visitExpressionObject(o, context);
			return true;
		}

		if (size <= 3) {

			if (size <= 2) {
				Object o = stmt.elementAt(0);
				if (o instanceof TableSelectExpression) {
					o = stmt.elementAt(1);
					if (o instanceof Operator) {
						visitExpressionObject(o, context);
						o = stmt.elementAt(0);
						visitTableObject((TableSelectExpression) o, context);
					} else {
						throw new RuntimeException("Failed to flatten SQL:" + stmt.toString());
					}
				} else {
					visitExpressionObject(o, context);
					o = stmt.elementAt(1);
					visitExpressionObject(o, context);
				}
				return true;

			} else {

				Object o = stmt.elementAt(0);
				Object o2 = stmt.elementAt(1);
				Object o3 = stmt.elementAt(2);

				if (o3 instanceof Operator) {
					visitExpressionObject(o, context);
					visitExpressionObject(o3, context);
					if (o2 instanceof Variable && o instanceof Variable) {
						visitExpressionObject((Operator) o3, (Variable) o, (Variable) o2, context);
					} else if (o2 instanceof TableSelectExpression) {
						visitTableObject((TableSelectExpression) o2, context);
					} else if (o2 instanceof ParameterSubstitution) {
						visitParameterSubstitution((ParameterSubstitution) o2, context);
					} else {
						visitExpressionObject(o2, context);
					}
					return true;
				}
			}

		}

		Object oper = stmt.last();

		Expression[] exps = stmt.split();
		Expression exp1 = exps[0];
		Expression exp2 = exps[1];

		boolean parantese = context.size() > 0;
		if (parantese) {
			stream.print("(");
			context.pop();
		}

		int size1 = exp1.size();

		if (size1 > 3 && (oper instanceof Operator.OrOperator || exp1.last() instanceof Operator.OrOperator))
			context.push("parant");

		visit(exp1, context);

		visitExpressionObject(oper, context);

		int size2 = exp2.size();

		if (size2 > 3 && (oper instanceof Operator.OrOperator || exp2.last() instanceof Operator.OrOperator))
			context.push("parant");

		visit(exp2, context);

		if (parantese)
			stream.print(")");

		return true;
	}

	/**
	 * Visit Variable
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visit(Variable stmt, Object context) {

		TableName table = stmt.getTableName();
		if (table != null) {
			visit(table, context);
			stream.print(".");
		}
		String column = stmt.getName();
		stream.print(column);

		return true;
	}

	/**
	 * Visit operator
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visit(Operator stmt, Object context) {

		stream.print(" ");
		if (stmt.getSubQueryFormRepresentation() == Operator.NONE)
			stream.print(stmt.toString());
		else if (stmt.getSubQueryFormRepresentation() == Operator.ANY) {
			if (stmt.is("exists"))
				stream.print("EXISTS");
			else
				stream.print("IN");
		} else if (stmt.getSubQueryFormRepresentation() == Operator.ALL)
			stream.print("NOT IN");
		stream.print(" ");

		return true;
	}

	/**
	 * Visit type
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visit(TType stmt, Object context) {

		stream.print(stmt.toString());

		return true;
	}

	/**
	 * Visit type
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visit(FunctionDef stmt, Object context) {

		String name = stmt.getName();
		Expression[] expList = stmt.getParameters();

		stream.print(name);
		stream.print("(");
		for (int i = 0; i < expList.length; i++) {
			Expression exp = expList[i];
			// visit(exp, new Stack());
			if (i > 0)
				stream.print(", ");
			visit(exp, new Stack());
		}
		stream.print(")");
		return true;
	}

	/**
	 * Visit type
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visit(TObject stmt, Object context) {

		boolean isString = stmt.getTType() == TType.STRING_TYPE;
		boolean isArray = stmt.getTType() == TType.ARRAY_TYPE;

		if (isString)
			stream.print("'");
		else if (isArray)
			stream.print("(");

		if (isArray) {

			Expression[] array = (Expression[]) stmt.getObject();
			for (int i = 0; array != null && i < array.length; i++) {
				if (i > 0)
					stream.print(",");
				visit(array[i], new Stack());
			}

		} else {
			stream.print(stmt.toString());
		}

		if (isString)
			stream.print("'");
		else if (isArray)
			stream.print(")");
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ibs.parser.sql.SQLVisitor#visitEnd(net.ibs.parser.sql.StatementTree, java.lang.Object)
	 */
	public void visitEnd(StatementTree stmt, Object context) {
		StatementTree stmtChild = stmt.getStatementChild();
		ArrayList<ByColumn> orderby = (ArrayList<ByColumn>) stmt.getObject("order_by");

		for (int i = 0; orderby != null && i < orderby.size(); i++) {
			if (i == 0)
				stream.print(" ORDER BY ");

			ByColumn col = orderby.get(i);

			if (i > 0)
				stream.print(",");

			visit(col, context);
		}
		if (stmtChild != null)
			visitEnd(stmtChild, context);

	}

	/**
	 * Visit order by column
	 * 
	 * @param stmt
	 * @param context
	 * @return
	 */
	public boolean visit(ByColumn stmt, Object context) {
		visit(stmt.exp, new Stack());
		if (!stmt.ascending)
			stream.print(" DESC");

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.ibs.parser.sql.SQLVisitor#visitEnd(net.ibs.parser.sql. TableSelectExpression, java.lang.Object)
	 */
	public void visitEnd(TableSelectExpression stmt, Object context) {
	}

	public boolean visit(Assignment stmt, Object context) {
		stream.print(stmt.getVariable().getName());
		stream.print(" = ");
		stream.print(stmt.getExpression().text().toString());

		return true;
	}

	public boolean visitTableName(String tableName, Object context) {
		if (firstTableName == null)
			firstTableName = tableName;

		stream.print(tableName);

		return true;
	}

	protected void flattenSelect(StatementTree stmt, Object context) {
		TableSelectExpression exp = stmt.getTableSelectExpression();
		if (exp != null)
			visit(exp, context);
	}

	protected void flattenInsert(StatementTree stmt, Object context) {
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
					stream.print(",");

				stream.print(col);
			}
			stream.print(") ");
		} else
			stream.print(" ");

		// Add the data assignment
		List<List<Expression>> expressions = (List<List<Expression>>) stmt.getObject("data_list");
		if (expressions != null && expressions.size() > 0) {
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
	}

	protected void flattenDelete(StatementTree stmt, Object context) {
		stream.print("DELETE FROM ");

		// Add table name
		String tableName = (String) stmt.getObject("table_name");
		visitTableName(tableName, context);

		// Add where clause
		SearchExpression whereExp = (SearchExpression) stmt.getObject("where_clause");
		if (whereExp != null && whereExp.getFromExpression() != null) {
			stream.print(" WHERE ");
			visit(whereExp, context);
		}
	}

	protected void flattenUpdate(StatementTree stmt, Object context) {
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
			stream.print(" WHERE ");
			visit(whereExp, context);
		}
	}

	/**
	 * Return true if the sql contains a * selection
	 * 
	 * @return
	 */
	public boolean hasSelectionAll() {
		return hasSelectAll;
	}

	public String getFirstTableName() {
		return firstTableName;
	}

}
