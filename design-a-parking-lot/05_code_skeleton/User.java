import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class User {
    private final UUID userId;
    private String name;
    private String phoneNumber;
    private List<Vehicle> vehicles;

    public User(UUID userId, String name, String phoneNumber) {
        this.userId = userId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.vehicles = new ArrayList<>();
    }

    public UUID getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    // TODO: Test this to show why this is bad
    // For example, someone could get the list of vehicles and modify the userID to another one - yikes
    // public List<Vehicle> getVehicles() {
    //     return vehicles;
    // }

    public List<Vehicle> getVehicles() {
        return Collections.unmodifiableList(vehicles);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void addVehicle(Vehicle vehicle) {
        if (vehicle.getUserId().equals(this.userId)) {
            vehicles.add(vehicle);
        } else {
            throw new IllegalArgumentException("Vehicle belongs to another user");
        }
    }

    public void removeVehicle(Vehicle vehicle) {
        vehicles.remove(vehicle);
    }

    @Override
    public String toString() {
        return "User{" +
               "userId=" + userId +
               ", name='" + name + '\'' +
               ", phoneNumber='" + phoneNumber + '\'' +
               ", vehicles=" + vehicles +
               '}';
    }
}