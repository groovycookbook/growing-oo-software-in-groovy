package unit.auctionsniper.xmpp

import java.util.logging.Logger

import auctionsniper.xmpp.LoggingXMPPFailureReporter

/**
 * Uses a custom hand-built mock instead of MockFor/StubFor.
 */
class LoggingXMPPFailureReporterTest extends GroovyTestCase {

    void testWritesMessageTranslationFailureToLog() {
        def overrides = [severe:{ msg ->
            assert msg == '<auction id> Could not translate message "bad message" because "java.lang.Exception: an exception"' }]
        Object[] loggerConstructorArgs = ['dummyName', null /* no resource bundle required */]
        Logger mockLogger = ProxyGenerator.INSTANCE.instantiateAggregateFromBaseClass(overrides, Logger, loggerConstructorArgs)
        def reporter = new LoggingXMPPFailureReporter(mockLogger)
        reporter.cannotTranslateMessage("auction id", "bad message", new Exception("an exception"))
    }
}
