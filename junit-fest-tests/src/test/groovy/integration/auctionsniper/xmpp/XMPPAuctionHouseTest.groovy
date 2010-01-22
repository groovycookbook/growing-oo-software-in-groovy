package integration.auctionsniper.xmpp

import static java.util.concurrent.TimeUnit.SECONDS

import java.util.concurrent.CountDownLatch

import org.junit.After
import org.junit.Before
import org.junit.Test

import harness.auctionsniper.ApplicationRunner
import harness.auctionsniper.FakeAuctionServer
import auctionsniper.Auction
import auctionsniper.AuctionEventListener
import auctionsniper.UserRequestListener.Item
import auctionsniper.xmpp.XMPPAuctionHouse

class XMPPAuctionHouseTest {
    private auctionServer = new FakeAuctionServer("item-54321")
    private XMPPAuctionHouse auctionHouse

    @Before void openConnection() {
        auctionHouse = XMPPAuctionHouse.connect(FakeAuctionServer.XMPP_HOSTNAME, ApplicationRunner.SNIPER_ID, ApplicationRunner.SNIPER_PASSWORD)
    }

    @After void closeConnection() {
        auctionHouse?.disconnect()
    }

    @Before void startAuction() {
        auctionServer.startSellingItem()
    }

    @After void stopAuction() {
        auctionServer.stop()
    }

    @Test void receivesEventsFromAuctionServerAfterJoining () {
        CountDownLatch auctionWasClosed = new CountDownLatch(1)
        Auction auction = auctionHouse.auctionFor(new Item(auctionServer.getItemId(), 567))
        auction.addAuctionEventListener(auctionClosedListener(auctionWasClosed))
        auction.join()
        auctionServer.hasReceivedJoinRequestFrom(ApplicationRunner.SNIPER_XMPP_ID)
        auctionServer.announceClosed()
        assert auctionWasClosed.await(4, SECONDS)
    }

    private AuctionEventListener auctionClosedListener (final CountDownLatch auctionWasClosed) {
        return new AuctionEventListener() {
            void auctionClosed() {
                auctionWasClosed.countDown()
            }

            void currentPrice(int price, int increment, AuctionEventListener.PriceSource priceSource) { }

            void auctionFailed() { }
        }
    }
}
