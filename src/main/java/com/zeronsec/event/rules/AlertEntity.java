package com.zeronsec.event.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AlertEntity {
	
	private HashMap<String, Object> alertFieldMap = new HashMap<>();

	private String token; 
//	private String timeField;
	private String operator;
	private List<String> identicalFields = new ArrayList<>();
	private List<String> uniqueFields= new ArrayList<>();
	private String type;
	private int value;
	private List<String> fields= new ArrayList<>();
	private String from;
	private String to;
	
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		alertFieldMap.put("token", token);
		this.token = token;
	}
//	public String getTimeField() {
//		return timeField;
//	}
//	public void setTimeField(String timeField) {
//		this.timeField = timeField;
//	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		alertFieldMap.put("operator", operator);
		this.operator = operator;
	}
	public List<String> getIdenticalFields() {
		return identicalFields;
	}
	public void setIdenticalFields(List<String> identicalFields) {
		alertFieldMap.put("identicalFields", identicalFields);
		this.identicalFields = identicalFields;
	}
	public List<String> getUniqueFields() {
		return uniqueFields;
	}
	public void setUniqueFields(List<String> uniqueFields) {
		this.uniqueFields = uniqueFields;
		alertFieldMap.put("uniqueFields", uniqueFields);

	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
		alertFieldMap.put("type", type);

	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
		alertFieldMap.put("value", value);

	}
	public List<String> getFields() {
		return fields;
	}
	public void setFields(List<String> fields) {
		this.fields = fields;
		alertFieldMap.put("fields", fields);

	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
		alertFieldMap.put("from", from);

	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
		alertFieldMap.put("to", to);

	}
	public HashMap<String, Object> getAlertFieldMap() {
		return alertFieldMap;
	}
}
