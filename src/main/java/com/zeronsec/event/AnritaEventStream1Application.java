package com.zeronsec.event;


import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.zeronsec.event.rules.RuleRepository;
import com.zeronsec.event.threadpool.EventPool;
import com.zeronsec.event.threadpool.EventProcessor;

@SpringBootApplication(scanBasePackages = { "com.zeronsec.event" })
//@ComponentScan(basePackages = "com.zeronsec.event")
public class AnritaEventStream1Application {
	static String KAFKA_TOPIC_INPUT = ConfigProperties.getProperty("KAFKA_TOPIC_INPUT");
	static String DRL_FILE_PATH = ConfigProperties.getProperty("DRL_FILE_PATH");
	static String KAFKA_NODE_INPUT = ConfigProperties.getProperty("KAFKA_NODE_INPUT");
	static String KAFKA_NODE_INPUT_PORT = ConfigProperties.getProperty("KAFKA_NODE_INPUT_PORT");
	static String KAFKA_NODE_OUTPUT = ConfigProperties.getProperty("KAFKA_NODE_OUTPUT");
	static String KAFKA_NODE_OUTPUT_PORT = ConfigProperties.getProperty("KAFKA_NODE_OUTPUT_PORT");

	static String KAFKA_TOPIC_OUTPUT = ConfigProperties.getProperty("KAFKA_TOPIC_OUTPUT");
	static String RULES_EVENT_DRL = ConfigProperties.getProperty("DRL_FILE_PATH");
	static final Logger LOGGER = Logger.getLogger(AnritaEventStream1Application.class.getName());
	@Autowired
	SpringPropertiesUtil propertiesUtil;
	
	public static void main(String[] args) {
		SpringApplication.run(AnritaEventStream1Application.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {

			System.out.println("Running the command-line application...");

			Properties properties = new Properties();
			//properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Replace with your Kafka broker address
			properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_NODE_INPUT +":" + KAFKA_NODE_INPUT_PORT); // Replace with your Kafka broker address
			
			properties.put(ConsumerConfig.GROUP_ID_CONFIG, "event-consumer-group");
			properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "com.zeronsec.event.CustomDeserializer");
			properties.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, RoundRobinAssignor.class.getName());
//			properties.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 100024);
//			properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 100024);
//			properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 2);
//			properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 50);
			
			
			
//			properties.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, "1000");

			Properties outputTopicproperties = new Properties();
			outputTopicproperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_NODE_OUTPUT + ":" + KAFKA_NODE_OUTPUT_PORT); // Replace with your
																									// Kafka
			outputTopicproperties.put(ConsumerConfig.GROUP_ID_CONFIG, "event-output-group");
			outputTopicproperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
					"org.apache.kafka.common.serialization.StringSerializer");
			outputTopicproperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
					"com.zeronsec.event.CustomSerializer");

