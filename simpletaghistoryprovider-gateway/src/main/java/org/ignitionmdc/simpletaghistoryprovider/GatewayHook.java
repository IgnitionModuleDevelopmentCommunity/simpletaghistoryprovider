package org.ignitionmdc.simpletaghistoryprovider;

import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.gateway.history.DataSink;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.sqltags.config.SQLTagHistoryProviderRecord;
import com.inductiveautomation.ignition.gateway.sqltags.config.SQLTagHistoryProviderType;
import com.inductiveautomation.ignition.gateway.sqltags.history.TagHistoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayHook extends AbstractGatewayModuleHook {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private GatewayContext context;
    private DataSink dataSink;

    @Override
    public void setup(GatewayContext gatewayContext) {
        this.context = gatewayContext;
        context.getTagManager().registerTagHistoryProvider(new SimpleTagHistoryProvider("ExampleProvider"));

        // Adding a DataSink so the TagHistoryProvider will show up in the tag's dropdown configuration
        dataSink = new EmptyDataSink("ExampleProvider");
        context.getHistoryManager().registerSink(dataSink);
    }

    @Override
    public void startup(LicenseState licenseState) {

    }

    @Override
    public void shutdown() {
        context.getTagManager().unregisterTagHistoryProvider("ExampleProvider");
        context.getHistoryManager().unregisterSink(dataSink, true);
    }

}
