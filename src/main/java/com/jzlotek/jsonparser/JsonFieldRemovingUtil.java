package com.jzlotek.jsonparser;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.util.*;

/**
 * @author jzlotek
 * @version 1.0.0
 * @apiNote Used to take an object or JSON and remove all except fields given by a path
 */
public class JsonFieldRemovingUtil {

    private static ObjectMapper mapper = new ObjectMapper();
    private static JsonNodeFactory factory = JsonNodeFactory.instance;

    private JsonFieldRemovingUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param object General object to be worked on
     * @param path   Single String of the path
     * @return String containing a JSON
     * @throws IOException When writing to a string fails
     */
    public static String keepFieldsString(Object object, String... path) throws IOException {
        return keepFieldsJson(object, path).toString();
    }

    /**
     * @param object General object to be worked on
     * @param path   List of Strings of the paths or a single string
     * @return JsonNode containing a JSON
     * @throws IOException When writing to a string fails
     */
    public static JsonNode keepFieldsJson(Object object, String... path) throws IOException {
        return keepInJson(object, Arrays.asList(path));
    }

    /**
     * @param object General object to be worked on
     * @param path   Single String of the path
     * @return String containing a JSON
     * @throws IOException When writing to a string fails
     */
    public static String removeFieldsString(Object object, String... path) throws IOException {
        return removeFieldsJson(object, path).toString();
    }

    /**
     * @param object General object to be worked on
     * @param path   List of Strings of the paths or a single string
     * @return JsonNode containing a JSON
     * @throws IOException When writing to a string fails
     */
    public static JsonNode removeFieldsJson(Object object, String... path) throws IOException {
        return removeFromJson(object, Arrays.asList(path));
    }

    /**
     * @param object General object to be worked on
     * @param paths  List of Strings of the paths
     * @return JsonNode containing a JSON
     * @throws IOException When writing to a string fails
     */
    private static JsonNode keepInJson(Object object, List<String> paths) throws IOException {
        if (object == null || (paths.size() == 1 && StringUtils.isEmpty(paths.get(0)))) {
            return factory.objectNode();
        }

        JsonNode node = objectToTree(object);

        //if the path array is empty, or is size 1 and the only entry is empty, or the
        // object is empty return the node as is
        if (CollectionUtils.isEmpty(paths)) {
            return node;
        }

//        HashMap<String, HashMap> map = createPathMap(paths);

        return keepJsonFields(node, paths.toArray(new String[paths.size()]));
    }

    private static JsonNode removeFromJson(Object object, List<String> paths) throws IOException {
        if (object == null) {
            return factory.objectNode();
        }

        JsonNode node = objectToTree(object);

        //if the path array is empty, or is size 1 and the only entry is empty, or the
        // object is empty return the node as is
        if (CollectionUtils.isEmpty(paths) || (paths.size() == 1 && StringUtils.isEmpty(paths.get(0)))) {
            return node;
        }
        HashMap<String, HashMap> map = createPathMap(paths);
        removeJsonFields(node, map);

        return node;
    }

    private static void removeJsonFields(JsonNode parent, HashMap<String, HashMap> map) {
        List<String> removeFields = new ArrayList<>();

        if (parent.isArray()) {
            ArrayNode arrayNode = (ArrayNode) parent;

            for (JsonNode element : arrayNode) {
                removeJsonFields(element, map);
            }

        } else {
            for (Map.Entry<String, HashMap> entry : map.entrySet()) {
                //Iterate and call recursively
                if (entry.getValue() != null && parent.get(entry.getKey()) != null) {
                    removeJsonFields(parent.get(entry.getKey()), entry.getValue());
                } else {
                    removeFields.add(entry.getKey());
                }
            }
            ((ObjectNode) parent).remove(removeFields);
        }
    }


    /**
     * @param parent JsonNode of the parent node to be worked on. Goes through children of node, if any
     * @param map    HashMap of the paths
     */
    private static void keepJsonFieldsOld(JsonNode parent, HashMap<String, HashMap> map) {
        List<String> removeFields = new ArrayList<>();
        Iterator<String> fieldNames = parent.getFieldNames();


        while (fieldNames.hasNext()) {
            removeFields.add(fieldNames.next());
        }

        if (parent.isArray()) {
            ArrayNode arrayNode = (ArrayNode) parent;

            for (JsonNode anArrayNode : arrayNode) {
                keepJsonFieldsOld(anArrayNode, map);
            }

        } else {

            for (Map.Entry<String, HashMap> entry : map.entrySet()) {
                //Remove fields that are ok to keep
                removeFields.remove(entry.getKey());

                //Iterate and call recursively
                if (entry.getValue() != null && parent.get(entry.getKey()) != null) {
                    keepJsonFieldsOld(parent.get(entry.getKey()), entry.getValue());
                }
            }

            ((ObjectNode) parent).remove(removeFields);
        }
    }

