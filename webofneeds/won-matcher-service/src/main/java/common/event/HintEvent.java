package common.event;

import com.hp.hpl.jena.rdf.model.Model;

import java.io.Serializable;
import java.net.URI;

/**
 * Event is used to generate hints inside the matcher-service
 *
 * User: hfriedrich
 * Date: 23.06.2015
 */
public class HintEvent implements Serializable
{
  private String fromNeedUri;
  private String fromWonNodeUri;
  private String toNeedUri;
  private String toWonNodeUri;

  private String matcherUri;
  private double score;

  private URI generatedEventUri;
  private String serializedExplanationModel;
  private String serializationLangName;
  private String serializationLangContentType;

  public HintEvent(String fromWonNodeUri, String fromNeedUri, String toWonNodeUri,
                   String toNeedUri, String matcherUri, double score) {

    this.fromWonNodeUri = fromWonNodeUri;
    this.fromNeedUri = fromNeedUri;
    this.toWonNodeUri = toWonNodeUri;
    this.toNeedUri = toNeedUri;
    this.matcherUri = matcherUri;
    this.score = score;
  }

  public String getFromNeedUri() {
    return fromNeedUri;
  }

  public String getToNeedUri() {
    return toNeedUri;
  }

  public String getFromWonNodeUri() {
    return fromWonNodeUri;
  }

  public String getToWonNodeUri() {
    return toWonNodeUri;
  }

  public String getMatcherUri() {
    return matcherUri;
  }

  public double getScore() {
    return score;
  }

  public Model deserializeExplanationModel() { return null; }

  public URI getGeneratedEventUri() {
    return generatedEventUri;
  }

  public void setGeneratedEventUri(final URI generatedEventUri) {
    this.generatedEventUri = generatedEventUri;
  }

  @Override
  public HintEvent clone() {
    HintEvent e = new HintEvent(fromWonNodeUri, fromNeedUri, toWonNodeUri, toNeedUri, matcherUri, score);
    e.setGeneratedEventUri(this.getGeneratedEventUri());
    return e;
  }

  @Override
  public String toString() {
    return "HintEvent: (" + getFromWonNodeUri() + ", " + getFromNeedUri() + ", " + getToWonNodeUri() + getToNeedUri()
      + ", " + getMatcherUri() + ", " + getScore() + ")";
  }
}