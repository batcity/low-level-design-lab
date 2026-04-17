import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParkingLot {
    private final UUID parkingLotId;
    private final int numFloors;
    private ConcurrentLinkedQueue<ParkingSpot> availableParkingSpots;
    private ConcurrentLinkedQueue<ParkingSpot> takenParkingSpots;

    public ParkingLot(int numFloors, ConcurrentLinkedQueue<ParkingSpot> availableParkingSpots) {
        this.parkingLotId = UUID.randomUUID();
        this.numFloors = numFloors;
        this.availableParkingSpots = availableParkingSpots;
        this.takenParkingSpots = new ConcurrentLinkedQueue<>();
    }

    public UUID getParkingLotId() {
        return parkingLotId;
    }

    public int getNumFloors() {
        return numFloors;
    }

    public ConcurrentLinkedQueue<ParkingSpot> getAvailableParkingSpots() {
        return availableParkingSpots;
    }

    public ParkingSpot getNextAvailableParkingSpot() {
        return availableParkingSpots.poll();
    }

    public boolean markParkingSpotAsTaken(ParkingSpot parkingSpot) {
        takenParkingSpots.add(parkingSpot);
        return true;
    }

    public boolean markParkingSpotAsAvailable(ParkingSpot parkingSpot) {
        availableParkingSpots.add(parkingSpot);
        return true;
    }

    @Override
    public String toString() {
        return "ParkingLot [parkingLotId=" + parkingLotId + ", numFloors=" + numFloors + ", availableParkingSpots="
                + availableParkingSpots + ", takenParkingSpots=" + takenParkingSpots + "]";
    }
}
