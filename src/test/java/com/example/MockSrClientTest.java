package com.example;

import com.google.protobuf.Message;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import v3.PersonOuterClass;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MockSrClientTest {

    Logger log = Logger.getLogger(MockSrClientTest.class);

    String mockSRUrl = "mock://localhost:8081";

    String dummyTopicName = "dummy";

    Map<String, Object> serdeMap = Map.of(
            KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, mockSRUrl,
                                          // either auto-register, or register schemas explicitly
                                          KafkaProtobufSerializerConfig.AUTO_REGISTER_SCHEMAS, true);

    SchemaRegistryClient srClient = new MockSchemaRegistryClient();
    KafkaProtobufSerializer<Message> serializer = new KafkaProtobufSerializer<>(srClient);
    KafkaProtobufDeserializer<Message> deserializer = new KafkaProtobufDeserializer<>(srClient);

    @Test
    void serializerMustRegisterSchemasAtSubjects() throws RestClientException, IOException {

        serializer.configure(serdeMap, false);

        assertThat(srClient.getAllSubjects()).isEmpty();

        PersonOuterClass.Person person = PersonOuterClass.Person.newBuilder()
                .setId(1)
                .setName("Me")
                .setEmail("my@email.com")
                .build();

        byte[] serializedPerson = serializer.serialize(dummyTopicName, person);

        log.info("subjects created: ");
        srClient.getAllSubjects().forEach(System.out::println);
        // dummy-value
        assertThat(srClient.getAllSubjects()).hasSize(1);

        //deserializer.configure(serdeMap, false);

        Message deserialized = deserializer.deserialize(dummyTopicName, serializedPerson);

        System.out.println(deserialized);

    }
}
