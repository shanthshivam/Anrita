package com.zeronsec.event;

import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zeronsec.event.object.Event;

public class CustomDeserializer implements Deserializer<Event> {
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public Event deserialize(String topic, byte[] data) {
        try {
            if (data == null){
                System.out.println("Null received at deserializing");
                return null;
            }
            //System.out.println("Deserializing...");
            return objectMapper.readValue(new String(data, "UTF-8"), Event.class);
        } catch (Exception e) {
            throw new SerializationException("Error when deserializing byte[] to Object");
        }
    }

    @Override
    public void close() {
    }
}
