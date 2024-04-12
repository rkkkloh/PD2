import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HtmlParser {
    public static void main(String[] args) {
        if (args[0].equals("0")) {
            String mode = args[0];
            try {
                crawlAndAppendData();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (args[0].equals("1")) {
            String mode = args[0];
            String task = args[1];
            String stock = args[2];
            String start = args[3];
            String end = args[4];
        }
        
/* 
        try {
            Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw/").get();
            System.out.println(doc.title());
            Elements nestedTables = doc.select("th");
            Elements tableRows = doc.select("td");
            for (Element nestedTable : nestedTables) {
                System.out.printf(nestedTable.html() + " ");
            }
            for (Element tableRow : tableRows) {
                System.out.printf(tableRow.html() + " ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
    }

    private static void crawlAndAppendData() throws IOException {
        Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw/").get();
        Elements rows = doc.select("table tr");
        //System.out.println(rows);
    
        List<String> stocksList = new ArrayList<>();
        List<String> pricesList = new ArrayList<>();

        for (Element row : rows) {
            Elements datas = row.select("td");
            for (int i = 0; i < datas.size(); i++) {
                    pricesList.add(datas.get(i).text());
            }
            Elements stocks = rows.select("th");
            for (int i = 0; i < stocks.size(); i++) {
                stocksList.add(stocks.get(i).text());
                //System.out.println(stocks.get(i).text());
            }
        }

    
        // Append to data.csv
        FileWriter writer = new FileWriter("data.csv", true);
        for (String stock : stocksList) {
            writer.append(stock).append(",");
        }
        writer.append("\n");
        for (String price : pricesList) {
            writer.append(price).append(",");
        }
        writer.append("\n");
        writer.close();
    }
}


