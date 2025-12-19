import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class ParkingService {

    private ConcurrentHashMap<UUID, ParkingSession> currentParkingSessionsByUserId = new ConcurrentHashMap<>();
    // Generating/Building a parkingLot and ParkingSpots
    private ConcurrentHashMap<Integer, ParkingSpot> parkingSpots = init();
    private ParkingLot parkingLot = new ParkingLot(2, parkingSpots);

    private ConcurrentHashMap<Integer, ParkingSpot> init() {
        ConcurrentHashMap<Integer, ParkingSpot> map = new ConcurrentHashMap<>();
        map.put(1, new ParkingSpot(true, 1, 1));
        map.put(2, new ParkingSpot(true, 1, 2));
        map.put(3, new ParkingSpot(true, 1, 3));
        map.put(4, new ParkingSpot(true, 2, 4));
        map.put(5, new ParkingSpot(true, 2, 5));
        return map;
    }

    public synchronized Optional<UUID> startParkingSession(User user, Vehicle vehicle) throws Exception {
        ConcurrentHashMap<Integer, ParkingSpot> availableParkingSpots = parkingLot.getAvailableParkingSpots();
        ParkingSpot first = availableParkingSpots.values()
        .stream()
        .findFirst()
        .orElse(null);

        if(first == null) {
            return Optional.empty();
        }

        ParkingSession parkingSession = new ParkingSession(user, vehicle, first);
        ParkingSession activeSession = currentParkingSessionsByUserId.putIfAbsent(user.getUserId(), parkingSession);

        if(activeSession != null) {
            return Optional.empty();
        }

        parkingLot.markParkingSpotAsTaken(first);
        return Optional.of(parkingSession.getParkingSessionId());
    }

    public synchronized boolean endParkingSession(User user) {
        ParkingSession currentParkingSession = currentParkingSessionsByUserId.get(user.getUserId());

        if(currentParkingSession==null) {
            return false;
        }

        ParkingSpot parkingSpot = currentParkingSession.getParkingSpot();
        parkingSpot.free();
        parkingLot.markParkingSpotAsAvailable(parkingSpot);
        currentParkingSession.endSession();
        currentParkingSessionsByUserId.remove(user.getUserId());
        
        return true;
    }
}