
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
public record TranslateAgent(EmbodiedAgent agent, Point translation) implements SelfDescribedAction<EmbodiedAgent> {
  @Override
  public EmbodiedAgent perform(ActionPerformer performer, Agent agent) throws ActionException {
    agent().bodyParts().forEach(b -> performer.perform(new TranslateBody(b, translation), agent));
    return agent();
  }
}
