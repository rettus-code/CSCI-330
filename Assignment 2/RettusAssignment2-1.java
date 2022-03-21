import java.util.*;
import java.io.FileInputStream;
import java.sql.*;
import java.sql.Driver;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

class RettusAssignment2 {
   
   static class StockData {	   
      private String ticker;
      private String date;
      private double oPrice;
      private double hPrice;
      private double lPrice;
      private double cPrice;
      private String split;
      private int count;
      
      private StockData(){}
      
      public StockData(String ticker, String date, double oPrice, double hPrice, double lPrice, double cPrice, String split){
         this.ticker = ticker;
         this.date = date;
         this.oPrice = oPrice;
         this.hPrice = hPrice;
         this.lPrice = lPrice;
         this.cPrice = cPrice;
         this.split = split;
      }
      public void setOPrice(double oPrice){
         this.oPrice = oPrice;
      }
      public void setHPrice(double hPrice){
         this.hPrice = hPrice;
      }
      public void setLPrice(double lPrice){
         this.lPrice = lPrice;
      }
      public void setCPrice(double cPrice){
         this.cPrice = cPrice;
      }
      public void setSplit(String split){
         this.split = split;
      }
   }
   
   static Connection conn;
   static final String prompt = "Enter ticker symbol [start/end dates]: ";
   //Made a small adjustment to main to allow start time without end time  
   public static void main(String[] args) throws Exception {
      String paramsFile = "readerparams.txt";
      if (args.length >= 1) {
         paramsFile = args[0];
      }
      Properties connectprops = new Properties();
      connectprops.load(new FileInputStream(paramsFile));
      try {
         Class.forName("com.mysql.jdbc.Driver");
         String dburl = connectprops.getProperty("dburl");
         String username = connectprops.getProperty("user");
         conn = DriverManager.getConnection(dburl, connectprops);
         System.out.printf("Database connection %s %s established.%n", dburl, username);
         Scanner in = new Scanner(System.in);
         System.out.print(prompt);
         String input = in.nextLine().trim();
         while (input.length() > 0) {
            String[] params = input.split("\\s+");
            String ticker = params[0];
            String startdate = null, enddate = null;
            if (getName(ticker)) {
               if (params.length >= 2) {
                  startdate = params[1];
               }
               if (params.length > 2) { 
                  enddate = params[2];
               }             
               Deque<StockData> data = getStockData(ticker, startdate, enddate);
               System.out.println();
               System.out.println("Executing investment strategy");
               doStrategy(ticker, data);
            } 
            System.out.println();
            System.out.print(prompt);
            input = in.nextLine().trim();
         }
         conn.close();
      } catch (SQLException ex) {
         System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n",
                           ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
      }
   }
   
