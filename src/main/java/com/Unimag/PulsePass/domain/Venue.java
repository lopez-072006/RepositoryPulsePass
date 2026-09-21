@Entity
@Table(name = "Venues")
public class Venue {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "city", nullable = false, length = 150)
    private String city;

    @Column(name = "address", nullable = false, length = 150)
    private String address;

    @Positive(message = "La capacidad debe ser mayor que 0")
    @Column(name = "capacity", nullable = false, length = 150, )
    private Integer capacity;

    @Column(name = "active", nullable = false, length = 150)
    private Boolean active;

    @OneToMany(mappedBy = "venue")
    private List<Event> events = new ArrayList<>();
}