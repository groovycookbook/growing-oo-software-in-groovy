package unit.auctionsniper

import static auctionsniper.SniperState.*
import static auctionsniper.AuctionEventListener.PriceSource.*

import auctionsniper.Auction
import auctionsniper.AuctionSniper
import auctionsniper.SniperListener
import auctionsniper.SniperSnapshot
import auctionsniper.UserRequestListener.Item
import groovy.mock.interceptor.MockFor

class AuctionSniperTest extends GroovyTestCase {
    def ITEM_ID = "item-id"
    def ITEM = new Item(ITEM_ID, 1234)
    def auctionMock = new MockFor(Auction)
    def auction
    def sniperListenerMock = new MockFor(SniperListener)
    def sniperListener
    def sniper

    void testHasInitialStateOfJoining() {
        createSniper()
        assert sniper.snapshot.state == JOINING
    }

    void testReportsLostWhenAuctionClosesImmediately() {
        sniperListenerMock.demand.sniperStateChanged { new SniperSnapshot(ITEM_ID, 0, 0, LOST) }
        createSniper()
        sniper.auctionClosed()
        verifyMocks()
    }

    void testBidsHigherAndReportsBiddingWhenNewPriceArrives() {
        def price = 1001
        def increment = 25
        def bid = price + increment

        sniperListenerMock.demand.sniperStateChanged { new SniperSnapshot(ITEM_ID, price, bid, BIDDING) }
        auctionMock.demand.bid { bid }
        createSniper()
        sniper.currentPrice(price, increment, FromOtherBidder)
        verifyMocks()
    }

    void testDoesNotBidAndReportsLosingIfFirstPriceIsAboveStopPrice() {
        def price = 1233
        def increment = 25

        sniperListenerMock.demand.sniperStateChanged { new SniperSnapshot(ITEM_ID, price, 0, LOSING) }
        createSniper()
        sniper.currentPrice(price, increment, FromOtherBidder)
        verifyMocks()
    }

    void testDoesNotBidAndReportsLosingIfSubsequentPriceIsAboveStopPrice() {
        def bid = 123 + 45

        auctionMock.demand.bid { bid }
        expectSniperBidding()
        sniperListenerMock.demand.sniperStateChanged { new SniperSnapshot(ITEM_ID, 2345, bid, LOSING) }
        createSniper()
        sniper.currentPrice(123, 45, FromOtherBidder)
        sniper.currentPrice(2345, 25, FromOtherBidder)
        verifyMocks()
    }

    void testDoesNotBidAndReportsLosingIfPriceAfterWinningIsAboveStopPrice() {
        def price = 1233
        def increment = 25
        def bid = 123 + 45

        expectSniperBidding()
        expectSniperWinning()
        auctionMock.demand.bid { bid }
        sniperListenerMock.demand.sniperStateChanged { new SniperSnapshot(ITEM_ID, price, bid, LOSING) }
        createSniper()
        sniper.currentPrice(123, 45, FromOtherBidder)
        sniper.currentPrice(168, 45, FromSniper)
        sniper.currentPrice(price, increment, FromOtherBidder)
        verifyMocks()
    }

    void testContinuesToBeLosingOnceStopPriceHasBeenReached() {
        def price1 = 1233
        def price2 = 1258

        sniperListenerMock.demand.sniperStateChanged {new SniperSnapshot(ITEM_ID, price1, 0, LOSING)}
        sniperListenerMock.demand.sniperStateChanged {new SniperSnapshot(ITEM_ID, price2, 0, LOSING)}
        createSniper()
        sniper.currentPrice(price1, 25, FromOtherBidder)
        sniper.currentPrice(price2, 25, FromOtherBidder)
        verifyMocks()
    }

    void testReportsLostIfAuctionClosesWhenBidding() {
        expectSniperBidding()
        ignoringAuction()
        sniperListenerMock.demand.sniperStateChanged{ new SniperSnapshot(ITEM_ID, 123, 168, LOST) }
        createSniper()
        sniper.currentPrice(123, 45, FromOtherBidder)
        sniper.auctionClosed()
        verifyMocks()
    }

    void testReportsLostIfAuctionClosesWhenLosing() {
        expectSniperLosing()
        sniperListenerMock.demand.sniperStateChanged{ new SniperSnapshot(ITEM_ID, 1230, 0, LOST) }
        createSniper()
        sniper.currentPrice(1230, 456, FromOtherBidder)
        sniper.auctionClosed()
        verifyMocks()
    }

    void testReportsIsWinningWhenCurrentPriceComesFromSniper() {
        expectSniperBidding()
        ignoringAuction()
        sniperListenerMock.demand.sniperStateChanged{ new SniperSnapshot(ITEM_ID, 135, 135, WINNING) }
        createSniper()
        sniper.currentPrice(123, 12, FromOtherBidder)
        sniper.currentPrice(135, 45, FromSniper)
        verifyMocks()
    }

    void testReportsWonIfAuctionClosesWhenWinning() {
        expectSniperBidding()
        expectSniperWinning()
        ignoringAuction()
        sniperListenerMock.demand.sniperStateChanged{ new SniperSnapshot(ITEM_ID, 135, 135, WON) }
        createSniper()
        sniper.currentPrice(123, 12, FromOtherBidder)
        sniper.currentPrice(135, 45, FromSniper)
        sniper.auctionClosed()
        verifyMocks()
    }

    void testReportsFailedIfAuctionFailsWhenBidding() {
        ignoringAuction()
        expectSniperBidding()
        expectSniperFailed()
        createSniper()
        sniper.currentPrice(123, 45, FromOtherBidder)
        sniper.auctionFailed()
        verifyMocks()
    }

    void testReportsFailedIfAuctionFailsImmediately() {
        sniperListenerMock.demand.sniperStateChanged{ SniperSnapshot.joining(ITEM_ID).failed() }
        createSniper()
        sniper.auctionFailed()
        verifyMocks()
    }

    void testReportsFailedIfAuctionFailsWhenLosing() {
        expectSniperLosing()
        expectSniperFailed()
        createSniper()
        sniper.currentPrice(1230, 456, FromOtherBidder)
        sniper.auctionFailed()
        verifyMocks()
    }

    void testReportsFailedIfAuctionFailsWhenWinning() {
        ignoringAuction()
        expectSniperBidding()
        expectSniperWinning()
        expectSniperFailed()
        createSniper()
        sniper.currentPrice(123, 12, FromOtherBidder)
        sniper.currentPrice(135, 45, FromSniper)
        sniper.auctionFailed()
        verifyMocks()
    }

    private createSniper() {
        sniperListener = sniperListenerMock.proxyDelegateInstance()
        auction = auctionMock.proxyDelegateInstance()
        sniper = new AuctionSniper(ITEM, auction)
        sniper.addSniperListener(sniperListener)
    }

    private verifyMocks() {
        sniperListenerMock.verify(sniperListener)
        auctionMock.verify(auction)
    }

    private ignoringAuction() {
        auctionMock.demand.bid{}
    }

    private expectSniperFailed() {
        sniperListenerMock.demand.sniperStateChanged { new SniperSnapshot(ITEM_ID, 00, 0, FAILED) }
    }

    private expectSniperBidding() {
        expectSniperState(BIDDING)
    }

    private expectSniperLosing() {
        expectSniperState(LOSING)
    }

    private expectSniperWinning() {
        expectSniperState(WINNING)
    }

    private expectSniperState(sniperState) {
        sniperListenerMock.demand.sniperStateChanged { it.state == sniperState }
    }

}
