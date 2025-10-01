// lab3.java by Lomakin Maksym 01.10.2025
import java.util.*;


interface Drawable {
    void draw();
}

abstract class Shape implements Drawable {
    protected String shapeColor;

    public Shape(String shapeColor) {
        this.shapeColor = shapeColor;
    }

    public abstract double calcArea();

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [color=" + shapeColor + ", area=" + calcArea() + "]";
    }
}

class Rectangle extends Shape {
    private double width, height;

    public Rectangle(String color, double width, double height) {
        super(color);
        this.width = width;
        this.height = height;
    }

    @Override
    public double calcArea() {
        return width * height;
    }

    @Override
    public void draw() {
        System.out.println("Drawing Rectangle");
    }

    @Override
    public String toString() {
        return super.toString() + " [width=" + width + ", height=" + height + "]";
    }
}

class Triangle extends Shape {
    private double base, height;

    public Triangle(String color, double base, double height) {
        super(color);
        this.base = base;
        this.height = height;
    }

    @Override
    public double calcArea() {
        return 0.5 * base * height;
    }

    @Override
    public void draw() {
        System.out.println("Drawing Triangle");
    }

    @Override
    public String toString() {
        return super.toString() + " [base=" + base + ", height=" + height + "]";
    }
}

class Circle extends Shape {
    private double radius;

    public Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    public double calcArea() {
        return Math.PI * radius * radius;
    }

    @Override
    public void draw() {
        System.out.println("Drawing Circle");
    }

    @Override
    public String toString() {
        return super.toString() + " [radius=" + radius + "]";
    }
}

class ShapeModel {
    private Shape[] shapes;

    public ShapeModel(Shape[] shapes) {
        this.shapes = shapes;
    }

    public Shape[] getShapes() {
        return shapes;
    }

    public double totalArea() {
        double sum = 0;
        for (Shape s : shapes) sum += s.calcArea();
        return sum;
    }

    public double totalAreaByType(Class<?> type) {
        double sum = 0;
        for (Shape s : shapes) {
            if (type.isInstance(s)) sum += s.calcArea();
        }
        return sum;
    }

    public void sortByArea() {
        Arrays.sort(shapes, Comparator.comparingDouble(Shape::calcArea));
    }

    public void sortByColor() {
        Arrays.sort(shapes, Comparator.comparing(s -> s.shapeColor));
    }
}

// --- biew ---

class ShapeView {
    public void displayShapes(Shape[] shapes) {
        for (Shape s : shapes) {
            System.out.println(s);
        }
    }

    public void displayTotalArea(double area) {
        System.out.println("Total area: " + area);
    }

    public void displayTotalAreaByType(double area, String type) {
        System.out.println("Total area for " + type + ": " + area);
    }
}

// --- controller ---

class ShapeController {
    private ShapeModel model;
    private ShapeView view;

    public ShapeController(ShapeModel model, ShapeView view) {
        this.model = model;
        this.view = view;
    }

    public void showAllShapes() {
        view.displayShapes(model.getShapes());
    }

    public void showTotalArea() {
        view.displayTotalArea(model.totalArea());
    }

    public void showTotalAreaByType(Class<?> type) {
        double area = model.totalAreaByType(type);
        view.displayTotalAreaByType(area, type.getSimpleName());
    }

    public void sortShapesByArea() {
        model.sortByArea();
    }

    public void sortShapesByColor() {
        model.sortByColor();
    }
}

// --- 3.4 task model ---

enum CardType { PUPIL, STUDENT, REGULAR }
enum CardTerm { MONTH, TEN_DAYS, NONE }
enum CardTrips { FIVE, TEN, NONE }
enum CardKind { TERM, TRIPS, ACCUMULATIVE }

abstract class Card {
    protected String id;
    protected CardType type;
    protected CardKind kind;

    public Card(String id, CardType type, CardKind kind) {
        this.id = id;
        this.type = type;
        this.kind = kind;
    }

    public String getId() { return id; }
    public CardType getType() { return type; }
    public CardKind getKind() { return kind; }

    public abstract boolean canPass();
    public abstract void use();
    public abstract String info();
}

class TermCard extends Card {
    private CardTerm term;
    private Date validUntil;

    public TermCard(String id, CardType type, CardTerm term, Date validUntil) {
        super(id, type, CardKind.TERM);
        this.term = term;
        this.validUntil = validUntil;
    }

