package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.solution.route.Tour;
import com.graphhopper.jsprit.core.problem.solution.route.TourVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class UpdateTourLoads implements StateUpdater, TourVisitor {

	private StateManager stateManager;

	private Capacity emptyLoad = Capacity.Builder.newInstance().build();

	public UpdateTourLoads(StateManager stateManager) {
		this.stateManager = stateManager;
	}

	@Override
	public void visit(Tour tour) {
		Capacity load = emptyLoad;
		for (TourActivity activity : tour.getTourActivities().getActivities()) {
			load = Capacity.addup(load, activity.getSize());
		}

		stateManager.putTypedInternalTourState(tour, InternalStates.TOUR_LOAD, load);
	}

}
