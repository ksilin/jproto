package com.example;

import com.google.protobuf.Message;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import v3.PersonOuterClass;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class DifferentiatingGenericDeserializerTest {

    Logger log = Logger.getLogger(DifferentiatingGenericDeserializerTest.class);

    String mockSRUrl = "mock://localhost:8081";

    String dummyTopicName = "dummy";

    Map<String, Object> serdeMap = Map.of(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, mockSRUrl);
    // DO NOT set the specific value type if you need generic deserialization.
    //                 KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, PersonOuterClass.Person.class);


    @Test
    void mustDeserializeSRProto(){

        KafkaProtobufSerializer<Message> serializer = new KafkaProtobufSerializer<>();
        serializer.configure(serdeMap, false);

        PersonOuterClass.Person person = PersonOuterClass.Person.newBuilder()
                .setId(1)
                .setName("Me")
                .setEmail("my@email.com")
                .build();

        byte[] srSerializedPerson = serializer.serialize(dummyTopicName, person);

        byte[] nonSRSerializedPerson = person.toByteArray();

        DifferentiatingGenericDeserializer<PersonOuterClass.Person> differentiatingDeserializer = new DifferentiatingGenericDeserializer<>(ProtoDescriptorMapper.getDescriptorToClassMap(), new PersonProtoNonSrDeserializer());
        differentiatingDeserializer.configure(serdeMap, false);

        PersonOuterClass.Person srPerson = differentiatingDeserializer.deserialize(dummyTopicName, srSerializedPerson);
        assertThat(srPerson).isEqualTo(person);

        PersonOuterClass.Person nonSrPerson = differentiatingDeserializer.deserialize(dummyTopicName, nonSRSerializedPerson);
        assertThat(nonSrPerson).isEqualTo(person);
    }
}
