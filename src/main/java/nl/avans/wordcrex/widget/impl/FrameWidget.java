package nl.avans.wordcrex.widget.impl;

import nl.avans.wordcrex.Main;
import nl.avans.wordcrex.particle.Particle;
import nl.avans.wordcrex.util.Assets;
import nl.avans.wordcrex.util.Colors;
import nl.avans.wordcrex.util.Fonts;
import nl.avans.wordcrex.view.impl.LoginView;
import nl.avans.wordcrex.view.impl.RegisterView;
import nl.avans.wordcrex.widget.Widget;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class FrameWidget extends Widget {
    private final Main main;
    private final SidebarWidget sidebar;
    private final ButtonWidget sidebarButton;

    public FrameWidget(Main main) {
        this.main = main;
        this.sidebar = new SidebarWidget(this.main);
        this.sidebarButton = new ButtonWidget(Assets.read("menu"), null, 0, 0, Main.TASKBAR_SIZE, Main.TASKBAR_SIZE, this.sidebar::toggle);
    }

    @Override
    public void draw(Graphics2D g) {
        this.sidebarButton.setEnabled(!this.main.isOpen(LoginView.class) && !this.main.isOpen(RegisterView.class));

        g.setColor(Colors.DARKERER_BLUE);
        g.fillRect(0, 0, Main.FRAME_SIZE, Main.TASKBAR_SIZE);
        g.setColor(Colors.DARK_BLUE);
        g.setFont(Fonts.BIG);
        g.drawString("WORDCREX", 39, 25);
        g.setFont(Fonts.NORMAL);
    }

    @Override
    public void update(Consumer<Particle> addParticle) {
        this.sidebarButton.setImage(Assets.read(this.sidebar.open() ? "close" : "menu"));
    }

    @Override
    public List<Widget> children() {
        return List.of(
            this.sidebarButton,
            this.sidebar,
            new ButtonWidget(null, Assets.read("close"), null, Main.FRAME_SIZE - Main.TASKBAR_SIZE, 0, Main.TASKBAR_SIZE, Main.TASKBAR_SIZE, Color.RED, Colors.DARK_RED, Color.WHITE, this.main::stop)
        );
    }

    @Override
    public boolean blocking() {
        return this.sidebar.open();
    }
}
