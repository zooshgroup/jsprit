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

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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

	private final ForwardTransportTime transportTime;

	private final VehicleRoutingActivityCosts activityCosts;

	private TourActivity prevAct = null;

	private double startAtPrevAct;

	private VehicleRoute route;

	private boolean beginFirst = false;

	private double actArrTime;

	private double actEndTime;

	private ActivityPolicy activityPolicy = ActivityPolicy.SHORTEST_WAIT_TIME;

	private int bestRun;
	private Map<TourActivity, ActivityTime> activityTimeMap;

	public ActivityTimeTracker(ForwardTransportTime transportTime, VehicleRoutingActivityCosts activityCosts) {
		super();
		this.transportTime = transportTime;
		this.activityCosts = activityCosts;
	}

	public ActivityTimeTracker(ForwardTransportTime transportTime, ActivityPolicy activityPolicy, VehicleRoutingActivityCosts activityCosts) {
		super();
		this.transportTime = transportTime;
		this.activityPolicy = activityPolicy;
		this.activityCosts = activityCosts;
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
			doRuns(route);

			ActivityTime firstAct = activityTimeMap.get(route.getActivities().get(0));

			prevAct = firstAct.getPrevAct();
			startAtPrevAct = firstAct.getStartAtPrevAct();
			actEndTime = firstAct.getStartAtPrevAct();
			this.route = route;
		} else {
			prevAct = route.getStart();
			startAtPrevAct = prevAct.getEndTime();
			actEndTime = startAtPrevAct;
			this.route = route;
			beginFirst = true;
		}
	}

	private void doRuns(VehicleRoute route) {
		int i = 0;
		bestRun = Integer.MAX_VALUE;
		activityTimeMap = null;
		for (TourActivity tourActivity : route.getActivities()) {
			run(i, tourActivity, route, true);
			if (bestRun == 0) {
				return;
			}
			run(i, tourActivity, route, false);
			if (bestRun == 0) {
				return;
			}

			i++;
		}
	}

	private void run(int index, TourActivity act, VehicleRoute route, boolean startAtBegin) {
		Map<TourActivity, ActivityTime> tourActivityMap = new HashMap<>();
		int totalWaitingTime = 0;
		try {
			Entry<Integer, Map<TourActivity, ActivityTime>> backward = runBackward(index, act, route, startAtBegin);
			Entry<Integer, Map<TourActivity, ActivityTime>> forward = runForward(index, act, route, startAtBegin);

			totalWaitingTime += backward.getKey();
			totalWaitingTime += forward.getKey();

			tourActivityMap.putAll(backward.getValue());
			tourActivityMap.putAll(forward.getValue());

			if (route.getActivities().size() != tourActivityMap.size()) {
				System.out.println("whaaat");
			}

			if (totalWaitingTime < bestRun) {
				bestRun = totalWaitingTime;
				activityTimeMap = tourActivityMap;
			}

		} catch (Exception e) {

		}

	}

	private Entry<Integer, Map<TourActivity, ActivityTime>> runForward(int index, TourActivity act, VehicleRoute route, boolean startAtBegin) {
		int totalWaitingTime = 0;
		Map<TourActivity, ActivityTime> forWardActivityMap = new HashMap<>();

		TourActivity prevActivity = route.getActivities().get(index);
		double prevActEndTime;
		if (startAtBegin) {
			prevActEndTime = act.getTheoreticalEarliestOperationStartTime() +
					activityCosts.getActivityDuration(prevActivity, act.getTheoreticalEarliestOperationStartTime(), route.getDriver(), route.getVehicle());
		} else {
			prevActEndTime = act.getTheoreticalLatestOperationStartTime() +
					activityCosts.getActivityDuration(prevActivity, act.getTheoreticalLatestOperationStartTime(), route.getDriver(), route.getVehicle());
		}

		for (index++; index < route.getActivities().size(); index++) {
			TourActivity activity = route.getActivities().get(index);

			double transportTime = this.transportTime.getTransportTime(prevActivity.getLocation(), activity.getLocation(), prevActEndTime, route.getDriver(), route.getVehicle());
			double arrivalTimeAtCurrAct = prevActEndTime + transportTime;

			double operationStartTime = Math.max(activity.getTheoreticalEarliestOperationStartTime(), arrivalTimeAtCurrAct);

			if (operationStartTime > activity.getTheoreticalLatestOperationStartTime()) {
				throw new UnsupportedOperationException();
			}

			double operationEndTime = operationStartTime + activityCosts.getActivityDuration(activity, arrivalTimeAtCurrAct, route.getDriver(), route.getVehicle());

			ActivityTime activityTime = new ActivityTime(arrivalTimeAtCurrAct, operationEndTime, prevActivity, prevActEndTime);
			forWardActivityMap.put(activity, activityTime);

			prevActEndTime = operationEndTime;
			prevActivity = activity;

			totalWaitingTime += operationStartTime - arrivalTimeAtCurrAct;
		}

		return new AbstractMap.SimpleEntry<>(totalWaitingTime, forWardActivityMap);
	}

	private Entry<Integer, Map<TourActivity, ActivityTime>> runBackward(int index, TourActivity act, VehicleRoute route, boolean startAtBegin) {
		int totalWaitingTime = 0;
		Map<TourActivity, ActivityTime> backwardActivityMap = new HashMap<>();

		TourActivity nextActivity = route.getActivities().get(index);

		double nextActStartTime;
		if (startAtBegin) {
			nextActStartTime = act.getTheoreticalEarliestOperationStartTime();
		} else {
			nextActStartTime = act.getTheoreticalLatestOperationStartTime();
		}

		for (index--; index >= 0; index--) {
			TourActivity activity = route.getActivities().get(index);

			double transportTime = this.transportTime.getTransportTime(activity.getLocation(), nextActivity.getLocation(), nextActStartTime, route.getDriver(), route.getVehicle());
			double leaveTimeAtCurrAct = nextActStartTime - transportTime;

			double tempEndTime =
					activity.getTheoreticalLatestOperationStartTime() + activityCosts.getActivityDuration(activity, leaveTimeAtCurrAct, route.getDriver(), route.getVehicle());

			double operationEndTime = Math.min(leaveTimeAtCurrAct, tempEndTime);
			double operationStartTime = operationEndTime - activityCosts.getActivityDuration(activity, operationEndTime, route.getDriver(), route.getVehicle());

			if (nextActStartTime > activity.getTheoreticalLatestOperationStartTime()) {
				throw new UnsupportedOperationException();
			}

			ActivityTime activityTime = new ActivityTime(nextActStartTime,
					nextActStartTime + activityCosts.getActivityDuration(nextActivity, nextActStartTime, route.getDriver(), route.getVehicle()), activity, operationEndTime);
			backwardActivityMap.put(nextActivity, activityTime);

			nextActStartTime = operationStartTime;

			nextActivity = activity;

			if (activity.getTheoreticalEarliestOperationStartTime() > operationStartTime) {
				totalWaitingTime += activity.getTheoreticalEarliestOperationStartTime() - operationStartTime;
			}
		}

		ActivityTime activityTime = new ActivityTime(nextActStartTime,
				nextActStartTime + activityCosts.getActivityDuration(nextActivity, nextActStartTime, route.getDriver(), route.getVehicle()),
				route.getStart(), nextActStartTime -
						this.transportTime.getTransportTime(route.getStart().getLocation(), nextActivity.getLocation(), nextActStartTime, route.getDriver(), route.getVehicle()));
		backwardActivityMap.put(nextActivity, activityTime);

		return new AbstractMap.SimpleEntry<>(totalWaitingTime, backwardActivityMap);
	}

	@Override
	public void visit(TourActivity activity) {
		if (activityPolicy == ActivityPolicy.SHORTEST_WAIT_TIME) {
			ActivityTime act = activityTimeMap.get(activity);

			prevAct = activity;
			startAtPrevAct = act.getActEndTime();
			actArrTime = act.getActArrTime();
			actEndTime = act.getActEndTime();
		} else {
			if (!beginFirst)
				throw new IllegalStateException("never called begin. this however is essential here");
			double transportTime = this.transportTime.getTransportTime(prevAct.getLocation(), activity.getLocation(), startAtPrevAct, route.getDriver(), route.getVehicle());
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
		double transportTime = this.transportTime.getTransportTime(prevAct.getLocation(), route.getEnd().getLocation(), startAtPrevAct, route.getDriver(), route.getVehicle());
		double arrivalTimeAtCurrAct = startAtPrevAct + transportTime;

		actArrTime = arrivalTimeAtCurrAct;
		actEndTime = arrivalTimeAtCurrAct;

		beginFirst = false;
	}

}
