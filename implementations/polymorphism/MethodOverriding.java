class Car {

  String make = "generic car";

  public String getMake(){
    return make;
  }
}

class Mercedes extends Car {

  String make = "Mercedes";

  public String getMake(){
    return make;
  }
}

class MethodOverriding {

  public static void main(String[] args) {

    Car genericCar = new Car();
    System.out.println("Calling generic car class' getMake method returns " + genericCar.getMake());
    Mercedes mercedesCar = new Mercedes();
    System.out.println("Calling Mercedes car class' getMake method returns " + mercedesCar.getMake());
  }
}
