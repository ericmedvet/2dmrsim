package it.units.erallab.mrsim2d.core.tasks;

import it.units.erallab.mrsim2d.core.geometry.BoundingBox;
import it.units.erallab.mrsim2d.core.geometry.Point;
import it.units.erallab.mrsim2d.core.geometry.Poly;

import java.util.Comparator;
import java.util.List;

public class Observation {

  public record Agent(
      List<Poly> polies,
      double terrainHeight
  ) {}

  private final List<Agent> agents;
  private BoundingBox allBoundingBox;
  private List<BoundingBox> boundingBoxes;
  private List<Point> centers;
  private Point firstAgentCenter;
  private BoundingBox firstAgentBoundingBox;
  private Point maxXAgentCenter;
  private Point minXAgentCenter;
  private Point maxYAgentCenter;
  private Point minYAgentCenter;

  public Observation(List<Agent> agents) {
    this.agents = agents;
  }

  public List<Agent> getAgents() {
    return agents;
  }

  public BoundingBox getAllBoundingBox() {
    if (allBoundingBox == null & !getBoundingBoxes().isEmpty()) {
      allBoundingBox = boundingBoxes.stream().reduce(BoundingBox::enclosing).orElse(null);
    }
    return allBoundingBox;
  }

  public List<BoundingBox> getBoundingBoxes() {
    if (boundingBoxes == null) {
      //noinspection OptionalGetWithoutIsPresent
      boundingBoxes = agents.stream()
          .map(a -> a.polies.stream()
              .map(Poly::boundingBox)
              .reduce(BoundingBox::enclosing)
              .get())
          .toList();
    }
    return boundingBoxes;
  }

  public List<Point> getCenters() {
    if (centers == null) {
      centers = agents.stream()
          .map(a -> Point.average(a.polies.stream()
              .map(Poly::center)
              .toArray(Point[]::new)))
          .toList();
    }
    return centers;
  }

  public Point getFirstAgentCenter() {
    if (firstAgentCenter == null && !getCenters().isEmpty()) {
      firstAgentCenter = getCenters().get(0);
    }
    return firstAgentCenter;
  }

  public Point getMaxXAgentCenter() {
    if (maxXAgentCenter == null && !getCenters().isEmpty()) {
      maxXAgentCenter = getCenters().stream().max(Comparator.comparing(Point::x)).orElse(null);
    }
    return maxXAgentCenter;
  }

  public Point getMinXAgentCenter() {
    if (minXAgentCenter == null && !getCenters().isEmpty()) {
      minXAgentCenter = getCenters().stream().min(Comparator.comparing(Point::x)).orElse(null);
    }
    return minXAgentCenter;
  }

  public Point getMaxYAgentCenter() {
    if (maxYAgentCenter == null && !getCenters().isEmpty()) {
      maxYAgentCenter = getCenters().stream().max(Comparator.comparing(Point::y)).orElse(null);
    }
    return maxYAgentCenter;
  }

  public Point getMinYAgentCenter() {
    if (minYAgentCenter == null && !getCenters().isEmpty()) {
      minYAgentCenter = getCenters().stream().min(Comparator.comparing(Point::y)).orElse(null);
    }
    return minYAgentCenter;
  }

  public BoundingBox getFirstAgentBoundingBox() {
    if (firstAgentBoundingBox == null && !getBoundingBoxes().isEmpty()) {
      firstAgentBoundingBox = boundingBoxes.get(0);
    }
    return firstAgentBoundingBox;
  }
}
