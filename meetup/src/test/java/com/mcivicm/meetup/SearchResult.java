package com.mcivicm.meetup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhang on 2017/10/11.
 */

public class SearchResult {

    private List<GeoName> geonames = new ArrayList<>();

    //我猜测jackson是根据方法名来解析属性而不是根据字段名来解析属性，因为方法名为getGeoNames时根本解析不出来
    public List<GeoName> getGeonames() {
        return geonames;
    }

    public void setGeonames(List<GeoName> geonames) {
        this.geonames = geonames;
    }

}