    /**
     * @param parent JsonNode of the parent node to be worked on. Goes through children of node, if any
     */
    private static JsonNode keepJsonFields(JsonNode parent, String... paths) {
        JsonNode created;
        if (parent.isArray())
            created = factory.arrayNode();
        else
            created = factory.objectNode();

        for (String path : paths) {
            iteratePath(parent, created, path);
        }

        return created;
    }


    private static void iteratePath(JsonNode parent, JsonNode update, String path) {
        List<String> split = Arrays.asList(path.split("\\."));
        JsonNode original = parent;
        String usedPaths = "";
        for (String field : split) {

            if (split.indexOf(field) == 0) {
                usedPaths = field + ".";
            } else {
                usedPaths += (field + ".");
            }
            original = original.get(field);
            if (original == null) break;

            processNode(original, update, field, path, usedPaths, split);
        }
    }

    private static void processNode(JsonNode original, JsonNode update, String field, String path, String usedPaths, List<String> split) {
        if (original.isArray()) {
            processNode((ArrayNode) original, update, field, path.replace(usedPaths, ""), split);
        } else {
            processNode(original, update, field, split);
        }
    }

    private static void processNode(JsonNode original, JsonNode update, String field, List<String> split) {
        if (split.indexOf(field) == split.size() - 1) {
            ObjectNode lastNode = (ObjectNode) update;
            for (int i = 0; i < split.size() - 1; i++) {
                lastNode = lastNode.with(split.get(i));
            }
            lastNode.putAll(createNode(field, original));
        }
    }

    private static void processNode(ArrayNode original, JsonNode update, String field, String remainingPath, List<String> split) {
        ((ObjectNode) update).put(field, factory.arrayNode());
        ArrayNode updateNode = (ArrayNode) update.get(field);
        Iterator<JsonNode> nodeIteratorOriginal = original.getElements();
        while (nodeIteratorOriginal.hasNext()) {
            if (split.indexOf(field) != split.size() - 1) {
                ObjectNode newNode = factory.objectNode();
                iteratePath(nodeIteratorOriginal.next(), newNode, remainingPath);
                updateNode.add(newNode);
            } else {
                updateNode.add(nodeIteratorOriginal.next());
            }
        }
    }

    private static ObjectNode createNode(String field, JsonNode node) {
        ObjectNode ret = factory.objectNode();
        if (node.isTextual())
            ret.put(field, node.getTextValue());
        else if (node.isObject())
            ret.put(field, node);
        else if (node.isBoolean())
            ret.put(field, node.getBooleanValue());
        else if (node.isDouble())
            ret.put(field, node.getDecimalValue());
        else if (node.isInt())
            ret.put(field, node.getIntValue());
        return ret;
    }


    private static JsonNode objectToTree(Object object) throws IOException {
        if (object instanceof JsonNode)
            return (JsonNode) object;

        String objectString;

        if (object.getClass() != String.class)
            objectString = mapper.writeValueAsString(object);
        else
            objectString = (String) object;

        return mapper.readTree(objectString);
    }

    /**
     * @param list List of Strings in the format path.to.field of fields to be included
     * @return HashMap of the paths that need to be included in the json
     */
    private static HashMap<String, HashMap> createPathMap(List<String> list) {
        HashMap<String, HashMap> pathMap = new HashMap<>();

        List<List<String>> splitPathList = new ArrayList<>();
        for (String path : list) {
            splitPathList.add(Arrays.asList(path.split("\\.")));
        }

        for (List<String> innerList : splitPathList) {
            HashMap<String, HashMap> current = pathMap;
            for (String path : innerList) {
                if (current.get(path) == null && innerList.indexOf(path) != innerList.size() - 1) {
                    HashMap<String, HashMap> temp = new HashMap<>();
                    current.put(path, temp);
                    current = temp;
                } else if (current.get(path) == null && innerList.indexOf(path) == innerList.size() - 1) {
                    current.put(path, null);
                } else if (current.get(path).getClass() == HashMap.class) {
                    current = (HashMap<String, HashMap>) current.get(path);
                }
            }
        }

        return pathMap;
    }
}
