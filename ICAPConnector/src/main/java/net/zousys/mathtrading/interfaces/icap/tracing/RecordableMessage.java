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
public class RecordableMessage <T extends ICMsg> {
    @Getter
    protected T icMsg;
    @Getter
    protected long time;

    /**
     *
     * @param name
     * @return
     * @param <T>
     * @throws IOException
     */
    public static <T extends ICMsg> RecordableMessage parse(String name) throws IOException {
        return parse(FileReader.readFileToBytes(name));
    }
    /**
     *
     * @param data
     * @return
     */
    public static <T extends ICMsg> RecordableMessage parse(byte[] data) {
        ByteBuffer bb = ByteBuffer.wrap(data);
        byte type = bb.get();
        bb.position(1);
        ByteBuffer sub = bb.slice();
        T msg = (T) RecordableMessage.getICMessage(null, EICMsgType.getName((int)type));
        ICMessageBuffer buffer = new ICMessageBuffer();
        buffer.put(sub);
        buffer.flip();
        buffer.limit(buffer.capacity());
        msg.unpack(buffer);
        RecordableMessage rm = new RecordableMessage(msg, System.currentTimeMillis());
        return rm;
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
        RecordableMessage.getICMessage(getIcMsg(), null).pack(mb);
        return mb.getBuffer();
    }

    /**
     *
     * @return
     * @param <T>
     */
    @SuppressWarnings("unchecked")
    public static final <T extends ICMsg> T getICMessage(ICMsg icMsg, EICMsgType msgtype) {
        if (icMsg==null && msgtype == null){
            return null;
        }
        EICMsgType type = icMsg == null ? msgtype : icMsg.getMsgType();
        switch (type) {
            case EICMsgType.eMsgPositiveLogin: {
                return icMsg==null? (T) new ICMsgPositiveLogin():(T) ((ICMsgPositiveLogin) icMsg);
            }
            case EICMsgType.eMsgClearBook: {
                return icMsg==null? (T) new ICMsgClearBookUpdate():(T) ((ICMsgClearBookUpdate)icMsg);
            }
            case EICMsgType.eMsgMessageLogUpdate: {
                return icMsg==null? (T) new ICMsgLogUpdate():(T) ((ICMsgLogUpdate)icMsg);
            }
            case EICMsgType.eMsgPositive: {
                return icMsg==null? (T) new ICMsgPositive():(T) ((ICMsgPositive) icMsg);
            }
            case EICMsgType.eMsgHeartbeat: {
                return icMsg==null? (T) new ICMsgHeartbeat():(T) ((ICMsgHeartbeat) icMsg);
            }
            case EICMsgType.eMsgElectronicTransaction: {
                return icMsg==null? (T) new ICMsgElectronicTransaction():(T) ((ICMsgElectronicTransaction) icMsg);
            }
            default: {
                return icMsg==null? (T) new ICMsg():(T) icMsg;
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
