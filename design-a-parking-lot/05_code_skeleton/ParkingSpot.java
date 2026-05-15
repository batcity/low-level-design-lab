import java.util.Objects;

public class ParkingSpot {
    
    private final int floor;
    private final int spotId;

    public ParkingSpot(int floor, int spotId) {
        this.floor = floor;
        this.spotId = spotId;
    }

    public int getFloor() {
        return floor;
    }

    public int getSpotId() {
        return spotId;
    }

    @Override
    public String toString() {
        return "ParkingSpot{" +
               "spotId=" + spotId +
               ", floor=" + floor +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParkingSpot)) return false;

        ParkingSpot that = (ParkingSpot) o;

        return floor == that.floor &&
            spotId == that.spotId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(floor, spotId);
    }
}