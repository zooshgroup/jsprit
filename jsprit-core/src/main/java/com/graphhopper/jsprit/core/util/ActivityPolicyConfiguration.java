package com.graphhopper.jsprit.core.util;

public class ActivityPolicyConfiguration {

	private double factoryUnloadTimeFactor;
	private double factoryStaticTime;

	public ActivityPolicyConfiguration(double factoryUnloadTimeFactor, double factoryStaticTime) {
		this.factoryUnloadTimeFactor = factoryUnloadTimeFactor;
		this.factoryStaticTime = factoryStaticTime;
	}

	public double getFactoryUnloadTimeFactor() {
		return factoryUnloadTimeFactor;
	}

	public void setFactoryUnloadTimeFactor(double factoryUnloadTimeFactor) {
		this.factoryUnloadTimeFactor = factoryUnloadTimeFactor;
	}

	public double getFactoryStaticTime() {
		return factoryStaticTime;
	}

	public void setFactoryStaticTime(double factoryStaticTime) {
		this.factoryStaticTime = factoryStaticTime;
	}

}
