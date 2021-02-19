package com.graphhopper.jsprit.core.problem.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.Tour;

public class HardTourLevelConstraintManager implements HardTourConstraint {

	private Collection<HardTourConstraint> hardConstraints = new ArrayList<>();

    public void addConstraint(HardTourConstraint constraint) {
        hardConstraints.add(constraint);
    }

    Collection<HardTourConstraint> getConstraints() {
        return Collections.unmodifiableCollection(hardConstraints);
    }
	
	@Override
	public boolean fulfilled(JobInsertionContext insertionContext, Tour tour) {
		for (HardTourConstraint constraint : hardConstraints) {
            if (!constraint.fulfilled(insertionContext, tour)) {
                return false;
            }
        }
        return true;
	}

}
