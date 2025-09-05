public class FactoryPattern {

  public static void main(String args[]) {
      System.out.println("Getting beverages from the factory");
      Beverage beverage = BeverageFactory.getBeverage("Coffee");
      beverage.printType();
      Beverage beverageTwo = BeverageFactory.getBeverage("Tea");
      beverageTwo.printType();
  }
}

interface Beverage {
    public void printType();
}

class Coffee implements Beverage{
  public void printType(){
    System.out.println("This is a Coffee");
  }
}

class Tea implements Beverage{
  public void printType(){
    System.out.println("This is a Tea");
  }
}

class BeverageFactory {
  public static Beverage getBeverage(String beverage) {

    if(beverage.equals("Coffee")) {
      return new Coffee();
    } else if(beverage.equals("Tea")) {
      return new Tea();
    }

    return null;
  }
}
