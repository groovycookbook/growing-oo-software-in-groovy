package unit.auctionsniper.xmpp

import org.jivesoftware.smack.packet.Message

import auctionsniper.AuctionEventListener
import auctionsniper.AuctionEventListener.PriceSource
import auctionsniper.xmpp.AuctionMessageTranslator
import auctionsniper.xmpp.XMPPFailureReporter
import org.gmock.GMockTestCase

class AuctionMessageTranslatorTest extends GMockTestCase {
    private SNIPER_ID = "sniper id"
    private UNUSED_CHAT = null
    XMPPFailureReporter failureReporter = mock(XMPPFailureReporter)
    AuctionEventListener listener = mock(AuctionEventListener)
    private translator = new AuctionMessageTranslator(SNIPER_ID, listener, failureReporter)

    void testNotifiesAuctionClosedWhenCloseMessageReceived() {
        listener.auctionClosed()
        play {
            translator.processMessage(UNUSED_CHAT, new Message(body: "SOLVersion: 1.1; Event: CLOSE;"))
        }
    }

    void testNotifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() {
        listener.currentPrice(192, 7, PriceSource.FromOtherBidder)
        play {
            translator.processMessage(UNUSED_CHAT, new Message(body:
                "SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;"))
        }
    }

    void testNotifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
        listener.currentPrice(192, 7, PriceSource.FromSniper)
        play {
            translator.processMessage(UNUSED_CHAT, new Message(body:
                "SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: $SNIPER_ID;"))
        }
    }

    void testNotifiesAuctionFailedWhenBadMessageReceived() {
        def badMessage = "a bad message"
        expectFailureWithMessage(badMessage)
        play {
            translator.processMessage(UNUSED_CHAT, new Message(body: badMessage))
        }
    }

    void testNotifiesAuctionFailedWhenEventTypeMissing() {
        def badMessage = "SOLVersion: 1.1; CurrentPrice: 234; Increment: 5; Bidder: $SNIPER_ID;"
        expectFailureWithMessage(badMessage)
        play {
            translator.processMessage(UNUSED_CHAT, new Message(body: badMessage))
        }
    }

    private expectFailureWithMessage(badMessage) {
        listener.auctionFailed()
        failureReporter.cannotTranslateMessage(
                SNIPER_ID, badMessage,
                match{ it instanceof Exception })
    }
}
