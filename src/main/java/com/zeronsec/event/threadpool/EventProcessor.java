package com.zeronsec.event.threadpool;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.zeronsec.event.ConfigProperties;
import com.zeronsec.event.object.Event;
@Component
public class EventProcessor implements Runnable {

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

	public KieContainer getKieContainer() {
		return kieContainer;
	}

	public void setKieContainer(KieContainer kieContainer) {
		this.kieContainer = kieContainer;
	}

	static String KAFKA_TOPIC_OUTPUT = ConfigProperties.getProperty("KAFKA_TOPIC_OUTPUT");

	private List<Event> eventList;
	private EventPool pool;
	private Producer<String, Object> producer;
	private KieContainer kieContainer;
	public String processorName;
	public EventProcessor(int i) {
		this.processorName = "EventProcessor-" +i;
	}
	
	public EventProcessor() {
		// TODO Auto-generated constructor stub
	}
	
	public List<Event> getEventList() {
		return eventList;
	}

	public void setEventList(List<Event> eventList) {
		this.eventList = eventList;
	}

	public EventProcessor(EventPool pool, Producer<String, Object> producer, List<Event> list, KieContainer kieContainer) {

		this.eventList = list;
		this.pool = pool;
		this.producer = producer;
		this.kieContainer = kieContainer;
	}

	@Override
	public void run() {
		KieSession kieSession = kieContainer.newKieSession();
		try {

			for (int i = 0; i < eventList.size(); i++) {
				kieSession.insert(eventList.get(i));
			}
			kieSession.fireAllRules();

			long startTime = System.currentTimeMillis();
			producer.send(new ProducerRecord<>(KAFKA_TOPIC_OUTPUT,eventList));
			System.out.println(Thread.currentThread().getName() + " Time taken to send to output topic " + eventList.size() + " no of events in " + (System.currentTimeMillis() - startTime) + " msecs");

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(Thread.currentThread().getName() + " Serious problem !!!!!!!!!!!!");
		} finally {
			pool.push(this);
			kieSession.dispose();
		}
	}
}
