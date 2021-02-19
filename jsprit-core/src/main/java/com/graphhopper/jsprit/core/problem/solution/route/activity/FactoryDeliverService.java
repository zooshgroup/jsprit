package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.FactoryDelivery;

public final class FactoryDeliverService extends AbstractActivity implements DeliveryActivity {

    private FactoryDelivery factoryDelivery;

    private Capacity capacity;

    private double arrTime;

    private double endTime;

    private double theoreticalEarliest = 0;

    private double theoreticalLatest = Double.MAX_VALUE;

    public FactoryDeliverService(FactoryDelivery factoryDelivery) {
        super();
        this.factoryDelivery = factoryDelivery;
        capacity = Capacity.invert(factoryDelivery.getSize());
    }

    private FactoryDeliverService(FactoryDeliverService deliveryActivity) {
        this.factoryDelivery = deliveryActivity.getJob();
        this.arrTime = deliveryActivity.getArrTime();
        this.endTime = deliveryActivity.getEndTime();
        capacity = deliveryActivity.getSize();
        setIndex(deliveryActivity.getIndex());
        this.theoreticalEarliest = deliveryActivity.getTheoreticalEarliestOperationStartTime();
        this.theoreticalLatest = deliveryActivity.getTheoreticalLatestOperationStartTime();
    }

    @Override
    public String getName() {
        return factoryDelivery.getType();
    }

    @Override
    public Location getLocation() {
        return factoryDelivery.getLocation();
    }

    @Override
    public void setTheoreticalEarliestOperationStartTime(double earliest) {
        theoreticalEarliest = earliest;
    }

    @Override
    public void setTheoreticalLatestOperationStartTime(double latest) {
        theoreticalLatest = latest;
    }


    @Override
    public double getTheoreticalEarliestOperationStartTime() {
        return theoreticalEarliest;
    }

    @Override
    public double getTheoreticalLatestOperationStartTime() {
        return theoreticalLatest;
    }

    @Override
    public double getOperationTime() {
        return factoryDelivery.getServiceDuration();
    }

    @Override
    public double getArrTime() {
        return arrTime;
    }

    @Override
    public double getEndTime() {
        return endTime;
    }

    @Override
    public void setArrTime(double arrTime) {
        this.arrTime = arrTime;
    }

    @Override
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    @Override
    public TourActivity duplicate() {
        return new FactoryDeliverService(this);
    }

    @Override
    public FactoryDelivery getJob() {
        return factoryDelivery;
    }

    public String toString() {
        return "[type=" + getName() + "][locationId=" + getLocation().getId()
            + "][size=" + getSize().toString()
            + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
            + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
    }

    @Override
    public Capacity getSize() {
        return capacity;
    }
}
