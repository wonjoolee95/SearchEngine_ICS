import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Stream;

public class Test {

    public static String findTitle(String html) {
        return html.substring(0, html.indexOf("<body>"));
    }

    public void process(Stream<String> stream) {

    }

    public static void main(String[] args) throws Exception {
        String fileName = "data/20/58";
        String html = "";

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    html += line;
                }
                if (html.contains("<body>")) {
                    html += "</body>";
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Document document = Jsoup.parse(html, "", Parser.xmlParser());
        System.out.println(document.ownText());
    }
}
