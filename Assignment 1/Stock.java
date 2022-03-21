public class Stock{
   String stock;
   String crazyDay;
   int cDays;
   String craziest;
   String split;
   int splits;
   
   public Stock(){}
   
   public Stock(String stock, String crazyDay, int cDays,
               String craziest, String split, int splits){
      this.stock = stock;
      this.crazyDay = crazyDay;       
      this.cDays = cDays;
      this.craziest = craziest;
      this.split = split;
      this.splits = splits;  
   }
   
   public String getStock(){
      return stock;
   }
   public String getCD(){
      return crazyDay;
   }
   public int getCDays(){
      return cDays;
   }
   public String getCraziest(){
      return craziest;
   }
   public String getSp(){
      return split;
   }
    public int getSplits(){
      return splits;
   }
   public void setStock(String s){
      this.stock = s;
   }
   public void setCD(String s){
      this.crazyDay = s;
   }
   public void setCDays(int n){
      this.cDays = n;
   }
   public void setCraziest(String s){
      this.craziest = s;
   }
   public void setSp(String s){
      this.split = s;
   }
    public void setSplits(int n){
      this.splits = n;
   }
}