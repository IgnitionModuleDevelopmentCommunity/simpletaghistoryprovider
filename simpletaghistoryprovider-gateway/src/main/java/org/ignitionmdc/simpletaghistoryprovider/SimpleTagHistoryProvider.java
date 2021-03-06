package org.ignitionmdc.simpletaghistoryprovider;

import com.inductiveautomation.ignition.common.*;
import com.inductiveautomation.ignition.common.browsing.*;
import com.inductiveautomation.ignition.common.model.values.BasicQuality;
import com.inductiveautomation.ignition.common.model.values.Quality;
import com.inductiveautomation.ignition.common.opc.OPCBrowseElement;
import com.inductiveautomation.ignition.common.sqltags.history.Aggregate;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.TagPathTree;
import com.inductiveautomation.ignition.common.sqltags.model.types.TagQuality;
import com.inductiveautomation.ignition.common.sqltags.parser.BasicTagPath;
import com.inductiveautomation.ignition.common.util.Timeline;
import com.inductiveautomation.ignition.common.util.TimelineSet;
import com.inductiveautomation.ignition.gateway.model.ProfileStatus;
import com.inductiveautomation.ignition.gateway.sqltags.history.TagHistoryProvider;
import com.inductiveautomation.ignition.gateway.sqltags.history.TagHistoryProviderInformation;
import com.inductiveautomation.ignition.gateway.sqltags.history.query.ColumnQueryDefinition;
import com.inductiveautomation.ignition.gateway.sqltags.history.query.HistoryQueryExecutor;
import com.inductiveautomation.ignition.gateway.sqltags.history.query.QueryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.tree.Tree;

import java.util.*;

public class SimpleTagHistoryProvider implements TagHistoryProvider
{
  
  private final Logger logger = LoggerFactory.getLogger(getClass());
  private String name;
  
  public SimpleTagHistoryProvider(String name)
  {
    this.name = name;
  }
  
  @Override
  public void startup()
  {
    logger.info("startup() called");
  }
  
  @Override
  public void shutdown()
  {
    logger.info("shutdown() called");
  }
  
  @Override
  public String getName()
  {
    logger.info("getName() called");
    return name;
  }
  
  @Override
  public List<Aggregate> getAvailableAggregates()
  {
    logger.info("getAvailableAggregates() called");
    return new ArrayList<Aggregate>();
  }
  
  @Override
  public ProfileStatus getStatus()
  {
    logger.info("getStatus() called");
    return ProfileStatus.RUNNING;
  }
  
  @Override
  public TagHistoryProviderInformation getStatusInformation()
  {
    logger.info("getStatusInformation() called");
    return TagHistoryProviderInformation.newBuilder().allowsStorage(false).status(getStatus()).name(getName()).build();
  }
  
  @Override
  public HistoryQueryExecutor createQuery(List<ColumnQueryDefinition> list, QueryController queryController)
  {
    logger.info("createQuery(list, queryController) called.  list: " + list.toString() + ", queryController(HistoryWriter): " + queryController.toString());
    for (ColumnQueryDefinition cqd : list)
    {
      logger.info("  colDef: " + cqd.toString());
      logger.info("    name: " + cqd.getColumnName());
    }
    logger.info("    queryId: " + queryController.getQueryId());
    logger.info(" queryStart: " + queryController.getQueryStart());
    logger.info("   queryEnd: " + queryController.getQueryEnd());
    logger.info("  blockSize: " + queryController.getBlockSize());
    logger.info("queryParams: " + queryController.getQueryParameters().toString());
    
    return new OneShotHistoryQueryExecutor(list, queryController);
    
  }
  
  
  
  @Override
  public BrowseResults<Result> browse(QualifiedPath qualifiedPath, BrowseFilter browseFilter)
  {
    //logger.info("HistoryQueryExecutor.browse(qualifiedPath, browseFilter) called.  qualifiedPath: "+qualifiedPath.toString()+", browseFilter: "+browseFilter.toString());
    BrowseResults<Result> result = new BrowseResults<>();
    ArrayList<Result> list = new ArrayList<>();
    
    // use the name of this history provider
    String histProvider = getName();
    
    // the history queries will not use these next two values
    // however, you can provide them if you want, for your reference
    String system = "default";
    String tagProvider = "default";
    
    // this will be used to simulate the REST call in an extremely simplified way
    // the qualifiedPath will be given to the browse() function, and the tags will be returned based on that path
    HashMap<String, String> RESTCall = new HashMap<String, String>();
    RESTCall.put("histprov:ExampleProvider", "folder;[ExampleProvider/default:default]SCC1");
    RESTCall.put("histprov:ExampleProvider:/drv:default:default:/tag:SCC1", "folder;[ExampleProvider/default:default]SCC1/SCC1_SKD0001");
    RESTCall.put("histprov:ExampleProvider:/drv:default:default:/tag:SCC1/SCC1_SKD0001", "tag;[ExampleProvider/default:default]SCC1/SCC1_SKD0001/BACK_PANEL_TEMP1");
    // simulated REST call
    String tagRESTResult = RESTCall.get(qualifiedPath.toString());
    
    // generate tags using the simulated call
    TagResult tagResult = new TagResult();
    tagResult.setType(WellKnownPathTypes.Tag);
    // check if the returned value is a tag or a folder
    
    String type = tagRESTResult.split(";")[0];
    String path = tagRESTResult.split(";")[1];
    
    if(type.startsWith("folder"))
    {
      tagResult.setHasChildren(true);
    }
    else
    {
      tagResult.setHasChildren(false);
    }
    
    QualifiedPath fullTagPath = QualifiedPathUtils.toPathFromHistoricalString(path);
    
    tagResult.setPath(fullTagPath);
    
    list.add(tagResult);
    
    result.setResults(list);
    result.setResultQuality(TagQuality.GOOD);
    return result;
  }
  
  @Override
  public TimelineSet queryDensity(List<QualifiedPath> list, Date date, Date date1, String s) throws Exception
  {
    logger.info("HistoryQueryExecutor.timelineSet(list, date, date1, s) called.  list: " + list.toString() + ", date: " + date.toString() + ", date1: " + date1.toString() + ", s: " + s);
    
    ArrayList<Timeline> timelines = new ArrayList<>();
    
    Timeline timeline = new Timeline();
    
    // first segment starts at the beginning of the timeline and lasts for 10 minutes
    long startTime = date.getTime();
    long endTime = startTime + 10 * 60 * 1000;
    timeline.addSegment(startTime, endTime);
    
    // second segment will start 10 minutes after the first segment, and last for 20 minutes
    startTime = endTime + 10 * 60 * 1000;
    endTime = startTime + 20 * 60 * 1000;
    timeline.addSegment(startTime, endTime);
    
    timelines.add(timeline);
    
    TimelineSet timelineSet = new TimelineSet(timelines);
    return timelineSet;
  }
}
