import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingLot {
    private final UUID parkingLotId;
    private final int numFloors;
    private ConcurrentHashMap<Integer, ParkingSpot> availableParkingSpots;
    private ConcurrentHashMap<Integer, ParkingSpot> takenParkingSpots;

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

    public ConcurrentHashMap<Integer, ParkingSpot> getTakenParkingSpots() {
        return takenParkingSpots;
    }

    public boolean markParkingSpotAsTaken(ParkingSpot parkingSpot) {
        availableParkingSpots.remove(parkingSpot.getSpotId());
        ParkingSpot parkingSpotVal = takenParkingSpots.putIfAbsent(parkingSpot.getSpotId(), parkingSpot);

        if(parkingSpotVal!=null) {
            return false;
        }

        parkingSpot.occupy();
        return true;
    }

    public boolean markParkingSpotAsAvailable(ParkingSpot parkingSpot) {
        takenParkingSpots.remove(parkingSpot.getSpotId());
        ParkingSpot parkingSpotVal = availableParkingSpots.putIfAbsent(parkingSpot.getSpotId(), parkingSpot);

        if(parkingSpotVal!=null) {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return "ParkingLot [parkingLotId=" + parkingLotId + ", numFloors=" + numFloors + ", availableParkingSpots="
                + availableParkingSpots + ", takenParkingSpots=" + takenParkingSpots + "]";
    }
}
