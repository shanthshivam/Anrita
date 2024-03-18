package com.zeronsec.event.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


class GroupCondition<T> {
	private final List<Condition> conditions = new ArrayList<>();
	private final GroupLogicalOperator groupLogicalOperator;
	private String groupCondition =""; 
	private boolean isConditionsPredicateGenerated = false;
	public String getGroupCondition() {
		return groupCondition;
	}


	public GroupCondition(GroupLogicalOperator groupLogicalOperator) {
		this.groupLogicalOperator = groupLogicalOperator;
	}

	
	public void addCondition(Condition condition) {
		conditions.add(condition);
	}
	
	public Predicate<HashMap<String, String>> getGroupConditionPredicate() {
		
//		HashMap<String, String > eventMaptestOddOne = new HashMap<>();
//		eventMaptestOddOne.put("ioc", "someVlaue");
//		eventMaptestOddOne.put("msg", "Connection Opened");
//		eventMaptestOddOne.put("eventDirection", "Inbound");
//		
//		//Predicate<HashMap<String, Object>> groupPredicate = null;
//		if (!isConditionsPredicateGenerated) {
//			System.out.println(conditions);
//			for (int i = 0; i < conditions.size(); i++) {
//				if (groupPredicate != null) {
//					if (conditions.get(i).getLogicalOperator() == LogicalOperator.AND) {
//						
//						groupPredicate.and(conditions.get(i).getPredicate());
//						System.out.println(groupPredicate + " " + conditions.get(i).attribute + " " + conditions.get(i).props + " getGroupConditionPredicate In AND " + groupPredicate.test(eventMaptestOddOne));
//						groupCondition = groupCondition + " && (" + conditions.get(i).ruleCondition + ")";
//
//					}
//					//
//					else {
//						groupPredicate.or(conditions.get(i).getPredicate());
//						System.out.println(groupPredicate + " " + conditions.get(i).attribute + " " + conditions.get(i).props + " getGroupConditionPredicate In AND " + groupPredicate.test(eventMaptestOddOne));
//						
//						groupCondition = groupCondition + " || (" + conditions.get(i).ruleCondition + ")";
//					}
//				} else {
//					groupPredicate = conditions.get(i).getPredicate();
//					System.out.println(groupPredicate + " " + conditions.get(i).attribute + " " + conditions.get(i).props + " getGroupConditionPredicate In AND " + groupPredicate.test(eventMaptestOddOne));
//					
//					groupCondition = "(" + conditions.get(i).ruleCondition + ")";
//
//				}
//			}
//			isConditionsPredicateGenerated = true;
//		}
		
		List<Predicate<HashMap<String,String>>> listOfPredicates = conditions.stream().map(x -> (Predicate<HashMap<String,String>>)x.getPredicate()).toList();
		if(conditions.stream().allMatch(x -> x.getLogicalOperator() == LogicalOperator.AND)) {
			
			
			return listOfPredicates.stream().reduce(Predicate::and).orElse(t -> true);
		}
		else { 
			//if(conditions.stream().allMatch(x -> x.getLogicalOperator() == LogicalOperator.OR)){
			return listOfPredicates.stream().reduce(Predicate::or).orElse(t -> true);
			
		}
//		else {
//			throw new Exception("Unsupported condition type in GroupCondition Predicate generation!");
//		}
	}

	public GroupLogicalOperator getGroupLogicalOperator() {
		return groupLogicalOperator;
	}

	public List<Condition> getConditions(){
		return conditions;
	}
	
}

//Enum to represent logical operators
enum GroupLogicalOperator {
	AND, OR
}