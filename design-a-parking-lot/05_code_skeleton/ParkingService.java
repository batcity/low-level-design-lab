import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class ParkingService {

    private Map<UUID, ParkingSession> currentParkingSessionsByUserId = new HashMap<>();
    // Generating/Building a parkingLot and ParkingSpots
    private ConcurrentHashMap<Integer, ParkingSpot> parkingSpots = init();
    private ParkingLot parkingLot = new ParkingLot(2, parkingSpots);

    private ConcurrentHashMap<Integer, ParkingSpot> init() {
        ConcurrentHashMap<Integer, ParkingSpot> map = new ConcurrentHashMap<>();
        map.put(1, new ParkingSpot(true, 1, 1));
        map.put(2, new ParkingSpot(true, 1, 2));
        map.put(3, new ParkingSpot(true, 1, 3));
        map.put(4, new ParkingSpot(true, 1, 4));
        map.put(5, new ParkingSpot(true, 1, 5));
        return map;
    }

    public boolean startParkingSession(User user, Vehicle vehicle) {
        ConcurrentHashMap<Integer, ParkingSpot> availableParkingSpots = parkingLot.getAvailableParkingSpots();
        ParkingSpot first = availableParkingSpots.values()
        .stream()
        .findFirst()
        .orElse(null);

        if(first == null) {
            return false;
        }

        ParkingSession parkingSession = new ParkingSession(user, vehicle, first);
        parkingLot.markParkingSpotAsTaken(first);
        currentParkingSessionsByUserId.put(user.getUserId(), parkingSession);

        return true;
    }
}