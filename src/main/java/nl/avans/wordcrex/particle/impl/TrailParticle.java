package nl.avans.wordcrex.particle.impl;

import nl.avans.wordcrex.particle.Particle;
import nl.avans.wordcrex.util.Colors;
import nl.avans.wordcrex.view.View;
import nl.avans.wordcrex.view.impl.LoginView;
import nl.avans.wordcrex.view.impl.RegisterView;

import java.awt.*;
import java.util.function.Consumer;

public class TrailParticle extends Particle {
    private int age = 12;

    public TrailParticle(int x, int y) {
        super(x, y, 0.0f, 0.0f, 0.0f, false);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Colors.DARKERER_BLUE);
        g.fillRect(this.getX() - this.age, this.getY() - this.age, this.age * 2, this.age * 2);
    }

    @Override
    public boolean update(Consumer<Particle> addParticle) {
        return this.age-- >= 0;
    }

    @Override
    public int priority() {
        return -1;
    }

    @Override
    public boolean persist(View<?> view) {
        return view.getClass() == LoginView.class || view.getClass() == RegisterView.class;
    }
}
