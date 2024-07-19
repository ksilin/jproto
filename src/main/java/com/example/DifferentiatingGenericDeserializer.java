package com.example;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.jboss.logging.Logger;

import java.util.Map;

public class DifferentiatingGenericDeserializer<T extends Message> implements Deserializer<T> {

    Logger log = Logger.getLogger(DifferentiatingGenericDeserializer.class);
    private static final byte MAGIC_BYTE = 0x0;
    private final KafkaProtobufDeserializer<DynamicMessage> dynamicDeserializer = new KafkaProtobufDeserializer<>();
    private final Map<Descriptors.Descriptor, Class<? extends Message>> descriptorToClassMap;

    private final Deserializer<T> nonSrDeserializer;

    public DifferentiatingGenericDeserializer(Map<Descriptors.Descriptor, Class<? extends Message>> descriptorToClassMap, Deserializer<T> nonSrDeserializer) {
        this.descriptorToClassMap = descriptorToClassMap;
        this.nonSrDeserializer = nonSrDeserializer;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        dynamicDeserializer.configure(configs, isKey);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            if (data[0] == MAGIC_BYTE) {
                log.info("found magic byte, deserializing with SR");
                DynamicMessage dynamicMessage = dynamicDeserializer.deserialize(topic, data);
                Descriptors.Descriptor descriptor = dynamicMessage.getDescriptorForType();

                if (descriptorToClassMap.containsKey(descriptor)) {
                    Class<? extends Message> targetClass = descriptorToClassMap.get(descriptor);
                    try {
                        Message.Builder builder = (Message.Builder) targetClass.getMethod("newBuilder").invoke(null);
                        return (T) builder.mergeFrom(dynamicMessage.toByteArray()).build();
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize message", e);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown message type: " + descriptor.getFullName());
                }
            } else {
                log.info("found NO magic byte, deserializing without SR");
                return nonSrDeserializer.deserialize(topic, data);
            }
        } catch (SerializationException e) {
            throw new SerializationException("Error deserializing message", e);
        }
    }
}
