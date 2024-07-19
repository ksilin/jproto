package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import org.junit.jupiter.api.Test;
import v3.PersonOuterClass;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class BuiltInParserAndDescriptorPojoSerdeTest {

    @Test
    void testBuiltInParserSerde() throws InvalidProtocolBufferException {

        PersonOuterClass.Person pojo = PersonOuterClass.Person.newBuilder()
                .setId(1)
                .setName("Me")
                .setEmail("my@email.com")
                .build();

        byte[] serialized = pojo.toByteArray();

        PersonOuterClass.Person deserialized = PersonOuterClass.Person.parseFrom(serialized);

        assert pojo.equals(deserialized);
    }


    @Test
    void testPojoSerdeWithFileDescriptor() throws Descriptors.DescriptorValidationException, IOException {
        Descriptors.FileDescriptor descriptor = loadDescriptor("person.desc");

        PersonOuterClass.Person protoGeneratedPerson = PersonOuterClass.Person.newBuilder()
                .setId(1)
                .setName("Me")
                .setEmail("my@email.com")
                .build();

        byte[] serializedProto = protoGeneratedPerson.toByteArray();

        PersonPojo deserializedPojo = deserializeWithDescriptor(serializedProto, descriptor);
        System.out.println(deserializedPojo);
    }


    public static Descriptors.FileDescriptor loadDescriptor(String descriptorFilePath) throws IOException, Descriptors.DescriptorValidationException, FileNotFoundException {
        //FileInputStream fis = new FileInputStream(descriptorFilePath);
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("person.desc");
        DescriptorProtos.FileDescriptorSet descriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(is);
        return Descriptors.FileDescriptor.buildFrom(descriptorSet.getFile(0), new Descriptors.FileDescriptor[]{});
    }


    // taking a JSON detour to get to POJO
    public static PersonPojo deserializeWithDescriptor(byte[] data, Descriptors.FileDescriptor fileDescriptor) throws IOException, Descriptors.DescriptorValidationException, FileNotFoundException {

        Descriptors.Descriptor descriptor = fileDescriptor.findMessageTypeByName("Person");

        DynamicMessage dynamicMessage = DynamicMessage.parseFrom(descriptor, data);

        String json = JsonFormat.printer().print(dynamicMessage);

        PersonPojo personPojo = new PersonPojo();
        Map<String, Object> map = new ObjectMapper().readValue(json, Map.class);

        personPojo.setName((String) map.get("name"));
        personPojo.setId((Integer) map.get("id"));

        return personPojo;
    }



}
