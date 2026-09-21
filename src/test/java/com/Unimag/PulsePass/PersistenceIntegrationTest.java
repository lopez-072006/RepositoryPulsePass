package com.Unimag.PulsePass;

import com.Unimag.PulsePass.domain.*;
import com.Unimag.PulsePass.domain.enums.*;
import com.Unimag.PulsePass.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("pulsepass_test")
            .withUsername("pulsepass")
            .withPassword("pulsepass");

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void verifyFlywayMigrationsRunSuccessfully() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true", Integer.class);
        assertThat(count).isNotNull().isGreaterThanOrEqualTo(2);
    }

    @Test
    void givenValidVenue_whenSave_thenCanBeRetrievedByCode() {
        Venue venue = new Venue();
        venue.setCode("VEN-SMR-01");
        venue.setName("Marina Convention Center");
        venue.setCity("Santa Marta");
        venue.setAddress("Carrera 1a");
        venue.setCapacity(5000);
        venue.setActive(true);

        Venue savedVenue = venueRepository.save(venue);
        assertThat(savedVenue.getId()).isNotNull();

        Optional<Venue> foundVenue = venueRepository.findByCode("VEN-SMR-01");
        assertThat(foundVenue).isPresent();
        assertThat(foundVenue.get().getName()).isEqualTo("Marina Convention Center");
    }

    @Test
    void givenDuplicateVenueCode_whenSaveAndFlush_thenThrowsException() {
        Venue venue1 = new Venue();
        venue1.setCode("VEN-DUP");
        venue1.setName("Venue Original");
        venue1.setCity("Ciudad");
        venue1.setCapacity(100);
        venue1.setActive(true);
        venueRepository.saveAndFlush(venue1);

        Venue venue2 = new Venue();
        venue2.setCode("VEN-DUP");
        venue2.setName("Venue Duplicado");
        venue2.setCity("Ciudad");
        venue2.setCapacity(200);
        venue2.setActive(true);

        assertThrows(DataIntegrityViolationException.class, () -> {
            venueRepository.saveAndFlush(venue2);
        });
    }

    @Test
    void givenInvalidCapacity_whenSaveAndFlush_thenThrowsExceptionDueToDatabaseCheck() {
        Venue venue = new Venue();
        venue.setCode("VEN-NEG");
        venue.setName("Venue Capacidad Invalida");
        venue.setCity("Ciudad");
        venue.setCapacity(-10);
        venue.setActive(true);

        assertThrows(DataIntegrityViolationException.class, () -> {
            venueRepository.saveAndFlush(venue);
        });
    }

    @Test
    void givenScenarioData_whenExecuted_thenVerifiesAllPersistenceLayerRules() {
        Venue venue = new Venue();
        venue.setCode("VEN-CORE-01");
        venue.setName("Estadio Principal");
        venue.setCity("Santa Marta");
        venue.setCapacity(20000);
        venue.setActive(true);
        venueRepository.save(venue);

        Event event = new Event();
        event.setEventCode("EVT-CORE-01");
        event.setName("Festival Musical Latino");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(30));
        event.setMinimumAge(18);
        event.setVenue(venue);
        eventRepository.save(event);

        User user = new User();
        user.setUsername("andreapulse");
        user.setEmail("andrea@pulsepass.com");
        user.setActive(true);
        userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setFirstName("Andrea");
        profile.setLastName("Gomez");
        profile.setBirthDate(LocalDate.of(1998, 5, 15));
        profile.setUser(user);
        userProfileRepository.save(profile);

        Ticket ticket = new Ticket();
        ticket.setTicketCode("TCK-000001");
        ticket.setType(TicketType.VIP);
        ticket.setPrice(new BigDecimal("250000.00"));
        ticket.setStatus(TicketStatus.PAID);
        ticket.setPurchaseDate(LocalDateTime.now());
        ticket.setUser(user);
        ticket.setEvent(event);
        ticketRepository.save(ticket);

        assertThat(ticketRepository.count()).isOne();
        List<Ticket> paidTickets = ticketRepository.findByEventEventCodeAndStatus(激"EVT-CORE-01", TicketStatus.PAID);
        assertThat(paidTickets).hasSize(1);
    }
}