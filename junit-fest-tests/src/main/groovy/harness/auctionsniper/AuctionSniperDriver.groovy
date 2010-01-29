package harness.auctionsniper

import static auctionsniper.ui.MainWindow.*
import static java.util.concurrent.TimeUnit.MILLISECONDS

import auctionsniper.ui.MainWindow
import java.awt.Frame
import org.fest.swing.fixture.FrameFixture
import org.fest.swing.driver.JTableHeaderLocation
import org.fest.swing.util.StringTextMatcher
import org.fest.swing.finder.WindowFinder
import org.fest.swing.core.Robot
import org.fest.swing.core.BasicRobot
import org.fest.swing.edt.GuiActionRunner
import org.fest.swing.edt.GuiQuery
import org.fest.swing.edt.GuiTask

@SuppressWarnings("unchecked")
public class AuctionSniperDriver {
    MainWindow main
    FrameFixture window

    public AuctionSniperDriver(int timeoutMillis) {
//        final Robot robot = BasicRobot.robotWithNewAwtHierarchy()
//        GuiActionRunner.execute(new GuiTask() {
//            void executeInEDT() {
//                window = WindowFinder.findFrame(MAIN_WINDOW_NAME).withTimeout(timeoutMillis, MILLISECONDS).using(robot)
//            }
//        })
        GuiActionRunner.execute(new GuiTask() {
            void executeInEDT() {
                main = Frame.frames.find{
                    it.name == MAIN_WINDOW_NAME && it.isShowing()
                }
                if (!main) main = Frame.frames.find{
                    it.name == MAIN_WINDOW_NAME
                }
            }
        })
        assert main, 'MainWindow not found'
        window = new FrameFixture(main)
//        window.show()
//        window = WindowFinder.findFrame(MAIN_WINDOW_NAME).withTimeout(timeoutMillis, MILLISECONDS).using(robot)
    }

    void hasTitle(String title) {
        assert window.target.title == title
//        assert main.title == title
    }

    void hasColumnTitles() {
        def expected = ['Item', 'LastPrice', 'Last Bid', 'State']
        sleep 300
        def header = window.table(SNIPERS_TABLE_NAME).tableHeader().target
        def location = new JTableHeaderLocation()
        expected.eachWithIndex { h, i->
            def matcher = new StringTextMatcher(h)
            assert location.indexOf(header, matcher) == i
        }
    }

    void showsSniperStatus(String itemId, int lastPrice, int lastBid, String statusText) {
        def row = [itemId, lastPrice.toString(), lastBid.toString(), statusText]
        sleep 300
        assert window.table(SNIPERS_TABLE_NAME).contents().any { it.toList() == row }
    }

    void startBiddingWithStopPrice(String itemId, int stopPrice) {
        replaceText(textField(NEW_ITEM_ID_NAME), itemId)
        replaceText(textField(NEW_ITEM_STOP_PRICE_NAME), stopPrice.toString())
        bidButton().click()
    }

    private replaceText(field, value) {
        field.deleteText()
        field.enterText(value)
    }

    private textField(String fieldName) {
        window.textBox(fieldName).focus()
    }

    private bidButton() {
        window.button(MainWindow.JOIN_BUTTON_NAME)
    }

    void dispose() {
        window.target.dispose()
        window.target.name = null
        window.cleanUp()
    }

}
