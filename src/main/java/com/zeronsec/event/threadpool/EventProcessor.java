package com.zeronsec.event.threadpool;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.zeronsec.event.ConfigProperties;

@Component
public class EventProcessor implements Runnable {

	static final Logger LOGGER = Logger.getLogger(EventProcessor.class.getName());
	static String KAFKA_TOPIC_INPUT = ConfigProperties.getProperty("KAFKA_TOPIC_INPUT");
	static String KAFKA_TOPIC_OUTPUT = ConfigProperties.getProperty("KAFKA_TOPIC_OUTPUT");

	private int processorId;

	private Properties properties;
	private Properties outputTopicproperties;
	private EventPool pool;
	private Producer<String, Object> producer;
	public String processorName;

	private Map<String, Predicate<HashMap<String, String>>> predicatesMap ;

	
	
	public Map<String, Predicate<HashMap<String, String>>> getPredicatesMap() {
		return predicatesMap;
	}

	public void setPredicatesMap(Map<String, Predicate<HashMap<String, String>>> predicatesMap) {
		this.predicatesMap = predicatesMap;
	}

	public int getProcessorId() {
		return processorId;
	}

	public void setProcessorId(int processorId) {
		this.processorId = processorId;
	}

	public String getProcessorName() {
		return processorName;
	}

	public EventProcessor(int i) {
		this.processorName = "EventProcessor-" + i;
	}

	public EventProcessor() {
		// TODO Auto-generated constructor stub
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getOutputTopicProperties() {
		return outputTopicproperties;
	}

	public void setOutputTopicProperties(Properties outputTopicproperties) {
		this.outputTopicproperties = outputTopicproperties;
	}

	public EventPool getPool() {
		return pool;
	}

	public void setPool(EventPool pool) {
		this.pool = pool;
	}

	public Producer<String, Object> getProducer() {
		return producer;
	}

	public void setProducer(Producer<String, Object> producer) {
		this.producer = producer;
	}

	@Override
	public void run() {
		long processorStartTime = System.currentTimeMillis();
		pool.setTime(processorName, System.currentTimeMillis(), 0);
		Consumer<String, List<HashMap<String,String>>> consumer = new KafkaConsumer<>(properties);

		// Subscribe to the Kafka topic
		consumer.subscribe(Collections.singletonList(KAFKA_TOPIC_INPUT));
		//consumer.assign(List.of(new TopicPartition(KAFKA_TOPIC_INPUT, processorId)));

		Producer<String, HashMap<String,List<String>>> producer = new KafkaProducer<>(outputTopicproperties);
		long totalEventsRunningCount = 0;
		
		while (true) {
			long progStartTime = System.currentTimeMillis();

//			ConsumerRecords<String, ArrayList<Event>> records = consumer.poll(Duration.ofMillis(50));

			ConsumerRecords<String, List<HashMap<String,String>>> records = consumer.poll(Duration.ofMillis(50));

			LOGGER.log(Level.INFO, "Polling for new records " + records.count() + " eventPool size "
					+ pool.size() + " took " + (System.currentTimeMillis() - progStartTime) + " msecs to poll");

			// Process the received events List<HashMap> objects
			records.forEach(record -> {
				long startTime = System.currentTimeMillis();
				HashMap<String,List<String>> processResultMap = new HashMap<>();
				
				
				List<HashMap<String, String>> eventList = record.value();
				List<String> eventsCountList = new ArrayList();
						eventsCountList.add(eventList.size()+"");
				processResultMap.put("eventsCount", eventsCountList );
				long ruleProcessingStartTime = System.nanoTime();

				try {
					predicatesMap.forEach((ruleId, predicate) -> {
						
								List<String> result = eventList.stream().filter(predicate).map(m -> m.get("eventId").toString())
										.collect(Collectors.toList());
								processResultMap.put(ruleId, result);	
						});
										
					producer.send(new ProducerRecord<>(KAFKA_TOPIC_OUTPUT, processorId, processorId+"", processResultMap));	
					LOGGER.log(Level.INFO,
							Thread.currentThread().getName() + " Time taken to send to output topic "
									+ eventList.size() + " no of events in " + (System.currentTimeMillis() - startTime)
									+ " msecs & Rules processing time " + (System.nanoTime()- ruleProcessingStartTime) + " nano seconds");
					pool.setTime(processorName, System.currentTimeMillis(), eventList.size());

				} catch (Exception e) {
					e.printStackTrace();
					// sent a report to a DB for resending the events.
					System.out.println(Thread.currentThread().getName()
							+ " Crashed proceessing Events. Serious problem !!!!!!!!!!!!");
				} finally {
					// pool.push(this);
					//kieSession.dispose();
				}

			});

		}
	}
}
