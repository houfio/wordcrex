package nl.avans.wordcrex.view.impl;

import nl.avans.wordcrex.Main;
import nl.avans.wordcrex.controller.impl.DashboardController;
import nl.avans.wordcrex.model.Game;
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

public class DashboardView extends View<DashboardController> {
    private final ListWidget<Game> list;
    private final DialogWidget dialog = new DialogWidget();

    public DashboardView(DashboardController controller) {
        super(controller);
        this.list = new ListWidget<>(
            72,
            96,
            (g, game) -> {
                var host = this.controller.isCurrentUser(game.host);
                var other = host ? game.opponent : game.host;
                var extra = game.inviteState == InviteState.PENDING ? host ? "Naar " : "Van " : "";

                g.setColor(Colors.DARK_YELLOW);
                g.fillOval(Main.TASKBAR_SIZE, 27, 42, 42);
                g.setFont(Fonts.BIG);
                g.setColor(Colors.DARKER_BLUE);
                StringUtil.drawCenteredString(g, Main.TASKBAR_SIZE, 27, 42, 42, other.substring(0, 1).toUpperCase());
                g.setFont(Fonts.NORMAL);
                g.setColor(Color.WHITE);
                g.drawString(extra + other, Main.TASKBAR_SIZE * 2 + 42, 44);
                g.setFont(Fonts.SMALL);
                g.setColor(Color.LIGHT_GRAY);
                g.drawString(game.dictionary.description, Main.TASKBAR_SIZE * 2 + 42, 60);
                g.setFont(Fonts.NORMAL);
            },
            (previous, next) -> previous == null || previous.state != next.state ? this.controller.getLabel(next) : null,
            (game) -> String.valueOf(game.id),
            this.controller::isSelectable,
            (game) -> {
                if (game.inviteState == InviteState.PENDING) {
                    this.dialog.show("Accepteren?", "Ja", "Nee", (positive) -> {
                        if (positive) {
                            this.controller.acceptInvite(game);
                        } else {
                            this.controller.rejectInvite(game);
                        }
                    });

                    return;
                }

                this.controller.navigateGame(game.id);
            }
        );
    }

    @Override
    public void draw(Graphics2D g) {
        if (this.controller.getGames().isEmpty()) {
            g.setColor(Color.WHITE);
            StringUtil.drawCenteredString(g, 0, Main.TASKBAR_SIZE, Main.FRAME_SIZE - Main.TASKBAR_SIZE, Main.FRAME_SIZE - Main.TASKBAR_SIZE, "No games");
        }
    }

    @Override
    public void update(Consumer<Particle> addParticle) {
        this.list.setItems(this.controller.getGames());
    }

    @Override
    public List<Widget> getChildren() {
        return List.of(
            this.list,
            new ButtonWidget("Nieuw spel", 0, Main.TASKBAR_SIZE, Main.FRAME_SIZE - Main.TASKBAR_SIZE, 72, this.controller::navigateInvite),
            this.dialog
        );
    }
}
