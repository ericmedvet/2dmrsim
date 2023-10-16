
package io.github.ericmedvet.mrsim2d.core;
public interface Environment extends ActionPerformer {

  double t();

  Snapshot tick();

}
