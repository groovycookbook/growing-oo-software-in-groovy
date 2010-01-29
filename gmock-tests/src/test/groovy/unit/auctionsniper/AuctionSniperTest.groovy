package unit.auctionsniper

import static auctionsniper.SniperState.*

import auctionsniper.Auction
import auctionsniper.AuctionSniper
import auctionsniper.SniperListener
import auctionsniper.SniperSnapshot
import auctionsniper.SniperState
import auctionsniper.AuctionEventListener.PriceSource
import auctionsniper.UserRequestListener.Item
import org.gmock.GMockTestCase

class AuctionSniperTest extends GMockTestCase {
    private ITEM_ID = "item-id"
    private ITEM = new Item(ITEM_ID, 1234)
    Auction auction = mock(Auction)
    SniperListener sniperListener = mock(SniperListener)
    def sniper = new AuctionSniper(ITEM, auction)

    protected void setUp() {
        sniper.addSniperListener(sniperListener)
    }

    void testHasInitialStateOfJoining() {
        ['itemId', 'lastPrice', 'lastBid', 'state'].each {
            assert sniper.snapshot."$it" == SniperSnapshot.joining(ITEM_ID)."$it"
        }
    }

    void testReportsLostWhenAuctionClosesImmediately() {
        sniperListener.sniperStateChanged(new SniperSnapshot(ITEM_ID, 0, 0, LOST)).atLeastOnce()
        play {
            sniper.auctionClosed()
        }
    }

    void testBidsHigherAndReportsBiddingWhenNewPriceArrives() {
        def price = 1001
        def increment = 25
        def bid = price + increment
        auction.bid(bid)
        sniperListener.sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, BIDDING)).atLeastOnce()
        play {
            sniper.currentPrice(price, increment, PriceSource.FromOtherBidder)
        }
    }

    void testDoesNotBidAndReportsLosingIfFirstPriceIsAboveStopPrice() {
        def price = 1233
        def increment = 25
        sniperListener.sniperStateChanged(new SniperSnapshot(ITEM_ID, price, 0, LOSING)).atLeastOnce()
        play {
            sniper.currentPrice(price, increment, PriceSource.FromOtherBidder)
        }
    }

    void testDoesNotBidAndReportsLosingIfSubsequentPriceIsAboveStopPrice() {
        allowingSniperBidding()
        def bid = 123 + 45
        auction.bid(bid)
//        sniperListener.sniperStateChanged(new SniperSnapshot(ITEM_ID, 2345, bid, LOSING)).atLeastOnce()
        play {
            sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
            sniper.currentPrice(2345, 25, PriceSource.FromOtherBidder)
        }
    }
/*
    void testDoesNotBidAndReportsLosingIfPriceAfterWinningIsAboveStopPrice() {
        final int price = 1233
        final int increment = 25

        allowingSniperBidding()
        allowingSniperWinning()
        context.checking(new Expectations() {
            {
                int bid = 123 + 45
                allowing(auction).bid(bid)

                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, LOSING)) when(sniperState.is("winning"))
            }
        })

        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
        sniper.currentPrice(168, 45, PriceSource.FromSniper)
        sniper.currentPrice(price, increment, PriceSource.FromOtherBidder)
    }

    void testContinuesToBeLosingOnceStopPriceHasBeenReached() {
        final Sequence states = context.sequence("sniper states")
        final int price1 = 1233
        final int price2 = 1258

        context.checking(new Expectations() {
            {
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price1, 0, LOSING)) inSequence(states)
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price2, 0, LOSING)) inSequence(states)
            }
        })

        sniper.currentPrice(price1, 25, PriceSource.FromOtherBidder)
        sniper.currentPrice(price2, 25, PriceSource.FromOtherBidder)
    }

    void testReportsLostIfAuctionClosesWhenBidding() {
        allowingSniperBidding()
        ignoringAuction()

        context.checking(new Expectations() {
            {
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 123, 168, LOST))
                when(sniperState.is("bidding"))
            }
        })

        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
        sniper.auctionClosed()
    }

    void testReportsLostIfAuctionClosesWhenLosing() {
        allowingSniperLosing()
        context.checking(new Expectations() {
            {
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 1230, 0, LOST))
                when(sniperState.is("losing"))
            }
        })

        sniper.currentPrice(1230, 456, PriceSource.FromOtherBidder)
        sniper.auctionClosed()
    }



    void testReportsIsWinningWhenCurrentPriceComesFromSniper() {
        allowingSniperBidding()
        ignoringAuction()
        context.checking(new Expectations() {
            {
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 135, WINNING)) when(sniperState.is("bidding"))
            }
        })

        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder)
        sniper.currentPrice(135, 45, PriceSource.FromSniper)
    }

    void testReportsWonIfAuctionClosesWhenWinning() {
        allowingSniperBidding()
        allowingSniperWinning()
        ignoringAuction()

        context.checking(new Expectations() {
            {
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 135, WON)) when(sniperState.is("winning"))
            }
        })

        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder)
        sniper.currentPrice(135, 45, PriceSource.FromSniper)
        sniper.auctionClosed()
    }

    void testReportsFailedIfAuctionFailsWhenBidding() {
        ignoringAuction()
        allowingSniperBidding()

        expectSniperToFailWhenItIs("bidding")

        sniper.currentPrice(123, 45, PriceSource.FromOtherBidder)
        sniper.auctionFailed()
    }

    void testReportsFailedIfAuctionFailsImmediately() {
        context.checking(new Expectations() {
            {
                atLeast(1).of(sniperListener).sniperStateChanged(SniperSnapshot.joining(ITEM_ID).failed())
            }
        })

        sniper.auctionFailed()
    }

    void testReportsFailedIfAuctionFailsWhenLosing() {
        allowingSniperLosing()

        expectSniperToFailWhenItIs("losing")

        sniper.currentPrice(1230, 456, PriceSource.FromOtherBidder)
        sniper.auctionFailed()
    }


    void testReportsFailedIfAuctionFailsWhenWinning() {
        ignoringAuction()
        allowingSniperBidding()
        allowingSniperWinning()

        expectSniperToFailWhenItIs("winning")

        sniper.currentPrice(123, 12, PriceSource.FromOtherBidder)
        sniper.currentPrice(135, 45, PriceSource.FromSniper)
        sniper.auctionFailed()
    }

    private expectSniperToFailWhenItIs(final String state) {
        sniperListener.sniperStateChanged(new SniperSnapshot(ITEM_ID, 00, 0, SniperState.FAILED))
        when(sniperState.is(state))
    }
*/
    private ignoringAuction() {
//        context.checking(new Expectations() { {
//                ignoring(auction)
//        } })
    }

    private allowingSniperBidding() {
        allowSniperStateChange(BIDDING, "bidding")
    }

    private allowingSniperLosing() {
        allowSniperStateChange(LOSING, "losing")
    }

    private allowingSniperWinning() {
        allowSniperStateChange(WINNING, "winning")
    }

    private allowSniperStateChange(SniperState newState, String oldState) {
        sniperListener.sniperStateChanged(match { it instanceof AuctionSniper &&
                it.snapshot.state.toString().toLowerCase() == newState.toLowerCase() } ).atMostOnce()
    }

//    private Matcher<SniperSnapshot> aSniperThatIs(final SniperState state) {
//        return new FeatureMatcher<SniperSnapshot, SniperState>(equalTo(state), "sniper that is ", "was") {
//            @Override
//            protected SniperState featureValueOf(SniperSnapshot actual) {
//                return actual.state
//            }
//        }
//    }
} 
