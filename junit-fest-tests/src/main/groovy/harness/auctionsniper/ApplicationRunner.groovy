package harness.auctionsniper

import static auctionsniper.ui.SnipersTableModel.textFor
import static harness.auctionsniper.FakeAuctionServer.XMPP_HOSTNAME

import javax.swing.SwingUtilities

import auctionsniper.Main
import auctionsniper.SniperState
import auctionsniper.ui.MainWindow

class ApplicationRunner {
    public static final String SNIPER_ID = "sniper"
    public static final String SNIPER_PASSWORD = "sniper"
    public static final String SNIPER_XMPP_ID = SNIPER_ID + "@" + XMPP_HOSTNAME + "/Auction"

    private AuctionLogDriver logDriver = new AuctionLogDriver()
    private AuctionSniperDriver driver

    void startBiddingIn(final FakeAuctionServer... auctions) {
        startSniper()
        for (FakeAuctionServer auction : auctions) {
            openBiddingFor(auction, Integer.MAX_VALUE)
        }
    }

    void startBiddingWithStopPrice(FakeAuctionServer auction, int stopPrice) {
        startSniper()
        openBiddingFor(auction, stopPrice)
    }

    void hasShownSniperHasLostAuction(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(SniperState.LOST))
    }

    void hasShownSniperIsBidding(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(SniperState.BIDDING))
    }

    void hasShownSniperIsWinning(FakeAuctionServer auction, int winningBid) {
        driver.showsSniperStatus(auction.itemId, winningBid, winningBid, textFor(SniperState.WINNING))
    }

    void hasShownSniperIsLosing(FakeAuctionServer auction, int lastPrice, int lastBid) {
        driver.showsSniperStatus(auction.itemId, lastPrice, lastBid, textFor(SniperState.LOSING))
    }

    void hasShownSniperHasWonAuction(FakeAuctionServer auction, int lastPrice) {
        driver.showsSniperStatus(auction.itemId, lastPrice, lastPrice, textFor(SniperState.WON))
    }

    void hasShownSniperHasFailed(FakeAuctionServer auction) {
        driver.showsSniperStatus(auction.itemId, 0, 0, textFor(SniperState.FAILED))
    }

    void reportsInvalidMessage(FakeAuctionServer auction, String brokenMessage) throws IOException {
        logDriver.hasEntry(brokenMessage)
    }

    void stop() {
        driver?.dispose()
    }

    private startSniper() {
        logDriver.clearLog()
        Thread.startDaemon("Test Application") {
            Main.main(XMPP_HOSTNAME, SNIPER_ID, SNIPER_PASSWORD)
        }
        makeSureAwtIsLoadedBeforeStartingTheDriverOnOSXToStopDeadlock()
        driver = new AuctionSniperDriver(1000)
        driver.hasTitle(MainWindow.APPLICATION_TITLE)
        driver.hasColumnTitles()
    }

    private openBiddingFor(FakeAuctionServer auction, int stopPrice) {
        final String itemId = auction.itemId
        driver.startBiddingWithStopPrice(itemId, stopPrice)
        driver.showsSniperStatus(itemId, 0, 0, textFor(SniperState.JOINING))
    }

    private makeSureAwtIsLoadedBeforeStartingTheDriverOnOSXToStopDeadlock() {
        try {
            SwingUtilities.invokeAndWait({})
        } catch (Exception e) {
            throw new AssertionError(e)
        }
    }
}
