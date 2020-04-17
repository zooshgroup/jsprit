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
package com.graphhopper.jsprit.core.util;

import java.util.HashMap;
import java.util.Map;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.cost.ForwardTransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class ActivityTimeTracker implements ActivityVisitor {

	public enum ActivityPolicy {

		AS_SOON_AS_TIME_WINDOW_OPENS, AS_SOON_AS_ARRIVED, SHORTEST_WAIT_TIME

	}

	class ActivityTime {
		private double actArrTime;
		private double actEndTime;
		private TourActivity prevAct;
		private double startAtPrevAct;

		public ActivityTime(double actArrTime, double actEndTime, TourActivity prevAct, double startAtPrevAct) {
			this.actArrTime = actArrTime;
			this.actEndTime = actEndTime;
			this.prevAct = prevAct;
			this.startAtPrevAct = startAtPrevAct;
		}

		public double getActArrTime() {
			return actArrTime;
		}

		public void setActArrTime(double actArrTime) {
			this.actArrTime = actArrTime;
		}

		public double getActEndTime() {
			return actEndTime;
		}

		public void setActEndTime(double actEndTime) {
			this.actEndTime = actEndTime;
		}

		public TourActivity getPrevAct() {
			return prevAct;
		}

		public void setPrevAct(TourActivity prevAct) {
			this.prevAct = prevAct;
		}

		public double getStartAtPrevAct() {
			return startAtPrevAct;
		}

		public void setStartAtPrevAct(double startAtPrevAct) {
			this.startAtPrevAct = startAtPrevAct;
		}

	}

	private final ForwardTransportTime forwardTransportTime;

	private final VehicleRoutingActivityCosts activityCosts;

	private TourActivity prevAct = null;

	private double startAtPrevAct;

	private VehicleRoute route;

	private boolean beginFirst = false;

	private double actArrTime;

	private double actEndTime;

	private ActivityPolicy activityPolicy = ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS;

	private Map<TourActivity, ActivityTime> activityTimeMap;
	private ActivityPolicyConfiguration activityPolicyConfiguration;
	private int capacity;

	public ActivityTimeTracker(ForwardTransportTime forwardTransportTime, VehicleRoutingActivityCosts activityCosts) {
		super();
		this.forwardTransportTime = forwardTransportTime;
		this.activityCosts = activityCosts;
	}

	public ActivityTimeTracker(ForwardTransportTime forwardTransportTime, ActivityPolicy activityPolicy, ActivityPolicyConfiguration activityPolicyConfiguration,
			VehicleRoutingActivityCosts activityCosts) {
		super();
		this.forwardTransportTime = forwardTransportTime;
		this.activityPolicy = activityPolicy;
		this.activityCosts = activityCosts;
		this.activityPolicyConfiguration = activityPolicyConfiguration;
	}

	public double getActArrTime() {
		return actArrTime;
	}

	public double getActEndTime() {
		return actEndTime;
	}

	@Override
	public void begin(VehicleRoute route) {
		if (activityPolicy == ActivityPolicy.SHORTEST_WAIT_TIME && !route.getActivities().isEmpty()) {
			createShortestWaitTimeRoute(route);

			ActivityTime firstAct = activityTimeMap.get(route.getActivities().get(0));

			prevAct = firstAct.getPrevAct();
			startAtPrevAct = firstAct.getStartAtPrevAct();
			actArrTime = firstAct.getStartAtPrevAct();
			actEndTime = firstAct.getStartAtPrevAct();
			this.route = route;
			this.capacity = 0;
		} else {
			prevAct = route.getStart();
			startAtPrevAct = prevAct.getEndTime();
			actArrTime = startAtPrevAct;
			actEndTime = startAtPrevAct;
			this.route = route;
			beginFirst = true;
		}
	}

	private void createShortestWaitTimeRoute(VehicleRoute route) {
		activityTimeMap = null;

		int waitStartAtOpen = run(route, route.getActivities().get(0).getTheoreticalEarliestOperationStartTime());
		if (waitStartAtOpen > 0) {
			TourActivity nextActivity = null;
			double nextActivityStartTime = 0;
			for (int i = route.getActivities().size() - 1; i >= 0; i--) {
				TourActivity activity = route.getActivities().get(i);
				if (nextActivity != null) {

					double transportTime =
							this.forwardTransportTime.getTransportTime(activity.getLocation(), nextActivity.getLocation(), nextActivityStartTime, route.getDriver(),
									route.getVehicle());
					nextActivityStartTime = nextActivityStartTime - transportTime;
					double serviceTime = activityCosts.getActivityDuration(activity, nextActivityStartTime, route.getDriver(), route.getVehicle());
					if (nextActivityStartTime < activity.getTheoreticalLatestOperationStartTime() + serviceTime) {

						nextActivityStartTime -= serviceTime;
					} else {
						nextActivityStartTime = activity.getTheoreticalLatestOperationStartTime();
					}

				} else {
					nextActivityStartTime = activity.getTheoreticalLatestOperationStartTime();
				}

				nextActivity = activity;
			}

			double possibleZeroWaitStart = route.getActivities().get(0).getTheoreticalEarliestOperationStartTime() + waitStartAtOpen;

			double startWithWait = Math.min(nextActivityStartTime, possibleZeroWaitStart);

			run(route, startWithWait);
		}
	}

	private int run(VehicleRoute vehicleRoute, double startTime) {
		TourActivity runPrevAct = vehicleRoute.getStart();

		double prevActEndTime =
				startTime - forwardTransportTime.getTransportTime(runPrevAct.getLocation(), vehicleRoute.getActivities().get(0).getLocation(), startTime, vehicleRoute.getDriver(),
						vehicleRoute.getVehicle());

		activityTimeMap = new HashMap<>();
		int totalWaitingTime = 0;

		for (TourActivity act : vehicleRoute.getActivities()) {
			double transportTime =
					forwardTransportTime.getTransportTime(runPrevAct.getLocation(), act.getLocation(), prevActEndTime, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
			double arrivalTimeAtCurrAct = prevActEndTime + transportTime;

			double operationStartTime = Math.max(act.getTheoreticalEarliestOperationStartTime(), arrivalTimeAtCurrAct);

			double operationEndTime = operationStartTime + activityCosts.getActivityDuration(act, arrivalTimeAtCurrAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());

			ActivityTime activityTime = new ActivityTime(arrivalTimeAtCurrAct, operationEndTime, runPrevAct, prevActEndTime);
			activityTimeMap.put(act, activityTime);

			prevActEndTime = operationEndTime;
			runPrevAct = act;

			totalWaitingTime += operationStartTime - arrivalTimeAtCurrAct;
		}

		return totalWaitingTime;
	}

	/**
	 * 
	 * @param capacity
	 * @return adding up milk capacities: NORMAL, GMO, COSHER
	 */
	private int sumCapacity(Capacity capacity) {
		return capacity.get(0) + capacity.get(1) + capacity.get(2);
	}

	@Override
	public void visit(TourActivity activity) {
		if (activityPolicy == ActivityPolicy.SHORTEST_WAIT_TIME) {
			ActivityTime act = activityTimeMap.get(activity);

			prevAct = activity;
			startAtPrevAct = act.getActEndTime();
			actArrTime = act.getActArrTime();
			actEndTime = act.getActEndTime();
			capacity += sumCapacity(activity.getSize());
		} else {
			if (!beginFirst)
				throw new IllegalStateException("never called begin. this however is essential here");
			double transportTime = forwardTransportTime.getTransportTime(prevAct.getLocation(), activity.getLocation(), startAtPrevAct, route.getDriver(), route.getVehicle());
			double arrivalTimeAtCurrAct = startAtPrevAct + transportTime;

			actArrTime = arrivalTimeAtCurrAct;
			double operationStartTime;

			if (activityPolicy.equals(ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS)) {
				operationStartTime = Math.max(activity.getTheoreticalEarliestOperationStartTime(), arrivalTimeAtCurrAct);
			} else if (activityPolicy.equals(ActivityPolicy.AS_SOON_AS_ARRIVED)) {
				operationStartTime = actArrTime;
			} else
				operationStartTime = actArrTime;

			double operationEndTime = operationStartTime + activityCosts.getActivityDuration(activity, actArrTime, route.getDriver(), route.getVehicle());

			actEndTime = operationEndTime;

			prevAct = activity;
			startAtPrevAct = operationEndTime;
		}
	}

	@Override
	public void finish() {
		double transportTime = forwardTransportTime.getTransportTime(prevAct.getLocation(), route.getEnd().getLocation(), startAtPrevAct, route.getDriver(), route.getVehicle());
		double arrivalTimeAtCurrAct = startAtPrevAct + transportTime;

		if (this.activityPolicyConfiguration != null && this.activityPolicyConfiguration.getFactoryUnloadTimeFactor() != null &&
				this.activityPolicyConfiguration.getFactoryStaticTime() != null) {
			// double operationTime = this.capacity *
			// this.activityPolicyConfiguration.getFactoryUnloadTimeFactor();
			// double staticTime = this.activityPolicyConfiguration.getFactoryStaticTime();

			actArrTime = arrivalTimeAtCurrAct;
			actEndTime = arrivalTimeAtCurrAct; // + operationTime + staticTime;
		} else {
			actArrTime = arrivalTimeAtCurrAct;
			actEndTime = arrivalTimeAtCurrAct;

			beginFirst = false;
		}

	}

}
