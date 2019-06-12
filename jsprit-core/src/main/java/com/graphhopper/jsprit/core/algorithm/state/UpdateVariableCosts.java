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
package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.cost.ForwardTransportCost;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.ActivityTimeTracker;
import com.graphhopper.jsprit.core.util.ActivityPolicyConfiguration;


/**
 * Updates total costs (i.e. transport and activity costs) at route and activity level.
 * <p>
 * <p>Thus it modifies <code>stateManager.getRouteState(route, StateTypes.COSTS)</code> and <br>
 * <code>stateManager.getActivityState(activity, StateTypes.COSTS)</code>
 */
public class UpdateVariableCosts implements ActivityVisitor, StateUpdater {

    private VehicleRoutingActivityCosts activityCost;

    private ForwardTransportCost transportCost;

    private StateManager states;

    private double totalOperationCost = 0.0;

    private VehicleRoute vehicleRoute = null;

    private TourActivity prevAct = null;

    private double startTimeAtPrevAct = 0.0;

    /**
     * Updates total costs (i.e. transport and activity costs) at route and activity level.
     * <p>
     * <p>Thus it modifies <code>stateManager.getRouteState(route, StateTypes.COSTS)</code> and <br>
     * <code>stateManager.getActivityState(activity, StateTypes.COSTS)</code>
     *
     * @param activityCost
     * @param transportCost
     * @param states
     */
    public UpdateVariableCosts(VehicleRoutingActivityCosts activityCost, VehicleRoutingTransportCosts transportCost, StateManager states) {
        super();
        this.activityCost = activityCost;
        this.transportCost = transportCost;
        this.states = states;
    }

	public UpdateVariableCosts(VehicleRoutingActivityCosts activityCosts, VehicleRoutingTransportCosts transportCosts, StateManager stateManager,
			ActivityTimeTracker.ActivityPolicy activityPolicy, ActivityPolicyConfiguration activityPolicyConfiguration) {
        this.activityCost = activityCosts;
        this.transportCost = transportCosts;
        this.states = stateManager;
    }

    @Override
    public void begin(VehicleRoute route) {
        vehicleRoute = route;
        prevAct = route.getStart();
        startTimeAtPrevAct = route.getStart().getEndTime();
    }

    @Override
    public void visit(TourActivity act) {
        double transportCost = this.transportCost.getTransportCost(prevAct.getLocation(), act.getLocation(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
        double actCost = activityCost.getActivityCost(act, act.getArrTime(), vehicleRoute.getDriver(), vehicleRoute.getVehicle());

        totalOperationCost += transportCost;
        totalOperationCost += actCost;

        states.putInternalTypedActivityState(act, InternalStates.COSTS, totalOperationCost);

        prevAct = act;
        startTimeAtPrevAct = act.getEndTime();
    }

    @Override
    public void finish() {
        double transportCost = this.transportCost.getTransportCost(prevAct.getLocation(), vehicleRoute.getEnd().getLocation(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
        double actCost = activityCost.getActivityCost(vehicleRoute.getEnd(), vehicleRoute.getEnd().getArrTime(), vehicleRoute.getDriver(), vehicleRoute.getVehicle());

        totalOperationCost += transportCost;
        totalOperationCost += actCost;

        states.putTypedInternalRouteState(vehicleRoute, InternalStates.COSTS, totalOperationCost);

        startTimeAtPrevAct = 0.0;
        prevAct = null;
        vehicleRoute = null;
        totalOperationCost = 0.0;
    }

}
