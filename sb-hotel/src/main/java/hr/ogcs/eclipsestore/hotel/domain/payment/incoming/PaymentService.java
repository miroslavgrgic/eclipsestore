package hr.ogcs.eclipsestore.hotel.domain.payment.incoming;

import hr.ogcs.eclipsestore.hotel.domain.booking.Booking;
import hr.ogcs.eclipsestore.hotel.domain.payment.model.Payment;
import hr.ogcs.eclipsestore.hotel.repository.StorageService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {

    private final StorageService storageService;

    public PaymentService(StorageService storageService) {
        this.storageService = storageService;
    }

    public void auditPayment(Booking booking) {
        Payment payment = new Payment(booking, "DummyProviderId", new Date());
        storageService.hotel.getPayments().put(UUID.randomUUID(), payment);
    }

    public Map<UUID, Payment> getAllPayments() {
        return storageService.hotel.getPayments();
    }
}
