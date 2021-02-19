package com.graphhopper.jsprit.core.problem.solution.route;

import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivities;

public class Tour {

	private VehicleRoute route;
	private int tourIndex;
	private final TourActivities tourActivities;

	public Tour(VehicleRoute route, int tourIndex) {
		this.route = route;
		this.tourIndex = tourIndex;
		tourActivities = new TourActivities();
	}

	public VehicleRoute getRoute() {
		return route;
	}

	public void setRoute(VehicleRoute route) {
		this.route = route;
	}

	public TourActivities getTourActivities() {
		return tourActivities;
	}

	public int getTourIndex() {
		return tourIndex;
	}

	public void setTourIndex(int tourIndex) {
		this.tourIndex = tourIndex;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((route == null) ? 0 : route.hashCode());
		result = prime * result + tourIndex;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tour other = (Tour) obj;
		if (route == null) {
			if (other.route != null)
				return false;
		} else if (!route.equals(other.route))
			return false;
		if (tourIndex != other.tourIndex)
			return false;
		return true;
	}

}
