import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class ParkingService {

    private ConcurrentHashMap<UUID, ParkingSession> currentParkingSessionsByUserId = new ConcurrentHashMap<>();
    // Generating/Building a parkingLot and ParkingSpots
    private ConcurrentHashMap<Integer, ParkingSpot> parkingSpots = init();
    private ParkingLot parkingLot = new ParkingLot(2, parkingSpots);

    private ConcurrentHashMap<Integer, ParkingSpot> init() {
        ConcurrentHashMap<Integer, ParkingSpot> map = new ConcurrentHashMap<>();
        map.put(1, new ParkingSpot(1, 1));
        map.put(2, new ParkingSpot(1, 2));
        map.put(3, new ParkingSpot(1, 3));
        map.put(4, new ParkingSpot(2, 4));
        map.put(5, new ParkingSpot(2, 5));
        return map;
    }

    public Optional<UUID> startParkingSession(User user, Vehicle vehicle) throws Exception {

        for (ParkingSpot spot : parkingLot.getAvailableParkingSpots().values()) {

            if (parkingLot.markParkingSpotAsTaken(spot)) {

                ParkingSession session = new ParkingSession(user, vehicle, spot);
                ParkingSession raced =
                        currentParkingSessionsByUserId.putIfAbsent(user.getUserId(), session);

                if (raced == null) {
                    return Optional.of(session.getParkingSessionId());
                }

                // same-user race → rollback spot
                parkingLot.markParkingSpotAsAvailable(spot);
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public boolean endParkingSession(User user) {

        ParkingSession session = currentParkingSessionsByUserId.remove(user.getUserId());

        if (session == null) {
            return false;
        }

        session.endSession();
        parkingLot.markParkingSpotAsAvailable(session.getParkingSpot());

        return true;
    }
}