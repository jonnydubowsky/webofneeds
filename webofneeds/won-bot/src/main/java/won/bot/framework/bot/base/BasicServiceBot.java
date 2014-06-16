/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package won.bot.framework.bot.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import won.bot.framework.component.needproducer.NeedProducer;
import won.bot.framework.component.nodeurisource.NodeURISource;
import won.matcher.component.MatcherNodeURISource;
import won.matcher.protocol.impl.MatcherProtocolMatcherServiceImplJMSBased;
import won.protocol.matcher.MatcherProtocolNeedServiceClientSide;
import won.protocol.owner.OwnerProtocolNeedServiceClientSide;
import won.protocol.util.linkeddata.LinkedDataSource;

/**
 * Base class for bots containing basic services.
 */
public abstract class BasicServiceBot extends BaseBot
{
  private NodeURISource nodeURISource;
  private MatcherNodeURISource matcherNodeURISource;
  private NeedProducer needProducer;
  private OwnerProtocolNeedServiceClientSide ownerService;
  private MatcherProtocolNeedServiceClientSide matcherProtocolNeedServiceClient;
  private MatcherProtocolMatcherServiceImplJMSBased matcherProtocolMatcherService;



  private LinkedDataSource linkedDataSource;

  protected NodeURISource getNodeURISource()
  {
    return nodeURISource;
  }

  protected MatcherNodeURISource getMatcheNodeURISource(){
    return matcherNodeURISource;
  }

  @Qualifier("default")
  @Autowired(required = true)
  public void setMatcherNodeURISource(final MatcherNodeURISource matcherNodeURISource) {
    this.matcherNodeURISource = matcherNodeURISource;
  }

  @Qualifier("default")
  @Autowired(required = true)
  public void setNodeURISource(final NodeURISource nodeURISource)
  {
    this.nodeURISource = nodeURISource;
  }

  protected OwnerProtocolNeedServiceClientSide getOwnerService()
  {
    return ownerService;
  }

  protected MatcherProtocolNeedServiceClientSide getMatcherProtocolNeedServiceClient(){
      return matcherProtocolNeedServiceClient;
  }

  protected MatcherProtocolMatcherServiceImplJMSBased getMatcherProtocolMatcherService(){
    return matcherProtocolMatcherService;
  }



  @Qualifier("default")
  @Autowired(required = true)
  public void setOwnerService(final OwnerProtocolNeedServiceClientSide ownerService)
  {
    this.ownerService = ownerService;
  }

  @Qualifier("default")
  @Autowired(required = true)
  public void setMatcherProtocolNeedServiceClient(final MatcherProtocolNeedServiceClientSide matcherProtocolNeedServiceClient){
      this.matcherProtocolNeedServiceClient = matcherProtocolNeedServiceClient;
  }

  @Qualifier("default")
  @Autowired(required = true)
  public void setMatcherProtocolMatcherService(final MatcherProtocolMatcherServiceImplJMSBased
                                                 matcherProtocolMatcherService){
    this.matcherProtocolMatcherService = matcherProtocolMatcherService;
  }

  protected NeedProducer getNeedProducer()
  {
    return needProducer;
  }

  @Qualifier("default")
  @Autowired(required = true)
  public void setNeedProducer(final NeedProducer needProducer)
  {
    this.needProducer = needProducer;
  }

  public LinkedDataSource getLinkedDataSource() { return linkedDataSource; }

  @Qualifier("default")
  @Autowired(required = true)
  public void setLinkedDataSource(final LinkedDataSource linkedDataSource) { this.linkedDataSource = linkedDataSource; }
}