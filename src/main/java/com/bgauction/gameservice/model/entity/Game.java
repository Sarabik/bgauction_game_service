package com.bgauction.gameservice.model.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringExclude;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Positive
    @Column(name = "user_id")
    private Long userId;

    @NotBlank
    @Size(min = 2)
    @Column(name = "title")
    private String title;

    @NotBlank
    @Size(min = 5)
    @Column(name = "description")
    private String description;

    @NotBlank
    @Size(min = 5)
    @Column(name = "game_condition")
    private String condition;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private GameLanguage language = GameLanguage.EN;

    @NotNull
    @Positive
    @Column(name = "min_players")
    private Integer minPlayers;

    @NotNull
    @Positive
    @Column(name = "max_players")
    private Integer maxPlayers;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GameStatus status = GameStatus.PUBLISHED;

    @CreationTimestamp
    @Column(name = "created", updatable = false)
    private LocalDateTime created;

    @OneToMany(mappedBy = "game", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToStringExclude
    private List<GameImage> images = new ArrayList<>();

    public void setImages(List<GameImage> images) {
        images.forEach(i -> i.setGame(this));
        this.images = images;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return Objects.equals(id, game.id)
                && Objects.equals(userId, game.userId)
                && Objects.equals(title, game.title)
                && Objects.equals(description, game.description)
                && Objects.equals(condition, game.condition)
                && language == game.language
                && Objects.equals(minPlayers, game.minPlayers)
                && Objects.equals(maxPlayers, game.maxPlayers)
                && status == game.status
                && Objects.equals(created, game.created);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, title, description, condition, language, minPlayers, maxPlayers, status, created);
    }
}
