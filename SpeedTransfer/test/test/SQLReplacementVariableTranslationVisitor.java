package test;

import java.util.Map;
import java.util.Stack;

import com.ss.speedtransfer.model.QueryDefinition;
import com.ss.speedtransfer.util.GenerationStream;
import com.ss.speedtransfer.util.StringMatcher;
import com.ss.speedtransfer.util.parser.sql.Expression;
import com.ss.speedtransfer.util.parser.sql.SQLVisitorFlattener;
import com.ss.speedtransfer.util.parser.sql.TObject;
import com.ss.speedtransfer.util.parser.sql.TType;
import com.ss.speedtransfer.util.parser.sql.TableName;
import com.ss.speedtransfer.util.parser.sql.Variable;


public class SQLReplacementVariableTranslationVisitor extends SQLVisitorFlattener {

	protected Map<String, String> valueMap = null;
	protected StringMatcher sm = new StringMatcher(QueryDefinition.REP_VAR_START_MARKER, QueryDefinition.REP_VAR_STOP_MARKER);

	public SQLReplacementVariableTranslationVisitor(Map<String, String> valueMap) {
		super();
		this.valueMap = valueMap;
	}

	public SQLReplacementVariableTranslationVisitor(GenerationStream stream) {
		super(stream);
	}

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
			stream.print(translate(stmt.toString()));
		}

		if (isString)
			stream.print("'");
		else if (isArray)
			stream.print(")");
		return true;
	}

	public boolean visit(Variable stmt, Object context) {

		TableName table = stmt.getTableName();
		if (table != null) {
			visit(table, context);
			stream.print(".");
		}
		String column = stmt.getName();
		stream.print(translate(column));

		return true;
	}

	protected String translate(String s) {
		return sm.replaceDataMarkers(s, valueMap);

	}

}
