package util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONString;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public final class ObjectStripper {

    private static String STANDARD_STRING = "";
    private static Boolean STANDARD_BOOLEAN = true;
    private static Integer STANDARD_NUMBER = 0;

    /**
     * Copy the response JSONObject and remove the values.
     * Also remove keys with values which where given in the request.
     * Finally also remove keys which are possibly random.
     *
     * @param response
     * @return JSONObject with standard values, but key structure intact.
     */
    public static JSONObject stripValues(JSONObject request, JSONObject response) {
        JSONObject structure = new JSONObject(response.toString());
        JSONObject copy = new JSONObject();

        Queue<Pair<JSONObject, JSONObject>> queue = new LinkedList<>();
        queue.add(new Pair<>(structure, copy));

        HashMap<String, Object> requestKeyValuePairs = getKeyValuePairs(request);

        // TODO something clever with arrays

        while (!queue.isEmpty()) {
            Pair<JSONObject, JSONObject> pair = queue.poll();
            JSONObject object = pair.getKey();
            JSONObject strippedObject = pair.getValue();

            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                String key = it.next();

//                if (requestKeyValuePairs.containsKey(key)) {
//                    // if the key was also in the request we leave it out of the feature vector
//                    continue;
//                }

                Object smallerObject = object.get(key);
                if (smallerObject instanceof JSONObject) {
                    JSONObject smallerStrippedObject = new JSONObject();
                    strippedObject.put(key, smallerStrippedObject);
                    queue.add(new Pair<>((JSONObject) object.get(key), smallerStrippedObject));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);

                    JSONArray smallerStrippedArray = new JSONArray();

                    strippedObject.put(key, smallerStrippedArray);

                    if (array.length() > 0) {
                        Object arrayObject = array.get(0);
                        if (arrayObject instanceof JSONObject) {
                            JSONObject evenSmallerObject = new JSONObject();
                            smallerStrippedArray.put(0, evenSmallerObject);

                            queue.add(new Pair<>((JSONObject) arrayObject, evenSmallerObject));
                        } else if (arrayObject instanceof String) {
                            smallerStrippedArray.put(0, STANDARD_STRING);
                        } else if (arrayObject instanceof Number) {
                            smallerStrippedArray.put(0, STANDARD_NUMBER);
                        } else if (arrayObject instanceof Boolean) {
                            smallerStrippedArray.put(0, STANDARD_BOOLEAN);
                        } else if (array.isNull(0)) {
                            smallerStrippedArray.put(0, "null");
                        } else if (arrayObject instanceof JSONArray) {
                            smallerStrippedArray.put(0, new JSONArray());
                        } else {
                            System.out.println("UNKNOWN ARRAY OBJECT TYPE");
                            System.out.println(arrayObject);
                            System.exit(0);
                        }
                        // TODO currently it is assuming no arrays in arrays
                    }
                } else if (!(requestKeyValuePairs.containsKey(key) && requestKeyValuePairs.get(key).equals(smallerObject))) {
                    if (smallerObject instanceof String) {
                        strippedObject.put(key, STANDARD_STRING);
                    } else if (smallerObject instanceof Number) {
                        strippedObject.put(key, STANDARD_NUMBER);
                    } else if (smallerObject instanceof Boolean) {
                        strippedObject.put(key, STANDARD_BOOLEAN);
                    } else if (object.isNull(key)) {
                        strippedObject.put(key, "null");
                    }
                }
            }
        }
        return copy;
    }

    /**
     * Check the key and value pairs in the request, match them with the key and value pairs in the response and if they are a match, do not include this pair in the feature vector.
     * @param request
     * @return the key-value pairs in the response JSON Object.
     */
    public static HashMap<String, Object> getKeyValuePairs(JSONObject request) {
        JSONObject structure = new JSONObject(request.toString());

        Queue<JSONObject> queue = new LinkedList<>();
        queue.add(structure);

        HashMap<String, Object> keyValuePairs = new HashMap<>();

        while (!queue.isEmpty()) {
            JSONObject object = queue.poll();
            Iterator<String> it = object.keys();
            while (it.hasNext()) {
                String key = it.next();

                Object smallerObject = object.get(key);
                keyValuePairs.put(key, smallerObject);

                if (smallerObject instanceof JSONObject) {
                    queue.add((JSONObject) object.get(key));
                } else if (smallerObject instanceof JSONArray) {
                    JSONArray array = ((JSONArray) smallerObject);
                    for (int i = 0; i < array.length(); i++) {
                        if (i > 0) {
                            array.remove(1);
                            continue;
                        }

                        Object arrayObject = array.get(i);
                        if (arrayObject instanceof JSONObject) {
                            queue.add((JSONObject) arrayObject);
                        } else if (arrayObject instanceof JSONString) {
                            array.put(i, arrayObject);
                        } else if (arrayObject instanceof Number) {
                            array.put(i, arrayObject);
                        } else if (arrayObject instanceof Boolean) {
                            array.put(i, arrayObject);
                        }
                        // TODO currently it is assuming no arrays in arrays
                    }
                }
            }
        }
        return keyValuePairs;
    }

}