    @Override
    public boolean canPass() {
        return new Date().before(validUntil);
    }

    @Override
    public void use() {
        // just check term
    }

    @Override
    public String info() {
        return String.format("TermCard[%s,%s,validUntil=%s]", type, term, validUntil);
    }
}

class TripsCard extends Card {
    private CardTrips tripsType;
    private int tripsLeft;

    public TripsCard(String id, CardType type, CardTrips tripsType, int tripsLeft) {
        super(id, type, CardKind.TRIPS);
        this.tripsType = tripsType;
        this.tripsLeft = tripsLeft;
    }

    @Override
    public boolean canPass() {
        return tripsLeft > 0;
    }

    @Override
    public void use() {
        if (tripsLeft > 0) tripsLeft--;
    }

    @Override
    public String info() {
        return String.format("TripsCard[%s,%s,tripsLeft=%d]", type, tripsType, tripsLeft);
    }
}

class AccumulativeCard extends Card {
    private double balance;
    private static final double FARE = 8.0;

    public AccumulativeCard(String id, double balance) {
        super(id, CardType.REGULAR, CardKind.ACCUMULATIVE);
        this.balance = balance;
    }

    @Override
    public boolean canPass() {
        return balance >= FARE;
    }

    @Override
    public void use() {
        if (balance >= FARE) balance -= FARE;
    }

    public void topUp(double amount) {
        balance += amount;
    }

    @Override
    public String info() {
        return String.format("AccumulativeCard[balance=%.2f]", balance);
    }
}

// for issued cards
class CardRegistry {
    private Map<String, Card> cards = new HashMap<>();

    public void issueCard(Card card) {
        cards.put(card.getId(), card);
    }

    public Card getCard(String id) {
        return cards.get(id);
    }

    public Collection<Card> getAllCards() {
        return cards.values();
    }
}

// turnstile statistics
class TurnstileStats {
    private int allowed = 0, denied = 0;
    private Map<CardType, Integer> allowedByType = new HashMap<>();
    private Map<CardType, Integer> deniedByType = new HashMap<>();

    public void record(Card card, boolean allowedPass) {
        CardType type = card.getType();
        if (allowedPass) {
            allowed++;
            allowedByType.put(type, allowedByType.getOrDefault(type, 0) + 1);
        } else {
            denied++;
            deniedByType.put(type, deniedByType.getOrDefault(type, 0) + 1);
        }
    }

    public void printStats() {
        System.out.println("Allowed: " + allowed + ", Denied: " + denied);
    }

    public void printStatsByType() {
        System.out.println("Allowed by type: " + allowedByType);
        System.out.println("Denied by type: " + deniedByType);
    }
}

// turnstile logic
class Turnstile {
    private CardRegistry registry;
    private TurnstileStats stats;

    public Turnstile(CardRegistry registry, TurnstileStats stats) {
        this.registry = registry;
        this.stats = stats;
    }

    public boolean tryPass(String cardId) {
        Card card = registry.getCard(cardId);
        if (card == null) {
            System.out.println("Card not found.");
            return false;
        }
        boolean canPass = card.canPass();
        if (canPass) card.use();
        stats.record(card, canPass);
        System.out.println(canPass ? "Access granted." : "Access denied.");
        return canPass;
    }
}

// --- 3.1 logic as method ---
public class lab3 {
    public static void runTask31() {
        String[] colors = {"Red", "Green", "Blue", "Yellow", "Black"};
        Random rand = new Random();

        Shape[] shapes = new Shape[10];
        for (int i = 0; i < shapes.length; i++) {
            int type = rand.nextInt(3);
            String color = colors[rand.nextInt(colors.length)];
            switch (type) {
                case 0:
                    shapes[i] = new Rectangle(color, rand.nextDouble() * 10 + 1, rand.nextDouble() * 10 + 1);
                    break;
                case 1:
                    shapes[i] = new Triangle(color, rand.nextDouble() * 10 + 1, rand.nextDouble() * 10 + 1);
                    break;
                case 2:
                    shapes[i] = new Circle(color, rand.nextDouble() * 10 + 1);
                    break;
            }
        }

        ShapeModel model = new ShapeModel(shapes);
        ShapeView view = new ShapeView();
        ShapeController controller = new ShapeController(model, view);

        System.out.println("Original data:");
        controller.showAllShapes();

        controller.showTotalArea();
        controller.showTotalAreaByType(Rectangle.class);
        controller.showTotalAreaByType(Triangle.class);
        controller.showTotalAreaByType(Circle.class);

        System.out.println("\nSorted by area:");
        controller.sortShapesByArea();
        controller.showAllShapes();

        System.out.println("\nSorted by color:");
        controller.sortShapesByColor();
        controller.showAllShapes();
    }

