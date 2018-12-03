package org.ignitionmdc.simpletaghistoryprovider;

import com.inductiveautomation.ignition.gateway.history.*;

import java.util.ArrayList;
import java.util.List;

public class EmptyDataSink implements DataSink {

    private String name;

    public EmptyDataSink(String name) {
        this.name = name;
    }

    @Override
    public String getPipelineName() {
        return name;
    }

    @Override
    public void startup() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public boolean isAccepting() {
        return true;
    }

    @Override
    public List<DataSinkInformation> getInfo() {
        return new ArrayList<>();
    }

    @Override
    public QuarantineManager getQuarantineManager() {
        return null;
    }

    @Override
    public void storeData(HistoricalData historicalData) throws Exception {

    }

    @Override
    public boolean acceptsData(HistoryFlavor historyFlavor) {
        return true;
    }

    @Override
    public boolean isLicensedFor(HistoryFlavor historyFlavor) {
        return true;
    }
}
