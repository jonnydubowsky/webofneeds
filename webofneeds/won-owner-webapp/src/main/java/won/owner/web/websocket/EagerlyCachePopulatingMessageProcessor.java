package won.owner.web.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import won.protocol.message.WonMessage;
import won.protocol.message.processor.WonMessageProcessor;
import won.protocol.message.processor.exception.WonMessageProcessingException;
import won.protocol.util.WonRdfUtils;
import won.protocol.util.linkeddata.CachingLinkedDataSource;
import won.protocol.util.linkeddata.LinkedDataSource;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

public class EagerlyCachePopulatingMessageProcessor implements WonMessageProcessor {

private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private LinkedDataSource linkedDataSourceOnBehalfOfNeed;
	
	@Autowired
	private ThreadPoolExecutor parallelRequestsThreadpool;
	
	@Override
	public WonMessage process(WonMessage message) throws WonMessageProcessingException {
		if (this.linkedDataSourceOnBehalfOfNeed != null && this.linkedDataSourceOnBehalfOfNeed instanceof CachingLinkedDataSource) {
			logger.debug("eagerly fetching delivery chain for mesasge {} into cache", message.getMessageURI());
			URI requester = message.getReceiverNeedURI();
			((CachingLinkedDataSource) linkedDataSourceOnBehalfOfNeed).addToCache(message.getCompleteDataset(),
					message.getMessageURI(), requester);
			//load the original message(s) into cache, too
			Set<URI> toLoad = new HashSet<URI>();
			addIfNotNull(toLoad, message.getIsRemoteResponseToMessageURI());
			addIfNotNull(toLoad, message.getIsResponseToMessageURI());
			addIfNotNull(toLoad, message.getCorrespondingRemoteMessageURI());
			List<URI> previous = WonRdfUtils.MessageUtils.getPreviousMessageUrisIncludingRemote(message);
			addIfNotNull(toLoad, previous);
			parallelRequestsThreadpool.submit(() -> toLoad.parallelStream().forEach(uri -> linkedDataSourceOnBehalfOfNeed.getDataForResource(uri, requester)));
		}
		return message;
	}
	
	public void setLinkedDataSourceOnBehalfOfNeed(LinkedDataSource linkedDataSourceOnBehalfOfNeed) {
		this.linkedDataSourceOnBehalfOfNeed = linkedDataSourceOnBehalfOfNeed;
	}
	
	public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
		this.parallelRequestsThreadpool = threadPoolExecutor;
	}

	private void addIfNotNull(Set<URI> uris, URI uri) {
		if (uri != null) {
			uris.add(uri);
		}
	}
	
	private void addIfNotNull(Set<URI> uris, List<URI> urisToAdd) {
		if (urisToAdd != null) {
			uris.addAll(urisToAdd);
		}
	}

}
