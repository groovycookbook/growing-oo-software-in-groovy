package unit.auctionsniper

import org.gmock.GMockTestCase

import auctionsniper.Auction
import auctionsniper.AuctionHouse
import auctionsniper.SniperCollector
import auctionsniper.SniperLauncher
import auctionsniper.UserRequestListener.Item
import auctionsniper.AuctionSniper

class SniperLauncherTest extends GMockTestCase {
    Auction auction = mock(Auction)
    AuctionHouse auctionHouse = mock(AuctionHouse)
    SniperCollector sniperCollector = mock(SniperCollector)
    private launcher = new SniperLauncher(auctionHouse, sniperCollector)
    private item = new Item("item 123", 456)

    void testAddsNewSniperToCollectorAndThenJoinsAuction() {
        auctionHouse.auctionFor(item).returns(auction)
        auction.addAuctionEventListener(sniperMatchingItem())
        sniperCollector.addSniper(sniperMatchingItem())
        auction.join()
        play {
            launcher.joinAuction(item)
        }
    }

    private sniperMatchingItem() {
        match { it instanceof AuctionSniper &&
                it.snapshot.itemId == item.identifier }
    }
}
