package unit.auctionsniper

import org.gmock.GMockTestCase

import auctionsniper.AuctionSniper
import auctionsniper.SniperPortfolio
import auctionsniper.UserRequestListener.Item
import auctionsniper.SniperPortfolio.PortfolioListener

class SniperPortfolioTest extends GMockTestCase {
    PortfolioListener listener = mock(PortfolioListener)
    def portfolio = new SniperPortfolio()

    void testNotifiesListenersOfNewSnipers() {
        def sniper = new AuctionSniper(new Item("item id", 123), null)
        listener.sniperAdded(sniper)
        play {
            portfolio.addPortfolioListener(listener)
            portfolio.addSniper(sniper)
        }
    }
}
