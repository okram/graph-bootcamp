package com.markorodriguez.gbc.movie

import com.tinkerpop.blueprints.pgm.Element
import com.tinkerpop.blueprints.pgm.Graph
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.rexster.ElementJSONObject
import com.tinkerpop.rexster.RexsterResourceContext
import java.util.Map.Entry
import org.codehaus.jettison.json.JSONArray
import org.codehaus.jettison.json.JSONObject
import com.tinkerpop.rexster.extension.*

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
@ExtensionNaming(namespace = "gbc", name = "collabFilter")
public class CollaborativeFilteringExtension extends AbstractRexsterExtension {

    @ExtensionDefinition(extensionPoint = ExtensionPoint.VERTEX)
    @ExtensionDescriptor(description = "Evaluate a collaborative filtering algorithm on a movie")
    public ExtensionResponse evaluateOnVertex(@RexsterContext RexsterResourceContext context,
                                              @RexsterContext Graph g,
                                              @RexsterContext Vertex v,
                                              @ExtensionRequestParameter(name = "minRating", description = "Minimum rating allowed for traversal") Integer rating) {

        if (!v['type'].equals("Title")) {
            return ExtensionResponse.error("The vertex type must be a Title not a " + v['type']);
        }

        try {
            def m = [:];
            v.inE('rated') {it['star'] >= rating}.outV.outE('rated') {it['star'] >= rating}.inV.except([v]).groupCount(m) >> -1;
            m = m.sort {a, b -> b.value <=> a.value}
            JSONArray results = new JSONArray();
            for (Entry entry: m.entrySet()) {
                ElementJSONObject object = new ElementJSONObject((Element) entry.getKey());
                object.put("score", entry.getValue());
                results.put(object);
            }
            return ExtensionResponse.ok(new JSONObject().put("results", results));
        } catch (Exception e) {
            return ExtensionResponse.error(e.getMessage());
        }

    }

    @ExtensionDefinition(extensionPoint = ExtensionPoint.VERTEX)
    @ExtensionDescriptor(description = "Evaluate a collaborative filtering algorithm on a movie")
    public ExtensionResponse evaluateOnVertexNoMinRating(@RexsterContext RexsterResourceContext context,
                                                         @RexsterContext Graph g,
                                                         @RexsterContext Vertex v) {

        return this.evaluateOnVertex(context, g, v, -1);

    }
}
