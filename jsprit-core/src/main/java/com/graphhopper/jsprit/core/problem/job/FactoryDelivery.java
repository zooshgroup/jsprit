package com.graphhopper.jsprit.core.problem.job;

public class FactoryDelivery extends Service {

    public static class Builder extends Service.Builder<FactoryDelivery> {

        /**
         * Returns a new instance of builder that builds a delivery.
         *
         * @param id the id of the delivery
         * @return the builder
         */
        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        Builder(String id) {
            super(id);
        }


        public Builder setMaxTimeInVehicle(double maxTimeInVehicle){
        	throw new UnsupportedOperationException("maxTimeInVehicle is not supported for FactoryDelivery");
        }

        /**
         * Builds Delivery.
         *
         * @return delivery
         * @throws IllegalArgumentException if neither locationId nor coord is set
         */
        @Override
        public FactoryDelivery build() {
            if (location == null) throw new IllegalArgumentException("location is missing");
            this.setType("factory_delivery");
            super.capacity = super.capacityBuilder.build();
            super.skills = super.skillBuilder.build();
            return new FactoryDelivery(this);
        }

    }

    FactoryDelivery(Builder builder) {
        super(builder);

    }

}
