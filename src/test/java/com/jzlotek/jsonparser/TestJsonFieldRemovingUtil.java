package com.jzlotek.jsonparser;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class TestJsonFieldRemovingUtil {

    private static ObjectMapper mapper = new ObjectMapper();
    private static String object;
    private static final Logger logger = LoggerFactory.getLogger(TestJsonFieldRemovingUtil.class);

    @Before
    public void setup() throws IOException {
        object = createTreeFromFile("object.json").toString();
        logger.info(object);
        assertNotNull(object);
    }

    @Test
    public void testGetFullObject() throws IOException {
        String result = JsonFieldRemovingUtil.removeFieldsString(object, "");
        logger.info(result);
        assertEquals(object, result);
    }

    @Test
    public void testGetFullObject_remove_except_field1() throws IOException {
        List<String> paths = new ArrayList<>();
        paths.add("field1");

        JsonNode result = JsonFieldRemovingUtil.keepFieldsJson(object, paths.toArray(new String[paths.size()]));
        logger.info(result.toString());

        assertEquals(1, result.get("field1").asInt());
        assertNull(result.get("field2"));
        assertNull(result.get("field3"));
    }

    @Test
    public void testArrayNode_remove_except_arrayfield2() throws IOException {
        List<String> paths = new ArrayList<>();
        paths.add("field3.array.arrayField2");

        JsonNode result = JsonFieldRemovingUtil.keepFieldsJson(object, paths.toArray(new String[paths.size()]));
        logger.info(result.toString());

        logger.info(result.get("field3").get("array").get(0).get("arrayField2").asText());

        assertEquals("AB", result.get("field3").get("array").get(0).get("arrayField2").asText());
        assertNull(result.get("field3").get("array").get(1).get("arrayField2"));
        assertNull(result.get("field3").get("array").get(2).get("arrayField2"));
        assertNull(result.get("field3").get("array").get(3));
        assertNull(result.get("field1"));
        assertNull(result.get("field2"));
    }

    @Test
    public void testNullInput() throws IOException {

        String result = JsonFieldRemovingUtil.keepFieldsString(object, "");
        logger.info(result);

        assertEquals("{}", result);
    }


    private JsonNode createTreeFromFile(String fileName) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return mapper.readTree(file);
    }
}
