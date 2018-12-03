package org.ignitionmdc.simpletaghistoryprovider;

import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataQuality;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataTypeClass;
import com.inductiveautomation.ignition.gateway.sqltags.history.query.ColumnQueryDefinition;
import com.inductiveautomation.ignition.gateway.sqltags.history.query.HistoryNode;
import com.inductiveautomation.ignition.gateway.sqltags.history.query.HistoryQueryExecutor;
import com.inductiveautomation.ignition.gateway.sqltags.history.query.QueryController;
import com.inductiveautomation.ignition.gateway.sqltags.history.query.columns.ProcessedHistoryColumn;

import java.util.*;

public class OneShotHistoryQueryExecutor implements HistoryQueryExecutor {

    QueryController controller;
    List<HistoryNode> columnNodes;
    List<ColumnQueryDefinition> list;
    boolean processed = false;
    long maxTSInData = -1;
    Random random = new Random();

    public OneShotHistoryQueryExecutor(List<ColumnQueryDefinition> list, QueryController controller) {
        this.controller = controller;
        this.list = list;
        columnNodes = new ArrayList<>();

        // Initialize the columnNodes
        long blockSize = controller.getBlockSize();
        for (int colNdx = 0; colNdx < list.size(); colNdx++) {
            String col = list.get(colNdx).getPath().getLastPathComponent();
            //String col = controller.getQueryParameters().getAliases().get(colNdx);
            HistoryNode node;
            ProcessedHistoryColumn column = new ProcessedHistoryColumn(col, blockSize <= 0);

            // Set the data types.  This would probably be more appropriate on initialize() if this step will take a while.
            // This is an example.  In a real implementation, you'll read and set the appropriate data type from the tag storage meta information
            column.setDataType(DataTypeClass.Float);
            columnNodes.add(column);

        }

    }

    // Provides the data structure to Ignition where we have our nodes stored
    @Override
    public List<? extends HistoryNode> getColumnNodes() {
        return columnNodes;
    }

    @Override
    public void initialize() throws Exception {
    }

    @Override
    public int getEffectiveWindowSizeMS() {
        // This rate is used by default if "Natural" is selected
        return 1000;
    }

    @Override
    public void startReading() throws Exception {
        // For this example, we'll 'read' everything in one shot, so we start and finish here.
        Date startDate = controller.getQueryParameters().getStartDate();
        Date endDate = controller.getQueryParameters().getEndDate();

        List<QualifiedValue> values;
        // Add all the other columns
        for (int colNdx = 0; colNdx < list.size(); colNdx++) {
            values = new ArrayList<>();
            values.add(new BasicQualifiedValue(random.nextFloat(), DataQuality.GOOD_DATA, startDate));
            values.add(new BasicQualifiedValue(random.nextFloat(), DataQuality.GOOD_DATA, addMinutes(startDate, 1)));
            values.add(new BasicQualifiedValue(random.nextFloat(), DataQuality.GOOD_DATA, addMinutes(startDate, 2)));
            values.add(new BasicQualifiedValue(random.nextFloat(), DataQuality.GOOD_DATA, endDate));
            ((ProcessedHistoryColumn) columnNodes.get(colNdx)).put(values);
        }
        maxTSInData = endDate.getTime();

    }

    @Override
    public void endReading() {
    }

    @Override
    public boolean hasMore() {
        return !processed;
    }

    @Override
    public long nextTime() {
        // Let them know that anything they ask for, we can give
        return Long.MAX_VALUE;
    }

    @Override
    public long processData() throws Exception {
        processed = true;
        // This should be the timestamp of the latest available record.
        return maxTSInData;
    }


    private Date addMinutes(Date date, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }
}
