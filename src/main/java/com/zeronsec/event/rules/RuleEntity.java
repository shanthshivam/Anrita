package com.zeronsec.event.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//import org.apache.commons.collections4.MultiMap;
//import org.apache.commons.collections4.map.MultiValueMap;

public class RuleEntity<T> {

	private List<GroupCondition> groupConditions = new ArrayList<>();
	private String ruleCondition = "";
	private boolean isGroupConditionsGenereated = false;

	public String getRuleCondition() {
//		ruleCondition = "";
//		for (int i = 0; i < groupConditions.size(); i++) {
//			System.out.println("Into RuletEntity getRuleCondition ");
//			if (ruleCondition =="") {
//				if (groupConditions.get(i).getGroupLogicalOperator() == GroupLogicalOperator.AND) {
//					ruleCondition = ruleCondition + " && (" + groupConditions.get(i).getGroupCondition() + ")";
//					System.out.println("Into && RuletEntity getRuleCondition " + ruleCondition);
//				}
//				//
//				else {
//					ruleCondition = ruleCondition + " || (" + groupConditions.get(i).getGroupCondition() + ")";
//					System.out.println("Into || RuletEntity getRuleCondition " + ruleCondition);
//				}
//			} else {
//				ruleCondition = "(" + groupConditions.get(i).getGroupCondition() + ")";
//				System.out.println("Into RuletEntity first getRuleCondition " + ruleCondition);
//			}
//		}
		return ruleCondition;
	}

	public List<GroupCondition> getGroupConditions() {
		return groupConditions;
	}

	private Predicate<HashMap<String, Object>> rulePredicate = null;
	private AlertEntity alertEntity;

	public AlertEntity getAlertEntity() {
		return alertEntity;
	}

	public void setAlertEntity(AlertEntity alertEntity) {
		this.alertEntity = alertEntity;
	}

	public RuleEntity() {
	}

	public void addGroupCondition(GroupCondition groupCondition) {
		groupConditions.add(groupCondition);
	}

	public Predicate<HashMap<String, String>> getRuleConditionPredicate()  {

//		if (!isGroupConditionsGenereated) {
//			ruleCondition = "";
//			rulePredicate = null;
//			for (int i = 0; i < groupConditions.size(); i++) {
//				if (rulePredicate != null) {
//					if (groupConditions.get(i).getGroupLogicalOperator() == GroupLogicalOperator.AND) {
//						rulePredicate.and(groupConditions.get(i).getGroupConditionPredicate());
//						ruleCondition = ruleCondition + " && (" + groupConditions.get(i).getGroupCondition() + ")";
//					} else {
//						rulePredicate.or(groupConditions.get(i).getGroupConditionPredicate());
//						ruleCondition = ruleCondition + " || (" + groupConditions.get(i).getGroupCondition() + ")";
//					}
//				} else {
//					rulePredicate = groupConditions.get(i).getGroupConditionPredicate();
//					ruleCondition = "(" + groupConditions.get(i).getGroupCondition() + ")";
//				}
//
//				isGroupConditionsGenereated = true;
//			}
//		}
//		return rulePredicate;

		List<Predicate<HashMap<String,String>>> listOfGroupPredicates = groupConditions.stream().map(x -> (Predicate<HashMap<String,String>>)x.getGroupConditionPredicate()).toList();
		
		if(groupConditions.stream().allMatch(x -> x.getGroupLogicalOperator() == GroupLogicalOperator.AND)) {
			
			
			return listOfGroupPredicates.stream().reduce(Predicate::and).orElse(t -> true);
		}
		else { 
			//if(groupConditions.stream().allMatch(x -> x.getGroupLogicalOperator() == GroupLogicalOperator.OR)){
			return listOfGroupPredicates.stream().reduce(Predicate::or).orElse(t -> true);
			
		}
//		else {
//			throw new Exception("Unsupported condition type in GroupCondition Predicate generation!");
//		}
		
	}
	
	public List<Condition> getListofConditions() {
		List<Condition> conditionList = new ArrayList<>();

		for (int i = 0; i < groupConditions.size(); i++) {
			List<Condition> list = groupConditions.get(i).getConditions();
			conditionList.addAll(list);
		}
		return conditionList;
	}

	public List<String> getUniqueFieldValues(String field) {

		List<String> uniqueFieldValues = getListofConditions().stream().filter(
				condition -> condition.getAttribute() != null && condition.getAttribute().equalsIgnoreCase(field))
				.map(Condition::getProps).collect(Collectors.toList());
		return uniqueFieldValues;
	}

	public List<String> getUniqueAttributeList() {

		List<String> attributesList = new ArrayList<>();

		List<Condition> conditionList = getListofConditions();

		attributesList = conditionList.stream().filter(condition -> condition.getAttribute() != null)
				.map(Condition::getAttribute).distinct().collect(Collectors.toList());

		return attributesList;
	}

	public List<String> getAttributeList() {

		List<String> attributesList = new ArrayList<>();

		List<Condition> conditionList = new ArrayList<>();

		for (int i = 0; i < groupConditions.size(); i++) {
			List<Condition> list = groupConditions.get(i).getConditions();
			conditionList.addAll(list);
		}

		attributesList = conditionList.stream().filter(condition -> condition.getAttribute() != null)
				.map(Condition::getAttribute).collect(Collectors.toList());

		return attributesList;
	}

//	public MultiMap<String, String> getAttributeProps() {
//
//		MultiValueMap<String, String> attributePropsMap = new MultiValueMap();
//
//		List<String> attributesValueList = new ArrayList<>();
//
//		List<Condition> conditionList = new ArrayList<>();
//
//		for (int i = 0; i < groupConditions.size(); i++) {
//			List<Condition> list = groupConditions.get(i).getConditions();
//			conditionList.addAll(list);
//		}
//
//		attributesValueList = conditionList.stream()
//				.filter(condition -> condition.getProps() != "" || condition.getProps() != null)
//				.map(Condition::getAttribute).distinct().collect(Collectors.toList());
//
//		List<String> uniqueAttributeList = getUniqueAttributeList();
//
//		for (int k = 0; k < conditionList.size(); k++) {
//
//			if (conditionList.get(k).getProps() != null && conditionList.get(k).getProps() != "") {
//				System.out.println("RuleEntity ----> " + conditionList.get(k).getProps());
//				attributePropsMap.put(conditionList.get(k).getAttribute(), conditionList.get(k).getProps());
//			}
//		}
//
//		return attributePropsMap;
//	}

}
