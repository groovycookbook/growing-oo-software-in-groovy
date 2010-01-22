package endtoend.auctionsniper

import org.junit.Test
import org.junit.After
import harness.auctionsniper.FakeAuctionServer
import harness.auctionsniper.ApplicationRunner
import static harness.auctionsniper.ApplicationRunner.*

class AuctionSniperEndToEndTest {
    private final FakeAuctionServer auction = new FakeAuctionServer("item-54321")
    private final FakeAuctionServer auction2 = new FakeAuctionServer("item-65432")
    private ApplicationRunner application = new ApplicationRunner()

    @Test
    void sniperJoinsAuctionUntilAuctionCloses() {
        auction.startSellingItem()
        application.startBiddingIn(auction)
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)
        auction.announceClosed()
        application.hasShownSniperHasLostAuction(auction, 0, 0)
    }

    @Test
    void sniperMakesAHigherBidButLoses () {
        auction.startSellingItem()

        application.startBiddingIn(auction)
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)
        auction.reportPrice(1000, 98, "other bidder")
        application.hasShownSniperIsBidding(auction, 1000, 1098)

        auction.hasReceivedBid(1098, SNIPER_XMPP_ID)

        auction.announceClosed()
        application.hasShownSniperHasLostAuction(auction, 1000, 1098)
    }

    @Test
    void sniperWinsAnAuctionByBiddingHigher () {
        auction.startSellingItem()

        application.startBiddingIn(auction)
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)
        auction.reportPrice(1000, 98, "other bidder")
        application.hasShownSniperIsBidding(auction, 1000, 1098)

        auction.hasReceivedBid(1098, SNIPER_XMPP_ID)

        auction.reportPrice(1098, 97, SNIPER_XMPP_ID)
        application.hasShownSniperIsWinning(auction, 1098)

        auction.announceClosed()
        application.hasShownSniperHasWonAuction(auction, 1098)
    }

    @Test
    void sniperBidsForMultipleItems () {
        auction.startSellingItem()
        auction2.startSellingItem()

        application.startBiddingIn(auction, auction2)
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)
        auction2.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)

        auction.reportPrice(1000, 98, "other bidder")
        auction.hasReceivedBid(1098, SNIPER_XMPP_ID)

        auction2.reportPrice(500, 21, "other bidder")
        auction2.hasReceivedBid(521, SNIPER_XMPP_ID)

        auction.reportPrice(1098, 97, SNIPER_XMPP_ID)
        auction2.reportPrice(521, 22, SNIPER_XMPP_ID)

        application.hasShownSniperIsWinning(auction, 1098)
        application.hasShownSniperIsWinning(auction2, 521)

        auction.announceClosed()
        auction2.announceClosed()

        application.hasShownSniperHasWonAuction(auction, 1098)
        application.hasShownSniperHasWonAuction(auction2, 521)
    }

    @Test
    void sniperLosesAnAuctionWhenThePriceIsTooHigh () {
        auction.startSellingItem()

        application.startBiddingWithStopPrice(auction, 1100)
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)
        auction.reportPrice(1000, 98, "other bidder")
        application.hasShownSniperIsBidding(auction, 1000, 1098)

        auction.hasReceivedBid(1098, SNIPER_XMPP_ID)

        auction.reportPrice(1197, 10, "third party")
        application.hasShownSniperIsLosing(auction, 1197, 1098)

        auction.reportPrice(1207, 10, "fourth party")
        application.hasShownSniperIsLosing(auction, 1207, 1098)
        auction.announceClosed()
        application.hasShownSniperHasLostAuction(auction, 1207, 1098)
    }

    @Test
    void sniperReportsInvalidAuctionMessageAndStopsRespondingToEvents () {
        String brokenMessage = "a broken message"
        auction.startSellingItem()
        auction2.startSellingItem()

        application.startBiddingIn(auction, auction2)
        auction.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)

        auction.reportPrice(500, 20, "other bidder")
        auction.hasReceivedBid(520, SNIPER_XMPP_ID)

        auction.sendInvalidMessageContaining(brokenMessage)
        application.hasShownSniperHasFailed(auction)

        auction.reportPrice(520, 21, "other bidder")
        waitForAnotherAuctionEvent()

        application.reportsInvalidMessage(auction, brokenMessage)
        application.hasShownSniperHasFailed(auction)
    }

    private waitForAnotherAuctionEvent() {
        auction2.hasReceivedJoinRequestFrom(SNIPER_XMPP_ID)
        auction2.reportPrice(600, 6, "other bidder")
        application.hasShownSniperIsBidding(auction2, 600, 606)
    }

    @After
    void stopAuction() {
        auction.stop()
        auction2.stop()
    }

    @After
    void stopApplication() {
        application.stop()
    }
} 
