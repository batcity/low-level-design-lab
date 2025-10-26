import java.util.UUID;
import java.time.Instant;

public class ParkingSession {
    
    private final UUID parkingSessionId;
    private final Instant startTime;
    private Instant endTime;
    private final User user;
    private final Vehicle vehicle;
    private final ParkingSpot parkingSpot;

    public ParkingSession(UUID parkingSessionId, Instant startTime, User user, Vehicle vehicle,
            ParkingSpot parkingSpot) {
        this.parkingSessionId = parkingSessionId;
        this.startTime = startTime;
        this.user = user;
        this.vehicle = vehicle;
        this.parkingSpot = parkingSpot;
    }

    public ParkingSession(User user, Vehicle vehicle, ParkingSpot parkingSpot) {
        this(UUID.randomUUID(), Instant.now(), user, vehicle, parkingSpot);
    }

    @Override
    public String toString() {
        return "ParkingSession [parkingSessionId=" + parkingSessionId + ", startTime=" + startTime + ", endTime="
                + endTime + ", user=" + user + ", vehicle=" + vehicle + ", parkingSpot=" + parkingSpot + "]";
    }

    public UUID getParkingSessionId() {
        return parkingSessionId;
    }
    public Instant getStartTime() {
        return startTime;
    }
    public Instant getEndTime() {
        return endTime;
    }
    public User getUser() {
        return user;
    }
    public Vehicle getVehicle() {
        return vehicle;
    }
    public ParkingSpot getParkingSpot() {
        return parkingSpot;
    }

    public void endSession() {
        if (this.endTime != null) {
            throw new IllegalStateException("Session already ended");
        }
        this.endTime = Instant.now();
    }

    public long getDurationMillis() {
        Instant end = (endTime != null) ? endTime : Instant.now();
        return end.toEpochMilli() - startTime.toEpochMilli();
    }
}
