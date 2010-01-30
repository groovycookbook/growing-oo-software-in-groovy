package unit.auctionsniper.xmpp

import java.util.logging.Logger

import auctionsniper.xmpp.LoggingXMPPFailureReporter
import org.gmock.GMockTestCase

class LoggingXMPPFailureReporterTest extends GMockTestCase {
    Logger logger = mock(Logger)
    private reporter = new LoggingXMPPFailureReporter(logger)

    void testWritesMessageTranslationFailureToLog() {
        logger.severe('<auction id> Could not translate message "bad message" because "java.lang.Exception: an exception"')
        play {
            reporter.cannotTranslateMessage("auction id", "bad message", new Exception("an exception"))
        }
    }
}
