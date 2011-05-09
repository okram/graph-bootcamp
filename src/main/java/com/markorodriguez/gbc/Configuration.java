package com.markorodriguez.gbc;

import com.tinkerpop.blueprints.pgm.Graph;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class Configuration {

    public static JSONObject object;

    static {
        try {
            JSONParser parser = new JSONParser();
            object = (JSONObject) parser.parse(new FileReader("properties.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Class<? extends Graph> getGraphClass(final String project) {
        try {
            return (Class<? extends Graph>) Class.forName((String) ((JSONObject) object.get(project)).get("graph"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getGraphDirectory(final String project) {
        try {
            return (String) ((JSONObject) object.get(project)).get("directory");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String toString() {
        return object.toJSONString();
    }

    public static void main(String[] args) {
        System.out.println(new Configuration().toString());
        System.out.println(new Configuration().getGraphClass("WordAssociation"));
    }
}