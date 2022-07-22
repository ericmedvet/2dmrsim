package it.units.erallab.mrsim.core.geometry;

import it.units.erallab.mrsim.util.DoubleRange;

public record Terrain(Poly poly, DoubleRange withinBordersXRange) {}
