package nl.avans.wordcrex.model;

import nl.avans.wordcrex.data.Database;
import nl.avans.wordcrex.util.Pollable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Game implements Pollable<Game> {
    private final Database database;

    public final int id;
    public final User host;
    public final User opponent;
    public final GameState state;
    public final InviteState inviteState;
    public final Dictionary dictionary;
    public final List<Tile> tiles;
    public final List<Letter> pool;
    public final List<Round> rounds;
    public final List<Message> messages;

    public Game(Game game, List<Tile> tiles) {
        this(game.database, game.id, game.host, game.opponent, game.state, game.inviteState, game.dictionary, tiles, game.pool, game.rounds, game.messages);
    }

    public Game(Game game, GameState state, InviteState inviteState, List<Letter> pool, List<Round> rounds, List<Message> messages) {
        this(game.database, game.id, game.host, game.opponent, state, inviteState, game.dictionary, game.tiles, pool, rounds, messages);
    }

    public Game(Database database, int id, User host, User opponent, GameState state, InviteState inviteState, Dictionary dictionary) {
        this(database, id, host, opponent, state, inviteState, dictionary, List.of(), List.of(), List.of(), List.of());
    }

    public Game(Database database, int id, User host, User opponent, GameState state, InviteState inviteState, Dictionary dictionary, List<Tile> tiles, List<Letter> pool, List<Round> rounds, List<Message> messages) {
        this.database = database;
        this.id = id;
        this.host = host;
        this.opponent = opponent;
        this.state = state;
        this.inviteState = inviteState;
        this.dictionary = dictionary;
        this.tiles = tiles;
        this.pool = pool;
        this.rounds = rounds;
        this.messages = messages;
    }

    @Override
    public Game initialize() {
        var tiles = new ArrayList<Tile>();

        this.database.select(
            "SELECT t.x, t.y, t.tile_type FROM tile t",
            (statement) -> {
            },
            (result) -> tiles.add(new Tile(result.getInt("x"), result.getInt("y"), result.getString("tile_type")))
        );

        return new Game(this.poll(), List.copyOf(tiles));
    }

    @Override
    public Game poll() {
        var ref = new Object() {
            GameState state;
            InviteState inviteState;
            List<Letter> pool = new ArrayList<>();
            List<Round> rounds = new ArrayList<>();
            List<Message> messages = new ArrayList<>();
        };
        this.database.select(
            "SELECT g.game_state, g.answer_player2 FROM game g JOIN turn t ON g.game_id = t.game_id WHERE g.game_id = ?",
            (statement) -> statement.setInt(1, this.id),
            (result) -> {
                ref.state = GameState.byState(result.getString("game_state"));
                ref.inviteState = InviteState.byState(result.getString("answer_player2"));
            }
        );
        this.database.select(
            "SELECT * FROM pot p WHERE p.game_id = ?",
            (statement) -> statement.setInt(1, this.id),
            (result) -> {
                var character = result.getString("symbol");
                ref.pool.add(this.pool.stream()
                    .filter((c) -> c.character.character.equals(character))
                    .findFirst()
                    .orElseThrow());
            }
        );
        this.database.select(
            "SELECT t.turn_id FROM turn t WHERE t.game_id = ?",
            (statement) -> statement.setInt(1, this.id),
            (result) -> ref.rounds.add(new Round(result.getInt("turn_id"), List.of(), null, null))
        );
        this.database.select(
            "SELECT * FROM chatline WHERE game_id = ? ORDER BY moment ASC",
            (statement) -> statement.setInt(1, this.id),
            (result) -> {
                ref.messages.add(
                    new Message(
                        this.host.username.equals(result.getString("username")) ? this.host : this.opponent,
                        result.getDate("moment"),
                        result.getString("message")
                    )
                );
            }
        );

        return new Game(this, ref.state, ref.inviteState, List.copyOf(ref.pool), List.copyOf(ref.rounds), List.copyOf(ref.messages));
    }

    @Override
    public User persist(User user) {
        var matches = user.games.stream()
            .map((match) -> match.id == this.id ? this : match)
            .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));

        return new User(user, matches);
    }

    public boolean isHostAuthenticated() {
        return this.host.authenticated;
    }

    public Round getLastRound() {
        if (this.rounds.size() == 0) {
            return null;
        }

        return this.rounds.get(this.rounds.size() - 1);
    }

    public int getHostScore() {
        return this.rounds.stream()
            .mapToInt((round) -> {
                if (round.hostTurn == null) {
                    return 0;
                }

                return round.hostTurn.score + round.hostTurn.bonus;
            })
            .sum();
    }

    public int getOpponentScore() {
        return this.rounds.stream()
            .mapToInt((round) -> {
                if (round.opponentTurn == null) {
                    return 0;
                }

                return round.opponentTurn.score + round.opponentTurn.bonus;
            })
            .sum();
    }

    public void startNewRound() {
        this.database.insert(
            "INSERT INTO turn (game_id, turn_id) VALUES (?, ?)",
            (statement) -> {
                statement.setInt(1, this.id);
                statement.setInt(2, this.rounds.size() + 1);
            }
        );

        var builder = new StringBuilder();
        var deck = new ArrayList<Letter>();
        var size = Math.min(7, this.pool.size() - 1);
        var random = new Random();

        for (int i = 0; i < size; i++) {
            var next = random.nextInt(this.pool.size());

            deck.add(this.pool.get(next));
            builder.append("(?, ?, ?) ");
        }

        this.database.insert(
            "INSERT INTO handletter (game_id, turn_id, letter_id) VALUES " + builder.toString(),
            (statement) -> {
                var offset = 0;

                for (int i = 0; i < size; i++) {
                    statement.setInt(1 + offset++, this.id);
                    statement.setInt(2 + offset++, this.rounds.size() + 1);
                    statement.setInt(3 + offset++, deck.get(i).id);
                }
            }
        );
    }

    public void sendChatMessage(String message) {
        this.database.insert("INSERT INTO chatline VALUES (?, ?, ?, ?)",
            (statement) -> {
                statement.setString(1, this.isHostAuthenticated() ? this.host.username : this.opponent.username);
                statement.setInt(2, this.id);
                statement.setTimestamp(3, new java.sql.Timestamp(new Date().getTime()));
                statement.setString(4, message);
            }
        );
    }
}
