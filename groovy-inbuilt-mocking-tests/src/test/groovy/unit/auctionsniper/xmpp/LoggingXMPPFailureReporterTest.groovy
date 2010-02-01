package unit.auctionsniper.xmpp

import java.util.logging.Logger

import auctionsniper.xmpp.LoggingXMPPFailureReporter

/**
 * Uses a custom hand-built mock instead of MockFor/StubFor.
 */
class LoggingXMPPFailureReporterTest extends GroovyTestCase {

    void testWritesMessageTranslationFailureToLog() {
        Object[] args = ['foo', null]
        def overrides = [severe:{ assert it == '<auction id> Could not translate message "bad message" because "java.lang.Exception: an exception"' }]
        Logger mockLogger = ProxyGenerator.INSTANCE.instantiateAggregateFromBaseClass(overrides, Logger, args)
        def reporter = new LoggingXMPPFailureReporter(mockLogger)
        reporter.cannotTranslateMessage("auction id", "bad message", new Exception("an exception"))
    }
}
