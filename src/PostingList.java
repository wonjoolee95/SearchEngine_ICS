import javafx.geometry.Pos;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// Class that represents the posting list, containing posting documents
public class PostingList {

    private ArrayList<PostingDocument> documents;

    public PostingList() throws Exception {
        this.documents = new ArrayList<>();
    }

    // Adds tf (term frequency) to doc
    public void add(String docID, int score) {
        if (!hasDocID(docID)) {
            this.documents.add(new PostingDocument(docID));
        }
        for (PostingDocument document : this.documents) {
            if (document.getDocID().equals(docID)) {
                document.incrementTf(score);
            }
        }
    }

    public void addScore(String docID, double score) {
        if (!hasDocID(docID)) {
            this.documents.add(new PostingDocument(docID));
        }
        for (PostingDocument document : this.documents) {
            if (document.getDocID().equals(docID)) {
                document.incrementScore(score);
            }
        }
    }

    // Returns true if doc already exists, otherwise false
    public boolean hasDocID(String docID) {
        for (PostingDocument document : this.documents) {
            if (document.getDocID().equals(docID)) {
                return true;
            }
        }
        return false;
    }

    public void sortByScore() {
        this.documents.sort((d1, d2) -> (int) d2.getScore() - (int) d1.getScore());
    }

    public void sortDocuments() {
        Collections.sort(this.documents, new Comparator<PostingDocument>() {
            @Override
            public int compare(PostingDocument d1, PostingDocument d2) {
                return -Double.compare(d1.getScore(), d2.getScore());
            }
        });
    }

    public void printDocuments(URLMapper urlMapper, int limit) {
        int range = Math.min(this.documents.size(), limit);
        for (int i = 0; i < range; i++) {
            System.out.println("    " + this.documents.get(i).getDocID() + " : " + this.documents.get(i).getScore() + " -- " + urlMapper.getURL(this.documents.get(i).getDocID()));
        }
    }

    public void writeDocuments(PrintWriter printWriter) {
        for (PostingDocument document : this.documents) {
            printWriter.print("(" + document.getDocID() + ", " + document.getScore() + ") -> ");
        }
        printWriter.println("END");
    }

    public int getDf() { return this.documents.size(); }

    public ArrayList<PostingDocument> getDocuments() { return this.documents; }

}
