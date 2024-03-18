package com.zeronsec.event.rules;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RuleRepository {

	private static Logger logger = Logger.getLogger("MyLogger");
	private ArrayList<String> attributeList = new ArrayList<>();
	private Map<String, String> attributeMap = new HashMap();
	private Map<String, List<Predicate>> predicatesMap = new HashMap();
	private Map<String, RuleEntity> ruleEntityMap = new HashMap();
	private Map<String, String> ruleIdRuleConditionMap = new HashMap<>();


	private Map<String, Predicate<HashMap<String, String>>> predicatesLookUpMap = new HashMap<String, Predicate<HashMap<String, String>>>();

	// JDBC driver name and database URL
	private String JDBC_DRIVER;
	private String DB_URL;

	// Database credentials
	private String USER;
	private String PASS;
	private String ruleType;
	
	private boolean isRuleGenerated = false;
	public RuleRepository(String JDBC_DRIVER, String DB_URL, String USER, String PASSWORD,String ruleType) {
		this.JDBC_DRIVER = JDBC_DRIVER;
		this.DB_URL = DB_URL;
		this.USER = USER;
		this.PASS = PASSWORD;
		this.ruleType = ruleType;
	}

	public RuleRepository(String JDBC_DRIVER, String DB_URL, String USER, String PASSWORD) {
		this.JDBC_DRIVER = JDBC_DRIVER;
		this.DB_URL = DB_URL;
		this.USER = USER;
		this.PASS = PASSWORD;
		// default to "windows"
		this.ruleType = "windows";
	}
	
	
	// static final String JDBC_DRIVER =
	// ConfigProperties.getProperty("JDBC_DRIVER");
//	static final String DB_URL = ConfigProperties.getProperty("DB_URL");
//
//	// Database credentials
//	static final String USER = ConfigProperties.getProperty("USER");
//	static final String PASS = ConfigProperties.getProperty("PASS");

	public Map<String, RuleEntity> getRuleEntityMap() {
		return ruleEntityMap;
	}

	public Map<String, Predicate<HashMap<String, String>>> getPredicatesLookUpMap() {
		return predicatesLookUpMap;
	}
	
	
	public static void main(String[] args) {
		new RuleRepository("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/anritarules", "anritarules", "anritarules", "windows*")
				.generateRules();
	}

	public void generateRules() {

		Connection conn = null;
		Statement stmt = null;
		long startTime = System.currentTimeMillis();
		try {

			// Register JDBC driver
			Class.forName(this.JDBC_DRIVER);

			// Open a connection
			System.out.println("Connecting to database..." + this.JDBC_DRIVER);
			conn = DriverManager.getConnection(this.DB_URL, this.USER, this.PASS);
			conn.setAutoCommit(false);
			returnRulesAsXML(conn);
			isRuleGenerated = true;
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException se2) {
			} // nothing we can do
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException se) {
				se.printStackTrace();
			} // end finally try
		} // end try
		System.out.println("Goodbye! " + (System.currentTimeMillis() - startTime) + " msecs");
	}// end main

	public void returnXML(Connection conn) {

		try {
			// Create a statement
			Statement stmt = conn.createStatement();
			// Execute a query
			String sql = "select a.token, a.attr, a.field_Type,a.group_token,c.token as \"query_token\", a.is_must as \"conditions_is_must\",d.token as \"query_group_token\",d.title,d.is_must,a.operator,a.props  from zeronsec.conditions a, zeronsec.alerts  b, zeronsec.queries c, zeronsec.query_groups d where b.token = c.alert_token and c.token = d.query_token and d.token = a.group_token;";
			ResultSet rs = stmt.executeQuery(sql);

			// Initialize a new document
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// Create the root element
			Element rootElement = doc.createElement("RULES");
			doc.appendChild(rootElement);

			int columnCount = rs.getMetaData().getColumnCount();
			// Process the result set
			while (rs.next()) {

				Element rule = doc.createElement("RULE");
				rootElement.appendChild(rule);
				for (int i = 1; i <= columnCount; i++) {

					Element id = doc.createElement(rs.getMetaData().getColumnName(i));
					id.appendChild(doc.createTextNode(rs.getString(rs.getMetaData().getColumnName(i))));
					rule.appendChild(id);

				}
			}

			// Close the connection
			conn.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void returnRulesAsXML(Connection conn) {

		try {
			// Create a statement
			Statement stmt = conn.createStatement();

			String sql = "select b.token, b.alert_token,b.op,b.params,b.timeframes,b.type,b.value,b.alertFields from alerts a, alert_aggregation b where a.token=b.alert_token";
			ResultSet rs = stmt.executeQuery(sql);

			class AlertAggregator {

				public AlertAggregator() {
					// TODO Auto-generated constructor stub
				}

				public String token;
				public String alertToken;
				public String operator;
				public String params;
				public String timeFrames;
				public String type;
				public int value;
				public String alertField;

			}
			;

			HashMap<String, AlertAggregator> alertAggregator = new HashMap<String, AlertAggregator>();
			while (rs.next()) {

				if (rs.getString("op") != null) {
					AlertAggregator ag = new AlertAggregator();
					ag.token = rs.getString("token");
					ag.alertToken = rs.getString("alert_token");
					ag.operator = rs.getString("op");
					ag.params = rs.getString("params");
					ag.timeFrames = rs.getString("timeframes");
					ag.type = rs.getString("type");
					ag.value = rs.getInt("value");
					ag.alertField = rs.getString("alertFields");

					alertAggregator.put(ag.alertToken, ag);
				}
			}

			// Execute a query
			sql = "select token,alert_token,index_name,time_field from queries where index_name='" +ruleType +"'" ;
			rs = stmt.executeQuery(sql);

			// Initialize a new document
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			// Create the root element
			Element rootElement = doc.createElement("RULES");
			doc.appendChild(rootElement);

			int columnCount = rs.getMetaData().getColumnCount();
			System.err.println("column .... " + columnCount);
			// Process the result set
			while (rs.next()) {
				RuleEntity ruleEntity = new RuleEntity();
				ruleEntity.setAlertEntity(new AlertEntity());

				Element rule = doc.createElement("RULE");
				rootElement.appendChild(rule);
				Element token = doc.createElement("TOKEN");
				String tokenId = rs.getString("token");
				// add the rule Entity here to global Entity Map
				ruleEntityMap.put(tokenId, ruleEntity);
				predicatesMap.put(tokenId, new ArrayList<Predicate>());
				predicatesLookUpMap.put(tokenId, null);
				token.appendChild(doc.createTextNode(rs.getString("token")));
				rule.appendChild(token);

				Element alertToken = doc.createElement("ALERT_TOKEN");
				alertToken.appendChild(doc.createTextNode(rs.getString("alert_token")));
				ruleEntity.getAlertEntity().setToken(rs.getString("alert_token"));
				rule.appendChild(alertToken);

				Element indexName = doc.createElement("INDEX_NAME");
				indexName.appendChild(doc.createTextNode(rs.getString("index_name")));
				rule.appendChild(indexName);

				Element timeField = doc.createElement("TIME_FIELD");
				timeField.appendChild(doc.createTextNode(rs.getString("time_field")));
				rule.appendChild(timeField);

				if (alertAggregator.get(rs.getString("alert_token")) != null) {
					Element alertOp = doc.createElement("ALERT_OPERATOR");
					alertOp.appendChild(doc.createTextNode(alertAggregator.get(rs.getString("alert_token")).operator));
					ruleEntity.getAlertEntity().setOperator(alertAggregator.get(rs.getString("alert_token")).operator);
					rule.appendChild(alertOp);

					Element alertParams = doc.createElement("ALERT_PARAMS");
					alertParams
							.appendChild(doc.createTextNode(alertAggregator.get(rs.getString("alert_token")).params));
					rule.appendChild(alertParams);
					Element alertParamsArray = doc.createElement("ALERT_PARAMS_ARRAY");

					JsonNode alertParamsJson = getRange(alertAggregator.get(rs.getString("alert_token")).params);

					JsonNode identicalFieldsNode = alertParamsJson.get("identical_fields");
//					System.out.println("CONTENT OF JSON NODE " + identicalFieldsNode.asText().toUpperCase());
					JsonNode uniqueFieldsNode = alertParamsJson.get("unique_fields");
//				    System.out.println("CONTENT OF JSON NODE " + uniqueFieldsNode.asText().toUpperCase());
					JsonNode groupByNode = alertParamsJson.get("groupBy");
					JsonNode includeNode = alertParamsJson.get("include");

					// Access the values from JsonNode
					if (identicalFieldsNode != null && identicalFieldsNode.isArray()) {
						for (JsonNode fieldNode : identicalFieldsNode) {
							String field = fieldNode.asText();
							//System.out.println("OUTPUT OF PARAMS IDENTICAL_FIELDS: " + field);
							Element alertIdenticalParams = doc.createElement("IDENTICAL_PARAMS");
							alertIdenticalParams.appendChild(doc.createTextNode(field));
							alertParamsArray.appendChild(alertIdenticalParams);
							ruleEntity.getAlertEntity().getIdenticalFields().add(field);
						}
					}

					// Access the values from JsonNode
					if (uniqueFieldsNode != null && uniqueFieldsNode.isArray()) {
						for (JsonNode fieldNode : uniqueFieldsNode) {
							String field = fieldNode.asText();
							//System.out.println("OUTPUT OF PARAMS UNIQUE_FIELDS: " + field);
							Element alertUniqueParams = doc.createElement("UNIQUE_PARAMS");
							alertUniqueParams.appendChild(doc.createTextNode(field));
							alertParamsArray.appendChild(alertUniqueParams);
							ruleEntity.getAlertEntity().getUniqueFields().add(field);

						}
					}
					rule.appendChild(alertParamsArray);

					// Access TimeFrame value from JsonNode
					Element alertTimeframe = doc.createElement("TIMEFRAME");
					JsonNode alertTimeframeJson = getRange(alertAggregator.get(rs.getString("alert_token")).timeFrames)
							.get("c");
					if (alertTimeframeJson != null) {
						Element from = doc.createElement("FROM");
						from.appendChild(doc.createTextNode(alertTimeframeJson.get("from").asText()));
						ruleEntity.getAlertEntity().setFrom(alertTimeframeJson.get("from").asText());
						Element to = doc.createElement("TO");
						to.appendChild(doc.createTextNode(alertTimeframeJson.get("to").asText()));
						ruleEntity.getAlertEntity().setTo(alertTimeframeJson.get("to").asText());
						alertTimeframe.appendChild(from);
						alertTimeframe.appendChild(to);
					}
					rule.appendChild(alertTimeframe);

					// Access Alert Fields from JsonNode
					int i = 0;
					Element alertFieldsArray = doc.createElement("ALERT_FIELDS_ARRAY");
					JsonNode alertFieldsJson = getRange(alertAggregator.get(rs.getString("alert_token")).alertField)
							.get("alert_fields");
					if (alertFieldsJson != null && alertFieldsJson.isArray()) {
						for (JsonNode fieldNode : alertFieldsJson) {
							String field = fieldNode.asText().replace(".keyword", "");
							Element alertUniqueParams = doc.createElement("ALERT_FIELDS");
							ruleEntity.getAlertEntity().getFields().add(field);
							alertUniqueParams.setAttribute("COUNT", i + " ");
							alertUniqueParams.appendChild(doc.createTextNode(field));
							alertFieldsArray.appendChild(alertUniqueParams);
							i++;

						}
					}
					rule.appendChild(alertFieldsArray);

					Element alertType = doc.createElement("ALERT_TYPE");
					alertType.appendChild(doc.createTextNode(alertAggregator.get(rs.getString("alert_token")).type));
					rule.appendChild(alertType);
					ruleEntity.getAlertEntity().setType(alertAggregator.get(rs.getString("alert_token")).type);
					Element alertValue = doc.createElement("ALERT_VALUE");
					alertValue.appendChild(doc
							.createTextNode(new String(alertAggregator.get(rs.getString("alert_token")).value + "")));
					rule.appendChild(alertValue);
					ruleEntity.getAlertEntity().setValue(alertAggregator.get(rs.getString("alert_token")).value);
					Element alertTimeFrames = doc.createElement("ALERT_TIME_FRAMES");
					alertTimeFrames.appendChild(
							doc.createTextNode(alertAggregator.get(rs.getString("alert_token")).timeFrames));
					rule.appendChild(alertTimeFrames);

					Element alertFields = doc.createElement("ALERT_FIELD");
					alertFields.appendChild(doc.createTextNode(alertFieldsJson.toString()));
					rule.appendChild(alertFields);

				} else {

					Element alertOp = doc.createElement("ALERT_OPERATOR");
					alertOp.appendChild(doc.createTextNode(""));
					rule.appendChild(alertOp);

					Element alertParams = doc.createElement("ALERT_PARAMS");
					alertParams.appendChild(doc.createTextNode(""));
					rule.appendChild(alertParams);

					Element alertType = doc.createElement("ALERT_TYPE");
					alertType.appendChild(doc.createTextNode(""));
					rule.appendChild(alertType);

					Element alertValue = doc.createElement("ALERT_VALUE");
					alertValue.appendChild(doc.createTextNode(""));
					rule.appendChild(alertValue);

					Element alertTimeFrames = doc.createElement("ALERT_TIME_FRAMES");
					alertTimeFrames.appendChild(doc.createTextNode(""));
					rule.appendChild(alertTimeFrames);

					Element alertFields = doc.createElement("ALERT_FIELD");
					alertFields.appendChild(doc.createTextNode(""));
					rule.appendChild(alertFields);

//					Element emptyMap = doc.createElement("EMPTY_MAP");

				}
				returnXMLQueryGroup(ruleEntity, tokenId, doc, rule, "GROUP",
						"SELECT * FROM query_groups WHERE query_token='" + rs.getString("token") + "'", conn);
			} // end of rs.next()
			long startTime = System.currentTimeMillis();

			TransformerFactory transformerFactory = TransformerFactory.newInstance();

			DOMSource source = new DOMSource(doc);

			// Write the content into XML file
			Transformer transformerXml = transformerFactory.newTransformer();
			// Enable indentation (pretty-print)//
			transformerXml.setOutputProperty(OutputKeys.INDENT, "yes");
			transformerXml.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			StreamResult result = new StreamResult(new File("/home/shanth/Rules.xml"));
			transformerXml.transform(source, result);
			

			attributeList.forEach(x -> System.out.println(x));
			List<String> uniqueAttributeList = attributeList.stream().distinct().collect(Collectors.toList());
			System.out.println("No of UniqueFields for " + ruleType + " is " + uniqueAttributeList.size() + " ****************************");
			uniqueAttributeList.forEach(x -> System.out.println(x));
			System.out.println("No of Rules for " + ruleType + " is " + predicatesLookUpMap.size() + " ****************************");
			
			
			// Close the connection
			conn.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void returnXMLQueryGroup(RuleEntity ruleEntity, String tokenId, Document doc, Element rule, String tag,
			String sql, Connection conn) {

		try {
			// Create a statement
			Statement stmt = conn.createStatement();

			// Execute a query
			// sql = "select a.token, a.attr, a.field_Type,a.group_token,c.token as
			// \"query_token\", a.is_must as \"conditions_is_must\",d.token as
			// \"query_group_token\",d.title,d.is_must,a.operator,a.props from
			// zeronsec.conditions a, zeronsec.alerts b, zeronsec.queries c,
			// zeronsec.query_groups d where b.token = c.alert_token and c.token =
			// d.query_token and d.token = a.group_token;";

			StringBuilder ruleGroup = new StringBuilder();
			ResultSet rs = stmt.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();
			// Process the result set

			boolean isFirst = true;
			while (rs.next()) {

				if (!isFirst) {
					boolean isMust = rs.getBoolean("is_Must");
					if (isMust) {

						ruleGroup.append(" && ");
						// ruleConditionsPredicate
					} else {
						ruleGroup.append(" || ");
						// ruleConditionsPredicate
					}
				}

				if (isFirst) {
					isFirst = false;
				}
				// adding GROUP TAG for every Rule Group
				Element group = doc.createElement(tag);
				rule.appendChild(group);
				boolean isMustFlag = rs.getBoolean("is_must");
				GroupCondition groupCondition = null;
				if (isMustFlag) {
					groupCondition = new GroupCondition(GroupLogicalOperator.AND);

				} else {
					groupCondition = new GroupCondition(GroupLogicalOperator.OR);

				}
				Element token = doc.createElement("TOKEN");
				token.appendChild(doc.createTextNode(rs.getString("token")));
				Element isMust = doc.createElement("IS_MUST");
				isMust.appendChild(doc.createTextNode(rs.getString("is_must")));
				Element title = doc.createElement("TITLE");
				title.appendChild(doc.createTextNode(rs.getString("title")));
				Element position = doc.createElement("POSITION");
				position.appendChild(doc.createTextNode(rs.getString("position")));
				group.appendChild(token);
				group.appendChild(isMust);
				group.appendChild(title);
				group.appendChild(position);
//				System.out.println("GROUPS " + rs.getString("token"));

				StringBuffer ruleCondition = new StringBuffer("(");
				returnXMLQueryConditions(groupCondition, tokenId, doc, group, "CONDITION",
						"SELECT * FROM conditions WHERE group_token='" + rs.getString("token") + "'", conn,
						ruleCondition);
				System.out.println("RULE CONDITION **********************" + ruleCondition.toString());
				System.out.println("RULE CONDITION - RuleEntity *********" + groupCondition.getGroupCondition());
				
				ruleEntity.addGroupCondition(groupCondition);
				ruleGroup.append(ruleCondition);
			}

			predicatesLookUpMap.put(tokenId, ruleEntity.getRuleConditionPredicate());

			Element ruleGroupPhrase = doc.createElement("RULE_GROUP_PHRASE");
			ruleGroupPhrase.appendChild(doc.createTextNode(ruleGroup.toString()));
			rule.appendChild(ruleGroupPhrase);
			//System.out.println(ruleEntity.getRuleConditionPredicate());
			System.out.println("RULE_GROUP_PHRASE " + ruleGroupPhrase);
			ruleIdRuleConditionMap.put(tokenId, ruleGroup.toString());

			// Close the connection
			stmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> getRuleIdRuleConditionMap() {
		return ruleIdRuleConditionMap;
	}

	public void returnXMLQueryConditions(GroupCondition groupCondtion, String tokenId, Document doc, Element group,
			String tag, String sql, Connection conn, StringBuffer ruleCondition) {

		try {
			// Create a statement
			Statement stmt = conn.createStatement();

			// Execute a query
			ResultSet rs = stmt.executeQuery(sql);
			int columnCount = rs.getMetaData().getColumnCount();
			// Process the result set
			boolean isFirst = true;
			while (rs.next()) {

				String attribute = rs.getString("attr");
				

				String fieldType = rs.getString("field_type");
				attributeMap.put(attribute, fieldType);
				boolean isMust = rs.getBoolean("is_must");
				String operator = rs.getString("operator");
				String props = rs.getString("props");

				if (operator.equalsIgnoreCase("eq")) {
					JsonNode node = new ObjectMapper().readTree(props);
					
					attributeList.add(attribute + "=" + node.get("value").asText());
				}
				if (!isFirst) {
					if (isMust) {

						//System.out.println("rulecondtion----->".toUpperCase() + ruleCondition);
						ruleCondition.append(" && ");

					} else {

						//System.out.println("rulecondtion----->".toUpperCase() + ruleCondition);
						ruleCondition.append(" || ");

					}
				}

				if (isFirst) {
					isFirst = false;
				}

				ruleCondition.append("(");

//				if (props.length() > 2) {

				attribute = attribute.replace(".keyword", "");

				if (attribute.contains(".")) {
//					System.err.println("is nested field.... ");
					attribute = returnMethodFormat(attribute);
//					System.err.println("a.. " + attribute);
				}
				// now process the condition based on field type
				switch (fieldType) {
				case "text":
					//System.out.println("The keyword type is 'text'");
					processText(groupCondtion, tokenId, ruleCondition, attribute, isMust, operator, props);
					break;
				case "keyword":
					System.out.println("The keyword type is 'text'");
					processText(groupCondtion, tokenId, ruleCondition, attribute, isMust, operator, props);
					break;

				case "date":
					//processDate(tokenId, ruleCondition, attribute, isMust, operator, props);
				case "int":
				case "long":
				case "float":
					System.out.println("The keyword type is 'long'");
					//processLong(tokenId, ruleCondition, attribute, isMust, operator, props);
					//ruleCondition.append(getValue(props));
					break;

				case "object":
					System.out.println("The keyword type is 'object'");
					//processObject(tokenId, ruleCondition, attribute, isMust, operator, props);
					// ruleCondition.append(".equals(" + getValue(props) + ")");
					break;

				case "ip":
					System.out.println("The 'keyword type is 'ip'");
					//processIp(tokenId, ruleCondition, attribute, isMust, operator, props);
					// Add your logic here
					break;

				case "boolean":
					System.out.println("The keyword type is 'boolean'");
					//processBoolean(tokenId, ruleCondition, attribute, isMust, operator, props);
					// Add your logic here
					break;

				default:
//					System.out.println("Invalid keyword type");
					break;
				}

				Element condition = doc.createElement(tag);
				group.appendChild(condition);
				for (int i = 1; i <= columnCount; i++) {

					Element id = doc.createElement(rs.getMetaData().getColumnName(i));
					//System.out.println(rs.getMetaData().getColumnName(i) + " is added  "
					//		+ rs.getString(rs.getMetaData().getColumnName(i)));
					id.appendChild(doc.createTextNode(rs.getString(rs.getMetaData().getColumnName(i))));
					condition.appendChild(id);

				}
				ruleCondition.append(")");

//				} /// only when props has data otherwise skip this iteration and go to the next
				/// condition

			} // end of resulset while iteration
			ruleCondition.append(")");

			Element ruleConditionPhrase = doc.createElement("RULE_CONDITION_PHRASE");
			ruleConditionPhrase.appendChild(doc.createTextNode(ruleCondition.toString()));
			group.appendChild(ruleConditionPhrase);

			// Close the connection
			stmt.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

//	private void processDate(String tokenId, StringBuffer ruleCondition, String attribute, boolean isMust,
//			String operator, String props) {
//		try {
//
//		} catch (Exception e) {
//			switch (operator) {
//			case "exist":
//				System.out.println("The operation is 'exist'");
//				ruleCondition.append(attribute);
//				ruleCondition.append(" != " + null);
//				break;
//			case "eq":
//				System.out.println("The operation is 'eq'");
//				ruleCondition.append(attribute);
//				ruleCondition.append(".equals(" + getValue(props) + ")");
//				break;
//			case "!eq":
//				System.out.println("The operation is '!eq'");
//				ruleCondition.append("!");
//				ruleCondition.append(attribute);
//				ruleCondition.append(".equals(" + getValue(props) + ")");
//				break;
//			case "!exist":
//				System.out.println("The operation is '!exist'");
//				ruleCondition.append(attribute);
//				ruleCondition.append(" == " + null);
//				break;
//			}
//		}
//	}

//	private void processObject(String tokenId, StringBuffer ruleCondition, String attribute, boolean isMust,
//			String operator, String props) {
//		try {
//			switch (operator) {
//			case "exist":
//				System.out.println("The operation is 'exist'");
//				ruleCondition.append(attribute);
//				ruleCondition.append(" != " + null);
//				break;
//			case "eq":
//				System.out.println("The operation is 'eq'");
//				ruleCondition.append(attribute);
//				ruleCondition.append(".equalsIgnoreCase(" + getValue(props) + ")");
//				break;
//			case "!eq":
//				System.out.println("The operation is '!eq'");
//				ruleCondition.append("!");
//				ruleCondition.append(attribute);
//				ruleCondition.append(".equalsIgnoreCase(" + getValue(props) + ")");
//				break;
//			case "!exist":
//				System.out.println("The operation is '!exist'");
//				ruleCondition.append(attribute);
//				ruleCondition.append(" == " + null);
//				break;
//			case "cont":
//				System.out.println("The operation is 'cont'");
//				// need to understand the difference
//				ruleCondition.append(attribute);
//				ruleCondition.append(".contains(" + getValue(props) + ")");
//				break;
//			case "!cont":
//				System.out.println("The operation is '!cont'");
//				ruleCondition.append("!");
//				ruleCondition.append(attribute);
//				ruleCondition.append(".contains(" + getValue(props) + ")");
//				// Add your logic here
//				break;
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	private String getAtrributeValue(String str) {
		System.out.println("Value is " + str);
		// Pattern pattern = Pattern.compile("\"value\":\"\\\\\"(.*?)\\\\\"\"");
//		Pattern pattern = Pattern.compile("\"value\":\"(.*?)\"");
//        Matcher matcher = pattern.matcher(str);
//        return matcher.group(1);

		String returnValue = str.substring(str.indexOf(":") + 1, str.lastIndexOf("\"") + 1);
        try {
			JsonNode jsonNode = new ObjectMapper().readTree(str);
			
			Iterator<String> iterator = jsonNode.fieldNames();
			while(iterator.hasNext()) {
                // Get the value from the JsonNode
				String fieldName = iterator.next();
                String fieldValue = jsonNode.get(fieldName).asText();
                returnValue = fieldValue;
                // Print the result
                System.out.println(fieldName + " Value: " + fieldValue);
            }
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		
		System.out.println(new Date() + " Prop Value is ".toUpperCase() + returnValue);
		return returnValue;
	}

	public JsonNode getRange(String str) throws JsonMappingException, JsonProcessingException {

		JsonNode props = new ObjectMapper().readTree(str);

		System.out.println("Value is " + props);
		return props;
	}
/**
	private void processIp(String tokenId, StringBuffer ruleCondition, String attribute, boolean isMust,
			String operator, String props) {
		try {
			switch (operator) {
			case "eq":
				ruleCondition.append(attribute);
				ruleCondition.append(" == " + getValue(props));
				break;
			case "!eq":
				ruleCondition.append(attribute);
				ruleCondition.append(" != " + getValue(props));
				break;
			case "exist":
				ruleCondition.append(attribute);
				ruleCondition.append(" != " + null);
				break;
			case "!exist":
				ruleCondition.append(attribute);
				ruleCondition.append(" == " + null);
				break;
			case "regex":
				ruleCondition.append(attribute);
				ruleCondition.append(".matches(" + getValue(props) + ")");
				break;
			case "ipRange":
				JsonNode range = getRange(props);
				checkIpRange(attribute, range.get("from").asText(), range.get("to").asText());
				break;
			case "public":
				boolean isPublic = checkPublicIp(getValue(props));
				break;
			case "private":
				boolean isPrivate = checkPrivateIp(getValue(props));
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
**/
	private boolean checkPrivateIp(String ip) {
		try {
			InetAddress inetAddress = InetAddress.getByName(ip);

			if (inetAddress.isSiteLocalAddress())
				return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	private boolean checkPublicIp(String ip) {
		try {
			InetAddress inetAddress = InetAddress.getByName(ip);

			if (!inetAddress.isSiteLocalAddress())
				return true;

			return false;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void checkIpRange(String ip, String startRange, String endRange) {
		try {
			long ipToCheck = ipToLong(ip);
			long startIp = ipToLong(startRange);
			long endIp = ipToLong(endRange);

			if (ipToCheck >= startIp && ipToCheck <= endIp) {
				System.out.println(ip + " is within the IP range.");
			} else {
				System.out.println(ip + " is not within the IP range.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private long ipToLong(String ipAddress) {
		try {
			InetAddress inetAddress = InetAddress.getByName(ipAddress);
			byte[] addressBytes = inetAddress.getAddress();
			long result = 0;

			for (byte octet : addressBytes) {
				result = (result << 8) | (octet & 0xFF);
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

//	private void processBoolean(String tokenId, StringBuffer ruleCondition, String attribute, boolean isMust,
//			String operator, String props) {
//		switch (operator) {
//		case "eq":
//			ruleCondition.append(attribute);
//			ruleCondition.append(" == " + getValue(props));
//			break;
//		case "!eq":
//			ruleCondition.append(attribute);
//			ruleCondition.append(" != " + getValue(props));
//			break;
//		case "exist":
//			ruleCondition.append(attribute);
//			ruleCondition.append(" != " + null);
//			break;
//		case "!exist":
//			ruleCondition.append(attribute);
//			ruleCondition.append(" == " + null);
//			break;
//		}
//	}
//
//	private void processLong(String tokenId, StringBuffer ruleCondition, String attribute, boolean isMust,
//			String operator, String props) throws JsonMappingException, JsonProcessingException {
//		switch (operator) {
//		case "eq":
//			Predicate<HashMap<String, String>> equals = x -> Long.parseLong(x.get(attribute).toString()) == Long
//					.parseLong(getValue(props));
//			predicatesMap.get(tokenId).add(equals);
//			ruleCondition.append(attribute);
//			ruleCondition.append(" == " + getValue(props));
//			break;
//		case "!eq":
//			Predicate<HashMap<String, String>> notEquals = x -> !(Long.parseLong(x.get(attribute).toString()) == Long
//					.parseLong(getValue(props)));
//			predicatesMap.get(tokenId).add(notEquals);
//			ruleCondition.append(attribute);
//			ruleCondition.append(" != " + getValue(props));
//			break;
//		case "exist":
//			Predicate<HashMap<String, String>> exists = x -> x.get(attribute) != null;
//			predicatesMap.get(tokenId).add(exists);
//			ruleCondition.append(attribute);
//			ruleCondition.append(" != " + null);
//			break;
//		case "!exist":
//			Predicate<HashMap<String, String>> notExists = x -> x.get(attribute) == null;
//			predicatesMap.get(tokenId).add(notExists);
//			ruleCondition.append(attribute);
//			ruleCondition.append(" == " + null);
//			break;
//		case "gt":
//			Predicate<HashMap<String, String>> greaterThan = x -> Long.parseLong(x.get(attribute).toString()) > Long
//					.parseLong(getValue(props));
//			predicatesMap.get(tokenId).add(greaterThan);
//			ruleCondition.append(attribute);
//			ruleCondition.append(" > " + getValue(props));
//			break;
//		case "lt":
//			Predicate<HashMap<String, String>> lesserThan = x -> Long.parseLong(x.get(attribute).toString()) < Long
//					.parseLong(getValue(props));
//			predicatesMap.get(tokenId).add(lesserThan);
//			ruleCondition.append(attribute);
//			ruleCondition.append(" < " + getValue(props));
//			break;
//		case "gte":
//			Predicate<HashMap<String, String>> greaterThanEquals = x -> Long
//					.parseLong(x.get(attribute).toString()) >= Long.parseLong(getValue(props));
//			predicatesMap.get(tokenId).add(greaterThanEquals);
//			ruleCondition.append(attribute);
//			ruleCondition.append(" >= " + getValue(props));
//			break;
//		case "lte":
//			Predicate<HashMap<String, String>> lesserThanEquals = x -> Long
//					.parseLong(x.get(attribute).toString()) <= Long.parseLong(getValue(props));
//			predicatesMap.get(tokenId).add(lesserThanEquals);
//			ruleCondition.append(attribute);
//			ruleCondition.append(" <= " + getValue(props));
//			break;
//		// Shanth - check with Krishna if this required - 7 Jan 2024
////		case "bet":
////			Predicate<HashMap<String, String>> between = x -> Long.parseLong( x.get(attribute).toString()) ==  Long.parseLong(getValue(props));
////			predicatesMap.get(tokenId).add(equals);
////			JsonNode range = getRange(props);
////			ruleCondition.append(attribute);
////			ruleCondition.append(" >= " + range.get("from").asLong() + " &&");
////			ruleCondition.append(attribute);
////			ruleCondition.append(" <= " + range.get("to").asLong());
////			break;
////		case "!bet":
////			JsonNode between = getRange(props);
////			ruleCondition.append(attribute);
////			ruleCondition.append(" !> " + between.get("from").asLong() + " &&");
////			ruleCondition.append(attribute);
////			ruleCondition.append(" !< " + between.get("to").asLong());
////			break;
//		}
//
//	}

	public void processText(GroupCondition groupCondition, String tokenId, StringBuffer ruleCondition, String attribute,
			boolean isMust, String operator, String props) {

		StringBuffer localRuleCondition = new StringBuffer();
		String ruleConditionPhrase = null;
		switch (operator) {
		case "exist":
			
			ruleConditionPhrase = attribute + " != null";
			Predicate<HashMap<String, String>> notNull = x -> x.get(attribute) != null;
			predicatesMap.get(tokenId).add(notNull);
			addConditionToGroupCondition(groupCondition, attribute, "text", getAtrributeValue(props), operator, isMust, notNull,ruleConditionPhrase);
			System.out.println("The operation is 'exist'");
			localRuleCondition.append(attribute);
			localRuleCondition.append(" != " + null);

			break;
		case "eq":
			ruleConditionPhrase = "(" + attribute + " != null) && (" + attribute + ".equalsIgnoreCase("+getAtrributeValue(props)+")";
			String atttributeValue = getAtrributeValue(props);
			Predicate<HashMap<String, String>> equals = x -> x.get(attribute) != null
					&& atttributeValue.equalsIgnoreCase(x.get(attribute));
			predicatesMap.get(tokenId).add(equals);
			addConditionToGroupCondition(groupCondition, attribute, "text", atttributeValue, operator, isMust, equals, ruleConditionPhrase);
			System.out.println("The operation is 'eq'");
			localRuleCondition.append(attribute);
			localRuleCondition.append(".equalsIgnoreCase(" + getAtrributeValue(props) + ")");
			break;
//		case "cont":
//			ruleConditionPhrase = "(" + attribute + " != null) && (" + attribute + ".contains("+getAtrributeValue(props)+")";
//			Predicate<HashMap<String, String>> contains = x -> x.get(attribute) != null
//					&& ((String) x.get(attribute)).toString().contains(getAtrributeValue(props));
//			predicatesMap.get(tokenId).add(contains);
//			addConditionToGroupCondition(groupCondition, attribute, "text", getAtrributeValue(props), operator, isMust, contains,ruleConditionPhrase);
//			System.out.println("The operation is 'cont'");
//			// need to understand the difference
//			localRuleCondition.append(attribute);
//			localRuleCondition.append(".contains(" + getAtrributeValue(props) + ")");
//			break;
//		case "!eq":
//			ruleConditionPhrase = "(" + attribute + " != null) && !(" + attribute + ".equalsIgnoreCase("+getAtrributeValue(props)+")";
//			Predicate<HashMap<String, String>> notEquals = x -> x.get(attribute) != null
//					&& !(((String) x.get(attribute)).toString().equalsIgnoreCase(getAtrributeValue(props)));
//			predicatesMap.get(tokenId).add(notEquals);
//			addConditionToGroupCondition(groupCondition, attribute, "text", getAtrributeValue(props), operator, isMust, notEquals, ruleConditionPhrase);
//			System.out.println("The operation is '!eq'");
//			localRuleCondition.append("!");
//			localRuleCondition.append(attribute);
//			localRuleCondition.append(".equalsIgnoreCase(" + getAtrributeValue(props) + ")");
//			break;
//		case "!cont":
//			ruleConditionPhrase = "(" + attribute + " != null) && !(" + attribute + ".contains("+getAtrributeValue(props)+")";
//
//			Predicate<HashMap<String, String>> notContains = x -> x.get(attribute) != null
//					&& (!((String) x.get(attribute)).toString().contains(getAtrributeValue(props)));
//			predicatesMap.get(tokenId).add(notContains);
//			addConditionToGroupCondition(groupCondition, attribute, "text", getAtrributeValue(props), operator, isMust, notContains,ruleConditionPhrase);
//			System.out.println("The operation is '!cont'");
//			localRuleCondition.append("!");
//			localRuleCondition.append(attribute);
//			localRuleCondition.append(".contains(" + getAtrributeValue(props) + ")");
//			// Add your logic here
//			break;
//		case "startsWith":
//			ruleConditionPhrase = "(" + attribute + " != null) && (" + attribute + ".startsWith("+getAtrributeValue(props)+")";
//			Predicate<HashMap<String, String>> startsWith = x -> x.get(attribute) != null
//					&& ((String) x.get(attribute)).toString().startsWith(getAtrributeValue(props));
//			predicatesMap.get(tokenId).add(startsWith);
//			addConditionToGroupCondition(groupCondition, attribute, "text", getAtrributeValue(props), operator, isMust, startsWith,ruleConditionPhrase);
//			System.out.println("The operation is 'startsWith'");
//			localRuleCondition.append(attribute);
//			localRuleCondition.append(".startsWith(" + getAtrributeValue(props) + ")");
//			// Add your logic here
//			break;
//		case "endsWith":
//			ruleConditionPhrase = "(" + attribute + " != null) && (" + attribute + ".endsWith("+getAtrributeValue(props)+")";
//			Predicate<HashMap<String, String>> endsWith = x -> x.get(attribute) != null
//					&& ((String) x.get(attribute)).toString().endsWith(getAtrributeValue(props));
//			predicatesMap.get(tokenId).add(endsWith);
//			addConditionToGroupCondition(groupCondition, attribute, "text", getAtrributeValue(props), operator, isMust, endsWith,ruleConditionPhrase);
//			System.out.println("The operation is 'endsWith'");
//			localRuleCondition.append(attribute);
//			localRuleCondition.append(".endsWith(" + getAtrributeValue(props) + ")");
//			// Add your logic here
//			break;
//		case "notStartsWith":
//			ruleConditionPhrase = "(" + attribute + " != null) && !(" + attribute + ".startsWith("+getAtrributeValue(props)+")";
//			Predicate<HashMap<String, String>> notStartsWith = x -> x.get(attribute) != null
//					&& (!((String) x.get(attribute)).toString().startsWith(getAtrributeValue(props)));
//			predicatesMap.get(tokenId).add(notStartsWith);
//			addConditionToGroupCondition(groupCondition, attribute, "text", getAtrributeValue(props), operator, isMust, notStartsWith,ruleConditionPhrase);
//			System.out.println("The operation is 'notStartsWith'");
//			localRuleCondition.append("!");
//			localRuleCondition.append(attribute);
//			localRuleCondition.append(".startsWith(" + getAtrributeValue(props) + ")");
//			// Add your logic here
//			break;
//		case "notEndsWith":
//			ruleConditionPhrase = "(" + attribute + " != null) && !(" + attribute + ".endsWith("+getAtrributeValue(props)+")";
//			Predicate<HashMap<String, String>> notEndsWith = x -> x.get(attribute) != null
//					&& (!((String) x.get(attribute)).toString().endsWith(getAtrributeValue(props)));
//			predicatesMap.get(tokenId).add(notEndsWith);
//			addConditionToGroupCondition(groupCondition, attribute, "text", getAtrributeValue(props), operator, isMust, notEndsWith,ruleConditionPhrase);
//			System.out.println("The operation is 'notEndsWith'");
//			localRuleCondition.append("!");
//			localRuleCondition.append(attribute);
//			localRuleCondition.append(".endsWith(" + getAtrributeValue(props) + ")");
//			// Add your logic here
//			break;
//		case "!exist":
//			ruleConditionPhrase = "(" + attribute + " == null)";
//			Predicate<HashMap<String, String>> isNull = x -> x.get(attribute)== null;
//			predicatesMap.get(tokenId).add(isNull);
//			addConditionToGroupCondition(groupCondition, attribute, "text", getAtrributeValue(props), operator, isMust, isNull,ruleConditionPhrase);
//			System.out.println("The operation is '!exist'");
//			localRuleCondition.append(attribute);
//			localRuleCondition.append(" == " + null);
//			break;
//		case "regex":
//			ruleConditionPhrase = "(" + attribute + " != null) && (" + attribute + ".matches("+getAtrributeValue(props)+")";
//			Predicate<HashMap<String, String>> inRegex = x -> x.get(attribute) != null
//					&& x.get(attribute).toString().matches(getAtrributeValue(props));
//			predicatesMap.get(tokenId).add(inRegex);
//			addConditionToGroupCondition(groupCondition, attribute, "text", getAtrributeValue(props), operator, isMust, inRegex,ruleConditionPhrase);
//			System.out.println("in regex....");
//			localRuleCondition.append(attribute);
//			localRuleCondition.append(".matches(" + getAtrributeValue(props) + ")");
//			break;
		default:
			System.out.println("Invalid operation");
			break;
		}
		logger.log(Level.INFO, "localRuleCondition ".toUpperCase() + localRuleCondition.toString() + " " + isMust);
		ruleCondition.append(localRuleCondition.toString());
	}

	public void addConditionToGroupCondition(GroupCondition groupCondition,String attribute, 
			String type, String props, String operator, boolean isMust, 
			Predicate<HashMap<String, String>> predicate, String ruleCondition) {
		if (isMust) {
			Condition condition = new Condition<>(predicate, LogicalOperator.AND);
			condition.attribute =attribute;
			condition.type =type;
//			System.out.println(" ADDING TO CONDITION ------------" + props);
			condition.props =props;
			condition.operator =operator;
			condition.isMust = isMust;
			condition.ruleCondition = ruleCondition;
			
			groupCondition.addCondition(condition);
		} else {
			Condition condition = new Condition<>(predicate, LogicalOperator.OR);
			condition.attribute =attribute;
			condition.type =type;
			condition.props =props;
//			System.out.println(" ADDING TO CONDITION ------------" + props);

			condition.operator =operator;
			condition.isMust = isMust;
			condition.ruleCondition = ruleCondition;
			groupCondition.addCondition(condition);
		}
	}

	public String returnMethodFormat(String attribute) {

		String[] parts = attribute.split("\\.");
		String newStr = "";
//			StringBuilder field = new StringBuilder();
//			field.append("%s");
//			List<String> temp = new ArrayList<>(); 
//			for (int i = 1; i < parts.length; i++) {
//				field.append(".get%s()");
//				temp.add(capitalize(parts[i]));
//				
//			}
//			newStr = String.format(field.toString(), temp);
		if (parts.length == 2) {

			String namePart = parts[0];
			String processPart = capitalize(parts[1]);

			newStr = String.format("%s.get%s()", namePart, processPart);
			System.out.println(newStr); // prints "name.getProcess().getHostname()"
		} else if (parts.length == 3) {

			String namePart = parts[0];
			String processPart = capitalize(parts[1]);
			String hostNamePart = capitalize(parts[2]);

			newStr = String.format("%s.get%s().get%s()", namePart, processPart, hostNamePart);
			System.out.println(newStr); // prints "name.getProcess().getHostname()"
		}

		System.out.println(" METHODS replaces " + newStr);
		return newStr;
	}

	public String capitalize(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

//    private static List<Condition<Integer>> getConditionsFromDatabase() {
//        // Simulated conditions from a database
//        Predicate<Integer> evenPredicate = x -> x % 2 == 0;
//        Predicate<Integer> greaterThanFivePredicate = x -> x > 5;
//
//        // Represent conditions using pairs of predicate and logical operator
//        return List.of(
//            new Condition<>(evenPredicate, LogicalOperator.AND),
//            new Condition<>(greaterThanFivePredicate, LogicalOperator.OR)
//        );
//    }
//    
//    // Method to build a combined predicate based on conditions
//    private static <T> Predicate<T> buildCombinedPredicate(List<Condition<T>> conditions) {
//        Predicate<T> combinedPredicate = x -> true;
//
//        for (Condition<T> condition : conditions) {
//            combinedPredicate = switch (condition.getLogicalOperator()) {
//                case AND -> combinedPredicate.and(condition.getPredicate());
//                case OR -> combinedPredicate.or(condition.getPredicate());
//            };
//        }
//
//        return combinedPredicate;
//    }

//    // Method to filter a list based on a predicate
//    private static <T> List<T> filterList(List<T> list, Predicate<T> predicate) {
//        return list.stream().filter(predicate).collect(Collectors.toList());
//    }
//    
	// Method to combine predicates based on a logical operator
	private Predicate<HashMap<String, String>> combinePredicatesAnd(
			List<Predicate<HashMap<String, String>>> predicates) {
		return predicates.stream().reduce(Predicate::and).orElse(x -> true);
	};

	// Method to combine predicates based on a logical operator
	private Predicate<HashMap<String, String>> combinePredicatesOr(
			List<Predicate<HashMap<String, String>>> predicates) {
		return predicates.stream().reduce(Predicate::or).orElse(x -> true);
	}

	// Method to filter a list based on a predicate
	private List<String> filterList(String key, List<HashMap<String, String>> list,
			Predicate<HashMap<String, String>> predicate) {
		return list.stream().filter(predicate).map(x -> x.get(key).toString()).collect(Collectors.toList());
	}

	public boolean validateGeneratedRulesAgainstEventDB() {
		
		if (!isRuleGenerated) {
			generateRules();
		}
			
		return true;
	}
	
}
