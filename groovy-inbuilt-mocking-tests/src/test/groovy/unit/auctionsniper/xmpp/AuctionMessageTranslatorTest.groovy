package unit.auctionsniper.xmpp

import org.jivesoftware.smack.packet.Message

import auctionsniper.AuctionEventListener
import auctionsniper.AuctionEventListener.PriceSource
import auctionsniper.xmpp.AuctionMessageTranslator
import auctionsniper.xmpp.XMPPFailureReporter
import groovy.mock.interceptor.MockFor

class AuctionMessageTranslatorTest extends GroovyTestCase {
    private SNIPER_ID = "sniper id"
    private UNUSED_CHAT = null
    def failureReporterMock = new MockFor(XMPPFailureReporter)
    XMPPFailureReporter failureReporter
    def listenerMock = new MockFor(AuctionEventListener)
    AuctionEventListener listener
    AuctionMessageTranslator translator

    void testNotifiesAuctionClosedWhenCloseMessageReceived() {
        listenerMock.demand.auctionClosed {}
        createTranslator()
        translator.processMessage(UNUSED_CHAT, new Message(body: "SOLVersion: 1.1; Event: CLOSE;"))
        verifyMocks()
    }

    void testNotifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() {
        listenerMock.demand.currentPrice{ price, increment, priceSource ->
            assert price == 192 && increment == 7 && priceSource == PriceSource.FromOtherBidder }
        createTranslator()
        translator.processMessage(UNUSED_CHAT, new Message(body:
            "SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;"))
        verifyMocks()
    }

    void testNotifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
        listenerMock.demand.currentPrice{ price, increment, priceSource ->
            assert price == 192 && increment == 7 && priceSource == PriceSource.FromSniper }
        createTranslator()
        translator.processMessage(UNUSED_CHAT, new Message(body:
            "SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: $SNIPER_ID;"))
        verifyMocks()
    }

    void testNotifiesAuctionFailedWhenBadMessageReceived() {
        def badMessage = "a bad message"
        expectFailureWithMessage(badMessage)
        createTranslator()
        translator.processMessage(UNUSED_CHAT, new Message(body: badMessage))
        verifyMocks()
    }

    void testNotifiesAuctionFailedWhenEventTypeMissing() {
        def badMessage = "SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: $SNIPER_ID;"
        expectFailureWithMessage(badMessage)
        createTranslator()
        translator.processMessage(UNUSED_CHAT, new Message(body: badMessage))
        verifyMocks()
    }

    private createTranslator() {
        listener = listenerMock.proxyDelegateInstance()
        failureReporter = failureReporterMock.proxyDelegateInstance()
        translator = new AuctionMessageTranslator(SNIPER_ID, listener, failureReporter)
    }

    private verifyMocks() {
        listenerMock.verify(listener)
        failureReporterMock.verify(failureReporter)
    }

    private expectFailureWithMessage(badMessage) {
        listenerMock.demand.auctionFailed{}
        failureReporterMock.demand.cannotTranslateMessage{ auctionId, failedMessage, exception ->
                assert auctionId == SNIPER_ID && failedMessage == badMessage && exception instanceof Exception
        }
    }
}
