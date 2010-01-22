package integration.auctionsniper.ui

import org.junit.Test

import harness.auctionsniper.AuctionSniperDriver
import auctionsniper.UserRequestListener
import auctionsniper.SniperPortfolio
import auctionsniper.ui.MainWindow

public class MainWindowTest {
    private final MainWindow mainWindow = new MainWindow(new SniperPortfolio())
    private final AuctionSniperDriver driver = new AuctionSniperDriver(100)

    @Test
    public void makesUserRequestWhenJoinButtonClicked() {
        mainWindow.addUserRequestListener({
            assert it.identifier == 'an item-id'
            assert it.stopPrice == 789
        } as UserRequestListener)
        driver.startBiddingWithStopPrice("an item-id", 789)
    }
}
