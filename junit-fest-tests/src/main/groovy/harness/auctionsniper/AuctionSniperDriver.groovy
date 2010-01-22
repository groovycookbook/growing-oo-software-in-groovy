package harness.auctionsniper;

import static auctionsniper.ui.MainWindow.NEW_ITEM_ID_NAME;
import static auctionsniper.ui.MainWindow.NEW_ITEM_STOP_PRICE_NAME;
import static java.lang.String.valueOf;

import auctionsniper.ui.MainWindow
import java.awt.Frame
import org.fest.swing.fixture.FrameFixture
import org.fest.swing.edt.GuiQuery
import org.fest.swing.edt.GuiActionRunner;

@SuppressWarnings("unchecked")
public class AuctionSniperDriver {
    MainWindow main
    FrameFixture window

    public AuctionSniperDriver(int timeoutMillis) {
        main = GuiActionRunner.execute(new GuiQuery<MainWindow>() {
            protected MainWindow executeInEDT() {
                def found = Frame.frames.find{
                    it instanceof MainWindow
                }
                found
            }
        })
        assert main, 'MainWindow not found'
        window = new FrameFixture(main)
        window.show()
    }

    void hasTitle(String title) {
        assert main.title == title
    }

    void hasColumnTitles() {
        println 'table header:' + window.table().tableHeader().dump()
//        JTableHeaderDriver headers = new JTableHeaderDriver(this,
//                JTableHeader.class);
//        headers.hasHeaders(
//                matching(withLabelText("Item"), withLabelText("Last Price"),
//                        withLabelText("Last Bid"), withLabelText("State")));
    }

    void showsSniperStatus(String itemId, int lastPrice, int lastBid, String statusText) {
        println 'table contents:' + Arrays.asList(window.table().contents())
//        JTableDriver table = new JTableDriver(this);
//        table.hasRow(
//                matching(withLabelText(itemId), withLabelText(valueOf(lastPrice)),
//                        withLabelText(valueOf(lastBid)), withLabelText(statusText)));
    }

    void startBiddingWithStopPrice(String itemId, int stopPrice) {
        replaceText(textField(NEW_ITEM_ID_NAME), itemId)
        replaceText(textField(NEW_ITEM_STOP_PRICE_NAME), valueOf(stopPrice))
        bidButton().click()
    }

    private replaceText(field, value) {
        field.deleteText()
        field.enterText(value)
    }

    private textField(String fieldName) {
        window.textBox(fieldName)
    }

    private bidButton() {
        window.button(MainWindow.JOIN_BUTTON_NAME)
    }

    void dispose() {
        window.cleanUp()
    }

}
