package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.user.model.User;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    private String name;
    private String description;
    private boolean available;
    @Transient
    private Booking lastBooking;
    @Transient
    private Booking nextBooking;
    @ElementCollection
    @CollectionTable(name = "comments", joinColumns = @JoinColumn(name = "item_id"))

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item item)) return false;

        return id == item.id;
    }

    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
