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
package com.graphhopper.jsprit.core.problem.solution.route.state;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.problem.solution.route.Tour;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

public interface RouteAndActivityStateGetter {


    public <T> T getActivityState(TourActivity act, StateId stateId, Class<T> type);

    public <T> T getActivityState(TourActivity act, Vehicle vehicle, StateId stateId, Class<T> type);

    public <T> T getRouteState(VehicleRoute route, StateId stateId, Class<T> type);

    public <T> T getRouteState(VehicleRoute route, Vehicle vehicle, StateId stateId, Class<T> type);
    
    public <T> T getTourState(Tour tour, StateId stateId, Class<T> type);

}
