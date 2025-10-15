//lab4 by Maksym Lomakin
import java.util.*;

// --- Passengers hierarchy ---
class Human {
    String name;
    public Human(String name) { this.name = name; }
    public String toString() { return name; }
}

class Fireman extends Human {
    public Fireman(String name) { super(name); }
}

class Policeman extends Human {
    public Policeman(String name) { super(name); }
}

// --- Vehicles hierarchy ---
class Vehicle<T extends Human> {
    private int maxSeats;
    private List<T> passengers = new ArrayList<>();

    public Vehicle(int maxSeats) { this.maxSeats = maxSeats; }

    public int getMaxSeats() { return maxSeats; }
    public int getOccupiedSeats() { return passengers.size(); }

    public void boardPassenger(T passenger) {
        if (passengers.size() >= maxSeats)
            throw new IllegalStateException("No free seats!");
        passengers.add(passenger);
    }

    public void dropPassenger(T passenger) {
        if (!passengers.remove(passenger))
            throw new IllegalArgumentException("Passenger not found!");
    }

    public List<T> getPassengers() { return passengers; }
}

// Bus and Taxi can carry any Human
class Bus extends Vehicle<Human> {
    public Bus(int maxSeats) { super(maxSeats); }
}

class Taxi extends Vehicle<Human> {
    public Taxi(int maxSeats) { super(maxSeats); }
}

// FireTruck only Fireman
class FireTruck extends Vehicle<Fireman> {
    public FireTruck(int maxSeats) { super(maxSeats); }
}

// PoliceCar only Policeman
class PoliceCar extends Vehicle<Policeman> {
    public PoliceCar(int maxSeats) { super(maxSeats); }
}

// --- Road class ---
class Road {
    public List<Vehicle<? extends Human>> carsInRoad = new ArrayList<>();

    public int getCountOfHumans() {
        int count = 0;
        for (Vehicle<? extends Human> v : carsInRoad) {
            count += v.getOccupiedSeats();
        }
        return count;
    }

    public void addCarToRoad(Vehicle<? extends Human> car) {
        carsInRoad.add(car);
    }
}

// --- Animals hierarchy ---
abstract class Animal {
    String name;
    public Animal(String name) { this.name = name; }
    public String toString() { return name; }
}

abstract class Mammal extends Animal {
    public Mammal(String name) { super(name); }
}

abstract class Bird extends Animal {
    public Bird(String name) { super(name); }
}

class Lion extends Mammal {
    public Lion(String name) { super(name); }
}

class Zebra extends Mammal {
    public Zebra(String name) { super(name); }
}

class Giraffe extends Mammal {
    public Giraffe(String name) { super(name); }
}

class Eagle extends Bird {
    public Eagle(String name) { super(name); }
}

// --- Cages hierarchy ---
class Cage<T extends Animal> {
    private int maxCapacity;
    private List<T> animals = new ArrayList<>();

    public Cage(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public int getMaxCapacity() { return maxCapacity; }
    public int getOccupiedPlaces() { return animals.size(); }

    public void addAnimal(T animal) {
        if (animals.size() >= maxCapacity)
            throw new IllegalStateException("No free places in cage!");
        animals.add(animal);
    }

    public void removeAnimal(T animal) {
        if (!animals.remove(animal))
            throw new IllegalArgumentException("Animal not found in cage!");
    }

    public List<T> getAnimals() { return animals; }
}

// Lion cage: only lions
class LionCage extends Cage<Lion> {
    public LionCage(int maxCapacity) { super(maxCapacity); }
}

// Bird cage: only birds
class BirdCage extends Cage<Bird> {
    public BirdCage(int maxCapacity) { super(maxCapacity); }
}

// Hoofed cage: only zebras and giraffes
class HoofedCage extends Cage<Mammal> {
    public HoofedCage(int maxCapacity) { super(maxCapacity); }
}

// --- Zoo class ---
class Zoo {
    public List<Cage<? extends Animal>> cages = new ArrayList<>();

    public int getCountOfAnimals() {
        int count = 0;
        for (Cage<? extends Animal> cage : cages) {
            count += cage.getOccupiedPlaces();
        }
        return count;
    }

    public void addCage(Cage<? extends Animal> cage) {
        cages.add(cage);
    }
}

// --- Main class with task selection ---
class lab4 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Оберіть завдання: 1 - транспорт, 2 - зоопарк");
        String choice = sc.nextLine().trim();

        if (choice.equals("1")) {
            // --- Task 4.1: Transport ---
            Bus bus = new Bus(2);
            Taxi taxi = new Taxi(2);
            FireTruck fireTruck = new FireTruck(2);
            PoliceCar policeCar = new PoliceCar(2);

            Human h1 = new Human("Ivan");
            Fireman f1 = new Fireman("Petro");
            Policeman p1 = new Policeman("Oleh");

            bus.boardPassenger(h1);
            bus.boardPassenger(f1);

            taxi.boardPassenger(h1);
            taxi.boardPassenger(p1);

            fireTruck.boardPassenger(f1);

            policeCar.boardPassenger(p1);

            Road road = new Road();
            road.addCarToRoad(bus);
            road.addCarToRoad(taxi);
            road.addCarToRoad(fireTruck);
            road.addCarToRoad(policeCar);

            System.out.println("Total humans on road: " + road.getCountOfHumans());

            // Test exceptions
            try {
                bus.boardPassenger(new Human("Extra"));
            } catch (Exception e) {
                System.out.println("Expected: " + e.getMessage());
            }
            try {
                bus.dropPassenger(new Human("Ghost"));
            } catch (Exception e) {
                System.out.println("Expected: " + e.getMessage());
            }
        } else if (choice.equals("2")) {
            // --- Task 4.2: Zoo ---
            LionCage lionCage = new LionCage(2);
            BirdCage birdCage = new BirdCage(2);
            HoofedCage hoofedCage = new HoofedCage(2);

            Lion l1 = new Lion("Simba");
            Lion l2 = new Lion("Mufasa");
            Eagle e1 = new Eagle("Freedom");
            Zebra z1 = new Zebra("Marty");
            Giraffe g1 = new Giraffe("Melman");

            lionCage.addAnimal(l1);
            lionCage.addAnimal(l2);

            birdCage.addAnimal(e1);

            hoofedCage.addAnimal(z1);
            hoofedCage.addAnimal(g1);

            Zoo zoo = new Zoo();
            zoo.addCage(lionCage);
            zoo.addCage(birdCage);
            zoo.addCage(hoofedCage);

            System.out.println("Total animals in zoo: " + zoo.getCountOfAnimals());

            // Test exceptions
            try {
                lionCage.addAnimal(new Lion("Scar"));
            } catch (Exception e) {
                System.out.println("Expected: " + e.getMessage());
            }
            try {
                birdCage.removeAnimal(new Eagle("Ghost"));
            } catch (Exception e) {
                System.out.println("Expected: " + e.getMessage());
            }
        } else {
            System.out.println("Невірний вибір.");
        }
    }
}
