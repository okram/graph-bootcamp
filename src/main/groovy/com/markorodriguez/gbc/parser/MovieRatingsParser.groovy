package com.markorodriguez.gbc.parser

import com.tinkerpop.blueprints.pgm.IndexableGraph
import com.tinkerpop.blueprints.pgm.util.EdgeHelper
import com.tinkerpop.blueprints.pgm.util.ElementHelper
import com.tinkerpop.blueprints.pgm.util.graphml.GraphMLReader
import com.tinkerpop.blueprints.pgm.util.graphml.GraphMLWriter
import com.tinkerpop.gremlin.Gremlin

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class MovieRatingsParser extends AbstractParser {

    static {
        Gremlin.load();
    }

    public MovieRatingsParser() {

        super("MovieRatings");
        IndexableGraph g = (IndexableGraph) super.getGraph();
        println "Loading original movie data..."
        GraphMLReader.inputGraph(g, new FileInputStream("data/movie-ratings/raw/movie-graph-original.xml"));

        println "Cleaning original movie data..."
        println "\tUpdating vertex properties..."
        ElementHelper.removeProperty("eigentest", g.V);
        ElementHelper.renameProperty("_stp_label", "name", g.V);
        ElementHelper.renameProperty("Name", "name", g.V);
        ElementHelper.renameProperty("_stp_type", "type", g.V);
        ElementHelper.removeProperty("_stp_id", g.V);
        ElementHelper.removeProperty("NotMPAA", g.V);
        ElementHelper.renameProperty("Title", "name", g.V);
        ElementHelper.renameProperty("Release Year", "year", g.V);
        ElementHelper.typecastProperty("year", Integer.class, g.V);
        ElementHelper.renameProperty("MPAA", "mpaa", g.V);

        println "\tUpdating edge properties..."
        ElementHelper.removeProperty("NotPaul", g.E);
        ElementHelper.renameProperty("Rating", "rating", g.E);
        ElementHelper.typecastProperty("rating", Integer.class, g.E);
        ElementHelper.removeProperty("_stp_id", g.E);
        ElementHelper.removeProperty("_stp_label", g.E);
        ElementHelper.removeProperty("Phantoms", g.E);
        ElementHelper.removeProperty("Paul", g.E);
        ElementHelper.removeProperty("Mary", g.E);

        println "\tRelabling edges..."
        List temp = [];
        g.E[[label: 'Rated']] >> temp;
        EdgeHelper.relabelEdges(g, temp, "rated");
        temp = []
        g.E[[label: 'Directed By']] >> temp;
        EdgeHelper.relabelEdges(g, temp, "director");
        g.E[[label: 'ActedIn']] >> temp;
        EdgeHelper.relabelEdges(g, temp, "acted");

        GraphMLWriter.outputGraph(g, new FileOutputStream("data/movie-ratings/movie-graph.xml"));

    }

    public static void main(String[] args) {
        new MovieRatingsParser();

    }
}
