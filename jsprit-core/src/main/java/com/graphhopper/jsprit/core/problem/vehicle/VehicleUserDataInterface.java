package com.graphhopper.jsprit.core.problem.vehicle;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

public interface VehicleUserDataInterface {

	public boolean canInsert(VehicleRoute vehicleRoute, Vehicle newVehicle, Job j);
}
