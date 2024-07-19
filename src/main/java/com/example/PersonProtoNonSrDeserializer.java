package com.example;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.common.serialization.Deserializer;
import org.jboss.logging.Logger;
import v3.PersonOuterClass;

public class PersonProtoNonSrDeserializer implements Deserializer<PersonOuterClass.Person> {

    Logger log = Logger.getLogger(PersonProtoNonSrDeserializer.class);

    @Override
    public PersonOuterClass.Person deserialize(String topic, byte[] data) {
        try {
            return PersonOuterClass.Person.parseFrom(data);
        } catch (InvalidProtocolBufferException e) {
            log.error(e);
            return null;
        }
    }
}
