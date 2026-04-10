import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ParkingLot {
    private final UUID parkingLotId;
    private final int numFloors;
    private ConcurrentHashMap<Integer, ParkingSpot> availableParkingSpots;
    private ConcurrentHashMap<Integer, ParkingSpot> takenParkingSpots;

    private ConcurrentLinkedQueue<ParkingSpot> availableParkingSpotsQueue;
    private ConcurrentLinkedQueue<ParkingSpot> takenParkingSpotsQueue;

    public ParkingLot(int numFloors, ConcurrentHashMap<Integer, ParkingSpot> availableParkingSpots) {
        this.parkingLotId = UUID.randomUUID();
        this.numFloors = numFloors;
        this.availableParkingSpots = availableParkingSpots;
        this.takenParkingSpots = new ConcurrentHashMap<>();
    }

    public UUID getParkingLotId() {
        return parkingLotId;
    }

    public int getNumFloors() {
        return numFloors;
    }

    public ConcurrentHashMap<Integer, ParkingSpot> getAvailableParkingSpots() {
        return availableParkingSpots;
    }

    public ParkingSpot getNextAvailableParkingSpot() {
        return availableParkingSpotsQueue.poll();
    }

    // public ConcurrentHashMap<Integer, ParkingSpot> getTakenParkingSpots() {
    //     return takenParkingSpots;
    // }

    public boolean markParkingSpotAsTaken(ParkingSpot parkingSpot) {
        takenParkingSpotsQueue.add(parkingSpot);
        return true;
    }

    public boolean markParkingSpotAsAvailable(ParkingSpot parkingSpot) {
        availableParkingSpotsQueue.add(parkingSpot);
        
        return true;
    }

    @Override
    public String toString() {
        return "ParkingLot [parkingLotId=" + parkingLotId + ", numFloors=" + numFloors + ", availableParkingSpots="
                + availableParkingSpots + ", takenParkingSpots=" + takenParkingSpots + "]";
    }
}
