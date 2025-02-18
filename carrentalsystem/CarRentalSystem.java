package carrentalsystem;

import java.util.*;
import java.util.concurrent.*;

public class CarRentalSystem {
    private final Map<CarType, ConcurrentSkipListSet<Station>> stationIndex = new ConcurrentHashMap<>();
    private final List<Station> stations = new CopyOnWriteArrayList<>();

    public void addStation(Station station) {
        stations.add(station);
        station.prices.keySet().forEach(type ->
            stationIndex.computeIfAbsent(type, t -> new ConcurrentSkipListSet<>(
                Comparator.comparingDouble(s -> s.getPrice(t))
            )).add(station));
    }

    public void addStationToIndex(Station station, CarType type) {
        stationIndex.computeIfAbsent(type, t -> new ConcurrentSkipListSet<>(
            Comparator.comparingDouble(s -> s.getPrice(t))
        )).add(station);
    }

    public void removeStationFromIndex(Station station, CarType type) {
        stationIndex.computeIfPresent(type, (_, set) -> {
            set.remove(station);
            return set;
        });
    }

    public List<Station> search(CarType type, Location userLocation) {
        ConcurrentSkipListSet<Station> stationsByPrice = stationIndex.get(type);
        if (stationsByPrice == null) return Collections.emptyList();

        List<Station> result = new ArrayList<>();
        List<Station> currentGroup = new ArrayList<>();
        double currentPrice = -1;

        for (Station station : stationsByPrice) {
            double price = station.getPrice(type);
            if (price != currentPrice) {
                if (!currentGroup.isEmpty()) {
                    sortByDistance(currentGroup, userLocation);
                    result.addAll(currentGroup);
                    currentGroup.clear();
                }
                currentPrice = price;
            }
            currentGroup.add(station);
        }

        if (!currentGroup.isEmpty()) {
            sortByDistance(currentGroup, userLocation);
            result.addAll(currentGroup);
        }

        return result;
    }

    private void sortByDistance(List<Station> stations, Location userLocation) {
        stations.sort(Comparator.comparingDouble(s -> 
            computeDistance(s.getLocation(), userLocation)));
    }

    private double computeDistance(Location a, Location b) {
        return Math.sqrt(
            Math.pow(a.getLatitude() - b.getLatitude(), 2) +
            Math.pow(a.getLongitude() - b.getLongitude(), 2)
        );
    }

    public Car bookCar(CarType type, Location userLocation) {
        List<Station> stations = search(type, userLocation);
        for (Station station : stations) {
            ConcurrentLinkedDeque<Car> cars = station.availableCars.get(type);
            if (cars == null) continue;
            Car car = cars.poll();
            if (car != null) {
                if (car.book()) {
                    station.availableCounts.get(type).decrementAndGet();
                    return car;
                } else {
                    cars.addFirst(car);
                }
            }
        }
        throw new CarNotAvailableException("No cars available for type: " + type);
    }

    public void returnCar(Car car, Station station) {
        car.returnToStation(station);
    }

    public static void main(String[] args) {
        CarRentalSystem system = new CarRentalSystem();
        Location loc1 = new Location(10.0, 20.0);
        Station station1 = new Station("Station1", loc1, system);
        station1.setPrice(CarType.SUV, 100.0);
        system.addStation(station1);

        new Car("C1", CarType.SUV, station1);
        new Car("C2", CarType.SUV, station1);

        Location userLoc = new Location(10.1, 20.1);
        List<Station> results = system.search(CarType.SUV, userLoc);
        System.out.println("Search results: " + results.size());

        try {
            Car bookedCar = system.bookCar(CarType.SUV, userLoc);
            System.out.println("Booked car: " + bookedCar.getType());
            system.returnCar(bookedCar, station1);
            System.out.println("Car returned");
        } catch (CarNotAvailableException e) {
            System.out.println(e.getMessage());
        }
    }
}
