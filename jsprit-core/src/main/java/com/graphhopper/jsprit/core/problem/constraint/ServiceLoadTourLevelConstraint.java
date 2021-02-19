package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.Tour;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

public class ServiceLoadTourLevelConstraint implements HardTourConstraint {

	private RouteAndActivityStateGetter stateManager;

	private Capacity defaultCapacity = Capacity.Builder.newInstance().build();

	public ServiceLoadTourLevelConstraint(RouteAndActivityStateGetter stateManager) {
		this.stateManager = stateManager;
	}

	@Override
	public boolean fulfilled(JobInsertionContext insertionContext, Tour tour) {
		Capacity currentLoad = stateManager.getTourState(tour, InternalStates.LOAD, Capacity.class);
		if (currentLoad == null) {
			currentLoad = defaultCapacity;
		}

		Capacity capacityDimensions = insertionContext.getNewVehicle().getType().getCapacityDimensions();

		Capacity newCapacity = Capacity.addup(currentLoad, insertionContext.getJob().getSize());

		return newCapacity.isLessOrEqual(capacityDimensions);
	}

}
