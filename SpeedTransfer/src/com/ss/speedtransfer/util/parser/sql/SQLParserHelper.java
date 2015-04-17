package com.ss.speedtransfer.util.parser.sql;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * The <code>SQLParserHelper</code> class
 */
public class SQLParserHelper {

	public static final String JOIN = "join";

	public static final String FULL_OUTER_JOIN = "full outer join";

	public static final String INNER_JOIN = "inner join";

	public static final String LEFT_OUTER_JOIN = "left outer join";

	public static final String RIGHT_OUTER_JOIN = "right outer join";

	public static final String[] JOIN_TYPES = { JOIN, INNER_JOIN, LEFT_OUTER_JOIN, RIGHT_OUTER_JOIN, FULL_OUTER_JOIN };

	public static final String[] FUNCTIONS = { "AVG", "COUNT", "MAX", "MIN", "SUM" };

	public static final String[] CONDITION_ITEMS = { "<", "<=", "<>", "=", ">", ">=", "Begins", "Contains", "Ends", "Like" };

	/** The sql string to parse */
	private String sql = null;

	/**
	 * Select expression from parsed sql
	 */
	private TableSelectExpression tableSelectExpr = null;

	/**
	 * Order by from parsed sql
	 */
	private List orderByList = null;

	/**
	 * Initializes a newly created <code>SQLParserHelper</code>
	 */
	public SQLParserHelper(String sql) {
		this.sql = sql;
	}

	/**
	 * Parsing the sql and saving a StatementTree
	 * 
	 * @throws ParseException
	 * @throws Exception
	 * @throws Throwable
	 */
	public void parse() throws Exception {
		/** the file instance */
		ByteArrayInputStream in = new ByteArrayInputStream(sql.getBytes());
		SimpleCharStream stream = new SimpleCharStream(in, 1, 1);
		SQLParserTokenManager lexer = new SQLParserTokenManager(stream);
		SQLParser parser = new SQLParser(lexer);
		StatementTree tree = parser.Statement();
		;

		tableSelectExpr = tree.getTableSelectExpression();
		if (tableSelectExpr == null)
			throw new Exception("Parsing error: The sql is not a valid selection statement!");
		orderByList = (List) tree.getObject("order_by");
		stream.Done();
	}

