package com.example;

import com.google.protobuf.Message;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import org.junit.jupiter.api.Test;
import v3.PersonOuterClass;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SrProtoSerdeTest {

    String mockSRUrl = "mock://localhost:8081";

    String dummyTopicName = "dummy";

    Map<String, String> serdeMap = Map.of(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, mockSRUrl);

    @Test
    void srUnspecificProtoTest() {

        KafkaProtobufSerializer<Message> serializer = new KafkaProtobufSerializer<>();
        serializer.configure(serdeMap, false);

        PersonOuterClass.Person person = PersonOuterClass.Person.newBuilder()
                .setId(1)
                .setName("Me")
                .setEmail("my@email.com")
                .build();

        byte[] serializedPerson = serializer.serialize(dummyTopicName, person);

        KafkaProtobufDeserializer<Message> deserializer = new KafkaProtobufDeserializer<>();
        deserializer.configure(serdeMap, false);

        Message deserialized = deserializer.deserialize(dummyTopicName, serializedPerson);

        System.out.println(deserialized);
    }


    @Test
    void srSpecificProtoTest() {

        KafkaProtobufSerializer<Message> serializer = new KafkaProtobufSerializer<>();
        serializer.configure(serdeMap, false);

        PersonOuterClass.Person person = PersonOuterClass.Person.newBuilder()
                .setId(1)
                .setName("Me")
                .setEmail("my@email.com")
                .build();

        byte[] serializedPerson = serializer.serialize(dummyTopicName, person);

        KafkaProtobufDeserializer<PersonOuterClass.Person> deserializer = new KafkaProtobufDeserializer<>();
        Map<String, Object> specificClassSserdeMap = Map.of(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, mockSRUrl,
                                                            KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, PersonOuterClass.Person.class);
        deserializer.configure(specificClassSserdeMap, false);

        PersonOuterClass.Person deserializedPerson = deserializer.deserialize(dummyTopicName, serializedPerson);

        assertThat(deserializedPerson).isEqualTo(person);
    }

}
