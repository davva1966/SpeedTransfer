package test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.ss.speedtransfer.util.StringHelper;


public class ReplacementVarTransl {

	public static void main(String[] args) {

		List<String> emptyVars = new ArrayList<String>();
		emptyVars.add("|var1|");
		emptyVars.add("|var2|");

		try {
			// String sql = "SELECT col1 FROM mytable  WHERE col2 >     (SELECT orderno FROM mytable1      WHERE backorder ='Y')";
			// String sql =
			//String sql = "SELECT col1 FROM mytable  WHERE col2 >     (SELECT orderno FROM mytable1      WHERE backorder =|var1| and backorder = var2 and col2 = 33 or berry = '|var1|') GROUP BY col1 HAVING havcol > '|var2|' and dag > '45'";
			 String sql = "SELECT col1 FROM mytable  WHERE backorder =var1 and backorder = var2 and berry = 'var1'";

			String newSql = removeEmptyVars(sql, emptyVars);
			System.out.println(newSql);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String removeEmptyVars(String sql, List<String> emptyVars) {

		NavigableMap<Integer, String> whereAndOrIndexes = getFilterAndOrIndexMap(sql);
		Map<Integer, String> emptyVarIndexMap = getEmptyVarIndexMap(emptyVars, sql);

		StringBuilder sb = new StringBuilder(sql);

		for (Integer pos : emptyVarIndexMap.keySet()) {
			int operKey = whereAndOrIndexes.lowerKey(pos);
			String oper = whereAndOrIndexes.get(operKey);
			int start = operKey;
			if (oper.equals("WHERE") || oper.equals("HAVING"))
				start = start + oper.length() + 1;

			int end = pos + emptyVarIndexMap.get(pos).length();
			if (sb.charAt(end) == '\'')
				end++;
			sb.replace(start, end, StringHelper.stringWithLength(end - start));
		}

		String newSql = sb.toString();

		newSql = cleanup(newSql, "WHERE");
		newSql = cleanup(newSql, "HAVING");

		return newSql;
	}

	protected static String cleanup(String sql, String clause) {

		// Cleanup, remove orphaned AND, OR and empty clauses
		NavigableMap<Integer, String> componentMap = new TreeMap<Integer, String>();
		String[] sqlComponents = sql.split(" ");
		int compNumber = 0;
		for (int i = 0; i < sqlComponents.length; i++) {
			if (sqlComponents[i].trim().length() > 0)
				componentMap.put(compNumber++, sqlComponents[i].trim());
		}

		List<String> remainingComponents = new ArrayList<String>();
		for (Integer key : componentMap.keySet()) {
			String component = componentMap.get(key);
			if (component.equalsIgnoreCase("OR") || component.equalsIgnoreCase("AND")) {
				int previousCompKey = componentMap.lowerKey(key);
				String previousComp = componentMap.get(previousCompKey);
				if (previousComp.equalsIgnoreCase(clause))
					continue;
			}
			if (component.equalsIgnoreCase(clause)) {
				int nextCompKey = componentMap.higherKey(key);
				String nextComp = componentMap.get(nextCompKey);
				if (nextComp == null || nextComp.equalsIgnoreCase(")"))
					continue;
			}
			remainingComponents.add(component);
		}

		StringBuilder sb = new StringBuilder();
		for (String component : remainingComponents) {
			sb.append(component);
			sb.append(" ");
		}

		return sb.toString();
	}

	protected static NavigableMap<Integer, String> getFilterAndOrIndexMap(String sql) {
		NavigableMap<Integer, String> filterAndOrIndexes = new TreeMap<Integer, String>();
		filterAndOrIndexes.putAll(getFilterIndexMap(sql, "WHERE"));
		filterAndOrIndexes.putAll(getFilterIndexMap(sql, "HAVING"));
		filterAndOrIndexes.putAll(getAndOrIndexMap(sql));

		return filterAndOrIndexes;

	}

	protected static NavigableMap<Integer, String> getFilterIndexMap(String sql, String clause) {
		NavigableMap<Integer, String> clauseIndexes = new TreeMap<Integer, String>();
		String tempSQL = sql.toUpperCase();

		int idx = tempSQL.indexOf(" " + clause + " ");
		while (idx > -1) {
			clauseIndexes.put(idx, clause);
			idx = tempSQL.indexOf(" " + clause + " ", idx + 1);
		}

		return clauseIndexes;

	}

	protected static NavigableMap<Integer, String> getAndOrIndexMap(String sql) {
		NavigableMap<Integer, String> andOrIndexes = new TreeMap<Integer, String>();
		String tempSQL = sql.toUpperCase();

		int idx = tempSQL.indexOf(" OR ");
		while (idx > -1) {
			andOrIndexes.put(idx, "OR");
			idx = tempSQL.indexOf(" OR ", idx + 1);
		}
		idx = tempSQL.indexOf(" AND ");
		while (idx > -1) {
			andOrIndexes.put(idx, "AND");
			idx = tempSQL.indexOf(" AND ", idx + 1);
		}

		return andOrIndexes;

	}

	protected static Map<Integer, String> getEmptyVarIndexMap(List<String> emptyVars, String sql) {
		Map<Integer, String> emptyVarIndexMap = new TreeMap<Integer, String>();
		String tempSql = sql.toUpperCase();

		for (String varName : emptyVars) {
			int idx = tempSql.indexOf(varName.toUpperCase());
			while (idx > -1) {
				emptyVarIndexMap.put(idx, varName);
				idx = tempSql.indexOf(varName.toUpperCase(), idx + 1);
			}
		}

		return emptyVarIndexMap;

	}

}
