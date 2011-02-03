package com.markorodriguez.gbc.parser;

import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.blueprints.pgm.util.IndexableGraphHelper;
import com.tinkerpop.blueprints.pgm.util.graphml.GraphMLWriter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WordAssociationParser {

    public WordAssociationParser(IndexableGraph graph) throws Exception {
        List<String> fileNames = Arrays.asList("Cue_Target_Pairs-A-B.txt", "Cue_Target_Pairs-C.txt", "Cue_Target_Pairs-D-F.txt", "Cue_Target_Pairs-G-K.txt", "Cue_Target_Pairs-L-O.txt", "Cue_Target_Pairs-P-R.txt", "Cue_Target_Pairs-S.txt", "Cue_Target_Pairs-T-Z.txt");
        int counter = 0;
        if (graph instanceof TransactionalGraph) {
            ((TransactionalGraph) graph).setTransactionMode(TransactionalGraph.Mode.MANUAL);
            ((TransactionalGraph) graph).startTransaction();
        }

        for (String fileName : fileNames) {
            System.out.println("\nProcessing " + fileName);
            BufferedReader br = new BufferedReader(new FileReader("data/word-association/raw/" + fileName));
            String line;
            while (null != (line = br.readLine())) {
                String[] entries = line.split(",");
                Vertex a = IndexableGraphHelper.addUniqueVertex(graph, null, graph.getIndex(Index.VERTICES, Vertex.class), "word", entries[0].trim());
                Vertex b = IndexableGraphHelper.addUniqueVertex(graph, null, graph.getIndex(Index.VERTICES, Vertex.class), "word", entries[1].trim());
                if (!a.equals(b)) {
                    Edge e = graph.addEdge(null, a, b, "similar_to");
                    e.setProperty("weight", new Integer(entries[4].trim()));
                    counter++;
                    if (counter % 1000 == 0) {
                        System.out.print(".");
                        if (graph instanceof TransactionalGraph) {
                            ((TransactionalGraph) graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
                            ((TransactionalGraph) graph).startTransaction();
                        }
                    }
                }
            }
        }

        if (graph instanceof TransactionalGraph) {
            ((TransactionalGraph) graph).stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        }

        System.out.println("\nExporting to GraphML representation");
        GraphMLWriter.outputGraph(graph, new FileOutputStream("data/word-association/word-graph.xml"));
        graph.shutdown();
    }

    public static void main(String[] args) throws Exception {
        new WordAssociationParser(new Neo4jGraph("/tmp/wordgraph"));
    }
}
