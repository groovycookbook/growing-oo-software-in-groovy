package unit.auctionsniper.ui

import auctionsniper.AuctionSniper
import auctionsniper.SniperSnapshot
import auctionsniper.UserRequestListener.Item
import auctionsniper.ui.Column
import auctionsniper.ui.SnipersTableModel
import auctionsniper.SniperState
import auctionsniper.util.Defect

import javax.swing.event.TableModelListener
import javax.swing.event.TableModelEvent
import groovy.mock.interceptor.MockFor

class SnipersTableModelTest extends GroovyTestCase {
    private ITEM_ID = "item 0"
    def listenerMock = new MockFor(TableModelListener)
    private model = new SnipersTableModel()
    private sniper = new AuctionSniper(new Item(ITEM_ID, 234), null)

    void testHasEnoughColumns() {
        assert model.columnCount == Column.values().length
    }

    void testSetsUpColumnHeadings() {
        for (Column column: Column.values()) {
            assertEquals(column.name, model.getColumnName(column.ordinal()))
        }
    }

    void testAcceptsNewSniper() {
        listenerMock.demand.tableChanged(withAnInsertionAtRow.curry(0))
        addListener()
        model.sniperAdded(sniper)
        assertRowMatchesSnapshot(0, SniperSnapshot.joining(ITEM_ID))
    }

    void testSetsSniperValuesInColumns() {
        def bidding = sniper.snapshot.bidding(555, 666)
        listenerMock.demand.tableChanged(withAnyInsertionEvent)
        listenerMock.demand.tableChanged(withAChangeInRow.curry(0))
        addListener()
        model.sniperAdded(sniper)
        model.sniperStateChanged(bidding)
        assertRowMatchesSnapshot(0, bidding)
    }

    void testNotifiesListenersWhenAddingASniper() {
        listenerMock.demand.tableChanged(withAnInsertionAtRow.curry(0))
        addListener()
        assertEquals(0, model.getRowCount())
        model.sniperAdded(sniper)
        assertEquals(1, model.getRowCount())
        assertRowMatchesSnapshot(0, SniperSnapshot.joining(ITEM_ID))
    }

    void testHoldsSnipersInAdditionOrder() {
        AuctionSniper sniper2 = new AuctionSniper(new Item("item 1", 345), null)
        listenerMock.demand.tableChanged(1..2) { }
        addListener()
        model.sniperAdded(sniper)
        model.sniperAdded(sniper2)
        assertEquals(ITEM_ID, cellValue(0, Column.ITEM_IDENTIFIER))
        assertEquals("item 1", cellValue(1, Column.ITEM_IDENTIFIER))
    }

    void testUpdatesCorrectRowForSniper() {
        def sniper2 = new AuctionSniper(new Item("item 1", 345), null)
        listenerMock.demand.tableChanged(1..2, withAnyInsertionEvent)
        listenerMock.demand.tableChanged(withAChangeInRow.curry(1))
        addListener()
        model.sniperAdded(sniper)
        model.sniperAdded(sniper2)
        def winning1 = sniper2.snapshot.winning(123)
        model.sniperStateChanged(winning1)
        assertRowMatchesSnapshot(1, winning1)
    }

    void testThrowsDefectIfNoExistingSniperForAnUpdate() {
        shouldFail(Defect) {
            model.sniperStateChanged(new SniperSnapshot("item 1", 123, 234, SniperState.WINNING))
        }
    }

    private addListener() {
        model.addTableModelListener(listenerMock.proxyDelegateInstance())
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

    private withAnyInsertionEvent = {ev ->
        assert ev instanceof TableModelEvent &&
                ev.type == TableModelEvent.INSERT
    }

    private withAnInsertionAtRow = {row, ev ->
        assert ev instanceof TableModelEvent &&
                ev.firstRow == row &&
                ev.lastRow == row &&
                ev.column == TableModelEvent.ALL_COLUMNS &&
                ev.type == TableModelEvent.INSERT &&
                ev.source == model
    }

    private withAChangeInRow = {row, ev ->
        assert ev instanceof TableModelEvent &&
                ev.firstRow == row &&
                ev.lastRow == row &&
                ev.source == model
    }

}
