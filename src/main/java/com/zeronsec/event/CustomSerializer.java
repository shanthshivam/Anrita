package com.zeronsec.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomSerializer implements Serializer<HashMap<String,List<String>>> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public byte[] serialize(String topic, HashMap<String,List<String>> data) {
        try {
            if (data == null){
                System.out.println("Null received at serializing");
                return null;
            }
            //System.out.println("Serializing...");
            return objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("Error when serializing ArrayList<Event> to byte[]");
        }
    }

    @Override
    public void close() {
    }
}
