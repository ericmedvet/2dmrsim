package io.github.ericmedvet.mrsim2d.sample;

import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.mrsim2d.buildable.PreparedNamedBuilder;

/**
 * @author "Eric Medvet" on 2023/01/03 for 2dmrsim
 */
public class Main {
  public static void main(String[] args) {
    NamedBuilder<Object> nb = PreparedNamedBuilder.get();
    System.out.println(nb.build("s.a.vsr.s.biped(w=4;h=3)"));
  }
}
