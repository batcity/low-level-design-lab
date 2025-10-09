public class AdapterDemo {
    // Target interface
    interface Charger {
        void charge();
    }

    // Adaptee
    static class EuCharger {
        public void chargeWithEUPort() {
            System.out.println("Charging with EU port...");
        }
    }

    // Adapter
    static class EuChargerAdapter implements Charger {
        private EuCharger euCharger;

        public EuChargerAdapter(EuCharger euCharger) {
            this.euCharger = euCharger;
        }

        @Override
        public void charge() {
            euCharger.chargeWithEUPort();
        }
    }

    // Factory
    static class ChargerFactory {
        public static Charger getCharger(String region) {
            switch (region) {
                case "EU": return new EuChargerAdapter(new EuCharger());
                default: throw new IllegalArgumentException("Unsupported region: " + region);
            }
        }
    }

    public static void main(String[] args) {
        Charger charger = ChargerFactory.getCharger("EU");
        charger.charge();
    }
}
