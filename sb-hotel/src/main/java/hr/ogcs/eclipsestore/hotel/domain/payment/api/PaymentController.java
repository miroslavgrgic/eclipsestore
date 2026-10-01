package hr.ogcs.eclipsestore.hotel.domain.payment.api;

import hr.ogcs.eclipsestore.hotel.domain.payment.model.Payment;
import hr.ogcs.eclipsestore.hotel.domain.payment.incoming.PaymentPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping(value = "/payments")
public class PaymentController {

    private final PaymentPort paymentPort;

    public PaymentController(PaymentPort paymentPort) {
        this.paymentPort = paymentPort;
    }

    @PostMapping
    public ResponseEntity<UUID> createPayment(@RequestBody Payment payment) {
        UUID id = paymentPort.createPayment(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping
    public Map<UUID, Payment> getAllPayments() {
        return paymentPort.getAllPayments();
    }

    @GetMapping(path = "/{id}")
    public Payment getPaymentById(@PathVariable("id") UUID id) {
        return paymentPort.getAllPayments().get(id);
    }

}
