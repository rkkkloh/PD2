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
import java.util.Collections;

public class HtmlParser {
    public static void main(String[] args) {
        String mode = args[0];
        try {
            if (mode.equals("0")) {
                FileWriter writer = new FileWriter("data.csv", true);
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
                    
                } else if (task.equals("2")) {
                    String stock = args[2];
                    String start = args[3];
                    String end = args[4];
                    task_2(stock, start, end);
                } else if (task.equals("3")) {
                    String stock = args[2];
                    String start = args[3];
                    String end = args[4];
                    task_3(stock, start, end);
                } else if (task.equals("4")) {
                    String stock = args[2];
                    String start = args[3];
                    String end = args[4];
                    task_4(stock, start, end);
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
        reader.close();
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

        for (int i = 0; i < stockList.size(); i++) {
            writer.append(stockList.get(i));

            if (i < stockList.size() - 1) {
                writer.append(",");
            }
        }

        writer.append("\n");
 
        BufferedReader reader = new BufferedReader(new FileReader("data.csv"));
        for (int i = 0; i < 30; i++) {
            String line = reader.readLine();
            writer.append(line.split(" ")[1]);

            if (i < (30 - 1)) {
                writer.append("\n");
            }

        }
        reader.close();
        writer.close();
    }

    private static void task_1(String stock, String start, String end) throws IOException {
        List<Double> movingAverage = new ArrayList<>();

        List<String> stockList = getStockList();

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
        String checkedMovingAverage;
        for (int i = 0; i < movingAverage.size(); i++) {
            checkedMovingAverage = checkDecimalPlaces(movingAverage.get(i));
            writer.append(checkedMovingAverage);
            //System.out.println(Double.toString(movingAverage.get(i)));
            if (i < movingAverage.size() - 1) {
                writer.append(",");
            } else {
                writer.append("\n");
            }
        }
        writer.close();

    }

    public static void task_2(String stock, String start, String end) throws IOException {
        int startingDay = Integer.parseInt(start);
        int endingDay = Integer.parseInt(end);
        int timeFrame = endingDay - startingDay + 1;
        List<String> fileContent = new ArrayList<>();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader("data.csv"));
        double sum = 0;
        double average = 0;
        double sumOfSubtraction = 0;
        String matchedLine = null;
        String temp;

        List<String> stockList = getStockList();

        while ((line = reader.readLine()) != null) {
            fileContent.add(line);
        }
        reader.close();

        temp = start;

        for (int i = 0; i < timeFrame; i++) {
            for (int j = 0; j < fileContent.size(); j++) {
                matchedLine = fileContent.get(j);
                if (fileContent.get(j).split(" ")[0].substring(3).equals(temp)) {
                    break;
                }
            }
            
            if (matchedLine != null) {
                String[] data = matchedLine.split(" ")[1].split(",");

                int stockDataIndex = stockList.indexOf(stock);

                sum += Double.parseDouble(data[stockDataIndex]);

                temp = Integer.toString((Integer.parseInt(temp)) + 1);
            }
        }
        average = sum/timeFrame;

        temp = start;
        for (int i = 0; i < timeFrame; i++) {
            for (int j = 0; j < fileContent.size(); j++) {
                matchedLine = fileContent.get(j);
                if (fileContent.get(j).split(" ")[0].substring(3).equals(temp)) {
                    break;
                }
            }
            
            if (matchedLine != null) {
                String[] data = matchedLine.split(" ")[1].split(",");

                int stockDataIndex = stockList.indexOf(stock);

                sumOfSubtraction += ((Double.parseDouble(data[stockDataIndex]) - average) * (Double.parseDouble(data[stockDataIndex]) - average));

                temp = Integer.toString((Integer.parseInt(temp)) + 1);
            }
        }

        double precision = 0.0001;
        double result = roundToTwoDecimalPlaces(squareRoot(sumOfSubtraction/(timeFrame-1), precision));
        String checkedResult = checkDecimalPlaces(result);

        FileWriter writer = new FileWriter("output.csv",true);
        writer.append(stock + "," + start + "," + end + "\n");
        writer.append(checkedResult).append("\n");
        writer.close();

    }

    public static void task_3(String doNothing, String start, String end) throws IOException{
        List<Double> standardDeviation = new ArrayList<>();
        int startingDay = Integer.parseInt(start);
        int endingDay = Integer.parseInt(end);
        int timeFrame = endingDay - startingDay + 1;
        List<String> fileContent = new ArrayList<>();
        String line;
        BufferedReader reader = new BufferedReader(new FileReader("data.csv"));
        double sum = 0;
        double average = 0;
        double sumOfSubtraction = 0;
        String matchedLine = null;
        String temp;

        List<String> stockList = getStockList();

        while ((line = reader.readLine()) != null) {
            fileContent.add(line);
        }
        reader.close();
        
        for (String stock : stockList) {
            temp = start;
            for (int i = 0; i < timeFrame; i++) {
                for (int j = 0; j < fileContent.size(); j++) {
                    matchedLine = fileContent.get(j);
                    if (fileContent.get(j).split(" ")[0].substring(3).equals(temp)) {
                        break;
                    }
                }
                
                if (matchedLine != null) {
                    String[] data = matchedLine.split(" ")[1].split(",");

                    int stockDataIndex = stockList.indexOf(stock);

                    sum += Double.parseDouble(data[stockDataIndex]);

                    temp = Integer.toString((Integer.parseInt(temp)) + 1);
                }
            }
            average = sum/timeFrame;

            temp = start;
            for (int i = 0; i < timeFrame; i++) {
                for (int j = 0; j < fileContent.size(); j++) {
                    matchedLine = fileContent.get(j);
                    if (fileContent.get(j).split(" ")[0].substring(3).equals(temp)) {
                        break;
                    }
                }
                
                if (matchedLine != null) {
                    String[] data = matchedLine.split(" ")[1].split(",");

                    int stockDataIndex = stockList.indexOf(stock);

                    sumOfSubtraction += ((Double.parseDouble(data[stockDataIndex]) - average) * (Double.parseDouble(data[stockDataIndex]) - average));

                    temp = Integer.toString((Integer.parseInt(temp)) + 1);
                }
            }

            double precision = 0.0001;
            double result = roundToTwoDecimalPlaces(squareRoot(sumOfSubtraction/(timeFrame-1), precision));
            standardDeviation.add(result);
            result = 0;
            average = 0;
            sum = 0;
            sumOfSubtraction = 0;
 
        }

        List<Double> sortedStandardDeviation = new ArrayList<>(standardDeviation);
        Collections.sort(sortedStandardDeviation,Collections.reverseOrder());
        Double first = sortedStandardDeviation.get(0);
        Double second = sortedStandardDeviation.get(1);
        Double third = sortedStandardDeviation.get(2);
        int index1 = standardDeviation.indexOf(first);
        //System.out.println(standardDeviation.get(index1));
        int index2 = standardDeviation.indexOf(second);
        int index3 = standardDeviation.indexOf(third);
        String First = stockList.get(index1);
        String Second = stockList.get(index2);
        String Third = stockList.get(index3);
        //System.out.println(sortedStandardDeviation);
        //System.out.println(standardDeviation);
        String data1 = checkDecimalPlaces(first);
        String data2 = checkDecimalPlaces(second);
        String data3 = checkDecimalPlaces(third);
        FileWriter writer = new FileWriter("output.csv",true);
        writer.append(First + "," + Second + "," + Third + "," + start + "," + end +"\n");
        writer.append(data1 + "," + data2 + "," + data3).append("\n");
        writer.close();
    }

    public static void task_4(String stock, String start, String end) {
        
    }

    public static String checkDecimalPlaces(double number) {
        if ((long)number == number) {
            return Long.toString((long)number);
        } 
        return Double.toString(number);
    }

    public static double roundToTwoDecimalPlaces(double number) {
        long roundedValue = round(number * 100);

        return (double) roundedValue / 100;
    }

    public static long round(double number) {
        return number >= 0 ? (long) (number + 0.5) : (long) (number - 0.5);
    }

    public static List<String> getStockList() throws IOException{
        Document doc = Jsoup.connect("https://pd2-hw3.netdb.csie.ncku.edu.tw/").get();
        Elements rows = doc.select("table tr");

        List<String> stockList = new ArrayList<>();

        for (Element row : rows) {
            Elements stocks = row.select("th");
            for (int i = 0; i < stocks.size(); i++) {
                stockList.add(stocks.get(i).text());
            }
        }

        return stockList;
    }

    public static double squareRoot(double number, double precision) {
        double guess = number / 2; // 初始猜測值，可以任意設定，一般取一半
        double previousGuess;
        
        do {
            previousGuess = guess;
            guess = (guess + number / guess) / 2;
        } while (absoluteValue(guess - previousGuess) >= precision);

        // 四捨五入到小數點後四位
        return roundToFourDecimalPlaces(guess);
    }

    public static double absoluteValue(double value) {
        if (value < 0) {
            return -value;
        } else {
            return value;
        }
    }

    public static double roundToFourDecimalPlaces(double value) {
        double factor = 10000.0;
        double temp = value * factor;
        double rounded = (int) (temp + 0.5);
        return rounded / factor;
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


