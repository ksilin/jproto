package com.example;

import com.example.protox.CustomerOuterClass;
import com.example.protox.OrderOuterClass;
import com.example.protox.ProductOuterClass;
import com.example.protox.ProductPersonOrderSumOuterClass;
import com.google.protobuf.Message;
import io.confluent.kafka.schemaregistry.ParsedSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializerConfig;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ReferenceAndAnyOfTest {

    Logger log = Logger.getLogger(ReferenceAndAnyOfTest.class);

    String mockSRUrl = "mock://localhost:8081";

    String dummyTopicName = "dummy";

    Map<String, Object> serdeMap = Map.of(
            KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, mockSRUrl,
            // either auto-register, or register schemas explicitly
            KafkaProtobufSerializerConfig.AUTO_REGISTER_SCHEMAS, true);

    SchemaRegistryClient srClient = new MockSchemaRegistryClient();
    KafkaProtobufSerializer<Message> serializer = new KafkaProtobufSerializer<>(srClient);
    KafkaProtobufDeserializer<Message> deserializer = new KafkaProtobufDeserializer<>(srClient);

    ProductOuterClass.Product product = ProductOuterClass.Product.newBuilder()
            .setId("1")
            .setName("myProduct")
            .build();

    CustomerOuterClass.Customer customer = CustomerOuterClass.Customer.newBuilder()
            .setId(123L)
            .setName("customerName")
            .setEmail("cusomter@fake.com")
            .build();

    OrderOuterClass.Order order = OrderOuterClass.Order.newBuilder().setOrderId(12345).setOrderAmount(99).setCustomer(customer).addProducts(product).addProducts(product).build();

    @Test
    void serializerMustRegisterSchemasAtSubjects() throws RestClientException, IOException {

        serializer.configure(serdeMap, false);

        assertThat(srClient.getAllSubjects()).isEmpty();

        byte[] serializedOrder = serializer.serialize(dummyTopicName, order);

        log.info("subjects created: ");
        srClient.getAllSubjects().forEach(System.out::println);
        // dummy-value - as well as the referenced ones
        // customer.proto
        // product.proto
        assertThat(srClient.getAllSubjects()).hasSize(3);

        //deserializer.configure(serdeMap, false);

        Message deserialized = deserializer.deserialize(dummyTopicName, serializedOrder);

        System.out.println(deserialized);
    }

    @Test
    void serializerMustRegisterSchemasAtSubjects2()throws RestClientException, IOException {

        serializer.configure(serdeMap, false);

        assertThat(srClient.getAllSubjects()).isEmpty();

        var oneOf = ProductPersonOrderSumOuterClass.ProductPersonOrderSum.newBuilder().setOrder(order).build();

        byte[] serializedOneOf = serializer.serialize(dummyTopicName, oneOf);

        log.info("subjects created: ");
        srClient.getAllSubjects().forEach(log::info);
        // dummy-value - as well as the referenced ones
        // customer.proto
        // product.proto
        // order.proto
        assertThat(srClient.getAllSubjects()).hasSize(4);

        List<ParsedSchema> schemas = srClient.getSchemas("dummy", true, false);

        log.infof("found %d schemas", schemas.size());
        schemas.forEach(log::info);

        Map<String, Object> deserializerConfig = Map.of(KafkaProtobufSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, mockSRUrl, KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, ProductPersonOrderSumOuterClass.ProductPersonOrderSum.class);
        KafkaProtobufDeserializer<ProductPersonOrderSumOuterClass.ProductPersonOrderSum> deserializer = new KafkaProtobufDeserializer<>(srClient);
        deserializer.configure(deserializerConfig, false);

        ProductPersonOrderSumOuterClass.ProductPersonOrderSum deserialized = deserializer.deserialize(dummyTopicName, serializedOneOf);

        log.info("has customer: " + deserialized.hasCustomer());
        log.info("has order: " + deserialized.hasOrder());
        log.info("has product: " + deserialized.hasProduct());

        log.info("customer: " + deserialized.getCustomer());
        log.info("order: " + deserialized.getOrder());
        log.info("product: " + deserialized.getProduct());
    }

}
