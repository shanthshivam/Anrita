package com.zeronsec.event;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.zeronsec.event.object.Event;
import com.zeronsec.event.threadpool.EventPool;
import com.zeronsec.event.threadpool.EventProcessor;

@SpringBootApplication
public class AnritaEventStream1Application {
	static String KAFKA_TOPIC_INPUT = ConfigProperties.getProperty("KAFKA_TOPIC_INPUT");
	static String DRL_FILE_PATH = ConfigProperties.getProperty("DRL_FILE_PATH");
	static String KAFKA_NODE_1 = ConfigProperties.getProperty("KAFKA_NODE_1");
	static String KAFKA_NODE_1_PORT = ConfigProperties.getProperty("KAFKA_NODE_1_PORT");
	static String KAFKA_TOPIC_OUTPUT = ConfigProperties.getProperty("KAFKA_TOPIC_OUTPUT");
	static String RULES_EVENT_DRL = ConfigProperties.getProperty("DRL_FILE_PATH");

	public static void main(String[] args) {
		SpringApplication.run(AnritaEventStream1Application.class, args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		return args -> {
			System.out.println("Running the command-line application...");

			Properties properties = new Properties();
			properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Replace with your Kafka broker
																						// address
			properties.put(ConsumerConfig.GROUP_ID_CONFIG, "event-consumer-group");
			properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
			properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "com.zeronsec.event.CustomDeserializer");

			Properties outputTopicproperties = new Properties();
			outputTopicproperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // Replace with your
																									// Kafka
			outputTopicproperties.put(ConsumerConfig.GROUP_ID_CONFIG, "event-output-group");
			outputTopicproperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
					"org.apache.kafka.common.serialization.StringSerializer");
			outputTopicproperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
					"com.zeronsec.event.CustomSerializer");
			KieServices kieServices = KieServices.Factory.get();
			KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
			kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_EVENT_DRL));
			KieBuilder kb = kieServices.newKieBuilder(kieFileSystem);
			kb.buildAll();
			KieModule kieModule = kb.getKieModule();
			KieContainer kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
			

			int poolSize = 10;
			EventPool eventProcessorPool = new EventPool<>(poolSize);

			// Create a Kafka consumer
			Consumer<String, Event> consumer = new KafkaConsumer<>(properties);

			// Subscribe to the Kafka topic
			consumer.subscribe(Collections.singletonList(KAFKA_TOPIC_INPUT));
			Producer<String, Object> producer = new KafkaProducer<>(outputTopicproperties);
				
			while (true) {
				// Poll for new records from the Kafka topic
				// System.out.println("Polling for new records");


				if (eventProcessorPool.size()> 0 ) {

					ConsumerRecords<String, Event> records = consumer.poll(Duration.ofMillis(100));
					System.out.println(
							"Polling for new records " + records.count() + " eventPool size " + eventProcessorPool.size());
					ArrayList<Event> list = new ArrayList<Event>();

					// Process the received Employee objects
					records.forEach(record -> {
						list.add(record.value());
					});
					
					EventProcessor processor = eventProcessorPool.pop();
					//String threadName = "EventProcessor-" + (eventProcessorPool.size() - 1);
					//processor = new EventProcessor(eventProcessorPool, producer, list,kieContainer);
					processor.setEventList(list);
					processor.setKieContainer(kieContainer);
					processor.setPool(eventProcessorPool);
					processor.setProducer(producer);
					
					Thread t = new Thread(processor,processor.processorName);
					t.start();
					System.out.println("New EventProcessor started " + processor.processorName);

				}
			}
		};
	}
}
