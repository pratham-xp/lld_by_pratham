package carrentalsystem;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;

public class Station {
    private final String name;
    private final Location location;
    final Map<CarType, Double> prices = new ConcurrentHashMap<>();
    final Map<CarType, ConcurrentLinkedDeque<Car>> availableCars = new ConcurrentHashMap<>();
    final Map<CarType, AtomicInteger> availableCounts = new ConcurrentHashMap<>();
    final CarRentalSystem system;

    public Station(String name, Location location, CarRentalSystem system) {
        this.name = name;
        this.location = location;
        this.system = system;
    }

    public void setPrice(CarType type, double price) {
        prices.put(type, price);
    }

    public double getPrice(CarType type) {
        return prices.getOrDefault(type, Double.MAX_VALUE);
    }

    public void addCar(Car car) {
        CarType type = car.getType();
        availableCars.computeIfAbsent(type, _ -> new ConcurrentLinkedDeque<>()).add(car);
        availableCounts.compute(type, (_, count) -> {
            if (count == null) {
                system.addStationToIndex(this, type);
                return new AtomicInteger(1);
            } else {
                count.incrementAndGet();
                return count;
            }
        });
    }

    public void removeCar(Car car) {
        CarType type = car.getType();
        if (availableCars.getOrDefault(type, new ConcurrentLinkedDeque<>()).remove(car)) {
            availableCounts.computeIfPresent(type, (_, count) -> {
                if (count.decrementAndGet() == 0) {
                    system.removeStationFromIndex(this, type);
                }
                return count;
            });
        }
    }

    public Location getLocation() { return location; }
    public String getName() { return name; }
}