   static boolean getName(String ticker) throws SQLException {
        try {
            PreparedStatement pStmt = conn.prepareStatement("SELECT name FROM company WHERE ticker = ?");
            pStmt.setString(1, ticker);
            ResultSet rs = pStmt.executeQuery();
            if(rs.next()){
                System.out.printf("%s%n", rs.getString("name"));
                return true;
            } else {
                System.out.printf("%s - No such ticker exists\n", ticker);
             return false;
            }
       } catch (SQLException ex) {
          System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n",
                           ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
       }
       return false;
   }
   /*Pulls data from the database parses it and adds it to the StockData object. passes data to other
   functions to modify StockData object values and print split information*/
   static Deque<StockData> getStockData(String ticker, String start, String end) throws SQLException{	  
      Deque<StockData> result = new ArrayDeque<>();
      ResultSet rs = null;
      int count = 0;
      /*if dates are null the if statements hard sets dates for start to the founding day of the NYSE and 
      end to the end of the century so only one query is required*/
      if (start == null){
         start = "1792.05.17";
      }
      if (end == null){
         end = "2099.12.31";
      }
      try {
         PreparedStatement pStmt = conn.prepareStatement("SELECT * FROM pricevolume WHERE ticker = ?" +
                                                         "AND transdate >= ? AND transdate <= ?" +
                                                         "ORDER BY transdate DESC");
         pStmt.setString(1, ticker);
         pStmt.setString(2, start);
         pStmt.setString(3, end);
         rs = pStmt.executeQuery();
         String open = "";
         String close = "";
         String date = "";
         String modifier = "";
         while(rs.next()){
            close = rs.getString("closeprice");
            date = rs.getString("transdate");
            String split = splitFinder(open, close, date);
            if (split != ""){
               count++;
               modifier += split.split(" ", 0)[0] + " ";
            }
            open = rs.getString("openprice");
            StockData sData = new StockData(rs.getString("ticker"),rs.getString("transdate"), Double.parseDouble(rs.getString("openprice")),
                       Double.parseDouble(rs.getString("highprice")), Double.parseDouble(rs.getString("lowprice")), 
                       Double.parseDouble(rs.getString("closeprice")), split);
            print(sData);
            result.add(splitModifier(sData, modifier));
         }
      } catch (SQLException ex) {
         System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n",
                           ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
      }
      System.out.printf("%d splits in %d trading days%n", count, result.size());
      return result;
   }
   /*Takes string values for open and close converts to double for comparison prints
   the split occurrence and returns it as a string for other uses.*/
   static String splitFinder(String sOpen, String sClose, String date){
      double close = Double.parseDouble(sClose);
      if (sOpen == ""){
         sOpen = sClose;
      }
      double open = Double.parseDouble(sOpen);
      String split = "";
      if (Math.abs(close/open - 3.0) < .3){
         split = "3:1 split on " + date + "\t" + sClose + " ---> " + sOpen;
      }
      if (Math.abs(close/open - 2.0) < .2){
         split = "2:1 split on " + date + "\t" + sClose + " ---> " + sOpen;
      }
      if (Math.abs(close/open - 1.5) < .15){
         split = "3:2 split on " + date + "\t" + sClose + " ---> " + sOpen;
      }
      return split;
   }
   static void print(StockData n){
      if (n.split != ""){
         System.out.printf("%s %s%n", n.ticker, n.split);
      }
   }
   /*Takes total splits value up to this point stored on StockData object updates it with new 
   split and concurrently modifies price values on the object before returning it to be added 
   to the Deque. This prevents the need to loop through the data twice*/
   static StockData splitModifier(StockData sData, String modifiers){
      String[] mods = modifiers.split(" ", 0);
      double modifier = 1.0;
      for (int i = 0; i < mods.length; i++){
         if (mods[i].equals("3:1")){
            modifier *= 3.0;
         }
         if (mods[i].equals("2:1")){
            modifier *= 2.0;
         }
         if (mods[i].equals("3:2")){
            modifier *= 1.5;
         }
      }
      sData.setCPrice(sData.cPrice / modifier);
      sData.setOPrice(sData.oPrice / modifier);
      sData.setLPrice(sData.lPrice / modifier);
      sData.setHPrice(sData.hPrice / modifier);
      return sData;
   }
   /*creates the day average and uses movAvg() to maintain it also contains the logic 
   for buying and selling IAW the instructions of the assignment and prints it accordingly*/
   static void doStrategy(String ticker, Deque<StockData> data) {
     double money = 0;
     int shares = 0;
     int transactions = 0;
     Deque<StockData> ftyDayAvg = new ArrayDeque<>();
     double currAvg = 0;
     StockData curr = new StockData();
     if (data.size() < 51) {
            System.out.printf("Less than 50 days of trading. 0 net gain.");
     } else {
         for (int i = 0; i < 50; i++){
            ftyDayAvg.add(data.getLast());
            data.removeLast();
         } 
         currAvg = movAvg(ftyDayAvg);
             int buy = 0;
            int sell = 0;        
         while(data.size() > 1) {
            curr = data.getLast();
            data.removeLast();
            double open = curr.oPrice;
            double close = curr.cPrice;
             if (close < currAvg && close/open < 0.97000001) {
               shares += 100;
               money -= 100 * data.getLast().oPrice;
               money -= 8;
               transactions++;
               buy++;
            } else if (shares >= 100 && open > currAvg && open/ftyDayAvg.getLast().cPrice > 1.00999999){
               shares -= 100;
               money += (100 * ((open + close)/2));
               money -= 8;
               transactions++;
               sell++;
            }
            ftyDayAvg.removeFirst();
            ftyDayAvg.addLast(curr);
            currAvg = movAvg(ftyDayAvg);
         }
         if (shares > 0) {
            System.out.println("test");
            money += data.getLast().oPrice * shares;
            transactions++;
         }
         

         System.out.printf("Transactions executed: %d%nNet Cash: %.2f%n", transactions, money);
      }
   }
   //determines current 50 day avg
   static double movAvg(Deque<StockData> ftyDayAvg) {
        List<StockData> listSwap = new ArrayList<>(ftyDayAvg);
        double sum = 0;
        for (StockData curr : listSwap) {
            sum += curr.cPrice;
        }
        return sum/50;
    } 
}
