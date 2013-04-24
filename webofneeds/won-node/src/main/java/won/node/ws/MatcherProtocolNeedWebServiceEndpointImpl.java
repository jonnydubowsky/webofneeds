/*
 * Copyright 2012  Research Studios Austria Forschungsges.m.b.H.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package won.node.ws;

import com.hp.hpl.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import won.protocol.exception.IllegalMessageForNeedStateException;
import won.protocol.exception.NoSuchConnectionException;
import won.protocol.exception.NoSuchNeedException;
import won.protocol.matcher.MatcherProtocolNeedService;
import won.protocol.model.Connection;
import won.protocol.model.Need;
import won.protocol.util.LazySpringBeanAutowiringSupport;
import won.protocol.ws.MatcherProtocolNeedWebServiceEndpoint;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.net.URI;
import java.util.Collection;

/**
 * User: fkleedorfer
 * Date: 14.11.12
 */
@WebService(serviceName = "matcherProtocol", targetNamespace = "http://www.webofneeds.org/protocol/matcher/soap/1.0/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class MatcherProtocolNeedWebServiceEndpointImpl extends LazySpringBeanAutowiringSupport implements MatcherProtocolNeedWebServiceEndpoint {
    @Autowired
    private MatcherProtocolNeedService matcherProtocolNeedService;

    @Override
    @WebMethod
    public void hint(
            @WebParam(name = "needURI") final URI needURI,
            @WebParam(name = "otherNeedURI") final URI otherNeedURI,
            @WebParam(name = "score") final double score,
            @WebParam(name = "originatorURI") final URI originatorURI) throws NoSuchNeedException, IllegalMessageForNeedStateException {
        wireDependenciesLazily();
        matcherProtocolNeedService.hint(needURI, otherNeedURI, score, originatorURI);
    }

    @Override
    @WebMethod(exclude = true)
    public void setMatcherProtocolNeedService(final MatcherProtocolNeedService matcherProtocolNeedService) {
        this.matcherProtocolNeedService = matcherProtocolNeedService;
    }

    @Override
    protected boolean isWired() {
        return this.matcherProtocolNeedService != null;
    }
}
