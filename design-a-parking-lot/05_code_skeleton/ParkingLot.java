import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParkingLot {
    private final UUID parkingLotId;
    private final int numFloors;
    private ConcurrentLinkedQueue<ParkingSpot> availableParkingSpots;

    public ParkingLot(int numFloors, ConcurrentLinkedQueue<ParkingSpot> availableParkingSpots) {
        this.parkingLotId = UUID.randomUUID();
        this.numFloors = numFloors;
        this.availableParkingSpots = availableParkingSpots;
    }

    public UUID getParkingLotId() {
        return parkingLotId;
    }

    public int getNumFloors() {
        return numFloors;
    }

    // This method is useful for my unit tests
    public ConcurrentLinkedQueue<ParkingSpot> getAvailableParkingSpots() {
        return availableParkingSpots;
    }

    public ParkingSpot tryAcquireSpot() {
        return availableParkingSpots.poll();
    }

     public void releaseSpot(ParkingSpot spot) {
        if (spot != null) {
            availableParkingSpots.add(spot);
        }
    }

    public boolean markParkingSpotAsAvailable(ParkingSpot parkingSpot) {
        availableParkingSpots.add(parkingSpot);
        return true;
    }

    @Override
    public String toString() {
        return "ParkingLot [parkingLotId=" + parkingLotId + ", numFloors=" + numFloors + ", availableParkingSpots="
    }
}
