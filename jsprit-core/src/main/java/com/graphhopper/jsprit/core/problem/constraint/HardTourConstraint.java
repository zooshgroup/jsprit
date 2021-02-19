package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.Tour;

public interface HardTourConstraint extends HardConstraint {
	
	public boolean fulfilled(JobInsertionContext insertionContext, Tour tour);

}
