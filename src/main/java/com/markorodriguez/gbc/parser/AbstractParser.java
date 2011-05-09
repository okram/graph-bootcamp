package com.markorodriguez.gbc.parser;

import com.markorodriguez.gbc.Configuration;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.impls.orientdb.OrientGraph;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraph;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class AbstractParser {

    protected final String project;

    public AbstractParser(final String project) {
        this.project = project;
    }

    public Graph getGraph() {
        Class clazz = Configuration.getGraphClass(project);
        if (clazz.equals(TinkerGraph.class)) {
            return new TinkerGraph();
        } else if (clazz.equals(OrientGraph.class)) {
            return new OrientGraph(Configuration.getGraphDirectory(project));
        } else if (clazz.equals(Neo4jGraph.class)) {
            return new Neo4jGraph(Configuration.getGraphDirectory(project));
        } else {
            throw new RuntimeException("No graph for project " + project);
        }

    }
}
