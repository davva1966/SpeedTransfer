package com.ss.speedtransfer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringMatcher {

	protected String startMarker = "@\\{";
	protected String stopMarker = "\\}";

	protected String startMarkerClean = null;
	protected String stopMarkerClean = null;

	public StringMatcher(String startMarker, String stopMarker) {
		this.startMarker = startMarker;
		this.stopMarker = stopMarker;

		startMarkerClean = startMarker.replaceAll("\\\\", "");
		stopMarkerClean = stopMarker.replaceAll("\\\\", "");

	}

	public String addMarkers(String data) {
		return startMarkerClean + data + stopMarkerClean;
	}

	public List<String> findAll(String data) {
		return parse(data);

	}

	protected String getRegEx() {
		return startMarker + "[a-zA-Z0-9\\_\\-]+?" + stopMarker;
	}

	protected List<String> parse(String data) {
		List<String> dataMarkers = new ArrayList<String>();
		Pattern pattern = Pattern.compile(getRegEx());
		Matcher matcher = pattern.matcher(data);
		while (matcher.find()) {
			String marker = matcher.group();
			if (dataMarkers.contains(marker) == false)
				dataMarkers.add(marker);
		}
		return cleanDataMarkers(dataMarkers);

	}

	public List<String> cleanDataMarkers(List<String> dataMarkers) {
		List<String> cleanList = new ArrayList<String>();
		for (String marker : dataMarkers)
			cleanList.add(cleanDataMarker(marker));
		return cleanList;
	}

	public String cleanDataMarker(String text) {
		return text.substring(startMarkerClean.length(), text.length() - (stopMarkerClean.length()));
	}

	protected Map<String, String> adjustValueMap(Map<String, String> valueMap) {
		Map<String, String> adjustedValueMap = new HashMap<String, String>();
		for (String dataMarker : valueMap.keySet()) {
			String value = valueMap.get(dataMarker);
			adjustedValueMap.put(adjustMarker(dataMarker), value);
		}
		return adjustedValueMap;
	}

	public String adjustMarker(String dataMarker) {
		if (dataMarker.startsWith(startMarkerClean) == false)
			dataMarker = startMarkerClean + dataMarker;
		if (dataMarker.endsWith(stopMarkerClean) == false)
			dataMarker = dataMarker + stopMarkerClean;
		return dataMarker;
	}

	public String replaceDataMarkers(String string, Map<String, String> valueMap) {
		Map<String, String> adjustedValueMap = adjustValueMap(valueMap);
		StringBuffer sb = new StringBuffer();
		Pattern pattern = Pattern.compile(getRegEx());
		Matcher matcher = pattern.matcher(string);
		while (matcher.find()) {
			if (adjustedValueMap.containsKey(matcher.group()))
				matcher.appendReplacement(sb, adjustedValueMap.get(matcher.group()));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public boolean isReplacementVar(String value) {
		if (value == null)
			return false;
		return value.trim().startsWith(startMarkerClean) && value.trim().endsWith(stopMarkerClean);
	}

}
