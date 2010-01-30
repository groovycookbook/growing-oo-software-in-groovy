package unit.auctionsniper.ui

import auctionsniper.AuctionSniper
import auctionsniper.SniperSnapshot
import auctionsniper.SniperState
import auctionsniper.UserRequestListener.Item
import auctionsniper.ui.Column
import auctionsniper.ui.SnipersTableModel
import auctionsniper.util.Defect
import org.gmock.GMockTestCase
import javax.swing.event.TableModelListener
import javax.swing.event.TableModelEvent

class SnipersTableModelTest extends GMockTestCase {
    private ITEM_ID = "item 0"
    TableModelListener listener = mock(TableModelListener)
    private model = new SnipersTableModel()
    private sniper = new AuctionSniper(new Item(ITEM_ID, 234), null)

    protected void setUp() {
        model.addTableModelListener(listener)
    }

    void testHasEnoughColumns() {
        assert model.columnCount == Column.values().length
    }

    void testSetsUpColumnHeadings() {
        for (Column column: Column.values()) {
            assertEquals(column.name, model.getColumnName(column.ordinal()))
        }
    }

    void testAcceptsNewSniper() {
        listener.tableChanged(withAnInsertionAtRow(0))
        play {
            model.sniperAdded(sniper)
            assertRowMatchesSnapshot(0, SniperSnapshot.joining(ITEM_ID))
        }
    }

    void testSetsSniperValuesInColumns() {
        def bidding = sniper.snapshot.bidding(555, 666)
        listener.tableChanged(withAnyInsertionEvent())
        listener.tableChanged(withAChangeInRow(0))
        play {
            model.sniperAdded(sniper)
            model.sniperStateChanged(bidding)
            assertRowMatchesSnapshot(0, bidding)
        }
    }

    void testNotifiesListenersWhenAddingASniper() {
        listener.tableChanged(withAnInsertionAtRow(0))
        play {
            assertEquals(0, model.getRowCount())
            model.sniperAdded(sniper)
            assertEquals(1, model.getRowCount())
            assertRowMatchesSnapshot(0, SniperSnapshot.joining(ITEM_ID))
        }
    }

    void testHoldsSnipersInAdditionOrder() {
        AuctionSniper sniper2 = new AuctionSniper(new Item("item 1", 345), null)
        listener.tableChanged(match{ true }).stub()
        play {
            model.sniperAdded(sniper)
            model.sniperAdded(sniper2)
            assertEquals(ITEM_ID, cellValue(0, Column.ITEM_IDENTIFIER))
            assertEquals("item 1", cellValue(1, Column.ITEM_IDENTIFIER))
        }
    }

    void testUpdatesCorrectRowForSniper() {
        def sniper2 = new AuctionSniper(new Item("item 1", 345), null)
        listener.tableChanged(withAnyInsertionEvent()).times(2)
        listener.tableChanged(withAChangeInRow(1))
        play {
            model.sniperAdded(sniper)
            model.sniperAdded(sniper2)
            def winning1 = sniper2.snapshot.winning(123)
            model.sniperStateChanged(winning1)
            assertRowMatchesSnapshot(1, winning1)
        }
    }

    void testThrowsDefectIfNoExistingSniperForAnUpdate() {
        play {
            shouldFail(Defect) {
                model.sniperStateChanged(new SniperSnapshot("item 1", 123, 234, SniperState.WINNING))
            }
        }
    }

    private assertRowMatchesSnapshot(int row, SniperSnapshot snapshot) {
        assert snapshot.itemId == cellValue(row, Column.ITEM_IDENTIFIER)
        assert snapshot.lastPrice == cellValue(row, Column.LAST_PRICE)
        assert snapshot.lastBid == cellValue(row, Column.LAST_BID)
        assert SnipersTableModel.textFor(snapshot.state) == cellValue(row, Column.SNIPER_STATE)
    }

    private cellValue(int rowIndex, Column column) {
        model.getValueAt(rowIndex, column.ordinal())
    }

    private withAnyInsertionEvent() {
        match {
            it instanceof TableModelEvent &&
            it.type == TableModelEvent.INSERT
        }
    }

    private withAnInsertionAtRow(row) {
        match {
            it instanceof TableModelEvent &&
            it.firstRow == row &&
            it.lastRow == row &&
            it.column == TableModelEvent.ALL_COLUMNS &&
            it.type == TableModelEvent.INSERT &&
            it.source == model
        }
    }

    private withAChangeInRow(row) {
        match {
            it instanceof TableModelEvent &&
            it.firstRow == row &&
            it.lastRow == row &&
            it.source == model
        }
    }

}
