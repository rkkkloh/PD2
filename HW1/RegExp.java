
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;

public class RegExp {
    
    public static void main(String[] args) {
        String str1 = args[1];
        String str2 = args[2];
        int s2Count = Integer.parseInt(args[3]);
        
        File file = new File(args[0]);
        if (file.length() == 0) {
            System.out.println("N1");
        }

        //For your testing of input correctness
        //System.out.println("The input file:"+args[0]);
        //System.out.println("str1="+str1);
        //System.out.println("str2="+str2);
        //System.out.println("num of repeated requests of str2 = "+s2Count);

        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.toLowerCase();
                String reverse = "";
                for(int i = line.length()-1;i >= 0;i--)
                    reverse += line.charAt(i);
                //System.out.println(reverse);
                if (line.equals(reverse)) {
                    if (line.length() != 0 && line.length() != 1)
                        System.out.print("Y,");
                    else
                        System.out.print("N,");
                } else 
                    System.out.print("N,");
                if (line.contains(str1))
                    System.out.print("Y,");
                else 
                    System.out.print("N,");
                int count = countOccurrences(line, str2);
                if (count >= s2Count)
                    System.out.print("Y,");
                else
                    System.out.print("N,");
                if (line.contains("a") && line.contains("bb")) {
                    if (line.indexOf("a") < line.lastIndexOf("bb"))
                        System.out.println("Y");
                    else 
                        System.out.println("N");
                } else
                    System.out.println("N");
                
            }
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static int countOccurrences(String line, String str) {
        int count = 0;
        int lastIndex = 0;
        while ((lastIndex = line.indexOf(str, lastIndex)) != -1) {
            System.out.println(lastIndex);
            count++;
            lastIndex += 1;
        }
        return count;
    }
}
