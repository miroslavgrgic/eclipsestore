package hr.ogcs.eclipsestore.hotel.service;

import hr.ogcs.eclipsestore.hotel.model.Booking;
import hr.ogcs.eclipsestore.hotel.model.BookingDto;
import hr.ogcs.eclipsestore.hotel.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final GuestService guestService;
    private final RoomService roomService;

    public BookingService(BookingRepository bookingRepository, GuestService guestService, RoomService roomService) {
        this.bookingRepository = bookingRepository;
        this.guestService = guestService;
        this.roomService = roomService;
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking createBooking(BookingDto bookingDto) {
        // check if room exists
        roomService.findById(bookingDto.getRoom().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room with ID " + bookingDto.getRoom().getId() + " does not exist"));

        // TODO check if guests already exist
        bookingDto.getGuests().stream().forEach(g -> g.setId(UUID.randomUUID()));

        Booking booking = new Booking();
        booking.setGuests(bookingDto.getGuests());
        booking.setRoom(bookingDto.getRoom());
        booking.setDate(bookingDto.getDate());
        var savedBooking = bookingRepository.save(booking);

        log.info("Created booking: {}", savedBooking);
        return savedBooking;
    }

    public void deleteBookingByID(String bookingID) {
        bookingRepository.deleteById(UUID.fromString(bookingID));
    }

}
