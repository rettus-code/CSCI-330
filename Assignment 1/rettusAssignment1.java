import java.io.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.lang.Math;
import java.io.FileWriter;
import java.io.IOException;

public class rettusAssignment1{
   public static void main(String[] args)
      throws FileNotFoundException
   {
      String stock = "";
      Stock name = new Stock();
      String path = "./StockmarketInput.txt";
      InputStream is = new FileInputStream(path);
      String open = "";
      PrintStream output =
            new PrintStream(new File("StockmarketOutput.txt"));
      try (Scanner sc = new Scanner(
                 is, StandardCharsets.UTF_8.name())) {
            /*check the input stream for a next line if exists grabs line and
            uses various methods to parse data into information to be stored
            on a stock object and added the formated information to the 
            output file*/
            while (sc.hasNextLine()) {
               String[] line = sc.nextLine().split("\t", 0);
               if (line[0].equals(stock)){
                  name = stockCrazy(name, line);
               } else {
                  name = objectFiler(name, output);
                  name.setStock(line[0]);
                  stock = line[0];
                  name = stockCrazy(name, line);
               }
               name = stockSplits(name, line, open);
               open = line[2];
            }
            name = objectFiler(name, output);
        }
   }
   public static Stock objectFiler(Stock name, PrintStream output)
      throws FileNotFoundException
   {
      /*if stock object contains value adds formatted object data to output file
      then calls objectBuilder to reset values. if object does not contain value
      then sets initial values to null so data can be appended*/
      if (name.getCD() != null){
            output.println("Processing " + name.getStock());
            for (int i = 1; i < 23; i++){
               output.print("=");
            }
            output.println("");
            String[] crazies = name.getCD().split(" ", 0);
            for (int i = 0; i < crazies.length; i++){
               output.println("Crazy day: " + crazies[i]);
            }
            output.println("Total crazy days = " + name.getCDays());
            output.println("The craziest day: " + name.getCraziest() + "\n");
            String[] splits = name.getSp().split(", ", 0);
            for (int i = 0; i < splits.length; i++){
               output.println(splits[i]);
            }
            output.println("Total number of splits = " + name.getSplits() + "\n\n");
       }
      
      objectBuilder(name);
      return name;
   }
   public static Stock objectBuilder(Stock name){
      //Sets or resets stock object to null settings 
      name.setCD("");
      name.setCDays(0);
      name.setCraziest("");
      name.setSp("");
      name.setSplits(0);
      return name;
   }
   public static Stock stockCrazy(Stock name, String[] line){
      /*checks each input line passed compares the low and high to see if
      it meets crazy criteria if it does adds to the stock.crazyday count if new
      comparison is the greatest difference stores the day and value in stock.craziest[]*/
      double low = Double.parseDouble(line[4]);
      double high = Double.parseDouble(line[3]);
      double checks = Math.round(((high - low)/high)*10000);
      double check = checks/100;
      if (check >= 15.0){
         if (name.getCraziest() == "") {
            name.setCraziest(line[1] + "\t" + check);
         }
         String[] arr = name.getCraziest().split("\t", 0);
         double craziest = Double.parseDouble(arr[1]);
         if (check > craziest){
            name.setCraziest(line[1] + "\t" + check);
         }
         String temp = line[1] + "\t" + check;
         name.setCD(name.getCD() + temp + " ");
         name.setCDays(name.getCDays() + 1);
      }
      
      return name;
   }
   public static Stock stockSplits(Stock name, String[] line, String num){
      /*checks for the splits using the provided equations adds to the total number of splits
      and records the formatted split information to the stock object in an array*/
      if (num == ""){
         return name;
      }
      double close = Double.parseDouble(line[5]);
      double open = Double.parseDouble(num);
      if (Math.abs(close/open - 3.0) < .3){
         name.setSplits(name.getSplits() + 1);
         name.setSp(name.getSp() + "3:1 split on " + line[1] + "\t\t" + line[5] + " ---> " + num + ", ");
      }
      if (Math.abs(close/open - 2.0) < .2){
         name.setSplits(name.getSplits() + 1);
         name.setSp(name.getSp() + "2:1 split on " + line[1] + "\t\t" + line[5] + " ---> " + num + ", ");
      }
      if (Math.abs(close/open - 1.5) < .15){
         name.setSplits(name.getSplits() + 1);
         name.setSp(name.getSp() + "3:2 split on " + line[1] + "\t\t" + line[5] + " ---> " + num + ", ");
      }
      
      return name;
   }
}

