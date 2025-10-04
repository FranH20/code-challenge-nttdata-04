package com.nttdata.fhuichic.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.quarkus.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {

    private static final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    public static <T> T generateObject(String path, Class<T> clazz) {
        try {
            ClassLoader classLoader = TestUtils.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(path);
            return mapper.readValue(inputStream, clazz);
        } catch (IOException ex) {
            Log.error(ex);
            return null;
        }
    }

    public static <T> List<T> generateObjectList(String path, Class<T> clazz) {
        try {
            ClassLoader classLoader = TestUtils.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(path);
            CollectionType listType = mapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
            return mapper.readValue(inputStream, listType);
        } catch (IOException ex) {
            Log.error(ex);
            return null;
        }
    }

}
