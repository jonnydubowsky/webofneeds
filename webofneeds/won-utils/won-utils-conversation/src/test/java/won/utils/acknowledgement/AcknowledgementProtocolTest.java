package won.utils.acknowledgement;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import won.protocol.agreement.HighlevelFunctionFactory;
import won.protocol.util.RdfUtils;

import java.io.IOException;
import java.io.InputStream;

public class AcknowledgementProtocolTest {

    private static final String inputFolder = "/won/utils/acknowledgement/input/";
    private static final String expectedOutputFolder = "/won/utils/acknowledgement/expected/";

    @BeforeClass
    public static void setLogLevel() {
    	Logger root = (Logger)LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    	root.setLevel(Level.INFO);	
    }

    @Test
    public void oneLocalMessageFailsLocallyTest () throws IOException {
        Dataset input = loadDataset( inputFolder + "one-local-message-fails-locally.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "one-local-message-fails-locally.trig");
        test(input,expectedOutput);
    }

    @Test
    public void oneLocalMessageFailsLocallyUnrealisticTest () throws IOException {
        Dataset input = loadDataset( inputFolder + "one-local-message-fails-locally-unrealistic.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "one-local-message-fails-locally-unrealistic.trig");
        test(input,expectedOutput);
    }

    @Test
    public void oneLocalMessageWithoutRemoteRemoteResponseTest () throws IOException {
        Dataset input = loadDataset( inputFolder + "one-local-message-without-remote-remote-response.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "one-local-message-without-remote-remote-response.trig");
        test(input,expectedOutput);
    }


    @Test
    public void oneLocalMessageWithoutRemoteResponseTest () throws IOException {
        Dataset input = loadDataset( inputFolder + "one-local-message-without-remote-response.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "one-local-message-without-remote-response.trig");
        test(input,expectedOutput);
    }

    @Test
    public void oneLocalMessageWithoutRemoteMessageTest () throws IOException {
        Dataset input = loadDataset( inputFolder + "one-local-message-without-remote-message.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "one-local-message-without-remote-message.trig");
        test(input,expectedOutput);
    }



    @Test
    public void everythingAcknowledgedTest () throws IOException {
        Dataset input = loadDataset( inputFolder + "all-messages-acknowledged.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "all-messages-acknowledged.trig");
        test(input,expectedOutput);
    }

    @Test
    public void oneLocalMessageFailsRemotelyTest () throws IOException {
        Dataset input = loadDataset( inputFolder + "one-local-message-fails-remotely.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "one-local-message-fails-remotely.trig");
        test(input,expectedOutput);
    }
    
    @Test
    public void twoProposalsTwoAgreementsBothCancelled () throws IOException {
        Dataset input = loadDataset( inputFolder + "twoProposalsTwoAgreementsBothCancelled.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "twoProposalsTwoAgreementsBothCancelled.trig");
        test(input,expectedOutput);
    }
    
    @Test
    public void twoProposalsTwoAgreementsOneCancelled () throws IOException {
        Dataset input = loadDataset( inputFolder + "twoProposalsTwoAgreementsOneCancelled.trig");
        Dataset expectedOutput = loadDataset( expectedOutputFolder + "twoProposalsTwoAgreementsOneCancelled.trig");
        test(input,expectedOutput);
    }
    
    public void test(Dataset input, Dataset expectedOutput) {

        // check that the computed dataset is the expected one
        Dataset actual = HighlevelFunctionFactory.getAcknowledgedSelection().apply(input);
        //TODO: remove before checking in
        RdfUtils.Pair<Dataset> diff = RdfUtils.diff(expectedOutput, actual);
        if (diff.getFirst().isEmpty() && diff.getSecond().isEmpty()) {
        } else {
            System.out.println("diff - only in expected:");
            RDFDataMgr.write(System.out, diff.getFirst(), Lang.TRIG);
            System.out.println("diff - only in actual:");
            RDFDataMgr.write(System.out, diff.getSecond(), Lang.TRIG);
        }
        Assert.assertTrue(RdfUtils.isIsomorphicWith(expectedOutput, actual));
    }

    private Dataset loadDataset(String path) throws IOException {

        InputStream is = null;
        Dataset dataset = null;
        try {
            is = getClass().getResourceAsStream(path);
            dataset = DatasetFactory.create();
            RDFDataMgr.read(dataset, is, RDFFormat.TRIG.getLang());
        } finally {
            if (is != null) {
                is.close();
            }
        }

        return dataset;
    }
    
    
}
