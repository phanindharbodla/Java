public class Deadlock {
static Integer maxGivableCash = 2143151   ;
static Integer currentCash    = 236532316 ;
  public static void main(String[] args){
    Thread transferToParentAccount = new Thread() {
      public void run() {
        synchronized(maxGivableCash){
          System.out.println("Process transferToParentAccount locked maxGivableCash");
          try{ 
            Thread.sleep(50); 
          } catch (InterruptedException e) {}
          synchronized(currentCash){
            System.out.println("Process transferToParentAccount locked currentCash");
          }
        }
      }
    };
    Thread transferToChildAccount = new Thread(){
      public void run(){
        synchronized(currentCash){
          System.out.println("Process transferToChildAccount locked currentCash");
          try{
    		Thread.sleep(50); 
    	  } catch (InterruptedException e){}
          synchronized(maxGivableCash){
            System.out.println("Process transferToChildAccount locked maxGivableCash");
          }
        }
      }
    };
    transferToParentAccount.start(); 
    transferToChildAccount.start();
  }
}