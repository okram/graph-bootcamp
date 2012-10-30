package com.thinkaurelius.bootcamp.movie

import com.thinkaurelius.titan.core.TitanFactory
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.TransactionalGraph
import com.tinkerpop.blueprints.Vertex
import com.tinkerpop.gremlin.groovy.Gremlin

/**
 * @author Marko A. Rodriguez (http://thinkaurelius.com)
 */
class MovieLensParser {

    static String dir = '/home/ubuntu/ml-1m/';
    static Map occupations;

    static {
        Gremlin.load()
        occupations = [0: "other", 1: "academic/educator", 2: "artist",
                3: "clerical/admin", 4: "college/grad student", 5: "customer service",
                6: "doctor/health care", 7: "executive/managerial", 8: "farmer",
                9: "homemaker", 10: "K-12 student", 11: "lawyer", 12: "programmer",
                13: "retired", 14: "sales/marketing", 15: "scientist", 16: "self-employed",
                17: "technician/engineer", 18: "tradesman/craftsman", 19: "unemployed", 20: "writer"]

    }

    public MovieLensParser(TransactionalGraph g) throws Exception {

        long counter = 0l;

        println 'Processing movies.dat...'
        // MovieID::Title::Genres
        new File(dir + 'movies.dat').eachLine { final String line ->
            String[] components = line.split("::");
            int movieId = new Integer(components[0]);
            String movieTitle = new String(components[1]);
            String genres = new String(components[2]);
            Vertex movieVertex = g.addVertex(['movieId': movieId, 'name': movieTitle, 'type': 'movie']);
            //println(movieId + " " + movieTitle + " " + generas);
            for (String genre: Arrays.asList(genres.split('\\|'))) {
                Iterator<Vertex> hits = g.V('name', genre).iterator();
                Vertex genreVertex = hits.hasNext() ? hits.next() : g.addVertex(['type': 'genre', 'name': genre]);
                g.addEdge(movieVertex, genreVertex, "genre");
            }
        }
        g.stopTransaction(TransactionalGraph.Conclusion.SUCCESS)

        println 'Processing users.dat...'
        // UserID::Gender::Age::Occupation::Zip-code
        new File(dir + 'users.dat').eachLine {final String line ->
            String[] components = line.split("::");
            int userId = new Integer(components[0]);
            String userGender = new String(components[1]);
            int userAge = new Integer(components[2]);
            String userOccupation = occupations.get(new Integer(components[3]));
            //println(userId + " " + userGender + " " + userAge + " " + userOccupation);
            Vertex userVertex = g.addVertex(['type': 'person', 'userId': userId, 'gender': userGender, 'age': userAge]);

            Iterator<Vertex> hits = g.V('name', userOccupation).iterator();
            Vertex occupationVertex;
            if (hits.hasNext()) {
                occupationVertex = hits.next();
            } else {
                occupationVertex = g.addVertex([type: 'occupation', name: userOccupation]);
            }
            g.addEdge(userVertex, occupationVertex, "occupation");
        }
        g.stopTransaction(TransactionalGraph.Conclusion.SUCCESS)

        println 'Processing ratings.dat...'
        // UserID::MovieID::Rating::Timestamp
        new File(dir + 'ratings.dat').eachLine {final String line ->
            String[] components = line.split("::");
            int userId = new Integer(components[0]);
            int movieId = new Integer(components[1]);
            int stars = new Integer(components[2]);

            // println(userId + " " + movieId + " " + stars);
            g.addEdge(g.V('userId', userId).next(), g.V('movieId', movieId).next(), "rated", [stars: stars]);
            if (counter++ % 50000 == 0) {
                System.out.println('Ratings edges processed: ' + counter);
                g.stopTransaction(TransactionalGraph.Conclusion.SUCCESS)
            }
        }
    }

    public static void main(String[] args) {
        Graph g = TitanFactory.open(dir + 'movielens');
        g.createKeyIndex('name', Vertex.class);
        g.createKeyIndex('userId', Vertex.class);
        g.createKeyIndex('movieId', Vertex.class);
        g.stopTransaction(TransactionalGraph.Conclusion.SUCCESS);
        new MovieLensParser(g);
        g.shutdown();
    }
}
