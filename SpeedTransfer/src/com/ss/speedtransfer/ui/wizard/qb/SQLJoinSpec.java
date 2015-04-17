// {{CopyrightNotice}}

package com.ss.speedtransfer.ui.wizard.qb;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ss.speedtransfer.util.parser.sql.SQLParserHelper;


/**
 * The <code>SQLTable</code> class
 */
public class SQLJoinSpec extends Object {

	/** JOIN TYPES */
	public static final String[] JOIN_TYPE_DESC = { "Standard", "All main table rows", "All joined table rows", "All rows" };

	/** Main table name */
	public String mainTableName = "";

	/** Join type */
	public String joinType = "";

	/** Joined table name */
	public String joinedTableName = "";

	/** Expression list */
	protected List expressionList = new ArrayList();

	public SQLJoinSpec() {
		super();
	}

	public SQLJoinSpec(SQLJoinSpec joinSpec) {
		super();
		mainTableName = joinSpec.mainTableName;
		joinedTableName = joinSpec.joinedTableName;
		joinType = joinSpec.joinType;

		for (Iterator iter = joinSpec.getExpressions().iterator(); iter.hasNext();) {
			String[] expressionPart = (String[]) iter.next();
			expressionList.add(expressionPart);

		}
	}

	public SQLJoinSpec(List joinSpec) {
		super();
		if (joinSpec != null && joinSpec.size() >= 4) {
			mainTableName = (String) joinSpec.get(0);
			joinType = (String) joinSpec.get(1);
			joinedTableName = (String) joinSpec.get(2);
			expressionList = (List) joinSpec.get(3);
		}

	}

	public List getExpressions() {
		return expressionList;
	}

	/**
	 * Convert join type from real syntax to description and the other way around.
	 * 
	 * @param joinType
	 * @return
	 */
	public static String convertJoinType(String joinType) {
		for (int i = 0; i < SQLParserHelper.JOIN_TYPES.length; i++) {
			String type = SQLParserHelper.JOIN_TYPES[i];
			String typeDesc = JOIN_TYPE_DESC[i];
			if (joinType.equals(type))
				return typeDesc;
			if (joinType.equals(typeDesc))
				return type;
		}
		return "";
	}

}
