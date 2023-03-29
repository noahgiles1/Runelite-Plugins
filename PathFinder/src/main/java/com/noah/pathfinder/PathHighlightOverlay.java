package com.noah.pathfinder;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Tile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;

import javax.inject.Inject;
import java.awt.*;
import java.util.ArrayList;


public class PathHighlightOverlay extends Overlay {
    private final Client client;
    private final PathHighlightConfig config;
    private final PathHighlightPlugin plugin;

    @Getter
    @Setter
    private boolean click;

    @Getter
    @Setter
    private boolean moving;

    @Setter
    private Tile destination = null;

    private ArrayList<LocalPoint> last = new ArrayList<>();

    private ArrayList<LocalPoint> current = new ArrayList<>();

    @Inject
    private PathHighlightOverlay(Client client, PathHighlightConfig config, PathHighlightPlugin plugin)
    {
        this.client = client;
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.MED);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!this.plugin.isDisplay()) {
            return null;
        }

        WorldPoint currPoint = client.getLocalPlayer().getWorldLocation();
        Tile selectedTile = client.getSelectedSceneTile();

        if (click) {
            destination = selectedTile;
            if (moving)
                last = current;
            moving = true;
            click = false;
        }
        if (destination != null) {
            WorldPoint dest = destination.getWorldLocation();
            for (LocalPoint point: last) {
                renderTile(graphics, point, config.secondaryPathColor());
            }
            if (!config.disableGuide()) {
                for (LocalPoint point: current) {
                    renderTile(graphics, point, config.highlightPathColor());
                }
            }
            if (dest.getX() == currPoint.getX() && dest.getY() == currPoint.getY() && dest.getPlane() == currPoint.getPlane()) {
                destination = null;
                moving = false;
                last = new ArrayList<>();
            }
            getTilePath(selectedTile, currPoint, graphics);
        } else {
            last = new ArrayList<>();
            getTilePath(selectedTile, currPoint, graphics);
        }
        return null;
    }

    private void getTilePath(Tile selectedTile, WorldPoint currPoint, Graphics2D graphics) {
        current = new ArrayList<>();
        if (selectedTile != null && currPoint != null)
        {
            WorldPoint selectedPoint = selectedTile.getWorldLocation();
            int xDist = selectedPoint.getX() - currPoint.getX();
            int yDist = selectedPoint.getY() - currPoint.getY();
            int dx = 0;
            int dy = 0;
            //Figure out which axis the character will walk along,
            //According to whether the x or y-distance to the destination is longer.
            if (Math.abs(xDist) > Math.abs(yDist))
            {
                dx = (int) Math.signum(xDist);
            }
            else
            {
                dy = (int) Math.signum(yDist);
            }

            // TODO figure out how to detect if the player is actually running
            boolean isRunning = true;

            boolean jumpedOver = isRunning;
            //Character will first walk on an axis until along a diagonal with the destination...
            while (Math.abs(xDist) != Math.abs(yDist))
            {
                currPoint = currPoint.dx(dx).dy(dy);
                LocalPoint pt = LocalPoint.fromWorld(client, currPoint.getX(), currPoint.getY());
                if (!jumpedOver || !config.skipJumpedTiles()) {
                    if (!moving) {
                        renderTile(graphics, pt, config.highlightPathColor());
                        last.add(pt);
                    } else {
                        current.add(pt);
                    }
                }
                jumpedOver ^= isRunning; // Flip only if running;
                xDist = selectedPoint.getX() - currPoint.getX();
                yDist = selectedPoint.getY() - currPoint.getY();
            }
            //...Then walk diagonally to the destination.
            dx = (int) Math.signum(xDist);
            dy = (int) Math.signum(yDist);
            while (xDist != 0 && yDist != 0)
            {
                currPoint = currPoint.dx(dx).dy(dy);
                LocalPoint pt = LocalPoint.fromWorld(client, currPoint.getX(), currPoint.getY());
                if (!config.skipJumpedTiles() || !jumpedOver) {
                    if (!moving) {
                        renderTile(graphics, pt, config.highlightPathColor());
                        last.add(pt);
                    } else {
                        current.add(pt);
                    }
                }
                jumpedOver ^= isRunning; // Flip only if running
                xDist = selectedPoint.getX() - currPoint.getX();
                yDist = selectedPoint.getY() - currPoint.getY();
            }

            if (!jumpedOver && config.skipJumpedTiles())
            { // Last tile was jumped over, flag was flipped
                LocalPoint pt = LocalPoint.fromWorld(client, currPoint.getX(), currPoint.getY());
                if (!moving) {
                    renderTile(graphics, pt, config.highlightPathColor());
                    last.add(pt);
                } else {
                    current.add(pt);
                }
            }
        }
    }

    private void renderTile(final Graphics2D graphics, final LocalPoint dest, final Color color)
    {
        if (dest == null)
        {
            return;
        }

        final Polygon poly = Perspective.getCanvasTilePoly(client, dest);

        if (poly == null)
        {
            return;
        }

        OverlayUtil.renderPolygon(graphics, poly, color);
    }
}
