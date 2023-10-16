
package io.github.ericmedvet.mrsim2d.engine.dyn4j;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.joint.Joint;

import java.util.Collection;
public interface MultipartBody {
  Collection<Body> getBodies();
  Collection<Joint<Body>> getJoints();
}
