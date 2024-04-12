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
                sortDataRows("data.csv");
            } else if (mode.equals("1")) {
                String task = args[1];
                if (task.equals("0")){
                    task_0();
                } else if (task.equals("1")) {
                    String stock = args[2];
                    String start = args[3];
                    String end = args[4];
                    FileWriter writer = new FileWriter("output.csv",true);
                    writer.append(stock + "," + start + "," + end).append("\n");
                    writer.close();
                    task_1(stock, start, end);
                    
                }
                
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

        if (!check(title)) {
            for (Element row : rows) {
                Elements datas = row.select("td");
                for (int i = 0; i < datas.size(); i++) {
                        pricesList.add(datas.get(i).text());
                }
            }
    
            // Append to data.csv
            FileWriter writer = new FileWriter("data.csv", true);
            writer.append(title + " ");
            for (int i = 0; i < pricesList.size(); i++) {
                writer.append(pricesList.get(i));

                if ( i < pricesList.size() - 1) {
                    writer.append(",");
                }
            }
            writer.append("\n");
            writer.close();
        }
    }

    private static Boolean check(String title) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("data.csv"));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim().split(" ")[0];
            if (title.equals(line)) {
                return true;
            }
        }
        return false;
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

        // Append to output.csv
        FileWriter writer = new FileWriter("output.csv",true);
        for (String stock : stockList) {
            writer.append(stock).append(",");
        }
        writer.append("\n");
 
        BufferedReader reader = new BufferedReader(new FileReader("data.csv"));
        for (int i = 0; i < 30; i++) {
            String line = reader.readLine();
            //System.out.println(line);
            writer.append(line.split(" ")[1]).append("\n");
        }
        reader.close();
        writer.close();
    }

    private static void task_1(String stock, String start, String end) throws IOException {
        List<Double> movingAverage = new ArrayList<>();
        Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw/").get();
        Elements rows = doc.select("table tr");

        List<String> stockList = new ArrayList<>();

        for (Element row : rows) {
            Elements stocks = row.select("th");
            for (int i = 0; i < stocks.size(); i++) {
                stockList.add(stocks.get(i).text());
            }
        }

        double sum = 0;
        int startingDay = Integer.parseInt(start);
        int endingDay = Integer.parseInt(end);
        int timeFrame = endingDay - startingDay + 1;

        BufferedReader reader = new BufferedReader(new FileReader("data.csv"));
        List<String> fileContent = new ArrayList<>();
        String line = null;
        String matchedLine = null;
        String temp = null;
        double result;

        while ((line = reader.readLine()) != null) {
            fileContent.add(line);
        }

        for (int i = 0; i < timeFrame - 4; i++) {
            temp = start;
            for (int j = 0;j < 5; j++) {
 
                for (int k = 0; k < fileContent.size(); k++) {
                    matchedLine = fileContent.get(k);
                    if (fileContent.get(k).split(" ")[0].substring(3).equals(temp)) {
                        break;
                    }
                }

                //System.out.println(matchedLine);
                
                if (matchedLine != null) {
                    String[] data = matchedLine.split(" ")[1].split(",");
 
                    int stockDataIndex = stockList.indexOf(stock);

                    sum += Double.parseDouble(data[stockDataIndex]);

                    temp = Integer.toString((Integer.parseInt(temp)) + 1);
                } 
            }
            start = Integer.toString(++startingDay);
            //System.out.println(sum);
            result = roundToTwoDecimalPlaces(sum/5);
            movingAverage.add(result);
            sum = 0;
            //System.out.println(sum/5);
        }
        reader.close();

        FileWriter writer = new FileWriter("output.csv",true);
        for (int i = 0; i < movingAverage.size(); i++) {
            writer.append(Double.toString(movingAverage.get(i)));
            //System.out.println(Double.toString(movingAverage.get(i)));
            if (i < movingAverage.size() - 1) {
                writer.append(",");
            }
        }
        writer.close();

    }

    public static double roundToTwoDecimalPlaces(double number) {
        long roundedValue = round(number * 100);

        return (double) roundedValue / 100;
    }

    public static long round(double number) {
        return number >= 0 ? (long) (number + 0.5) : (long) (number - 0.5);
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


