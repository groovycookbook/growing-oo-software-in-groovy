package harness.auctionsniper

import java.util.logging.LogManager

import auctionsniper.xmpp.XMPPAuctionHouse

class AuctionLogDriver {
    private logFile = new File(XMPPAuctionHouse.LOG_FILE_NAME)

    void hasEntry(string) throws IOException {
        assert logFile.text.contains(string)
    }

    void clearLog() {
        logFile.delete()
        LogManager.logManager.reset()
    }
}
