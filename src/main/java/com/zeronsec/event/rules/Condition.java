package com.zeronsec.event.rules;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


class Condition<T> {
    private Predicate<HashMap<String, String>> predicate;
    private LogicalOperator logicalOperator;
    public String ruleCondition;
    public String attribute;
    public String type;
    public boolean isMust;
    public String operator;
    public String props;
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isMust() {
		return isMust;
	}
	public void setMust(boolean isMust) {
		this.isMust = isMust;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public String getProps() {
		return props;
	}
	public void setProps(String props) {
		this.props = props;
	}

	public Condition(Predicate<HashMap<String, String>> predicate, LogicalOperator logicalOperator) {
        this.predicate = predicate;
        this.logicalOperator = logicalOperator;
    }
    

    public Predicate<HashMap<String, String>> getPredicate() {
        return predicate;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }
}
//Enum to represent logical operators
enum LogicalOperator {
 AND, OR
}

