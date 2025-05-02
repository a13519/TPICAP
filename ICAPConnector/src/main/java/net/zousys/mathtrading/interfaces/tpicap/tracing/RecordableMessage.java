package net.zousys.mathtrading.interfaces.tpicap.tracing;

import com.icap.iConnect.srcMsgs.enums.EICMsgType;
import com.icap.iConnect.srcMsgs.iCMsg.*;
import com.icap.iConnect.srcMsgs.iCUtils.ICMessageBuffer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.zousys.mathtrading.interfaces.tpicap.ParsingException;
import net.zousys.mathtrading.interfaces.util.FileReader;

import java.io.IOException;
import java.nio.ByteBuffer;

@AllArgsConstructor
public class RecordableMessage<T extends ICMsg> {
    @Getter
    protected T icMsg;
    @Getter
    protected long time;

    /**
     * @param name
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T extends ICMsg> RecordableMessage parse(String name) throws IOException, ParsingException {
        return parse(FileReader.readFileToBytes(name));
    }

    /**
     * @param data
     * @return
     */
    public static <T extends ICMsg> RecordableMessage parse(byte[] data) throws ParsingException {
        try {
            ByteBuffer bb = ByteBuffer.wrap(data);
            byte type = bb.get();
            bb.position(1);
            ByteBuffer sub = bb.slice();
            T msg = (T) RecordableMessage.getICMessage(null, EICMsgType.getName((int) type));
            ICMessageBuffer buffer = new ICMessageBuffer();
            buffer.put(sub);
            buffer.flip();
            buffer.limit(buffer.capacity());
            msg.unpack(buffer);
            return  new RecordableMessage(msg, System.currentTimeMillis());
        } catch (Exception e) {
            throw new ParsingException("ICMsg parsing exception");
        }
    }

    /**
     * @return
     */
    public byte[] serialize() {
        ByteBuffer mbb = getByteBuffer();
        ByteBuffer bb = ByteBuffer.allocate(mbb.capacity() + 1);
        bb.put((byte) icMsg.getMsgType().getValue());
        bb.put(mbb);
        return bb.array();
    }

    /**
     * @return
     */
    public final ByteBuffer getByteBuffer() {
        ICMessageBuffer mb = new ICMessageBuffer();
        RecordableMessage.getICMessage(getIcMsg(), null).pack(mb);
        return mb.getBuffer();
    }

    /**
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static final <T extends ICMsg> T getICMessage(ICMsg icMsg, EICMsgType msgtype) {
        if (icMsg == null && msgtype == null) {
            return null;
        }
        EICMsgType type = icMsg == null ? msgtype : icMsg.getMsgType();
        switch (type) {
            case EICMsgType.eMsgPositiveLogin: {
                return icMsg == null ? (T) new ICMsgPositiveLogin() : (T) ((ICMsgPositiveLogin) icMsg);
            }
            case EICMsgType.eMsgClearBook: {
                return icMsg == null ? (T) new ICMsgClearBookUpdate() : (T) ((ICMsgClearBookUpdate) icMsg);
            }
            case EICMsgType.eMsgMessageLogUpdate: {
                return icMsg == null ? (T) new ICMsgLogUpdate() : (T) ((ICMsgLogUpdate) icMsg);
            }
            case EICMsgType.eMsgPositive: {
                return icMsg == null ? (T) new ICMsgPositive() : (T) ((ICMsgPositive) icMsg);
            }
            case EICMsgType.eMsgHeartbeat: {
                return icMsg == null ? (T) new ICMsgHeartbeat() : (T) ((ICMsgHeartbeat) icMsg);
            }
            case EICMsgType.eMsgElectronicTransaction: {
                return icMsg == null ? (T) new ICMsgElectronicTransaction() : (T) ((ICMsgElectronicTransaction) icMsg);
            }
            default: {
                return icMsg == null ? (T) new ICMsg() : (T) icMsg;
            }
        }
    }


    public static void main(String[] args) throws IOException {

    }


}