//			// Create a Kafka consumer
//			Consumer<String, Event> consumer = new KafkaConsumer<>(properties);
//
//			// Subscribe to the Kafka topic
//			consumer.subscribe(Collections.singletonList(KAFKA_TOPIC_INPUT));
//			Producer<String, Object> producer = new KafkaProducer<>(outputTopicproperties);

			Map<String,String> propertiesMap = propertiesUtil.getAllPropWithPrefix("rules");
			
			List<HashMap<String,String>> listOfRuleCategories = getRuleCatetories(propertiesMap);
			HashMap<String, RuleRepository> ruleReposMap = new HashMap<>();
			
			listOfRuleCategories.forEach(map -> {
				
				Optional<String> dbUrlOpt = map.entrySet().stream().filter(entry -> entry.getKey().contains("db_url")).map(entry -> entry.getValue()).findFirst();
				String dbUrl = dbUrlOpt.get();
				Optional<String> dbClassOpt = map.entrySet().stream().filter(entry -> entry.getKey().contains("db_class")).map(entry -> entry.getValue()).findFirst();
				String dbClass = dbClassOpt.get();
				Optional<String> dbUserOpt = map.entrySet().stream().filter(entry -> entry.getKey().contains("db_user")).map(entry -> entry.getValue()).findFirst();
				String dbUser = dbUserOpt.get();
				Optional<String> dbPassOpt = map.entrySet().stream().filter(entry -> entry.getKey().contains("db_pass")).map(entry -> entry.getValue()).findFirst();
				String dbPass = dbPassOpt.get();
				
				RuleRepository predicateMaker = new RuleRepository(dbClass,
						dbUrl, dbUser, dbPass);
				predicateMaker.generateRules();
				
				Optional<String> keyCatetoryOptional = map.entrySet().stream().filter(entry -> entry.getKey().contains("db_pass")).map(entry -> entry.getKey()).findFirst();
				String key = keyCatetoryOptional.get();
				StringTokenizer token = new StringTokenizer(key, ".");
				token.nextToken();
				ruleReposMap.put( token.nextToken(),predicateMaker );
				
			});
			
			RuleRepository predicateMaker = ruleReposMap.get("firewall");
			Map<String, Predicate<HashMap<String, String>>> predicatesMap = predicateMaker.getPredicatesLookUpMap();

			
			// pool size is the no of partitions
			int poolSize = 8;

			long eventRuningCount = 0;
			long startTime = System.currentTimeMillis();
			
			EventPool eventProcessorPool = new EventPool<>(poolSize);
			boolean isWarmUp = true;
			while (true) {
				// Poll for new records from the Kafka topic
				// System.out.println("Polling for new records");

				if (isWarmUp) {
					int loopCount = eventProcessorPool.size();
					for (int i = 0; i < loopCount; i++) {
						
						EventProcessor processor = eventProcessorPool.pop();
						processor.setProcessorId(i);
						processor.setProperties(properties);
						processor.setOutputTopicProperties(outputTopicproperties);
						processor.setPool(eventProcessorPool);
						processor.setPredicatesMap(predicatesMap);
						Thread t = new Thread(processor, processor.getProcessorName());
						t.start();
						LOGGER.log(Level.INFO,
								Thread.currentThread().getName() + " started " + processor.getProcessorName());
						isWarmUp = false;
					}
				} 
//					else {
//					//if (eventProcessorPool.size() == 0) {
//						
//						
//						LOGGER.log(Level.INFO, Thread.currentThread().getName() + " All Processors are busy  ");
//						Thread.sleep(2000);
//						Map<String, Map<Long,Integer>> eventsTimeMap = eventProcessorPool.getEventsTimeMap();
//						
//						
//						Iterator<String> iterator = eventsTimeMap.keySet().iterator();
//						while (iterator.hasNext()) {
//							String key = iterator.next();
//							
//							Map<Long, Integer> synchronizedTreeMap = eventsTimeMap.get(key);
//
//							int eventsCount = 0;
//							ArrayList<Long> list = new ArrayList<>();
//					        synchronized (synchronizedTreeMap) {
//					            for (long keyTMap : synchronizedTreeMap.keySet()) {
//					            	list.add(keyTMap);
//					                int value = synchronizedTreeMap.get(keyTMap);
//					                eventsCount += value;
//					            }
//					        }
//							
//							if (list.size() > 0 && eventsCount > 0) {
//								
//													        
//					        LOGGER.log(Level.INFO, Thread.currentThread().getName() + " " + key + " processed " + eventsCount + " events in " + (list.get(list.size()-1) - list.get(0)) + " msecs");
//							}
//							else {
//								LOGGER.log(Level.INFO, Thread.currentThread().getName() + " No events have been processed yet!!!!!!!");
//								
//							}
//							
//						}
////					}
//				}

			}
		};
	}
	private List<HashMap<String,String>> getRuleCatetories(Map<String, String> propertiesMap) {
		
		HashMap<String,String> uniqueGroups = new HashMap<String, String>();
		
		Iterator<String> keyIterator = propertiesMap.keySet().iterator();
		
		while(keyIterator.hasNext()) {
			String uniqueGroup = keyIterator.next();

			StringTokenizer tokens = new StringTokenizer(uniqueGroup,".");
			tokens.nextToken();
						
			uniqueGroups.put(tokens.nextToken(), tokens.nextToken());
		}
		List<HashMap<String,String>> listOfCategoryProps = new ArrayList<>();
		uniqueGroups.forEach( (key, value) -> {
			Map<String,String> map = propertiesMap.entrySet().stream()
			.filter(entry -> entry.getKey().contains(key))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			listOfCategoryProps.add(new HashMap(map));
		});
		return listOfCategoryProps;
	}
	
	public static <K, V> Map<K, V> deepCopyHashMap(Map<K, V> original) {
        Map<K, V> copy = new HashMap<>();

        for (Map.Entry<K, V> entry : original.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();

            // If the value is an object, create a deep copy of it
            if (value instanceof Cloneable) {
                try {
                    // Assuming the value has a public clone method (you may need to handle differently for specific classes)
                    @SuppressWarnings("unchecked")
                    V clonedValue = (V) value.getClass().getMethod("clone").invoke(value);
                    copy.put(key, clonedValue);
                } catch (Exception e) {
                    e.printStackTrace(); // Handle the exception according to your needs
                }
            } else {
                // If the value is not an object or does not support cloning, use the original value
                copy.put(key, value);
            }
        }

        return copy;
    }
}
