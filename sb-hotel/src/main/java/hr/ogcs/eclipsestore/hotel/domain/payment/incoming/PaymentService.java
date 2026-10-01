package hr.ogcs.eclipsestore.hotel.domain.payment.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.payment.model.Payment;
import hr.ogcs.eclipsestore.hotel.repository.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    private final StorageService storageService;

    public PaymentService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void auditPayment(Booking booking) {
        Payment payment = Payment.builder()
                .booking(booking)
                .paymentProviderId("DummyProviderId")
                .paymentDate(new Date())
                .build();
        storageService.hotel.getPayments().put(UUID.randomUUID(), payment);
    }

    public Map<UUID, Payment> getAllPayments() {
        return storageService.hotel.getPayments();
    }

    public UUID createPayment(Payment payment) {
        if (payment == null || payment.getBooking() == null) {
            throw new IllegalArgumentException("Invalid payment");
        }

        UUID id = UUID.randomUUID();
        storageService.hotel.getPayments().put(id, payment);
        storageService.store(storageService.hotel.getPayments());
        log.info("Created Payment with ID {}", id);
        return id;
    }
}
