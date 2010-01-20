package auctionsniper

import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.SwingUtilities
import auctionsniper.ui.MainWindow
import auctionsniper.xmpp.XMPPAuctionHouse

class Main {
  private static final int ARG_HOSTNAME = 0
  private static final int ARG_USERNAME = 1
  private static final int ARG_PASSWORD = 2

  private final SniperPortfolio portfolio = new SniperPortfolio()

  private MainWindow ui

  Main() {
    startUserInterface()
  }

  static main(String... args) {
    Main main = new Main()
    XMPPAuctionHouse auctionHouse = XMPPAuctionHouse.connect(args[ARG_HOSTNAME], args[ARG_USERNAME], args[ARG_PASSWORD])
    main.disconnectWhenUICloses(auctionHouse)
    main.addUserRequestListenerFor(auctionHouse)
  }

  private startUserInterface() {
    SwingUtilities.invokeAndWait(new Runnable() {
      public void run() {
        ui = new MainWindow(portfolio)
      }
    })
  }

  private disconnectWhenUICloses(final XMPPAuctionHouse auctionHouse) {
    ui.addWindowListener(new WindowAdapter() { 
      @Override public void windowClosed(WindowEvent e) { 
        auctionHouse.disconnect()
      } 
    })
  } 

  private addUserRequestListenerFor(final AuctionHouse auctionHouse) {
    ui.addUserRequestListener(new SniperLauncher(auctionHouse, portfolio))
  } 
}
