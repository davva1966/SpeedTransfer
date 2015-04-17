// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.sql.Types;

import com.ss.speedtransfer.util.parser.sql.Expression;
import com.ss.speedtransfer.util.parser.sql.SQLParserHelper;
import com.ss.speedtransfer.util.parser.sql.SelectColumn;


/**
 * The <code>SQLColumn</code> class
 */
public class SQLColumn extends Object {

	/** Column table */
	public String table = "";

	/** Column name */
	public String name = "";

	/** Column remark */
	public String remark = "";

	/** Column dataType */
	public String dataType = "";

	/** Column data type name */
	public String dataTypeName = "";

	/** Column length */
	public String length = "";

	/** Column decimals */
	public String decimals = "";

	/** The alias of this column */
	public String alias = "";

	/** Table qualified */
	public boolean qualified = false;

	/** The function used on this column */
	public boolean hasFunction = false;

	/** The aggregate function used on this column */
	public String aggregateFunction;

	/** Is the aggregate function distinct */
	public boolean aggregateFunctionDistinct = false;

	/** Column sql string */
	public String sqlString = "";

	public SQLColumn() {
		super();
	}

	public SQLColumn(SQLColumn column) {
		super();
		table = column.table;
		name = column.name;
		remark = column.remark;
		dataType = column.dataType;
		dataTypeName = column.dataTypeName;
		length = column.length;
		decimals = column.decimals;
		alias = column.alias;
		qualified = column.qualified;
		hasFunction = column.hasFunction;
		aggregateFunction = column.aggregateFunction;
		aggregateFunctionDistinct = column.aggregateFunctionDistinct;
		sqlString = column.sqlString;

	}

	public SQLColumn(SelectColumn column) {
		super();
		if (column != null)
			parseSelectColumn(column);
	}

	public String getQualifiedName() {
		StringBuffer qualName = new StringBuffer();
		if (table != null && table.trim().length() > 0) {
			qualName.append(table);
			qualName.append(".");
		}

		qualName.append(name);

		return qualName.toString();

	}

	public String getDisplayName() {
		if (alias != null && alias.trim().length() > 0)
			return alias;
		else if (!hasFunction)
			return name;
		else
			return "";

	}

	public String getDescription() {
		if (hasFunction) {
			if (alias != null && alias.trim().length() > 0) {
				int idx = sqlString.toUpperCase().indexOf(" AS ");
				if (idx > -1)
					return sqlString.substring(0, idx);
			}
			return sqlString;
		}
		return null;

	}

	protected void parseSelectColumn(SelectColumn column) {
		if (column.alias != null && column.alias.trim().length() > 0)
			alias = column.alias;

		Expression e = column.expression;
		String expString = e.text().toString().trim();

		// Normal column
		if (expString.matches("[a-öA-Ö_0-9]+\\.?\\w*")) {
			int idx = expString.indexOf(".");
			if (idx > -1) {
				table = expString.substring(0, idx);
				name = expString.substring(idx + 1);
				qualified = true;
			} else {
				name = expString;
			}
			sqlString = expString;
			hasFunction = false;
		} else {
			hasFunction = true;

			// Translate expression
			String translated = expString.replace("distinct_count(", "count(distinct ");
			translated = translated.replace("distinct_avg(", "avg(distinct ");
			translated = translated.replace("distinct_min(", "min(distinct ");
			translated = translated.replace("distinct_max(", "max(distinct ");
			translated = translated.replace("distinct_sum(", "sum(distinct ");
			translated = translated.replace("sql_trim('both',' ',", "trim(");
			String translatedUpper = translated.toUpperCase();

			// Find aggregate function
			aggregateFunction = null;
			for (int i = 0; i < SQLParserHelper.FUNCTIONS.length; i++) {
				String func = SQLParserHelper.FUNCTIONS[i] + "(";
				if (translatedUpper.indexOf(func) > -1) {
					aggregateFunction = SQLParserHelper.FUNCTIONS[i];
					break;
				}
			}

			// Distinct ?
			if (aggregateFunction != null) {
				if (translatedUpper.indexOf("DISTINCT ") > -1)
					aggregateFunctionDistinct = true;

				// Find name
				int from = translated.indexOf("(");
				int to = translated.lastIndexOf(")");
				if (from > -1 && to > -1) {
					name = translated.substring(from + 1, to).trim();
					if (aggregateFunctionDistinct) {
						from = name.toUpperCase().indexOf("DISTINCT ") + "DISTINCT ".length();
						name = name.substring(from).trim();
					}
					name = name.replace(" ", "");
				}

				// Find table
				int idx = name.indexOf(".");
				if (idx > -1) {
					String work = name;
					table = work.substring(0, idx).trim();
					name = work.substring(idx + 1).trim();
					qualified = true;
				}

				// If name is an expression set this column as a calculated
				// value
				if (name.indexOf("+") > -1 || name.indexOf("-") > -1 || name.indexOf("*") > -1 || name.indexOf("/") > -1) {
					aggregateFunction = "";
					aggregateFunctionDistinct = false;
					name = translated;
					table = "";
					qualified = false;
				}
			}

			else {
				name = translated;
			}

			sqlString = translated;
		}

	}

	public void updateSQLString() {
		StringBuffer sql = new StringBuffer();
		if (!hasFunction) {
			if (qualified)
				sql.append(table.trim() + ".");
			sql.append(name.trim());
		} else if (aggregateFunction != null && aggregateFunction.trim().length() > 0) {
			sql.append(aggregateFunction.toLowerCase());
			sql.append("(");
			if (aggregateFunctionDistinct)
				sql.append("distinct ");
			if (qualified)
				sql.append(table.trim() + ".");
			sql.append(name.trim());
			sql.append(")");
		} else {
			sql.append(sqlString.trim());
		}

		sqlString = sql.toString();
	}

	public boolean isNumeric() {

		try {
			int dt = Integer.parseInt(dataType);
			switch (dt) {
			case Types.BIGINT:
				return true;
			case Types.DATE:
				return true;
			case Types.DECIMAL:
				return true;
			case Types.DOUBLE:
				return true;
			case Types.FLOAT:
				return true;
			case Types.INTEGER:
				return true;
			case Types.NUMERIC:
				return true;
			case Types.REAL:
				return true;
			case Types.SMALLINT:
				return true;
			case Types.TIME:
				return true;
			case Types.TIMESTAMP:
				return true;
			case Types.TINYINT:
				return true;
			default:
				break;
			}

			return false;
		} catch (Exception e) {
		}

		return false;

	}
}
