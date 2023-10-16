
package io.github.ericmedvet.mrsim2d.core.actions;

import io.github.ericmedvet.mrsim2d.core.ActionPerformer;
import io.github.ericmedvet.mrsim2d.core.Agent;
import io.github.ericmedvet.mrsim2d.core.EmbodiedAgent;
import io.github.ericmedvet.mrsim2d.core.SelfDescribedAction;
import io.github.ericmedvet.mrsim2d.core.engine.ActionException;
import io.github.ericmedvet.mrsim2d.core.geometry.Point;
public record AddAndTranslateAgent(EmbodiedAgent agent, Point translation) implements SelfDescribedAction<EmbodiedAgent> {
  @Override
  public EmbodiedAgent perform(ActionPerformer performer, Agent agent) throws ActionException {
    EmbodiedAgent embodiedAgent = (EmbodiedAgent) performer.perform(new AddAgent(agent()), agent)
        .outcome().orElseThrow(() -> new ActionException(this, "Undoable addition"));
    performer.perform(new TranslateAgent(embodiedAgent, translation), agent);
    return embodiedAgent;
  }
}
