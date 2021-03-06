package nl.avans.wordcrex;

import joptsimple.OptionParser;
import nl.avans.wordcrex.controller.Controller;
import nl.avans.wordcrex.controller.impl.LoginController;
import nl.avans.wordcrex.data.Database;
import nl.avans.wordcrex.model.Wordcrex;
import nl.avans.wordcrex.particle.Particle;
import nl.avans.wordcrex.util.*;
import nl.avans.wordcrex.view.View;
import nl.avans.wordcrex.widget.Widget;
import nl.avans.wordcrex.widget.impl.FrameWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main extends JPanel {
    public static final Random RANDOM = new Random();
    public static final int FRAME_SIZE = 512;
    public static final int TASKBAR_SIZE = 32;

    private static final RenderingHints RENDERING_HINTS = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    private static final Stroke OUTLINE = new BasicStroke(2);

    public final boolean debug;

    private final JFrame frame;
    private final Database database;
    private final List<Widget> widgets;
    private final List<Particle> particles;
    private final Loop loop;
    private final Listener listener;
    private final Stack<Controller<?>> controllers;

    private Wordcrex model;

    private Main(JFrame frame, String config, boolean debug) {
        this.frame = frame;
        this.database = new Database(config, debug);
        this.widgets = new CopyOnWriteArrayList<>();
        this.particles = new CopyOnWriteArrayList<>();
        this.listener = new Listener(this.frame, this);
        this.controllers = new Stack<>();
        this.model = Wordcrex.initialize(this.database);
        this.debug = debug;

        this.setFont(Fonts.NORMAL);
        this.setForeground(Color.WHITE);
        this.setBackground(Colors.DARKER_BLUE);
        this.setFocusable(true);
        this.setFocusTraversalKeysEnabled(false);
        this.setDoubleBuffered(true);
        this.addMouseListener(this.listener);
        this.addMouseMotionListener(this.listener);
        this.addKeyListener(this.listener);
        this.openController(LoginController.class, Function.identity());

        this.loop = Loop.start(Map.of(
            5, this::poll,
            30, this::update,
            60, this::repaint
        ), this::handleError);
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        var g = (Graphics2D) graphics;

        g.setRenderingHints(Main.RENDERING_HINTS);
        g.setStroke(Main.OUTLINE);

        this.drawParticles(g, false);

        var view = this.getView();
        var drawn = new ArrayList<Widget>();
        var unblocked = this.getWidgets(false);

        if (view != null) {
            drawn.addAll(this.widgets.stream()
                .filter((widget) -> (!widget.top() || !unblocked.contains(widget)) && widget.childOf(view))
                .peek((widget) -> widget.draw(g))
                .collect(Collectors.toList()));

            view.drawForeground(g);
        }

        this.drawParticles(g, true);

        this.widgets.stream()
            .filter((widget) -> drawn.indexOf(widget) == -1)
            .forEach((widget) -> widget.draw(g));
    }

    public void stop() {
        this.loop.stop();
        this.frame.dispatchEvent(new WindowEvent(this.frame, WindowEvent.WINDOW_CLOSING));
    }

    public <T extends Persistable> void openController(Class<? extends Controller<T>> cls, Function<Wordcrex, T> fn) {
        this.controllers.clear();
        this.pushController(cls, fn);
    }

    public void popController() {
        this.controllers.pop();
        this.openView(this.controllers.peek().createView());
    }

    public <T extends Persistable> void pushController(Class<? extends Controller<T>> cls, Function<Wordcrex, T> fn) {
        this.controllers.push(this.createController(cls, fn));
        this.openView(this.controllers.peek().createView());
    }

    public boolean hasPrevious() {
        return this.controllers.size() > 1;
    }

    public void addParticle(Particle particle) {
        this.particles.add(particle);
    }

    public Wordcrex getModel() {
        return this.model;
    }

    public void updateModel(Persistable model) {
        this.model = model.persist(this.model);
    }

    public List<Widget> getWidgets(boolean blocked) {
        var blocker = this.widgets.stream()
            .filter(Widget::blocking)
            .reduce((a, b) -> b)
            .orElse(null);

        if (blocked || blocker == null) {
            return List.copyOf(this.widgets);
        }

        return this.widgets.stream()
            .filter((widget) -> widget.childOf(blocker))
            .collect(Collectors.toList());
    }

    public boolean isOpen(Class<? extends View<?>> cls) {
        return this.widgets.stream()
            .anyMatch(cls::isInstance);
    }

    public void tabFocus(boolean reverse) {
        var widgets = this.getWidgets(false).stream()
            .filter(Widget::focusable)
            .collect(Collectors.toList());
        var updated = false;

        for (int i = 0; i < widgets.size(); i++) {
            var widget = widgets.get(i);

            if (!widget.hasFocus()) {
                continue;
            }

            if (reverse && i == 0) {
                widgets.get(widgets.size() - 1).requestFocus();
            } else if (reverse) {
                widgets.get(i - 1).requestFocus();
            } else if (i < widgets.size() - 1) {
                widgets.get(i + 1).requestFocus();
            } else {
                widgets.get(0).requestFocus();
            }

            updated = true;
        }

        if (!updated && !widgets.isEmpty()) {
            widgets.get(reverse ? widgets.size() - 1 : 0).requestFocus();
        }
    }

    public void handleError(Exception e) {
        if (this.debug) {
            e.printStackTrace();
        }

        this.database.rollback();
    }

    private void openView(View<?> view) {
        this.widgets.clear();

        var dead = this.particles.stream()
            .filter((particle) -> !particle.persist(view))
            .collect(Collectors.toList());
        this.particles.removeAll(dead);

        this.addWidget(view, new ArrayList<>());
        this.addWidget(new FrameWidget(this), new ArrayList<>());
    }

    private <T extends Persistable> Controller<T> createController(Class<? extends Controller<T>> cls, Function<Wordcrex, T> fn) {
        try {
            return cls.getConstructor(Main.class, Function.class).newInstance(this, fn);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void addWidget(Widget widget, List<Widget> parents) {
        parents.forEach(widget::addParent);
        widget.update(this::addParticle);
        parents.add(widget);
        this.widgets.add(widget);
        widget.children().forEach((child) -> this.addWidget(child, parents));
    }

    private void poll() {
        if (this.controllers.empty()) {
            return;
        }

        this.controllers.peek().poll();

        var count = this.database.flush();

        if (this.debug) {
            System.out.println("Executed " + count + " quer" + (count == 1 ? "y" : "ies"));
        }
    }

    private void update() {
        this.widgets.forEach((widget) -> widget.update(this::addParticle));

        var dead = this.particles.stream()
            .filter((particle) -> !particle.update(this::addParticle))
            .collect(Collectors.toList());
        this.particles.removeAll(dead);

        var view = this.getView();

        if (view == null) {
            return;
        }

        if (view.requestingInitialize()) {
            this.openView(view);
            this.listener.mouseMoved(null);

            return;
        }

        var requester = this.getWidgets(false).stream()
            .filter((widget) -> widget.requestingFocus() && widget.focusable())
            .findFirst()
            .orElse(null);

        if (requester == null || requester.hasFocus()) {
            return;
        }

        this.getWidgets(true).forEach((widget) -> widget.setFocus(false));
        requester.setFocus(true);
    }

    private void drawParticles(Graphics2D g, boolean foreground) {
        this.particles.stream()
            .filter((particle) -> particle.foreground == foreground)
            .sorted(Comparator.comparingInt(Particle::priority))
            .forEach((particle) -> particle.draw(g));
    }

    private View<?> getView() {
        return this.widgets.stream()
            .filter(View.class::isInstance)
            .map(View.class::cast)
            .findFirst()
            .orElse(null);
    }

    public static void main(String[] args) {
        var parser = new OptionParser();
        var config = parser.accepts("database").withRequiredArg().defaultsTo("prod");
        var debug = parser.accepts("debug").withOptionalArg().ofType(Boolean.class).defaultsTo(true);

        var parsed = parser.parse(args);
        var frame = new JFrame();

        frame.setTitle("Wordcrex");
        frame.setSize(Main.FRAME_SIZE, Main.FRAME_SIZE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setUndecorated(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setIconImage(Assets.read("icon"));
        frame.add(new Main(frame, parsed.valueOf(config), parsed.has(debug) ? parsed.valueOf(debug) : false));
        frame.setVisible(true);
    }
}
