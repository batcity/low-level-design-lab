public class ParkingSpot {
    
    private boolean available;
    private final int floor;
    private final int spotId;

    public ParkingSpot(boolean available, int floor, int spotId) {
        this.available = available;
        this.floor = floor;
        this.spotId = spotId;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getFloor() {
        return floor;
    }

    public int getSpotId() {
        return spotId;
    }

    public void occupy() {
        this.available = false;
    }

    public void free() {
        this.available = true;
    }

    @Override
    public String toString() {
        return "ParkingSpot{" +
               "spotId=" + spotId +
               ", floor=" + floor +
               ", available=" + available +
               '}';
    }
}