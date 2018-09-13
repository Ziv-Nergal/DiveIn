package com.example.user.divein;

public class Dive {

    public String site_name;
    public String description;
    public String bottom_type;
    public String max_depth;
    public String salinity_type;
    public String water_type;

    public String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Dive(String site_name,
                String description,
                String bottom_type,
                String max_depth,
                String salinity_type,
                String water_type,
                String id) {

        this.site_name = site_name;
        this.description = description;
        this.bottom_type = bottom_type;
        this.max_depth = max_depth;
        this.salinity_type = salinity_type;
        this.water_type = water_type;
        this.id = id;
    }

    public Dive() {}

    public String getSite_name() {
        return site_name;
    }

    public void setSite_name(String site_name) {
        this.site_name = site_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBottom_type() {
        return bottom_type;
    }

    public void setBottom_type(String bottom_type) {
        this.bottom_type = bottom_type;
    }

    public String getMax_depth() {
        return max_depth;
    }

    public void setMax_depth(String max_depth) {
        this.max_depth = max_depth;
    }

    public String getSalinity_type() {
        return salinity_type;
    }

    public void setSalinity_type(String salinity_type) {
        this.salinity_type = salinity_type;
    }

    public String getWater_type() {
        return water_type;
    }

    public void setWater_type(String water_type) {
        this.water_type = water_type;
    }
}
