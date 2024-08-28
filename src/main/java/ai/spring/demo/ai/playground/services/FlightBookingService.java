package ai.spring.demo.ai.playground.services;

import ai.spring.demo.ai.playground.data.*;
import ai.spring.demo.ai.playground.services.BookingTools.BookingDetails;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class FlightBookingService {

    private final BookingData db;

    public FlightBookingService() {
        db = new BookingData();

        initDemoData();
    }

    private void initDemoData() {
        // Indian names (Male and Female)
        List<String> indianMaleFirstNames = List.of("Raj", "Arjun", "Vijay", "Manoj", "Kumar");
        List<String> indianFemaleFirstNames = List.of("Priya", "Anjali", "Latha", "Deepa", "Radha");
        List<String> indianLastNames = List.of("Menon", "Ravi", "Krishnan", "Pillai", "Iyer");

        // Malay names (Male and Female)
        List<String> malayMaleFirstNames = List.of("Ahmad", "Mohd", "Zainal", "Azman", "Rosli");
        List<String> malayFemaleFirstNames = List.of("Siti", "Aisyah", "Nurul", "Fatimah", "Zarina");
        List<String> malayLastNames = List.of("Ismail", "Rahman", "Hassan", "Abdullah", "Yusof");

        // Chinese names (Male and Female)
        List<String> chineseMaleFirstNames = List.of("Wei", "Yong", "Liang", "Jun", "Feng");
        List<String> chineseFemaleFirstNames = List.of("Mei", "Fang", "Xiu", "Ling", "Hua");
        List<String> chineseLastNames = List.of("Lim", "Tan", "Wong", "Lee", "Chong");

        // Foreign names (Male and Female)
        List<String> foreignMaleFirstNames = List.of("John", "Michael", "Robert", "James", "David");
        List<String> foreignFemaleFirstNames = List.of("Jane", "Sarah", "Emily", "Anna", "Laura");
        List<String> foreignLastNames = List.of("Doe", "Smith", "Johnson", "Williams", "Taylor");

        // Domestic Malaysian airport codes
        List<String> airportCodes = List.of(
                "KUL", "SZB", "PEN", "JHB", "LGK", "BKI", "KCH", "TGG", "KUA", "AOR",
                "SDK", "LDU", "MYY", "BTU", "TWU"
        );

        Random random = new Random();

        var customers = new ArrayList<Customer>();
        var bookings = new ArrayList<Booking>();

        for (int i = 0; i < 10; i++) {
            String firstName;
            String lastName;

            if (i % 4 == 0) { // Indian Names
                if (i % 2 == 0) {
                    firstName = indianMaleFirstNames.get(random.nextInt(indianMaleFirstNames.size()));
                } else {
                    firstName = indianFemaleFirstNames.get(random.nextInt(indianFemaleFirstNames.size()));
                }
                lastName = indianLastNames.get(random.nextInt(indianLastNames.size()));
            } else if (i % 4 == 1) { // Malay Names
                if (i % 2 == 0) {
                    firstName = malayMaleFirstNames.get(random.nextInt(malayMaleFirstNames.size()));
                } else {
                    firstName = malayFemaleFirstNames.get(random.nextInt(malayFemaleFirstNames.size()));
                }
                lastName = malayLastNames.get(random.nextInt(malayLastNames.size()));
            } else if (i % 4 == 2) { // Chinese Names
                if (i % 2 == 0) {
                    firstName = chineseMaleFirstNames.get(random.nextInt(chineseMaleFirstNames.size()));
                } else {
                    firstName = chineseFemaleFirstNames.get(random.nextInt(chineseFemaleFirstNames.size()));
                }
                lastName = chineseLastNames.get(random.nextInt(chineseLastNames.size()));
            } else { // Foreign Names
                if (i % 2 == 0) {
                    firstName = foreignMaleFirstNames.get(random.nextInt(foreignMaleFirstNames.size()));
                } else {
                    firstName = foreignFemaleFirstNames.get(random.nextInt(foreignFemaleFirstNames.size()));
                }
                lastName = foreignLastNames.get(random.nextInt(foreignLastNames.size()));
            }

            // Generate random airport codes for 'from' and 'to'
            String from = airportCodes.get(random.nextInt(airportCodes.size()));
            String to = airportCodes.get(random.nextInt(airportCodes.size()));
            BookingClass bookingClass = BookingClass.values()[random.nextInt(BookingClass.values().length)];

            // Create customer and booking
            Customer customer = new Customer();
            customer.setFirstName(firstName);
            customer.setLastName(lastName);

            LocalDate date = LocalDate.now().plusDays(2 * i);
            Booking booking = new Booking("FUN-" + (i + 1), date, customer, BookingStatus.CONFIRMED, from, to, bookingClass);
            customer.getBookings().add(booking);

            customers.add(customer);
            bookings.add(booking);
        }

        // Reset the database on each start
        db.setCustomers(customers);
        db.setBookings(bookings);
    }

    public List<BookingDetails> getBookings() {
        return db.getBookings().stream().map(this::toBookingDetails).toList();
    }

    private Booking findBooking(String bookingNumber, String firstName, String lastName) {
        return db.getBookings().stream()
                .filter(b -> b.getBookingNumber().equalsIgnoreCase(bookingNumber))
                .filter(b -> b.getCustomer().getFirstName().equalsIgnoreCase(firstName))
                .filter(b -> b.getCustomer().getLastName().equalsIgnoreCase(lastName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));
    }

    public BookingDetails getBookingDetails(String bookingNumber, String firstName, String lastName) {
        var booking = findBooking(bookingNumber, firstName, lastName);
        return toBookingDetails(booking);
    }

    public void changeBooking(String bookingNumber, String firstName, String lastName, String newDate, String from, String to) {
        var booking = findBooking(bookingNumber, firstName, lastName);
        if(booking.getDate().isBefore(LocalDate.now().plusDays(1))){
            throw new IllegalArgumentException("Booking cannot be changed within 24 hours of the start date.");
        }
        booking.setDate(LocalDate.parse(newDate));
        booking.setFrom(from);
        booking.setTo(to);
    }

    public void cancelBooking(String bookingNumber, String firstName, String lastName) {
        var booking = findBooking(bookingNumber, firstName, lastName);
        if (booking.getDate().isBefore(LocalDate.now().plusDays(2))) {
            throw new IllegalArgumentException("Booking cannot be cancelled within 48 hours of the start date.");
        }
        booking.setBookingStatus(BookingStatus.CANCELLED);
    }

    private BookingDetails toBookingDetails(Booking booking){
        return new BookingDetails(
                booking.getBookingNumber(),
                booking.getCustomer().getFirstName(),
                booking.getCustomer().getLastName(),
                booking.getDate(),
                booking.getBookingStatus(),
                booking.getFrom(),
                booking.getTo(),
                booking.getBookingClass().toString()
        );
    }
}
