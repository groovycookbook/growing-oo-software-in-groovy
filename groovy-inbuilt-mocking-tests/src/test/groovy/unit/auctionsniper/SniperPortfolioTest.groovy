package unit.auctionsniper

import auctionsniper.AuctionSniper
import auctionsniper.SniperPortfolio
import auctionsniper.UserRequestListener.Item
import auctionsniper.SniperPortfolio.PortfolioListener
import groovy.mock.interceptor.MockFor

class SniperPortfolioTest extends GroovyTestCase {
    def listenerMock = new MockFor(PortfolioListener)
    PortfolioListener listener
    def portfolio = new SniperPortfolio()

    void testNotifiesListenersOfNewSnipers() {
        def sniper = new AuctionSniper(new Item("item id", 123), null)
        listenerMock.demand.sniperAdded{ assert it.is(sniper) }
        listener = listenerMock.proxyDelegateInstance()
        portfolio.addPortfolioListener(listener)
        portfolio.addSniper(sniper)
        listenerMock.verify(listener)
    }
}
