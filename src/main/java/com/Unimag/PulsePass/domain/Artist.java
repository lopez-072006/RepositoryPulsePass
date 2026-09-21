@Entity
@Table(name = "artists")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage_name", nullable = false, unique = true, length = 150)
    private String stageName;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "genre", length = 100)
    private String genre;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @ManyToMany(mappedBy = "artists")
    private Set<Event> events = new HashSet<>();  
}