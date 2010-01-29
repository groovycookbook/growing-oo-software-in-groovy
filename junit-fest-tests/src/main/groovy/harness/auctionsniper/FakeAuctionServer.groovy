package harness.auctionsniper

import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

import org.jivesoftware.smack.Chat
import org.jivesoftware.smack.ChatManagerListener
import org.jivesoftware.smack.MessageListener
import org.jivesoftware.smack.XMPPConnection
import org.jivesoftware.smack.packet.Message

import auctionsniper.xmpp.XMPPAuction

class FakeAuctionServer {
    public static final String ITEM_ID_AS_LOGIN = "auction-%s"
    public static final String AUCTION_RESOURCE = "Auction"
    public static final String XMPP_HOSTNAME = "localhost"

    private static final String AUCTION_PASSWORD = "auction"
    private final SingleMessageListener messageListener = new SingleMessageListener()
    private final String itemId
    private final XMPPConnection connection

    private Chat currentChat

    FakeAuctionServer(String itemId) {
        this.itemId = itemId
        this.connection = new XMPPConnection(XMPP_HOSTNAME)
    }

    void startSellingItem() {
        connection.connect()
        connection.login(String.format(ITEM_ID_AS_LOGIN, itemId), AUCTION_PASSWORD, AUCTION_RESOURCE)
        connection.chatManager.addChatListener(new ChatManagerListener() {
            void chatCreated(Chat chat, boolean createdLocally) {
                currentChat = chat
                chat.addMessageListener(messageListener)
            }
        })
    }

    void sendInvalidMessageContaining(String brokenMessage) {
        currentChat.sendMessage(brokenMessage)
    }

    void reportPrice(int price, int increment, String bidder) {
        currentChat.sendMessage("SOLVersion: 1.1; Event: PRICE; CurrentPrice: $price; Increment: $increment; Bidder: $bidder;")
    }

    void hasReceivedJoinRequestFrom(String sniperId) {
        receivesAMessageMatching(sniperId, XMPPAuction.JOIN_COMMAND_FORMAT)
    }

    void hasReceivedBid(int bid, String sniperId) {
        receivesAMessageMatching(sniperId, String.format(XMPPAuction.BID_COMMAND_FORMAT, bid))
    }

    private receivesAMessageMatching(String sniperId, String messageMatcher) {
        messageListener.receivesAMessage(messageMatcher)
        assert currentChat.participant == sniperId
    }

    void announceClosed() {
        currentChat.sendMessage("SOLVersion: 1.1; Event: CLOSE;")
    }

    void stop() {
        connection.disconnect()
    }

    String getItemId() {
        return itemId
    }

    class SingleMessageListener implements MessageListener {
        private messages = new ArrayBlockingQueue<Message>(1)

        void processMessage(Chat chat, Message message) {
            messages.add(message)
        }

        void receivesAMessage() {
            assert messages.poll(5, TimeUnit.SECONDS)
        }

        void receivesAMessage(match) {
            def message = messages.poll(5, TimeUnit.SECONDS)
            println "match: $match, message: ${message?.dump()}"
            if (message?.bodies && message?.bodies?.size()) println 'value: ' + message.bodies.toList()[0].dump()
            assert message?.body?.contains(match)
        }
    }

}
