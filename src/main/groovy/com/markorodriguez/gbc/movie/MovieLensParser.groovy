package com.markorodriguez.gbc.movie

import com.tinkerpop.blueprints.pgm.Edge
import com.tinkerpop.blueprints.pgm.TransactionalGraph
import com.tinkerpop.blueprints.pgm.Vertex
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper
import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper.CommitManager
import com.tinkerpop.gremlin.Gremlin
import com.tinkerpop.gremlin.GremlinTokens.T

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class MovieLensParser {

    static Map occupations;

    static {
        Gremlin.load();
        occupations = [0: "other", 1: "academic/educator", 2: "artist",
                3: "clerical/admin", 4: "college/grad student", 5: "customer service",
                6: "doctor/health care", 7: "executive/managerial", 8: "farmer",
                9: "homemaker", 10: "K-12 student", 11: "lawyer", 12: "programmer",
                13: "retired", 14: "sales/marketing", 15: "scientist", 16: "self-employed",
                17: "technician/engineer", 18: "tradesman/craftsman", 19: "unemployed", 20: "writer"]

    }

    public MovieLensParser(TransactionalGraph g) throws Exception {

        CommitManager manager = TransactionalGraphHelper.createCommitManager(g, 5000);

        // MovieID::Title::Genres
        new File('/Users/marko/Desktop/million-ml-data/movies.dat').eachLine {final String line ->
            String[] components = line.split("::");
            int movieId = new Integer(components[0]);
            String movieTitle = new String(components[1]);
            String generas = new String(components[2]);
            Vertex movieVertex = g.addVertex(['movieId': movieId, 'name': movieTitle, 'type': 'movie']);
            //println(movieId + " " + movieTitle + " " + generas);
            for (String genera: Arrays.asList(generas.split('\\|'))) {
                Iterator<Vertex> hits = g.idx(T.v)[[name: genera]].iterator();
                Vertex generaVertex;
                if (hits.hasNext()) {
                    generaVertex = hits.next();
                } else {
                    generaVertex = g.addVertex(['type': 'genera', 'name': genera]);
                }
                g.addEdge(movieVertex, generaVertex, "hasGenera");
                manager.incrCounter();
                if (manager.atCommit()) {
                    print '.';
                }
            }
            manager.incrCounter();
            if (manager.atCommit()) {
                print '.';
            }
        }

        // UserID::Gender::Age::Occupation::Zip-code
        new File('/Users/marko/Desktop/million-ml-data/users.dat').eachLine {final String line ->
            String[] components = line.split("::");
            int userId = new Integer(components[0]);
            String userGender = new String(components[1]);
            int userAge = new Integer(components[2]);
            String userOccupation = occupations.get(new Integer(components[3]));
            //println(userId + " " + userGender + " " + userAge + " " + userOccupation);
            Vertex userVertex = g.addVertex(['type': 'person', 'userId': userId, 'gender': userGender, 'age': userAge]);

            Iterator<Vertex> hits = g.idx(T.v)[[name: userOccupation]].iterator();
            Vertex occupationVertex;
            if (hits.hasNext()) {
                occupationVertex = hits.next();
            } else {
                occupationVertex = g.addVertex(['type': 'occupation', 'name': userOccupation]);
            }
            g.addEdge(userVertex, occupationVertex, "hasOccupation");
            manager.incrCounter();
            if (manager.atCommit()) {
                print '.';
            }
        }

        // UserID::MovieID::Rating::Timestamp
        new File('/Users/marko/Desktop/million-ml-data/ratings.dat').eachLine {final String line ->
            String[] components = line.split("::");
            int userId = new Integer(components[0]);
            int movieId = new Integer(components[1]);
            int stars = new Integer(components[2]);

            // println(userId + " " + movieId + " " + stars);
            Edge edge = g.addEdge(g.idx(T.v)[[userId: userId]] >> 1, g.idx(T.v)[[movieId: movieId]] >> 1, "rated");
            edge.setProperty('stars', stars);
            manager.incrCounter();
            if (manager.atCommit()) {
                print '.';
            }
        }

        manager.close();

    }

    public static void main(String[] args) {
        TransactionalGraph g = new Neo4jGraph('/data/movieLens');
        new MovieLensParser(g);
        g.shutdown();
    }
}
