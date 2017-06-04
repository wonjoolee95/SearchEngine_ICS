import java.io.*;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

// Main class of the search engine, builds the inverted index
public class IndexBuilder {
    private Map<String,PostingList> index;
    private Map<String,Double> distance;
    private URLMapper urlMapper;

    // 1. Build the index
    // 2. Process query

    private IndexBuilder() throws Exception {
        this.index = new HashMap<>();
        this.distance = new HashMap<>();
        this.urlMapper = new URLMapper();
    }

    private  void buildIndex() throws Exception {
        readAllFiles();
        scoreIndex();
        calculateDistance();
        DatabaseSetup databaseSetup = new DatabaseSetup();
        databaseSetup.setUpDatabase(this.index, this.urlMapper, this.distance);
    }

    private void readAllFiles() {
        try {
            // Iterate through each file in [0, 74] folders
            for (int i = 65; i < 75; i++) {
                String basePath = "data/" + i + "/";
                File folder = new File(basePath);
                File[] files = folder.listFiles();
                // For each file, call tokenize
                for (File file : files) {
                    File input = new File(basePath + file.getName());
                    Document HtmlDoc = Jsoup.parse(input, "UTF-8", "");
                    if (file.isFile()) {
                        tokenizeTitle(i + "/" + file.getName());
                        tokenize(HtmlDoc.body().toString(), i + "/" + file.getName(), 1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tokenizeTitle(String docID) throws Exception {
        StringBuilder html = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader("data/" + docID))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    html.append(line);
                }
                if (html.toString().contains("<body>")) {
                    html.append("</body>");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (html.toString().contains("<body>")) {
            Document document = Jsoup.parse(html.toString(), "", Parser.xmlParser());
            tokenize(document.ownText(), docID, 2);
        }
    }

    private void tokenize(String html, String docID, int score) throws Exception {
        StringTokenizer st = new StringTokenizer(removeNonalpha(html)," ");
        while (st.hasMoreTokens()) {
            String token = Stemmizer.englishStemmize(st.nextToken());
            if (!this.index.containsKey(token)) {
                this.index.put(token, new PostingList());
            }
            this.index.get(token).add(docID, score);
        }
    }

    private String removeNonalpha(String text) {
        return text.replaceAll("[^A-Za-z0-9']", " ");
    }

    private void scoreIndex() {
        int CORPUS_SIZE = 37500;
        for (Map.Entry<String,PostingList> entry : this.index.entrySet()) {
            int df = entry.getValue().getDf();
            for (PostingDocument document : entry.getValue().getDocuments()) {
                double score = Math.log(1 + document.getTf()) * Math.log(CORPUS_SIZE / df);
                document.setScore(score);
            }
            entry.getValue().sortByScore();
        }
    }

    private void calculateDistance() {
        for (Map.Entry<String,PostingList> entry : this.index.entrySet()) {
            for (PostingDocument document : entry.getValue().getDocuments()) {
                if (this.distance.containsKey(document.getDocID())) {
                    this.distance.put(document.getDocID(), this.distance.get(document.getDocID()) + Math.pow(document.getScore(), 2));
                } else {
                    this.distance.put(document.getDocID(), Math.pow(document.getScore(), 2));
                }
            }
        }
        for (Map.Entry<String, Double> entry : this.distance.entrySet()) {
            this.distance.put(entry.getKey(), Math.sqrt(this.distance.get(entry.getKey())));
        }
    }

    private void processQuery(String query) throws Exception {
        // 1. read, tokenize the query
        // 2. calcuate the document vector
        // 3. perform dot product
        // 4. give relevant results
        Map<String,Integer> queryVector = buildQueryVector(query);
        Map<String,Map<String,Double>> documentVectors = buildDocumentVectors(queryVector);
        PostingList rankedDocuments = computeDotProduct(queryVector, documentVectors);
        rankedDocuments.sortDocuments();

        System.out.println(query + " : " + rankedDocuments.getDocuments().size() + " documents found");
        rankedDocuments.printDocuments(this.urlMapper, 10);
    }

    private Map<String,Integer> buildQueryVector(String query) {
        Map<String,Integer> queryVector = new HashMap<>();

        for (String queryTerm : removeNonalpha(query).split(" ")) {
            String stemmizedQuery = Stemmizer.englishStemmize(queryTerm);
            if (queryVector.containsKey(stemmizedQuery)) {
                queryVector.put(stemmizedQuery, queryVector.get(stemmizedQuery) + 1);
            } else {
                queryVector.put(stemmizedQuery, 1);
            }
        }
        return queryVector;
    }

    private Map<String,Map<String,Double>> buildDocumentVectors(Map<String,Integer> queryVector) {
        Map<String,Map<String,Double>> documentVectors = new HashMap<>();

        for (Map.Entry<String,Integer> entry : queryVector.entrySet()) {
            if (this.index.containsKey(entry.getKey())) {
                // Documents contain the query term
                for (PostingDocument document : this.index.get(entry.getKey()).getDocuments()) {
                    if (documentVectors.containsKey(document.getDocID())) {
                        documentVectors.get(document.getDocID()).put(entry.getKey(), document.getScore() / this.distance.get(document.getDocID()));
                    } else {
                        documentVectors.put(document.getDocID(), new HashMap<>());
                        documentVectors.get(document.getDocID()).put(entry.getKey(), document.getScore() / this.distance.get(document.getDocID()));
                    }
                }
            }
        }
        return documentVectors;
    }

    private PostingList computeDotProduct(Map<String,Integer> queryVector, Map<String,Map<String,Double>> documentVectors) throws Exception {
        PostingList rankedDocuments = new PostingList();

        for (Map.Entry<String,Integer> query : queryVector.entrySet()) {
            for (Map.Entry<String,Map<String,Double>> document : documentVectors.entrySet()) {
                if (document.getValue().containsKey(query.getKey())) {
                    rankedDocuments.addScore(document.getKey(), query.getValue() * document.getValue().get(query.getKey()));
                }
            }
        }
        return rankedDocuments;
    }

    private int getNumberOfUniqueTerms() {
        return this.index.size();
    }

    private int getNumberOfDocuments() {
        HashSet<String> seenDocuments = new HashSet<>();
        for (Map.Entry<String,PostingList> entry : this.index.entrySet()) {
            for (PostingDocument document : entry.getValue().getDocuments()) {
                if (!seenDocuments.contains(document.getDocID())) {
                    seenDocuments.add(document.getDocID());
                }
            }
        }
        return seenDocuments.size();
    }

    private void showQueryResults(String query) throws Exception {
        if (this.index.containsKey(query)) {
            System.out.println(query + " : " + this.index.get(query).getDf() + " documents found");
            this.index.get(query).printDocuments(this.urlMapper, 10);
        } else {
            System.out.println(query + " : 0 documents found");
        }
    }

    public static void main(String[] args) throws Exception{
        System.out.println();
        System.out.println("Starting to build index..");
        IndexBuilder builder = new IndexBuilder();
        builder.buildIndex();
        System.out.println("Finished building index!");
        System.out.println();
        Scanner s = new Scanner(System.in);
        System.out.println("Number of unique terms: " + builder.getNumberOfUniqueTerms());
        System.out.println("Number of documents: " + builder.getNumberOfDocuments());
        System.out.println();
        while (true) {
            System.out.print("QUERY : ");
            String query = s.nextLine();
            builder.processQuery(query);
            // builder.showQueryResults(Stemmizer.englishStemmize(query));
            System.out.println();
        }

    }

}
