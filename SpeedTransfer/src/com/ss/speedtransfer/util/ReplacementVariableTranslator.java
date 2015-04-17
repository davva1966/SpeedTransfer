package com.ss.speedtransfer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import com.ss.speedtransfer.model.QueryDefinition;


public class ReplacementVariableTranslator {

	protected Map<String, String> valueMap;
	protected List<String> excludeIfEmptyVars = null;

	public ReplacementVariableTranslator(Map<String, String> valueMap, List<String> excludeIfEmptyVars) {
		super();
		this.valueMap = valueMap;
		this.excludeIfEmptyVars = excludeIfEmptyVars;

	}

	public String translateSQL(String sql) {
		
		String newSQL = sql;
		
		sql = sql.replaceAll("\n", " ");
		sql = sql.replaceAll("\r", " ");
		sql = sql.replaceAll("\t", " ");

		StringMatcher sm = new StringMatcher(QueryDefinition.REP_VAR_START_MARKER, QueryDefinition.REP_VAR_STOP_MARKER);

		List<String> adjustedExcludeIfEmptyVars = new ArrayList<String>();
		if (excludeIfEmptyVars != null && excludeIfEmptyVars.size() > 0) {
			for (String marker : excludeIfEmptyVars) {
				String value = valueMap.get(marker);
				if (value == null || value.trim().length() == 0)
					adjustedExcludeIfEmptyVars.add(sm.addMarkers(marker));
			}
			if (adjustedExcludeIfEmptyVars.size() > 0)
				newSQL = removeEmptyVars(sql, adjustedExcludeIfEmptyVars);
		}

		if (valueMap != null && valueMap.size() > 0) {
			newSQL = sm.replaceDataMarkers(newSQL, valueMap);
		}

		return newSQL;
	}

	protected String removeEmptyVars(String sql, List<String> emptyVars) {

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
			if (end < sb.length() && sb.charAt(end) == '\'')
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
				int previousCompKey = -1;
				try {
					previousCompKey = componentMap.lowerKey(key);
				} catch (Exception e) {
				}
				if (previousCompKey > -1) {
					String previousComp = componentMap.get(previousCompKey);
					if (previousComp.equalsIgnoreCase(clause))
						continue;
				} else {
					continue;
				}

			}
			if (component.equalsIgnoreCase(clause)) {
				int nextCompKey = -1;
				try {
					nextCompKey = componentMap.higherKey(key);
				} catch (Exception e) {
				}
				if (nextCompKey > -1) {
					nextCompKey = componentMap.higherKey(key);
					String nextComp = componentMap.get(nextCompKey);
					if (nextComp == null || nextComp.equalsIgnoreCase(")"))
						continue;
				} else {
					continue;
				}
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
