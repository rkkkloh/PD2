import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class HtmlParser {
    public static void main(String[] args) {
        String mode = args[0];
        try {
            if (mode.equals("0")) {
                crawlAndAppendData();
            }
            //sortDataRows("data.csv");
            if (mode.equals("1")) {
                String task = args[1];
                if (task.equals("0")){
                    task_0();
                } else if (task.equals("1")) {

                }
                //String stock = args[2];
                //String start = args[3];
                //String end = args[4];
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void crawlAndAppendData() throws IOException {
        Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw/").get();
        Elements rows = doc.select("table tr");
    
        //List<String> dayList = new ArrayList<>();
        String title = doc.title();
        List<String> pricesList = new ArrayList<>();

        for (Element row : rows) {
            Elements datas = row.select("td");
            for (int i = 0; i < datas.size(); i++) {
                    pricesList.add(datas.get(i).text());
            }
        }

        // Append to data.csv
        FileWriter writer = new FileWriter("data.csv", true);
        writer.append(title + " ");
        for (String price : pricesList) {
            writer.append(price).append(",");
        }
        writer.append("\n");
        writer.close();
    }

    private static void task_0() throws IOException {
        Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw/").get();
        Elements rows = doc.select("table tr");

        List<String> stockList = new ArrayList<>();

        for (Element row : rows) {
            Elements stocks = row.select("th");
            for (int i = 0; i < stocks.size(); i++) {
                stockList.add(stocks.get(i).text());
            }
        }

        // Append to output.txt
        FileWriter writer = new FileWriter("output.txt",true);
        for (String stock : stockList) {
            writer.append(stock).append(",");
        }
        writer.append("\n");
 
        BufferedReader reader = new BufferedReader(new FileReader("data.csv"));
        for (int i = 0; i < 30; i++) {
            String line = reader.readLine();
            //System.out.println(line);
            writer.append(line).append("\n");
        }
        reader.close();
        writer.close();
    }

    public static void sortDataRows(String filename) throws IOException {
        // Read the data from the file
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        List<String> lines = new ArrayList<>();
        String line;
        while ((line = reader.readLine()) != null) {
            lines.add(line);
        }
        reader.close();

        // Sort the lines based on day labels without using Collections.sort()
        for (int i = 0; i < lines.size() - 1; i++) {
            for (int j = 0; j < lines.size() - i - 1; j++) {
                String day1 = lines.get(j).split(" ")[0];
                String day2 = lines.get(j + 1).split(" ")[0];
                if (Integer.parseInt(day1.substring(3)) > Integer.parseInt(day2.substring(3))) {
                    // Swap lines
                    String temp = lines.get(j);
                    lines.set(j, lines.get(j + 1));
                    lines.set(j + 1, temp);
                }
            }
        }

        // Write the sorted data back to the file
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (String sortedLine : lines) {
            writer.write(sortedLine);
            writer.newLine();
        }
        writer.close();
    }
}



