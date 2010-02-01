package unit.auctionsniper

import auctionsniper.Auction
import auctionsniper.AuctionHouse
import auctionsniper.SniperCollector
import auctionsniper.SniperLauncher
import auctionsniper.UserRequestListener.Item
import auctionsniper.AuctionSniper
import groovy.mock.interceptor.MockFor

class SniperLauncherTest extends GroovyTestCase {
    def auctionMock = new MockFor(Auction)
    Auction auction
    def auctionHouseMock = new MockFor(AuctionHouse)
    AuctionHouse auctionHouse
    def sniperCollectorMock = new MockFor(SniperCollector)
    SniperCollector sniperCollector
    def launcher = new SniperLauncher(auctionHouse, sniperCollector)
    def item = new Item("item 123", 456)

    void testAddsNewSniperToCollectorAndThenJoinsAuction() {
        def sniperMatchingItem = { assert it instanceof AuctionSniper && it.snapshot.itemId == item.identifier }

        auctionMock.demand.addAuctionEventListener(sniperMatchingItem)
        auctionMock.demand.join{}
        auction = auctionMock.proxyDelegateInstance()

        auctionHouseMock.demand.auctionFor{ assert it == item; auction }
        auctionHouse = auctionHouseMock.proxyDelegateInstance()

        sniperCollectorMock.demand.addSniper(sniperMatchingItem)
        sniperCollector = sniperCollectorMock.proxyDelegateInstance()

        def launcher = new SniperLauncher(auctionHouse, sniperCollector)
        launcher.joinAuction(item)

        sniperCollectorMock.verify(sniperCollector)
        auctionHouseMock.verify(auctionHouse)
        auctionMock.verify(auction)
    }

}