    // --- 3.4 logic as method ---
    public static void runTask34(Scanner scanner) {
        CardRegistry registry = new CardRegistry();
        TurnstileStats stats = new TurnstileStats();
        Turnstile turnstile = new Turnstile(registry, stats);

        // deno: issue some cards
        registry.issueCard(new TermCard("T1", CardType.PUPIL, CardTerm.MONTH, addDays(new Date(), 30)));
        registry.issueCard(new TermCard("T2", CardType.STUDENT, CardTerm.TEN_DAYS, addDays(new Date(), 10)));
        registry.issueCard(new TripsCard("TR1", CardType.REGULAR, CardTrips.FIVE, 5));
        registry.issueCard(new TripsCard("TR2", CardType.STUDENT, CardTrips.TEN, 10));
        registry.issueCard(new AccumulativeCard("A1", 50.0));

        // harcdoded denied cards ---
        // expired TermCard
        registry.issueCard(new TermCard("DT1", CardType.PUPIL, CardTerm.MONTH, addDays(new Date(), -1)));
        // tripsCard with zero trips
        registry.issueCard(new TripsCard("DT2", CardType.REGULAR, CardTrips.FIVE, 0));
        // accumulativeCard with insufficient balance
        registry.issueCard(new AccumulativeCard("DA1", 2.0));

        while (true) {
            System.out.println("\n1 - Try pass\n2 - Issue card\n3 - Show stats\n4 - Show stats by type\n0 - Exit");
            int choice = scanner.nextInt();
            scanner.nextLine();
            if (choice == 0) break;
            switch (choice) {
                case 1:
                    System.out.print("Enter card id: ");
                    String id = scanner.nextLine();
                    turnstile.tryPass(id);
                    break;
                case 2:
                    System.out.print("Type (1-Pupil,2-Student,3-Regular): ");
                    int t = scanner.nextInt();
                    scanner.nextLine();
                    CardType type = CardType.values()[t-1];
                    System.out.print("Kind (1-Term,2-Trips,3-Accumulative): ");
                    int k = scanner.nextInt();
                    scanner.nextLine();
                    CardKind kind = CardKind.values()[k-1];
                    String newId = UUID.randomUUID().toString().substring(0, 6);
                    Card card = null;
                    if (kind == CardKind.TERM) {
                        System.out.print("Term (1-Month,2-10days): ");
                        int term = scanner.nextInt();
                        scanner.nextLine();
                        CardTerm cardTerm = term == 1 ? CardTerm.MONTH : CardTerm.TEN_DAYS;
                        int days = cardTerm == CardTerm.MONTH ? 30 : 10;
                        card = new TermCard(newId, type, cardTerm, addDays(new Date(), days));
                    } else if (kind == CardKind.TRIPS) {
                        System.out.print("Trips (1-5,2-10): ");
                        int trips = scanner.nextInt();
                        scanner.nextLine();
                        CardTrips cardTrips = trips == 1 ? CardTrips.FIVE : CardTrips.TEN;
                        int tripsCount = trips == 1 ? 5 : 10;
                        card = new TripsCard(newId, type, cardTrips, tripsCount);
                    } else if (kind == CardKind.ACCUMULATIVE) {
                        if (type != CardType.REGULAR) {
                            System.out.println("Only regular cards can be accumulative.");
                            break;
                        }
                        System.out.print("Initial balance: ");
                        double bal = scanner.nextDouble();
                        scanner.nextLine();
                        card = new AccumulativeCard(newId, bal);
                    }
                    if (card != null) {
                        registry.issueCard(card);
                        System.out.println("Issued card: " + card.info() + ", id=" + card.getId());
                    }
                    break;
                case 3:
                    stats.printStats();
                    break;
                case 4:
                    stats.printStatsByType();
                    break;
            }
        }
    }

    private static Date addDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    //  main 
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Select task: 1 - Shapes (3.1), 2 - Turnstile (3.4)");
        int task = scanner.nextInt();
        scanner.nextLine();
        if (task == 1) {
            runTask31();
        } else if (task == 2) {
            runTask34(scanner);
        }
    }
}
