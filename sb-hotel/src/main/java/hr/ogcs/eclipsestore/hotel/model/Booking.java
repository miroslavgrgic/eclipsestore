package hr.ogcs.eclipsestore.hotel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "booking")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @OneToMany
    @JoinColumn(name = "guest_id", referencedColumnName = "id")
    List<Guest> guests;

    @OneToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id")
    Room room;

    private LocalDate date;

}
