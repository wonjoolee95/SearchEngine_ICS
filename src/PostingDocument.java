
// Class that represents each posting document
public class PostingDocument {

    private String docID;
    private int tf;
    private double score;

    public PostingDocument(String docID) {
        this.docID = docID;
        this.tf = 0;
        this.score = 0.00;
    }

    public void incrementTf(int score) {
        this.tf += score;
    }

    public void incrementScore(double score) {
        this.score += score;
    }

    public void setTf(int tf) {
        this.tf = tf;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getTf() {
        return this.tf;
    }

    public double getScore() {
        return this.score;
    }

    public String getDocID() {
        return docID;
    }
}
