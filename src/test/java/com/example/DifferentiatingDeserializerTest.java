package com.example;

import com.google.protobuf.Message;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;
import v3.PersonOuterClass;

import java.util.Map;

public class DifferentiatingDeserializerTest {

    Logger log = Logger.getLogger(DifferentiatingDeserializerTest.class);

    String mockSRUrl = "mock://localhost:8081";
    Map<String, Object> serdeMap = Map.of(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, mockSRUrl,
                                          KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, PersonOuterClass.Person.class);


    @Test
    void mustDeserializeSRProto(){

        KafkaProtobufSerializer<Message> serializer = new KafkaProtobufSerializer<>();
        serializer.configure(serdeMap, false);

        PersonOuterClass.Person person = PersonOuterClass.Person.newBuilder()
                .setId(1)
                .setName("Me")
                .setEmail("my@email.com")
                .build();

        byte[] srSerializedPerson = serializer.serialize("dummy", person);

        byte[] nonSRSerializedPerson = person.toByteArray();

        KafkaProtobufDeserializer<PersonOuterClass.Person> srProtoDeserializer = new KafkaProtobufDeserializer<>();
        srProtoDeserializer.configure(serdeMap, false);

        DifferentiatingDeserializer<PersonOuterClass.Person> differentiatingDeserializer = new DifferentiatingDeserializer<>(srProtoDeserializer, new PersonProtoNonSrDeserializer());
        //differentiatingDeserializer.configure(serdeMap, false);

        PersonOuterClass.Person srPerson = differentiatingDeserializer.deserialize("dummy", srSerializedPerson);
        log.info(srPerson);

        PersonOuterClass.Person nonSrPerson = differentiatingDeserializer.deserialize("dummy", nonSRSerializedPerson);

        log.info(nonSrPerson);
    }
}
