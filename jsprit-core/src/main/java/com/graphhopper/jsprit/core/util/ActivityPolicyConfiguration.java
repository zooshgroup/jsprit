package com.graphhopper.jsprit.core.util;

public class ActivityPolicyConfiguration {

	private Double factoryUnloadTimeFactor;
	private Double factoryStaticTime;

	public ActivityPolicyConfiguration(String factoryUnloadTimeFactor, String factoryStaticTime) {
		if (factoryUnloadTimeFactor != null) {
			this.factoryUnloadTimeFactor = Double.parseDouble(factoryUnloadTimeFactor);
		}

		if (factoryUnloadTimeFactor != null) {
			this.factoryStaticTime = Double.parseDouble(factoryStaticTime);
		}
	}

	public Double getFactoryUnloadTimeFactor() {
		return factoryUnloadTimeFactor;
	}

	public void setFactoryUnloadTimeFactor(Double factoryUnloadTimeFactor) {
		this.factoryUnloadTimeFactor = factoryUnloadTimeFactor;
	}

	public Double getFactoryStaticTime() {
		return factoryStaticTime;
	}

	public void setFactoryStaticTime(Double factoryStaticTime) {
		this.factoryStaticTime = factoryStaticTime;
	}

}
