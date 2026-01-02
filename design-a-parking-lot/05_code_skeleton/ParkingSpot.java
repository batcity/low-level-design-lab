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
}