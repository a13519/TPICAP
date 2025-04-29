package net.zousys.mathtrading.interfaces.icap.tracing;

import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.*;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.zousys.mathtrading.interfaces.util.FileReader;

import java.io.IOException;
import java.nio.ByteBuffer;
@AllArgsConstructor
public class  RecordableMessage {
    @Getter
    protected ICMsg icMsg;
    @Getter
    protected long time;
    /**
     *
     * @param name
     * @return
     * @param <T>
     * @throws IOException
     */
    public static <T extends ICMsg> T parse(String name) throws IOException {
        return parse(FileReader.readFileToBytes(name));
    }
    /**
     *
     * @param data
     * @return
     */
    public static <T extends ICMsg> T parse(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        byte type = bb.get();
        ICMsg icMsgr = new ICMsg();
        icMsgr.setMsgType(EICMsgType.getName((int)type));
        bb.position(1);
        ByteBuffer sub = bb.slice();
        return (T) new RecordableMessage(icMsgr, System.currentTimeMillis()).getICMessage(true);
    }

    /**
     *
     * @return
     */
    public byte[] serialize() {
        ByteBuffer mbb = getByteBuffer();
        ByteBuffer bb = ByteBuffer.allocate(mbb.capacity()+1);
        bb.put((byte)icMsg.getMsgType().getValue());
        bb.put(mbb);
        return bb.array();
    }

    /**
     *
     * @return
     */
    public final ByteBuffer getByteBuffer() {
        ICMessageBuffer mb = new ICMessageBuffer();
        getICMessage(false).pack(mb);
        return mb.getBuffer();
    }

    /**
     *
     * @return
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public <T extends ICMsg> T getICMessage(boolean init) {
        EICMsgType type = icMsg.getMsgType();
        switch (type) {
            case EICMsgType.eMsgPositiveLogin: {
                return init? (T) new ICMsgPositiveLogin():(T) ((ICMsgPositiveLogin) icMsg);
            }
            case EICMsgType.eMsgClearBook: {
                return init? (T) new ICMsgClearBookUpdate():(T) ((ICMsgClearBookUpdate)icMsg);
            }
            case EICMsgType.eMsgMessageLogUpdate: {
                return init? (T) new ICMsgLogUpdate():(T) ((ICMsgLogUpdate)icMsg);
            }
            case EICMsgType.eMsgPositive: {
                return init? (T) new ICMsgPositive():(T) ((ICMsgPositive) icMsg);
            }
            case EICMsgType.eMsgHeartbeat: {
                return init? (T) new ICMsgHeartbeat():(T) ((ICMsgHeartbeat) icMsg);
            }
            default: {
                return init? (T) new ICMsg():(T) icMsg;
            }
        }
    }


        public static void main(String[] args) throws IOException {
            // Example ByteBuffer
            ByteBuffer buffer = ByteBuffer.wrap(FileReader.readFileToBytes("/Users/songzou/Documents/IdeaProjects/TPICAP/ICAPConnector/src/test/resources/1745911349680_eMsgPositiveLogin.irm"));

            // Skip the first byte (move position to index 1)
            buffer.position(1);

            // Create a new ByteBuffer from the current position to the end
            ByteBuffer subBuffer = buffer.slice();
            buffer.flip();
            // Print the contents of the subBuffer
            while (subBuffer.hasRemaining()) {
                System.out.println(subBuffer.get());
            }
        }


}