	/**
	 * Get all tables
	 * 
	 * @return all tables
	 */
	public ArrayList getTables() {
		if (tableSelectExpr != null) {
			FromClause fromClause = tableSelectExpr.getFrom_clause();
			if (fromClause != null) {
				ArrayList list = new ArrayList();
				ArrayList allTables = (ArrayList) fromClause.allTables();
				for (Iterator iter = allTables.iterator(); iter.hasNext();) {
					FromTableDef tableDef = (FromTableDef) iter.next();
					list.add(tableDef);
				}
				return list;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Get selected columns
	 * 
	 * @return selected columns
	 */
	public List getColumns() {

		if (tableSelectExpr != null) {
			List col = new ArrayList();
			ArrayList columns = tableSelectExpr.getColumns();

			for (Iterator iter = columns.iterator(); iter.hasNext();) {
				SelectColumn column = (SelectColumn) iter.next();
				col.add(column);
			}

			return col;
		} else {
			return null;
		}
	}

	/**
	 * Get group by columns
	 * 
	 * @return group by columns
	 */
	public List getGroupByColumns() {

		List cols = new ArrayList();
		if (tableSelectExpr != null) {
			List columns = tableSelectExpr.getGroup_by();
			for (Iterator iter = columns.iterator(); iter.hasNext();) {
				ByColumn column = (ByColumn) iter.next();
				String[] colArr = new String[1];
				colArr[0] = column.exp.text().toString();
				cols.add(colArr);
			}
		}

		return cols;

	}

	/**
	 * Get distinct flag
	 * 
	 * @return distinct flag
	 */
	public boolean getDistinct() {
		return tableSelectExpr.distinct;
	}

	/**
	 * Get where clause
	 * 
	 * @return where clause string
	 */
	public String getWhereClause() {
		SearchExpression searchExpr = tableSelectExpr.getWhere_clause();
		if (searchExpr != null) {
			Expression fromExpr = searchExpr.getFromExpression();
			if (fromExpr != null)
				return fromExpr.text().toString();
		}
		return null;
	}

	/**
	 * Get the order by columns
	 * 
	 * @return order by columns
	 */
	public List getOrderByColumns() {

		List cols = new ArrayList();
		if (orderByList.size() != 0) {
			for (Iterator iter = orderByList.iterator(); iter.hasNext();) {
				ByColumn column = (ByColumn) iter.next();
				String[] colArr = new String[3];

				colArr[0] = "";
				colArr[1] = column.exp.text().toString();
				colArr[2] = "";

				// Function on column?
				for (int i = 0; i < FUNCTIONS.length; i++) {
					String func = FUNCTIONS[i] + "(";
					if (colArr[1].toUpperCase().indexOf(func) > -1) {
						colArr[0] = FUNCTIONS[i];

						// Extract column name
						int from = colArr[1].indexOf("(");
						int to = colArr[1].lastIndexOf(")");
						if (from > -1 && to > -1) {
							String name = colArr[1].substring(from + 1, to).trim();
							name = name.replace(" ", "");
							colArr[1] = name;
						}

						break;

					}
				}

				// Descending ?
				if (column.ascending == false)
					colArr[2] = "desc";

				cols.add(colArr);
			}
		}

		return cols;

	}

	/**
	 * Get filter expressions
	 * 
	 * @return filter expression arraylist
	 */
	public List getFilterExpression() {

		if (tableSelectExpr == null)
			System.out.println("Parsing error: The sql is not a valid selection statement!");

		Expression whereExpr = tableSelectExpr.where_clause.getFromExpression();
		return buildFilterRows(whereExpr);

	}

	/**
	 * Get having expressions
	 * 
	 * @return having expression arraylist
	 */
	public List getHavingExpression() {

		if (tableSelectExpr == null)
			System.out.println("Parsing error: The sql is not a valid selection statement!");

		Expression havingExpr = tableSelectExpr.having_clause.getFromExpression();
		List compoundedList = buildFilterRows(havingExpr);
		List havingList = new ArrayList();
		for (Iterator iter = compoundedList.iterator(); iter.hasNext();) {
			String[] cond = (String[]) iter.next();
			String[] expandedCond = new String[8];

			expandedCond[0] = cond[0];
			expandedCond[1] = cond[1];
			expandedCond[3] = cond[2];

			// Function on value 1 ?
			for (int i = 0; i < FUNCTIONS.length; i++) {
				String func = FUNCTIONS[i] + "(";
				if (cond[2].toUpperCase().indexOf(func) > -1) {
					expandedCond[2] = FUNCTIONS[i];

					// Extract value 1
					int from = cond[2].indexOf("(");
					int to = cond[2].lastIndexOf(")");
					if (from > -1 && to > -1) {
						String name = cond[2].substring(from + 1, to).trim();
						name = name.replace(" ", "");
						expandedCond[3] = name;
					}

					break;

				}
			}

			expandedCond[4] = cond[3];
			expandedCond[6] = cond[4];

			// Function on value 2 ?
			for (int i = 0; i < FUNCTIONS.length; i++) {
				String func = FUNCTIONS[i] + "(";
				if (cond[4].toUpperCase().indexOf(func) > -1) {
					expandedCond[5] = FUNCTIONS[i];

					// Extract value 2
					int from = cond[4].indexOf("(");
					int to = cond[4].lastIndexOf(")");
					if (from > -1 && to > -1) {
						String name = cond[4].substring(from + 1, to).trim();
						name = name.replace(" ", "");
						expandedCond[6] = name;
					}

					break;
				}
			}

			expandedCond[7] = cond[5];

			havingList.add(expandedCond);

		}

		return havingList;

	}

	public List buildFilterRows(Expression exp) {

		if (exp == null)
			return new ArrayList();
		;

		ArrayList filterColl = new ArrayList();
		ArrayList syntaxColl = new ArrayList();
		expandForOper(exp, "or", syntaxColl);

		int idx = 0;
		Object element = (Object) syntaxColl.get(idx);
		while (idx < syntaxColl.size()) {
			String[] filter = new String[6];

			if (element instanceof SQLFilterSyntaxToken) {
				SQLFilterSyntaxToken token = (SQLFilterSyntaxToken) element;
				if (token.type == SQLFilterSyntaxToken.OPER) {
					filter[0] = token.value;
					idx++;
					element = (Object) syntaxColl.get(idx);
				}
				if (token.type == SQLFilterSyntaxToken.LPAR) {
					filter[1] = token.value;
					idx++;
					element = (Object) syntaxColl.get(idx);
				}
			}

			if (element instanceof SQLFilterSyntaxToken) {
				SQLFilterSyntaxToken token = (SQLFilterSyntaxToken) element;
				if (token.type == SQLFilterSyntaxToken.LPAR) {
					filter[1] = token.value;
					idx++;
					element = (Object) syntaxColl.get(idx);
				}
			}

			if (element instanceof Expression) {
				Expression e = (Expression) element;
				unpackElements(e, filter);
				idx++;
				if (idx < syntaxColl.size()) {
					element = (Object) syntaxColl.get(idx);
				}
			}

			if (element instanceof SQLFilterSyntaxToken) {
				SQLFilterSyntaxToken token = (SQLFilterSyntaxToken) element;
				if (token.type == SQLFilterSyntaxToken.RPAR) {
					filter[5] = token.value;
					idx++;
					if (idx < syntaxColl.size()) {
						element = (Object) syntaxColl.get(idx);
					}
				}
			}

			filterColl.add(filter);

		}

		return filterColl;

	}

	public void expandForOper(Expression exp, String oper, ArrayList<Object> syntaxColl) {

		String nextOper = null;
		if (oper.equalsIgnoreCase("or")) {
			nextOper = "and";
		} else {
			nextOper = "or";
		}

		ArrayList operList = new ArrayList();
		exp.breakByOperator(operList, oper);
		Expression nextExp = null;
		if (operList.size() > 1) {
			for (Iterator iter = operList.iterator(); iter.hasNext();) {
				nextExp = (Expression) iter.next();

				ArrayList tempList = new ArrayList();
				nextExp.breakByOperator(tempList, nextOper);
				if (tempList.size() > 1) {
					syntaxColl.add(new SQLFilterSyntaxToken("(", SQLFilterSyntaxToken.LPAR));
				}
				expandForOper(nextExp, nextOper, syntaxColl);
				if (tempList.size() > 1) {
					syntaxColl.add(new SQLFilterSyntaxToken(")", SQLFilterSyntaxToken.RPAR));
				}
				if (iter.hasNext()) {
					syntaxColl.add(new SQLFilterSyntaxToken(oper, SQLFilterSyntaxToken.OPER));
				}
			}
		} else {
			nextExp = (Expression) operList.get(0);
			ArrayList tempList = new ArrayList();
			nextExp.breakByOperator(tempList, nextOper);
			if (tempList.size() > 1) {
				expandForOper(nextExp, nextOper, syntaxColl);
			} else {
				syntaxColl.add(nextExp);
			}

		}

	}

	public void unpackElements(Expression exp, String[] filter) {

		filter[2] = exp.elementAt(0).toString();
		filter[4] = exp.elementAt(1).toString();

		Object element = exp.elementAt(2);
		if (element instanceof Operator) {
			String oper = element.toString();
			if (oper.equalsIgnoreCase("like")) {
				if (filter[4].endsWith("%") && filter[4].startsWith("%")) {
					filter[4] = filter[4].substring(1, filter[4].length() - 1);
					oper = "Contains";
				} else if (filter[4].endsWith("%")) {
					filter[4] = filter[4].substring(0, filter[4].length() - 1);
					oper = "Begins";
				} else if (filter[4].startsWith("%")) {
					filter[4] = filter[4].substring(1);
					oper = "Ends";
				}
			}
			filter[3] = oper;
		}

	}

	/**
	 * Get join criterias Join criterias contains String arrays with 4 element: [Main table, Join type, Joined table, Columns (comma separated)]
	 * 
	 * @return join criterias
	 */
	public List getJoinCriterias(String schemaSeparator) {
		if (schemaSeparator == null || schemaSeparator.trim().length() == 0)
			schemaSeparator = ".";

		FromClause fromClause = tableSelectExpr.getFrom_clause();
		if (fromClause != null) {
			JoiningSet joiningSet = fromClause.getJoinSet();
			if (joiningSet != null) {
				ArrayList joinList = new ArrayList();
				ArrayList joinCriteria = null;

				String mainTable = null;
				String joinType = null;
				String joinedTable = null;
				String columns = null;

				// Get all tables the first is the main table
				ArrayList allTables = (ArrayList) fromClause.allTables();

				// ------------- Add First Main table -----------------
				FromTableDef tableDef = (FromTableDef) allTables.get(0);
				if (tableDef.getAlias() != null && tableDef.getAlias().trim().length() > 0)
					mainTable = tableDef.getAlias();
				else
					mainTable = tableDef.getName();

				// ------------- Add join type ------------------------
				for (int i = 0; i < joiningSet.getTableCount() - 1; i++) {
					Expression expression = fromClause.getOnExpression(i);
					if (expression != null) {
						List onList = new ArrayList();
						if (expression.size() > 0) {
							switch (joiningSet.getJoinType(i)) {
							case JoiningSet.INNER_JOIN:
								joinType = INNER_JOIN;
								break;
							case JoiningSet.LEFT_OUTER_JOIN:
								joinType = LEFT_OUTER_JOIN;
								break;
							case JoiningSet.RIGHT_OUTER_JOIN:
								joinType = RIGHT_OUTER_JOIN;
								break;
							case JoiningSet.FULL_OUTER_JOIN:
								joinType = FULL_OUTER_JOIN;
								break;
							}

							// --------- Add joined table ---------------
							tableDef = (FromTableDef) allTables.get(i + 1);
							if (tableDef.getAlias() != null && tableDef.getAlias().trim().length() > 0)
								joinedTable = tableDef.getAlias();
							else
								joinedTable = tableDef.getName();
							// --------- Add columns --------------------
							Expression onExpr = fromClause.getOnExpression(i);
							onList = buildFilterRows(onExpr);
							columns = onExpr.text().toString();
						}

						// Adjust schema separator
						mainTable = mainTable.replace(".", schemaSeparator);
						joinedTable = joinedTable.replace(".", schemaSeparator);

						// Add to joinList
						joinCriteria = new ArrayList();
						if (i == 0)
							joinCriteria.add(mainTable);
						else
							joinCriteria.add("");
						joinCriteria.add(joinType);
						joinCriteria.add(joinedTable);
						joinCriteria.add(onList);
						joinList.add(joinCriteria);
					}
				}
				return joinList;
			}
		}
		return null;
	}

}