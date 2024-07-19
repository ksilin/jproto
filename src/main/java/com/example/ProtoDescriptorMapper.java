package com.example;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import v3.PersonOuterClass;

import java.util.HashMap;
import java.util.Map;

public class ProtoDescriptorMapper {
    public static Map<Descriptors.Descriptor, Class<? extends Message>> getDescriptorToClassMap() {
        Map<Descriptors.Descriptor, Class<? extends Message>> map = new HashMap<>();
        map.put(PersonOuterClass.Person.getDescriptor(), PersonOuterClass.Person.class);
        // Add other message types here
        return map;
    }

}
