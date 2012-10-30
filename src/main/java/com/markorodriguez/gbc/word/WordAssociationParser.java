package com.markorodriguez.gbc.word;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.GraphHelper;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WordAssociationParser {

    static String dir = "/Users/marko/Desktop/wordassociation/raw/";

    public WordAssociationParser(Graph graph) throws Exception {
        List<String> fileNames = Arrays.asList("Cue_Target_Pairs-A-B.txt", "Cue_Target_Pairs-C.txt", "Cue_Target_Pairs-D-F.txt", "Cue_Target_Pairs-G-K.txt", "Cue_Target_Pairs-L-O.txt", "Cue_Target_Pairs-P-R.txt", "Cue_Target_Pairs-S.txt", "Cue_Target_Pairs-T-Z.txt");
        for (String fileName : fileNames) {
            System.out.println("\nProcessing " + fileName);
            BufferedReader br = new BufferedReader(new FileReader(dir + fileName));
            String line;
            while (null != (line = br.readLine())) {
                String[] entries = line.split(",");
                Iterator<Vertex> hits = graph.getVertices("word", entries[0].trim()).iterator();
                Vertex a = hits.hasNext() ? hits.next() : GraphHelper.addVertex(graph, null, "word", entries[0].trim());
                hits = graph.getVertices("word", entries[1].trim()).iterator();
                Vertex b = hits.hasNext() ? hits.next() : GraphHelper.addVertex(graph, null, "word", entries[1].trim());
                if (!a.equals(b))
                    graph.addEdge(null, a, b, "similarTo").setProperty("weight", new Integer(entries[4].trim()));
            }
        }

        System.out.println("\nExporting to GraphML representation");
        GraphMLWriter.outputGraph(graph, new FileOutputStream(dir + "word-association.xml"));
        graph.shutdown();
    }

    public static void main(String[] args) throws Exception {
        new WordAssociationParser(new TinkerGraph());
    }
}
