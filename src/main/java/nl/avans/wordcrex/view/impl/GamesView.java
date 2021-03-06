package nl.avans.wordcrex.view.impl;

import nl.avans.wordcrex.Main;
import nl.avans.wordcrex.controller.impl.GamesController;
import nl.avans.wordcrex.model.Game;
import nl.avans.wordcrex.model.GameState;
import nl.avans.wordcrex.model.InviteState;
import nl.avans.wordcrex.particle.Particle;
import nl.avans.wordcrex.util.Colors;
import nl.avans.wordcrex.util.Fonts;
import nl.avans.wordcrex.util.StringUtil;
import nl.avans.wordcrex.view.View;
import nl.avans.wordcrex.widget.Widget;
import nl.avans.wordcrex.widget.impl.ButtonWidget;
import nl.avans.wordcrex.widget.impl.DialogWidget;
import nl.avans.wordcrex.widget.impl.ListWidget;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class GamesView extends View<GamesController> {
    private final ListWidget<Game> list;
    private final DialogWidget dialog = new DialogWidget();

    public GamesView(GamesController controller) {
        super(controller);
        this.list = new ListWidget<>(
            64,
            96,
            "Geen spellen of uitdagingen",
            (game) -> String.valueOf(game.id),
            (previous, next) -> {
                var label = this.controller.getLabel(next);

                if (previous != null && this.controller.getLabel(previous).equals(label)) {
                    return null;
                }

                return label;
            },
            (g, game) -> {
                var other = this.controller.isCurrentUser(game.host) ? game.opponent : game.host;

                g.setColor(Colors.DARK_YELLOW);
                g.fillOval(Main.TASKBAR_SIZE, 27, 42, 42);
                g.setFont(Fonts.BIG);
                g.setColor(Colors.DARKER_BLUE);
                StringUtil.drawCenteredString(g, Main.TASKBAR_SIZE, 27, 42, 42, other.substring(0, 1).toUpperCase());
                g.setFont(Fonts.NORMAL);
                g.setColor(Color.WHITE);
                g.drawString(this.controller.getBigExtra(game) + other, Main.TASKBAR_SIZE * 2 + 42, 44);
                g.setFont(Fonts.SMALL);
                g.setColor(Color.LIGHT_GRAY);
                g.drawString(this.controller.getSmallExtra(game) + game.dictionary.name, Main.TASKBAR_SIZE * 2 + 42, 60);
                g.setFont(Fonts.NORMAL);

                if (game.state != GameState.PENDING && game.getLastRound() != null) {
                    var metrics = g.getFontMetrics();
                    var round = game.getLastRound();
                    var score = " " + round.hostScore + " - " + round.opponentScore + " ";
                    var width = metrics.stringWidth(score);

                    g.setColor(Colors.DARK_BLUE);
                    g.fillRect(450 - width, 34, width, 28);
                    g.setColor(Color.WHITE);
                    g.drawString(score, 450 - width, 54);
                }
            },
            this.controller::canClick,
            (game) -> {
                if (game.inviteState == InviteState.PENDING) {
                    this.dialog.show("Accepteren?", "JA", "NEE", (positive) -> {
                        if (positive) {
                            this.controller.acceptInvite(game);
                        } else {
                            this.controller.rejectInvite(game);
                        }
                    });

                    return;
                }

                this.controller.navigateGame(game);
            }
        );
    }

    @Override
    public void draw(Graphics2D g) {
    }

    @Override
    public void update(Consumer<Particle> addParticle) {
        this.list.setItems(this.controller.getGames());
    }

    @Override
    public List<Widget> children() {
        return List.of(
            this.list,
            new ButtonWidget("NIEUW SPEL", 0, Main.TASKBAR_SIZE, Main.FRAME_SIZE - Main.TASKBAR_SIZE, 64, this.controller::navigateInvite),
            this.dialog
        );
    }
}
