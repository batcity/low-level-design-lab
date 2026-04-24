import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

class ParkingService {

    private ConcurrentHashMap<UUID, ParkingSession> currentParkingSessionsByUserId = new ConcurrentHashMap<>();
    // Generating/Building a parkingLot and ParkingSpots
    private ConcurrentLinkedQueue<ParkingSpot> parkingSpots = init();
    private ParkingLot parkingLot = new ParkingLot(2, parkingSpots);

    private ConcurrentLinkedQueue<ParkingSpot> init() {
        ConcurrentLinkedQueue<ParkingSpot> parkingSpots = new ConcurrentLinkedQueue<>();
        parkingSpots.add(new ParkingSpot(1, 1));
        parkingSpots.add(new ParkingSpot(1, 2));
        parkingSpots.add(new ParkingSpot(1, 3));
        parkingSpots.add(new ParkingSpot(2, 4));
        parkingSpots.add(new ParkingSpot(2, 5));
        return parkingSpots;
    }

    public Optional<UUID> startParkingSession(User user, Vehicle vehicle) throws Exception {

        ParkingSpot spot = parkingLot.tryAcquireSpot();
        if (spot == null) return Optional.empty();

        ParkingSession session = new ParkingSession(user, vehicle, spot);
        ParkingSession raced = currentParkingSessionsByUserId.putIfAbsent(user.getUserId(), session);

        if (raced != null) {
            parkingLot.releaseSpot(spot);
            return Optional.empty();
        }

        return Optional.of(session.getParkingSessionId());
    }

    public boolean endParkingSession(User user) {

        ParkingSession session = currentParkingSessionsByUserId.remove(user.getUserId());

        if (session == null) {
            return false;
        }

        session.endSession();
        parkingLot.releaseSpot(session.getParkingSpot());

        return true;
    }
}