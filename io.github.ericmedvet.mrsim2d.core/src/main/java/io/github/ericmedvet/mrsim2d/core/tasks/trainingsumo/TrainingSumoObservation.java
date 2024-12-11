package io.github.ericmedvet.mrsim2d.core.tasks.trainingsumo;

import io.github.ericmedvet.mrsim2d.core.bodies.RigidBody;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
import io.github.ericmedvet.mrsim2d.core.tasks.AgentsObservation;

import java.util.List;

public class TrainingSumoObservation extends AgentsObservation {

    private final Point rigidBodyPosition;

    public TrainingSumoObservation(List<Agent> agents, RigidBody rigidBody) {
        super(agents);
        this.rigidBodyPosition = rigidBody.poly().center();
    }

    public Point getRigidBodyPosition() {
        return rigidBodyPosition;
    }
}