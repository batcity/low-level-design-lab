import java.util.UUID;

public class Vehicle {
    private final UUID vehicleId;
    private final VehicleType vehicleType;
    private UUID userId;

    public Vehicle(UUID vehicleId, VehicleType vehicleType, UUID userId) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        this.userId = userId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
               "vehicleId=" + vehicleId +
               ", vehicleType=" + vehicleType +
               ", userId=" + userId +
               '}';
    }
}
