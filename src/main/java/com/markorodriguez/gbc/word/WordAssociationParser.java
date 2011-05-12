package com.markorodriguez.gbc.word;

import com.markorodriguez.gbc.Configuration;
import com.markorodriguez.gbc.AbstractParser;
import com.tinkerpop.blueprints.pgm.*;
import com.tinkerpop.blueprints.pgm.util.IndexableGraphHelper;
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper;
import com.tinkerpop.blueprints.pgm.util.graphml.GraphMLWriter;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class WordAssociationParser extends AbstractParser {

    public WordAssociationParser() throws Exception {
        super("WordAssociation");
        IndexableGraph graph = (IndexableGraph) this.getGraph();
        List<String> fileNames = Arrays.asList("Cue_Target_Pairs-A-B.txt", "Cue_Target_Pairs-C.txt", "Cue_Target_Pairs-D-F.txt", "Cue_Target_Pairs-G-K.txt", "Cue_Target_Pairs-L-O.txt", "Cue_Target_Pairs-P-R.txt", "Cue_Target_Pairs-S.txt", "Cue_Target_Pairs-T-Z.txt");
        TransactionalGraphHelper.CommitManager manager = TransactionalGraphHelper.createCommitManager((TransactionalGraph) graph, 1500);

        for (String fileName : fileNames) {
            System.out.println("\nProcessing " + fileName);
            BufferedReader br = new BufferedReader(new FileReader(Configuration.getGraphDataDirectory("WordAssociation") + "/raw/" + fileName));
            String line;
            while (null != (line = br.readLine())) {
                String[] entries = line.split(",");
                Vertex a = IndexableGraphHelper.addUniqueVertex(graph, null, graph.getIndex(Index.VERTICES, Vertex.class), "word", entries[0].trim());
                Vertex b = IndexableGraphHelper.addUniqueVertex(graph, null, graph.getIndex(Index.VERTICES, Vertex.class), "word", entries[1].trim());
                if (!a.equals(b)) {
                    Edge e = graph.addEdge(null, a, b, "similar_to");
                    e.setProperty("weight", new Integer(entries[4].trim()));
                    manager.incrCounter();
                    if (manager.atCommit()) {
                        System.out.print(".");
                    }
                }
            }
        }

        manager.close();

        System.out.println("\nExporting to GraphML representation");
        GraphMLWriter.outputGraph(graph, new FileOutputStream(Configuration.getGraphDataDirectory("WordAssociation") + "/word-association.xml"));
        graph.shutdown();
    }

    public static void main(String[] args) throws Exception {
        new WordAssociationParser();
    }
}
