package com.example;

import com.google.protobuf.Message;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.jboss.logging.Logger;

import java.util.Map;

public class DifferentiatingDeserializer<T extends Message> implements Deserializer<T> {

    Logger log = Logger.getLogger(DifferentiatingDeserializer.class);
    private static final byte MAGIC_BYTE = 0x0;
    private final KafkaProtobufDeserializer<T> srDeserializer;

    private final Deserializer<T> nonSrDeserializer;

    public DifferentiatingDeserializer(KafkaProtobufDeserializer<T> srDeserializer, Deserializer<T> nonSrDeserializer) {
        this.srDeserializer = srDeserializer;
        this.nonSrDeserializer = nonSrDeserializer;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        srDeserializer.configure(configs, isKey);
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }

        try {
            if (data[0] == MAGIC_BYTE) {
                log.info("found magic byte, deserializing with SR");
                return srDeserializer.deserialize(topic, data);
                // Extract schema ID and validate it
                // ByteBuffer buffer = ByteBuffer.wrap(data);
                // buffer.get(); // Skip magic byte
                   //int schemaId = buffer.getInt();
//                // Check if schema ID is valid by attempting to retrieve it from Schema Registry
//                try {
//                    schemaRegistryClient.getSchemaById(schemaId);
//                    return (T) protoDeserializer.deserialize(topic, data);
//                } catch (Exception e) {
//                    return (T) deserializeRegularMessage(data);
//                }
            } else {
                log.info("found NO magic byte, deserializing without SR");
                return nonSrDeserializer.deserialize(topic, data);
            }
        } catch (SerializationException e) {
            throw new SerializationException("Error deserializing message", e);
        }
    }
}
