/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphhopper.jsprit.core.algorithm.recreate;

import java.util.ArrayList;
import java.util.List;

import com.graphhopper.jsprit.core.problem.solution.route.Tour;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.FactoryDeliverService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Created by schroeder on 19/05/15.
 */
class InsertActivityListener implements EventListener {

    @Override
    public void inform(Event event) {
        if (event instanceof InsertActivity) {
            InsertActivity insertActivity = (InsertActivity) event;
            if (!insertActivity.getNewVehicle().isReturnToDepot()) {
                if (insertActivity.getIndex() >= insertActivity.getVehicleRoute().getActivities().size()) {
                    insertActivity.getVehicleRoute().getEnd().setLocation(insertActivity.getActivity().getLocation());
                }
            }
            insertActivity.getVehicleRoute().getTourActivities().addActivity(insertActivity.getIndex(), ((InsertActivity) event).getActivity());
            recalculateTours(insertActivity.getVehicleRoute());
        }
    }
    
    private void recalculateTours(VehicleRoute vehicleRoute) {
		List<Tour> tours = new ArrayList<>();
		int index = 0;
		
		Tour tour = new Tour(vehicleRoute, index);
		for (TourActivity tourActivity : vehicleRoute.getActivities()) {
			tour.getTourActivities().addActivity(tourActivity);
			if (tourActivity instanceof FactoryDeliverService) {
				tours.add(tour);
				tour = new Tour(vehicleRoute, ++index);
			}
		}
		
		vehicleRoute.setTours(tours);
	}

}
