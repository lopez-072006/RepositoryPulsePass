package com.Unimag.PulsePass;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.Unimag.PulsePass.domain.Artist;
import com.Unimag.PulsePass.domain.Event;
import com.Unimag.PulsePass.domain.Ticket;
import com.Unimag.PulsePass.domain.User;
import com.Unimag.PulsePass.domain.UserProfile;
import com.Unimag.PulsePass.domain.Venue;
import com.Unimag.PulsePass.domain.enums.EventCategory;
import com.Unimag.PulsePass.domain.enums.EventStatus;
import com.Unimag.PulsePass.domain.enums.TicketStatus;
import com.Unimag.PulsePass.domain.enums.TicketType;
import com.Unimag.PulsePass.repository.ArtistRepository;
import com.Unimag.PulsePass.repository.EventRepository;
import com.Unimag.PulsePass.repository.TicketRepository;
import com.Unimag.PulsePass.repository.UserProfileRepository;
import com.Unimag.PulsePass.repository.UserRepository;
import com.Unimag.PulsePass.repository.VenueRepository;

@Testcontainers
@SpringBootTest
@Transactional
class PersistenceIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18-alpine")
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
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true",
                Integer.class
        );

        assertThat(count)
                .isNotNull()
                .isGreaterThanOrEqualTo(3);
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

        Optional<Venue> foundVenue =
                venueRepository.findByCode("VEN-SMR-01");

        assertThat(foundVenue).isPresent();

        assertThat(foundVenue.get().getName())
                .isEqualTo("Marina Convention Center");
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

        assertThrows(
                DataIntegrityViolationException.class,
                () -> venueRepository.saveAndFlush(venue2)
        );
    }


    @Test
    void givenInvalidCapacity_whenSaveAndFlush_thenThrowsExceptionDueToDatabaseCheck() {

        Venue venue = new Venue();

        venue.setCode("VEN-NEG");
        venue.setName("Venue Capacidad Invalida");
        venue.setCity("Ciudad");
        venue.setCapacity(-10);
        venue.setActive(true);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> venueRepository.saveAndFlush(venue)
        );
    }


   

    @Test
    void givenEventCode_whenFindByEventCode_thenReturnsEvent() {

        Venue venue = new Venue();

        venue.setCode("VEN-CODE-01");
        venue.setName("Venue Code Test");
        venue.setCity("Santa Marta");
        venue.setCapacity(1000);
        venue.setActive(true);

        venueRepository.save(venue);


        Event event = new Event();

        event.setEventCode("EVT-CODE-01");
        event.setName("Evento Code Test");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(10));
        event.setMinimumAge(18);
        event.setVenue(venue);

        eventRepository.save(event);


        Optional<Event> result =
                eventRepository.findByEventCode("EVT-CODE-01");


        assertThat(result).isPresent();

        assertThat(result.get().getName())
                .isEqualTo("Evento Code Test");
    }


    

    @Test
    void givenPublishedEvents_whenFindByStatusOrderByEventDate_thenReturnsOrderedEvents() {

        Venue venue = new Venue();

        venue.setCode("VEN-STATUS-01");
        venue.setName("Status Venue");
        venue.setCity("Santa Marta");
        venue.setCapacity(1000);
        venue.setActive(true);

        venueRepository.save(venue);


        Event laterEvent = new Event();

        laterEvent.setEventCode("EVT-STATUS-02");
        laterEvent.setName("Evento Publicado Tarde");
        laterEvent.setCategory(EventCategory.MUSIC);
        laterEvent.setStatus(EventStatus.PUBLISHED);
        laterEvent.setEventDate(LocalDateTime.now().plusDays(20));
        laterEvent.setMinimumAge(18);
        laterEvent.setVenue(venue);

        eventRepository.save(laterEvent);


        Event soonerEvent = new Event();

        soonerEvent.setEventCode("EVT-STATUS-01");
        soonerEvent.setName("Evento Publicado Pronto");
        soonerEvent.setCategory(EventCategory.MUSIC);
        soonerEvent.setStatus(EventStatus.PUBLISHED);
        soonerEvent.setEventDate(LocalDateTime.now().plusDays(10));
        soonerEvent.setMinimumAge(18);
        soonerEvent.setVenue(venue);

        eventRepository.save(soonerEvent);


        Event draftEvent = new Event();

        draftEvent.setEventCode("EVT-STATUS-03");
        draftEvent.setName("Evento Draft");
        draftEvent.setCategory(EventCategory.MUSIC);
        draftEvent.setStatus(EventStatus.DRAFT);
        draftEvent.setEventDate(LocalDateTime.now().plusDays(5));
        draftEvent.setMinimumAge(18);
        draftEvent.setVenue(venue);

        eventRepository.save(draftEvent);


        List<Event> result =
                eventRepository.findByStatusOrderByEventDateAsc(
                        EventStatus.PUBLISHED
                );


        assertThat(result)
                .hasSize(2);

        assertThat(result.get(0).getEventCode())
                .isEqualTo("EVT-STATUS-01");

        assertThat(result.get(1).getEventCode())
                .isEqualTo("EVT-STATUS-02");
    }


    

    @Test
    void givenVenueCode_whenFindByVenueCode_thenReturnsEventsOfVenue() {

        Venue venue = new Venue();

        venue.setCode("VEN-FIND-01");
        venue.setName("Venue Search");
        venue.setCity("Barranquilla");
        venue.setCapacity(1500);
        venue.setActive(true);

        venueRepository.save(venue);


        Event event1 = new Event();

        event1.setEventCode("EVT-VEN-01");
        event1.setName("Evento Venue 1");
        event1.setCategory(EventCategory.MUSIC);
        event1.setStatus(EventStatus.PUBLISHED);
        event1.setEventDate(LocalDateTime.now().plusDays(10));
        event1.setMinimumAge(18);
        event1.setVenue(venue);

        eventRepository.save(event1);


        Event event2 = new Event();

        event2.setEventCode("EVT-VEN-02");
        event2.setName("Evento Venue 2");
        event2.setCategory(EventCategory.CULTURE);
        event2.setStatus(EventStatus.PUBLISHED);
        event2.setEventDate(LocalDateTime.now().plusDays(15));
        event2.setMinimumAge(12);
        event2.setVenue(venue);

        eventRepository.save(event2);


        List<Event> result =
                eventRepository.findByVenueCode("VEN-FIND-01");


        assertThat(result)
                .hasSize(2);

        assertThat(result)
                .extracting(Event::getEventCode)
                .containsExactlyInAnyOrder(
                        "EVT-VEN-01",
                        "EVT-VEN-02"
                );
    }



    @Test
    void givenArtist_whenFindEventsByArtistStageName_thenReturnsEvents() {

        /*
         * Solar Beat ya existe porque fue creado por Flyway V2.
         * Por eso lo buscamos en lugar de intentar insertarlo otra vez.
         */
        Artist artist = artistRepository
                .findByStageName("Solar Beat")
                .orElseThrow();


        Venue venue = new Venue();

        venue.setCode("VEN-ART-01");
        venue.setName("Artist Venue");
        venue.setCity("Santa Marta");
        venue.setCapacity(1000);
        venue.setActive(true);

        venueRepository.save(venue);


        Event event = new Event();

        event.setEventCode("EVT-ART-01");
        event.setName("Solar Beat Live");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(20));
        event.setMinimumAge(18);
        event.setVenue(venue);

        event.getArtists().add(artist);

        eventRepository.save(event);


        List<Event> result =
                eventRepository.findEventsByArtistStageName("solar beat");


        assertThat(result).hasSize(1);

        assertThat(result.get(0).getEventCode())
                .isEqualTo("EVT-ART-01");
    }


   

    @Test
    void givenCityAndArtist_whenFindByCityAndArtist_thenReturnsMatchingEvents() {

        
        Artist artist = artistRepository
                .findByStageName("Neon Waves")
                .orElseThrow();


        Venue venue = new Venue();

        venue.setCode("VEN-CITY-01");
        venue.setName("City Venue");
        venue.setCity("Barranquilla");
        venue.setCapacity(2000);
        venue.setActive(true);

        venueRepository.save(venue);


        Event event = new Event();

        event.setEventCode("EVT-CITY-01");
        event.setName("Neon Waves Barranquilla");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(15));
        event.setMinimumAge(18);
        event.setVenue(venue);

        event.getArtists().add(artist);

        eventRepository.save(event);


        List<Event> result =
                eventRepository.findByCityAndArtist(
                        "Barranquilla",
                        "neon waves"
                );


        assertThat(result).hasSize(1);

        assertThat(result.get(0).getEventCode())
                .isEqualTo("EVT-CITY-01");
    }


    

    @Test
    void givenPublishedFutureEvents_whenFindRecommendedEvents_thenReturnsOrderedMatches() {

        
        Artist artist = artistRepository
                .findByStageName("Ocean Drive")
                .orElseThrow();


        Venue venue = new Venue();

        venue.setCode("VEN-REC-01");
        venue.setName("Recommended Venue");
        venue.setCity("Santa Marta");
        venue.setCapacity(3000);
        venue.setActive(true);

        venueRepository.save(venue);


        Event laterEvent = new Event();

        laterEvent.setEventCode("EVT-REC-02");
        laterEvent.setName("Ocean Drive Later");
        laterEvent.setCategory(EventCategory.MUSIC);
        laterEvent.setStatus(EventStatus.PUBLISHED);
        laterEvent.setEventDate(LocalDateTime.now().plusDays(20));
        laterEvent.setMinimumAge(18);
        laterEvent.setVenue(venue);

        laterEvent.getArtists().add(artist);

        eventRepository.save(laterEvent);


        Event soonerEvent = new Event();

        soonerEvent.setEventCode("EVT-REC-01");
        soonerEvent.setName("Ocean Drive Soon");
        soonerEvent.setCategory(EventCategory.MUSIC);
        soonerEvent.setStatus(EventStatus.PUBLISHED);
        soonerEvent.setEventDate(LocalDateTime.now().plusDays(10));
        soonerEvent.setMinimumAge(18);
        soonerEvent.setVenue(venue);

        soonerEvent.getArtists().add(artist);

        eventRepository.save(soonerEvent);


        List<Event> result =
                eventRepository.findRecommendedEvents(
                        EventStatus.PUBLISHED,
                        LocalDateTime.now(),
                        "Santa Marta",
                        "ocean"
                );


        assertThat(result).hasSize(2);

        assertThat(result.get(0).getEventCode())
                .isEqualTo("EVT-REC-01");

        assertThat(result.get(1).getEventCode())
                .isEqualTo("EVT-REC-02");
    }


   

    @Test
    void givenEmail_whenFindByEmailIgnoreCase_thenReturnsUser() {

        User user = new User();

        user.setUsername("queryuser");
        user.setEmail("QueryUser@PulsePass.com");
        user.setActive(true);

        userRepository.save(user);


        Optional<User> result =
                userRepository.findByEmailIgnoreCase(
                        "queryuser@pulsepass.com"
                );


        assertThat(result).isPresent();

        assertThat(result.get().getUsername())
                .isEqualTo("queryuser");
    }


    

    @Test
    void givenUser_whenSaveProfile_thenProfileIsAssociatedWithUser() {

        User user = new User();

        user.setUsername("profileuser");
        user.setEmail("profileuser@pulsepass.com");
        user.setActive(true);

        userRepository.save(user);


        UserProfile profile = new UserProfile();

        profile.setFirstName("Cristian");
        profile.setLastName("Garaviz");
        profile.setPhone("3000000000");
        profile.setCity("Santa Marta");
        profile.setBirthDate(LocalDate.of(2001, 1, 1));
        profile.setUser(user);

        UserProfile savedProfile =
                userProfileRepository.save(profile);


        assertThat(savedProfile.getId())
                .isNotNull();

        assertThat(savedProfile.getUser())
                .isEqualTo(user);

        assertThat(savedProfile.getUser().getEmail())
                .isEqualTo("profileuser@pulsepass.com");
    }


    

    @Test
    void givenUserEmailAndStatus_whenFindTickets_thenReturnsMatchingTickets() {

        User user = new User();

        user.setUsername("ticketuser");
        user.setEmail("ticketuser@pulsepass.com");
        user.setActive(true);

        userRepository.save(user);


        Venue venue = new Venue();

        venue.setCode("VEN-TICKET-01");
        venue.setName("Ticket Venue");
        venue.setCity("Santa Marta");
        venue.setCapacity(1000);
        venue.setActive(true);

        venueRepository.save(venue);


        Event event = new Event();

        event.setEventCode("EVT-TICKET-01");
        event.setName("Ticket Event");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(10));
        event.setMinimumAge(18);
        event.setVenue(venue);

        eventRepository.save(event);


        Ticket paidTicket = new Ticket();

        paidTicket.setTicketCode("TCK-USER-PAID");
        paidTicket.setType(TicketType.GENERAL);
        paidTicket.setPrice(new BigDecimal("100000"));
        paidTicket.setStatus(TicketStatus.PAID);
        paidTicket.setPurchaseDate(LocalDateTime.now());
        paidTicket.setUser(user);
        paidTicket.setEvent(event);

        ticketRepository.save(paidTicket);


        Ticket reservedTicket = new Ticket();

        reservedTicket.setTicketCode("TCK-USER-RESERVED");
        reservedTicket.setType(TicketType.VIP);
        reservedTicket.setPrice(new BigDecimal("200000"));
        reservedTicket.setStatus(TicketStatus.RESERVED);
        reservedTicket.setPurchaseDate(LocalDateTime.now());
        reservedTicket.setUser(user);
        reservedTicket.setEvent(event);

        ticketRepository.save(reservedTicket);


        List<Ticket> result =
                ticketRepository.findByUserEmailIgnoreCaseAndStatus(
                        "TICKETUSER@PULSEPASS.COM",
                        TicketStatus.PAID
                );


        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getTicketCode())
                .isEqualTo("TCK-USER-PAID");
    }


    

    @Test
    void givenEventCodeAndPaidStatus_whenFindTickets_thenReturnsPaidTicketsOnly() {

        Venue venue = new Venue();

        venue.setCode("VEN-PAID-01");
        venue.setName("Paid Venue");
        venue.setCity("Santa Marta");
        venue.setCapacity(2000);
        venue.setActive(true);

        venueRepository.save(venue);


        Event event = new Event();

        event.setEventCode("EVT-PAID-01");
        event.setName("Paid Event");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(20));
        event.setMinimumAge(18);
        event.setVenue(venue);

        eventRepository.save(event);


        User user = new User();

        user.setUsername("paiduser");
        user.setEmail("paiduser@pulsepass.com");
        user.setActive(true);

        userRepository.save(user);


        Ticket paidTicket = new Ticket();

        paidTicket.setTicketCode("TCK-PAID-01");
        paidTicket.setType(TicketType.GENERAL);
        paidTicket.setPrice(new BigDecimal("120000"));
        paidTicket.setStatus(TicketStatus.PAID);
        paidTicket.setPurchaseDate(LocalDateTime.now());
        paidTicket.setUser(user);
        paidTicket.setEvent(event);

        ticketRepository.save(paidTicket);


        Ticket reservedTicket = new Ticket();

        reservedTicket.setTicketCode("TCK-RESERVED-01");
        reservedTicket.setType(TicketType.GENERAL);
        reservedTicket.setPrice(new BigDecimal("120000"));
        reservedTicket.setStatus(TicketStatus.RESERVED);
        reservedTicket.setPurchaseDate(LocalDateTime.now());
        reservedTicket.setUser(user);
        reservedTicket.setEvent(event);

        ticketRepository.save(reservedTicket);


        List<Ticket> result =
                ticketRepository.findByEventEventCodeAndStatus(
                        "EVT-PAID-01",
                        TicketStatus.PAID
                );


        assertThat(result)
                .hasSize(1);

        assertThat(result.get(0).getTicketCode())
                .isEqualTo("TCK-PAID-01");
    }


    

    @Test
    void givenPaidAndUnpaidTickets_whenCountPaidTickets_thenCountsOnlyPaid() {

        Venue venue = new Venue();

        venue.setCode("VEN-COUNT-01");
        venue.setName("Count Venue");
        venue.setCity("Santa Marta");
        venue.setCapacity(3000);
        venue.setActive(true);

        venueRepository.save(venue);


        Event event = new Event();

        event.setEventCode("EVT-COUNT-01");
        event.setName("Count Event");
        event.setCategory(EventCategory.MUSIC);
        event.setStatus(EventStatus.PUBLISHED);
        event.setEventDate(LocalDateTime.now().plusDays(30));
        event.setMinimumAge(18);
        event.setVenue(venue);

        eventRepository.save(event);


        User user = new User();

        user.setUsername("countuser");
        user.setEmail("countuser@pulsepass.com");
        user.setActive(true);

        userRepository.save(user);


        for (int i = 1; i <= 3; i++) {

            Ticket ticket = new Ticket();

            ticket.setTicketCode("TCK-COUNT-0" + i);
            ticket.setType(TicketType.GENERAL);
            ticket.setPrice(new BigDecimal("100000"));
            ticket.setStatus(TicketStatus.PAID);
            ticket.setPurchaseDate(LocalDateTime.now());
            ticket.setUser(user);
            ticket.setEvent(event);

            ticketRepository.save(ticket);
        }


        Ticket reservedTicket = new Ticket();

        reservedTicket.setTicketCode("TCK-COUNT-04");
        reservedTicket.setType(TicketType.GENERAL);
        reservedTicket.setPrice(new BigDecimal("100000"));
        reservedTicket.setStatus(TicketStatus.RESERVED);
        reservedTicket.setPurchaseDate(LocalDateTime.now());
        reservedTicket.setUser(user);
        reservedTicket.setEvent(event);

        ticketRepository.save(reservedTicket);


        long count =
                ticketRepository.countPaidTicketsByEventCode(
                        "EVT-COUNT-01",
                        TicketStatus.PAID
                );


        assertThat(count)
                .isEqualTo(3);
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


        assertThat(ticketRepository.count())
                .isOne();


        List<Ticket> paidTickets =
                ticketRepository.findByEventEventCodeAndStatus(
                        "EVT-CORE-01",
                        TicketStatus.PAID
                );

        assertThat(paidTickets)
                .hasSize(1);
    }
}