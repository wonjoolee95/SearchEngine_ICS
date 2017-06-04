
import org.tartarus.snowball.ext.EnglishStemmer;
import org.tartarus.snowball.ext.PorterStemmer;

class Stemmizer {

    public static String porterStemmize(String word) {
        PorterStemmer stem = new PorterStemmer();
        stem.setCurrent(word);
        stem.stem();
        return stem.getCurrent();
    }

    public static String englishStemmize(String word) {
        EnglishStemmer english = new EnglishStemmer();
        english.setCurrent(word.toLowerCase());
        english.stem();
        return english.getCurrent();
    }

    public static void main(String[] args) {
        Stemmizer stemmizer = new Stemmizer();
        String[] words = "says say saying said saw seen see seeing?".split(" ");
        for (String word : words) {
            System.out.println(word);
            System.out.println("  Porter: " + stemmizer.porterStemmize(word));
            System.out.println("  English: " + stemmizer.englishStemmize(word));
        }
    }


}
